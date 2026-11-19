package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.Order
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OrderWhatsAppHelper {
    const val STORE_PHONE = "201002047073"

    fun formatOrderMessage(order: Order): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar", "EG"))
        val dateStr = dateFormat.format(Date(order.createdAtTimestamp))

        val sb = StringBuilder()
        sb.append("🛒 *طلب جديد من متجر هايبر الحياة*\n")
        sb.append("📅 *التاريخ:* $dateStr\n")
        sb.append("📄 *رقم الفاتورة:* ${order.id}\n")
        sb.append("👤 *العميل:* ${order.customerName}\n")
        sb.append("📞 *الهاتف:* ${order.customerPhone}\n")
        sb.append("📍 *العنوان:* ${order.customerAddress}\n")
        sb.append("💳 *طريقة الدفع:* ${if (order.paymentMethod == "cash") "نقداً عند الاستلام 💵" else "بطاقة ائتمان 💳"}\n\n")

        sb.append("🛍️ *المنتجات المطلوبة:*\n")
        val items = order.getItems()
        items.forEachIndexed { index, item ->
            val total = item.price * item.quantity
            sb.append("${index + 1}. ${item.name} (${item.weight}) × ${item.quantity} = ${String.format(Locale.US, "%.2f", total)} ج.م\n")
        }

        sb.append("\n🧾 *المجموع الفرعي:* ${String.format(Locale.US, "%.2f", order.subtotal)} ج.م\n")
        sb.append("🚚 *رسوم التوصيل:* ${if (order.deliveryFee == 0.0) "مجاني 🎉" else "${String.format(Locale.US, "%.2f", order.deliveryFee)} ج.م"}\n")
        sb.append("💵 *الإجمالي الكلي:* ${String.format(Locale.US, "%.2f", order.grandTotal)} ج.م\n")

        return sb.toString()
    }

    fun getWhatsAppWebUrl(order: Order, customPhone: String? = null): String {
        val message = formatOrderMessage(order)
        val targetPhone = (customPhone ?: STORE_PHONE).replace("+", "").replace(" ", "").trim()
        val encodedMsg = try {
            URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            message
        }
        return "https://wa.me/$targetPhone?text=$encodedMsg"
    }

    fun getWhatsAppQrCodeUrl(order: Order, customPhone: String? = null): String {
        val webUrl = getWhatsAppWebUrl(order, customPhone)
        val encoded = try {
            URLEncoder.encode(webUrl, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            webUrl
        }
        return "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=$encoded"
    }

    fun copyOrderToClipboard(context: Context, order: Order) {
        val message = formatOrderMessage(order)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("فاتورة هايبر الحياة", message)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ نص الفاتورة بالكامل للحافظة 📋", Toast.LENGTH_SHORT).show()
    }

    fun openWhatsAppForOrder(context: Context, order: Order, customPhone: String? = null) {
        val message = formatOrderMessage(order)
        val targetPhone = (customPhone ?: STORE_PHONE).replace("+", "").replace(" ", "").trim()

        // 1. Always copy text to clipboard first as a fail-safe guarantee
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("فاتورة هايبر الحياة", message)
        clipboard.setPrimaryClip(clip)

        val encodedMsg = try {
            URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            message
        }

        // Try direct WhatsApp scheme first
        val directUri = Uri.parse("whatsapp://send?phone=$targetPhone&text=$encodedMsg")
        val directIntent = Intent(Intent.ACTION_VIEW, directUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(directIntent)
            Toast.makeText(context, "جاري فتح واتساب... تم نسخ الفاتورة للحافظة 📋", Toast.LENGTH_SHORT).show()
            return
        } catch (e: Exception) {
            // WhatsApp app is not installed or scheme unhandled, try browser link
        }

        // Try Web WhatsApp link
        val webUri = Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=$encodedMsg")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(webIntent)
            Toast.makeText(context, "تم نسخ الفاتورة وجاري فتح المتصفح 📋", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // If browser is also unavailable (e.g. headless emulator), fall back to system Share sheet
            shareOrderText(context, order)
        }
    }

    fun sendSmsOrder(context: Context, order: Order, customPhone: String? = null) {
        val targetPhone = (customPhone ?: STORE_PHONE).replace("+", "").replace(" ", "").trim()
        val message = formatOrderMessage(order)
        val smsUri = Uri.parse("smsto:$targetPhone")
        val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            shareOrderText(context, order)
        }
    }

    fun callStore(context: Context, customPhone: String? = null) {
        val targetPhone = customPhone ?: STORE_PHONE
        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$targetPhone")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(callIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "رقم الإدارة: $targetPhone", Toast.LENGTH_LONG).show()
        }
    }

    fun shareOrderText(context: Context, order: Order) {
        val message = formatOrderMessage(order)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة فاتورة هايبر الحياة").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            copyOrderToClipboard(context, order)
        }
    }
}
