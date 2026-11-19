package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale

object OrderNotificationHelper {

    const val CHANNEL_ID = "admin_orders_channel"
    const val CHANNEL_NAME = "طلبات الشراء الجديدة - إدارة هايبر الحياة"
    const val TOPIC_ADMIN_ORDERS = "admin_orders"

    fun initAndSubscribe(context: Context) {
        createNotificationChannel(context)
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_ADMIN_ORDERS)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FCM", "Successfully subscribed to topic: $TOPIC_ADMIN_ORDERS")
                    } else {
                        Log.w("FCM", "Failed to subscribe to topic: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            Log.w("FCM", "Error subscribing to FCM topic: ${e.message}")
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات صوتية فورية للإدارة عند وصول أي طلب شراء جديد من العملاء"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(soundUri, audioAttributes)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNewOrderNotification(
        context: Context,
        orderId: String,
        customerName: String,
        total: Double
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_admin_orders", true)
            putExtra("target_order_id", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val totalFormatted = String.format(Locale.US, "%.2f", total)
        val notificationText = if (total > 0) {
            "العميل: $customerName • الإجمالي: $totalFormatted ج.م (اضغط لمعاينة وتجهيز الطلب)"
        } else {
            "العميل: $customerName • اضغط لمعاينة تفاصيل الفاتورة وتجهيز الطلب"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🚨 طلب شراء جديد وارد! #$orderId")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.w("Notification", "POST_NOTIFICATIONS permission not granted yet: ${e.message}")
        } catch (e: Exception) {
            Log.w("Notification", "Failed to post notification: ${e.message}")
        }
    }
}
