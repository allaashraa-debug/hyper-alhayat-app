package com.example.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class HyperFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM token received: $token")
        OrderNotificationHelper.initAndSubscribe(applicationContext)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Push message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val orderId = data["order_id"] ?: remoteMessage.notification?.title?.substringAfter("#") ?: "جديد"
        val customerName = data["customer_name"] ?: remoteMessage.notification?.body ?: "عميل هايبر الحياة"
        val total = data["grand_total"]?.toDoubleOrNull() ?: 0.0

        OrderNotificationHelper.showNewOrderNotification(
            context = applicationContext,
            orderId = orderId,
            customerName = customerName,
            total = total
        )
    }
}
