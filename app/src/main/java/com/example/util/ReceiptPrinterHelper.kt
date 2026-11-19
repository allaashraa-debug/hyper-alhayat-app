package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPrinterHelper {

    fun generateReceiptHtml(order: Order): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar", "EG"))
        val dateStr = dateFormat.format(Date(order.createdAtTimestamp))
        val items = order.getItems()

        val itemsHtml = buildString {
            items.forEach { item ->
                val lineTotal = item.price * item.quantity
                append("""
                    <tr>
                        <td style="text-align: right; padding: 4px 2px;">
                            <strong>${item.name}</strong><br/>
                            <small style="color: #666;">${item.weight} (${String.format(Locale.US, "%.1f", item.price)} × ${item.quantity})</small>
                        </td>
                        <td style="text-align: left; padding: 4px 2px; vertical-align: top; white-space: nowrap;">
                            <strong>${String.format(Locale.US, "%.2f", lineTotal)} ج.م</strong>
                        </td>
                    </tr>
                """.trimIndent())
            }
        }

        return """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="utf-8">
                <style>
                    @page {
                        size: 58mm auto;
                        margin: 2mm;
                    }
                    body {
                        font-family: 'Courier New', Courier, monospace, 'Cairo', sans-serif;
                        font-size: 11px;
                        line-height: 1.3;
                        color: #000;
                        margin: 0;
                        padding: 4px;
                        width: 54mm;
                        max-width: 58mm;
                    }
                    .center { text-align: center; }
                    .bold { font-weight: bold; }
                    .store-title { font-size: 16px; font-weight: 900; margin-bottom: 2px; }
                    .subtitle { font-size: 10px; color: #333; margin-bottom: 4px; }
                    .divider { border-top: 1px dashed #000; margin: 6px 0; }
                    .double-divider { border-top: 2px solid #000; margin: 6px 0; }
                    table { width: 100%; border-collapse: collapse; font-size: 10.5px; }
                    .total-box { font-size: 13px; font-weight: bold; }
                    .barcode { letter-spacing: 4px; font-family: monospace; font-size: 14px; margin: 6px 0; }
                </style>
            </head>
            <body>
                <div class="center">
                    <div class="store-title">🛒 هايبر الحياة 🛒</div>
                    <div class="subtitle">سوبر ماركت &amp; طلبات دليفري سريعة</div>
                    <div>مدينة بدر - الإسكان الاجتماعي</div>
                    <div>فرع الياسمين &amp; فرع القرنفل</div>
                    <div class="bold">هاتف / واتساب: 01002047073</div>
                </div>

                <div class="double-divider"></div>

                <div>
                    <div><strong>رقم الفاتورة:</strong> #${order.id}</div>
                    <div><strong>التاريخ:</strong> $dateStr</div>
                    <div><strong>العميل:</strong> ${order.customerName}</div>
                    <div><strong>الهاتف:</strong> ${order.customerPhone}</div>
                    <div><strong>العنوان:</strong> ${order.customerAddress}</div>
                    <div><strong>طريقة الدفع:</strong> ${if (order.paymentMethod == "card") "بطاقة بنكية / فيزا" else "دفع كاش عند الاستلام"}</div>
                </div>

                <div class="divider"></div>

                <div class="center bold" style="margin-bottom: 4px;">تفاصيل المشتريات</div>
                <table>
                    <thead>
                        <tr style="border-bottom: 1px solid #000;">
                            <th style="text-align: right; padding: 2px;">الصنف</th>
                            <th style="text-align: left; padding: 2px;">المجموع</th>
                        </tr>
                    </thead>
                    <tbody>
                        $itemsHtml
                    </tbody>
                </table>

                <div class="divider"></div>

                <table>
                    <tr>
                        <td style="text-align: right;">مجموع الأصناف:</td>
                        <td style="text-align: left;">${String.format(Locale.US, "%.2f", order.subtotal)} ج.م</td>
                    </tr>
                    <tr>
                        <td style="text-align: right;">خدمة التوصيل:</td>
                        <td style="text-align: left;">${if (order.deliveryFee == 0.0) "مجاناً (عرض 🎁)" else "${String.format(Locale.US, "%.2f", order.deliveryFee)} ج.م"}</td>
                    </tr>
                    <tr class="total-box">
                        <td style="text-align: right; padding-top: 4px;">الإجمالي الصافي:</td>
                        <td style="text-align: left; padding-top: 4px;">${String.format(Locale.US, "%.2f", order.grandTotal)} ج.م</td>
                    </tr>
                </table>

                <div class="double-divider"></div>

                <div class="center">
                    <div class="barcode">||| ||||| |||| || |||</div>
                    <div class="bold">شكراً لتسوقكم من هايبر الحياة!</div>
                    <div style="font-size: 9px; margin-top: 4px;">نتشرف بخدمتكم دائماً • دليفري لجميع مناطق بدر</div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun printReceipt(context: Context, order: Order) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "خدمة الطباعة غير متوفرة في النظام", Toast.LENGTH_SHORT).show()
                return
            }

            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printAdapter = webView.createPrintDocumentAdapter("بون_فاتورة_${order.id}")
                    val printAttributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    printManager.print("بون-هايبر-الحياة-${order.id}", printAdapter, printAttributes)
                }
            }

            val html = generateReceiptHtml(order)
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            Toast.makeText(context, "خطأ أثناء تجهيز الطباعة: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateEscPosPlainText(order: Order): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar", "EG"))
        val dateStr = dateFormat.format(Date(order.createdAtTimestamp))
        val items = order.getItems()

        return buildString {
            appendLine("================================")
            appendLine("       🛒 هايبر الحياة 🛒        ")
            appendLine("  سوبر ماركت & طلبات دليفري سريعة ")
            appendLine("   مدينة بدر - الياسمين & القرنفل  ")
            appendLine("    هاتف / واتساب: 01002047073    ")
            appendLine("================================")
            appendLine("رقم الفاتورة: #${order.id}")
            appendLine("التاريخ: $dateStr")
            appendLine("العميل: ${order.customerName}")
            appendLine("الهاتف: ${order.customerPhone}")
            appendLine("العنوان: ${order.customerAddress}")
            appendLine("الدفع: ${if (order.paymentMethod == "card") "فيزا / بطاقة" else "كاش عند الاستلام"}")
            appendLine("--------------------------------")
            appendLine("الأصناف المطلوبة:")
            items.forEach { item ->
                val lineTotal = item.price * item.quantity
                appendLine("• ${item.name} (${item.weight})")
                appendLine("  ${item.quantity} × ${String.format(Locale.US, "%.1f", item.price)} = ${String.format(Locale.US, "%.2f", lineTotal)} ج.م")
            }
            appendLine("--------------------------------")
            appendLine("مجموع الأصناف: ${String.format(Locale.US, "%.2f", order.subtotal)} ج.م")
            appendLine("خدمة التوصيل: ${if (order.deliveryFee == 0.0) "مجاناً" else "${String.format(Locale.US, "%.2f", order.deliveryFee)} ج.م"}")
            appendLine("الإجمالي الصافي: ${String.format(Locale.US, "%.2f", order.grandTotal)} ج.م")
            appendLine("================================")
            appendLine(" شكراً لثقتكم بنا - هايبر الحياة ")
            appendLine("================================")
        }
    }

    fun shareEscPosText(context: Context, order: Order) {
        val text = generateEscPosPlainText(order)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "بون فاتورة #${order.id} - هايبر الحياة")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "إرسال البون إلى طابعة البلوتوث أو مشاركته")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
