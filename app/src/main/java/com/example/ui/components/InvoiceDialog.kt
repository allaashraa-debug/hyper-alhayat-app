package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import com.example.util.ReceiptPrinterHelper
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Order
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandRed
import com.example.ui.theme.TextSecondary
import com.example.util.OrderWhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceDialog(
    order: Order,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar", "EG"))
    val dateStr = dateFormat.format(Date(order.createdAtTimestamp))
    val items = order.getItems()

    var showRawText by remember { mutableStateOf(false) }
    var showQrCode by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }

    val formattedMessage = remember(order) { OrderWhatsAppHelper.formatOrderMessage(order) }
    val qrCodeUrl = remember(order) { OrderWhatsAppHelper.getWhatsAppQrCodeUrl(order) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "نجاح",
                        tint = BrandGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "تم تسجيل طلبك بنجاح! 🚀",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandDark
                )
                Text(
                    text = "فاتورة الشراء تم حفظها ومتاحة للإرسال الفوري",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notice Banner for Emulator Testing vs Real Phone
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تنبيه أثناء التجربة في المتصفح:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "المحاكي السحابي يعمل بسيرفر منفصل ولا يرى تطبيقات هاتفك. يمكنك مسح باركود QR بكاميرا هاتفك الشخصي لفتح واتساب بهاتفك فوراً!",
                            fontSize = 10.sp,
                            color = Color(0xFF1E40AF),
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Printable style receipt container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "هايبر الحياة - Hyper Al-Hayat",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandDark,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "فاتورة شراء رقمية • كود: ${order.id}",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = dateStr,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFD1D5DB)
                        )

                        // Customer Details
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ReceiptDetailRow(label = "العميل:", value = order.customerName)
                            ReceiptDetailRow(label = "الهاتف:", value = order.customerPhone)
                            ReceiptDetailRow(label = "العنوان:", value = order.customerAddress)
                            ReceiptDetailRow(
                                label = "الدفع:",
                                value = if (order.paymentMethod == "cash") "كاش عند الاستلام 💵" else "بطاقة ائتمان 💳"
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFD1D5DB)
                        )

                        // Items Table Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "المنتج", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text(text = "الكمية", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text(text = "الإجمالي", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Items List
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandDark
                                        )
                                        Text(
                                            text = item.weight,
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Text(
                                        text = "× ${item.quantity}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandDark,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.2f", item.price * item.quantity)} ج.م",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandDark
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFD1D5DB)
                        )

                        // Summary Totals
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ReceiptDetailRow(
                                label = "المجموع الفرعي:",
                                value = "${String.format(Locale.US, "%.2f", order.subtotal)} ج.م"
                            )
                            ReceiptDetailRow(
                                label = "رسوم التوصيل:",
                                value = if (order.deliveryFee == 0.0) "مجاني 🎉" else "${String.format(Locale.US, "%.2f", order.deliveryFee)} ج.م"
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 3.dp),
                                color = Color(0xFFE5E7EB)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "الإجمالي الكلي:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrandDark
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", order.grandTotal)} ج.م",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandRed
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // QR Code Feature for Real Phone Scanning
                Button(
                    onClick = { showQrCode = !showQrCode },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("toggle_qr_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showQrCode) "إخفاء الباركود ▲" else "مسح باركود QR بهاتفك لفتح واتساب 📱",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                AnimatedVisibility(visible = showQrCode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                        border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "وجّه كاميرا هاتفك الحقيقي للشاشة الآن 📸",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5B21B6),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(190.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = qrCodeUrl,
                                    contentDescription = "QR Code للطلب",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(174.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "سيفتح واتساب فوراً على هاتفك الشخصي مع رسالة الفاتورة جاهزة للإرسال إلى: 01002047073",
                                fontSize = 10.sp,
                                color = Color(0xFF6D28D9),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Primary WhatsApp Action Button
                Button(
                    onClick = {
                        OrderWhatsAppHelper.openWhatsAppForOrder(context, order)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("whatsapp_invoice_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "إرسال لواتساب (مع نسخ فوري للحافظة) 💬",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Secondary Quick Actions Row (Copy, Share, Call)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Copy Button
                    Button(
                        onClick = {
                            OrderWhatsAppHelper.copyOrderToClipboard(context, order)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("copy_invoice_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "نسخ الفاتورة 📋",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // SMS Button
                    OutlinedButton(
                        onClick = {
                            OrderWhatsAppHelper.sendSmsOrder(context, order)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = BrandDark
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "رسالة SMS ✉️",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandDark
                        )
                    }

                    // Call Button
                    OutlinedButton(
                        onClick = {
                            OrderWhatsAppHelper.callStore(context)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = BrandDark
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "اتصال 📞",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // POS Thermal Print & Dispatch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            ReceiptPrinterHelper.printReceipt(context, order)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "طباعة بون حراري (POS) 🖨️",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            ReceiptPrinterHelper.shareEscPosText(context, order)
                        },
                        modifier = Modifier
                            .weight(0.9f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = BrandDark
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "مشاركة نص البون 📲",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Toggle Explanation about emulator vs APK
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showExplanation = !showExplanation }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF4B5563)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showExplanation) "إخفاء التوضيح ▲" else "لماذا يطلب المتصفح تحميل واتساب أثناء التجربة؟ ▼",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B5563)
                    )
                }

                AnimatedVisibility(visible = showExplanation) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "1. التطبيق يعمل حالياً داخل محاكي افتراضي سحابي في السيرفر، ولا يمكن للمحاكي الوصول للتطبيقات المثبتة على هاتفك الشخصي.",
                                fontSize = 10.sp,
                                color = Color(0xFF334155),
                                lineHeight = 14.sp
                            )
                            Text(
                                text = "2. عند تحميل ملف الـ APK وتثبيته على هاتفك، سيتعرف أندرويد تلقائياً على تطبيق واتساب المثبت ويفتحه مباشرة.",
                                fontSize = 10.sp,
                                color = Color(0xFF334155),
                                lineHeight = 14.sp
                            )
                            Text(
                                text = "3. أثناء التجربة الحالية: استخدم زر 'مسح باركود QR' بكاميرا هاتفك أو اضغط 'نسخ الفاتورة' للصقها فوراً.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreen,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("close_invoice_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "إغلاق النافذة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B5563)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 10.sp, color = BrandDark, fontWeight = FontWeight.Bold)
    }
}
