package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Order
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandRed
import com.example.ui.theme.TextSecondary
import com.example.util.ReceiptPrinterHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerOrdersDialog(
    orders: List<Order>,
    onDismiss: () -> Unit,
    onViewOrderInvoice: (Order) -> Unit,
    onReorderOrder: (Order) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar", "EG"))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = BrandRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "فواتيري السابقة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandDark
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_my_orders_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Text(
                    text = "سجل طلباتك وفواتيرك المسجلة من هذا الجهاز:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                HorizontalDivider(color = Color(0xFFF0F0F0))

                if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لم تقم بإجراء أي طلبات بعد",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(orders, key = { it.id }) { order ->
                            CustomerOrderCard(
                                order = order,
                                dateStr = dateFormat.format(Date(order.createdAtTimestamp)),
                                onViewInvoice = { onViewOrderInvoice(order) },
                                onReorder = { onReorderOrder(order) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "إغلاق",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B5563)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerOrderCard(
    order: Order,
    dateStr: String,
    onViewInvoice: () -> Unit,
    onReorder: () -> Unit
) {
    val context = LocalContext.current
    val (statusLabel, statusBg, statusText) = when (order.status) {
        "processing" -> Triple("قيد التجهيز 🟡", Color(0xFFFEF3C7), Color(0xFF92400E))
        "delivery" -> Triple("مع المندوب 🛵", Color(0xFFE0F2FE), Color(0xFF0369A1))
        "completed" -> Triple("تم التوصيل 🔵", Color(0xFFDBEAFE), Color(0xFF1E40AF))
        "cancelled" -> Triple("تم الإلغاء ❌", Color(0xFFFEE2E2), Color(0xFF991B1B))
        "archived" -> Triple("مؤرشف ⚪", Color(0xFFF3F4F6), Color(0xFF4B5563))
        else -> Triple("طلب جديد 🟢", Color(0xFFD1FAE5), Color(0xFF065F46))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewInvoice() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فاتورة #${order.id}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = BrandDark
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = dateStr,
                fontSize = 11.sp,
                color = TextSecondary
            )

            // Items preview
            val items = order.getItems()
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items.take(3).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${item.name} (${item.weight}) × ${item.quantity}",
                            fontSize = 11.sp,
                            color = Color(0xFF4B5563)
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.2f", item.price * item.quantity)} ج.م",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4B5563)
                        )
                    }
                }
                if (items.size > 3) {
                    Text(
                        text = "+ ${items.size - 3} منتجات أخرى...",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(vertical = 2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "الإجمالي: ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.2f", order.grandTotal)} ج.م",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandRed
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandRed.copy(alpha = 0.1f))
                        .clickable { onViewInvoice() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = BrandRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "عرض الفاتورة",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandRed
                    )
                }
            }

            // Action row: Re-order button and Print Receipt button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReorder,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "إعادة طلب نفس المنتجات 🔄",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = { ReceiptPrinterHelper.printReceipt(context, order) },
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "طباعة البون",
                        tint = BrandDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "طباعة بون 🖨️",
                        fontSize = 11.sp,
                        color = BrandDark
                    )
                }
            }
        }
    }
}
