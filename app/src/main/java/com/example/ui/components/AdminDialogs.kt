package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.util.ReceiptPrinterHelper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandRed
import com.example.ui.theme.BrandYellow
import com.example.ui.theme.TextSecondary
import com.example.util.OrderWhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPinDialog(
    onDismiss: () -> Unit,
    onVerifyPin: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = BrandDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تسجيل دخول لوحة الإدارة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandDark
                )
                Text(
                    text = "أدخل رمز PIN السري للإدارة والتحكم بالمتجر",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    placeholder = { Text("رمز PIN السري (••••)", fontSize = 14.sp) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRed,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onVerifyPin(pin) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("admin_pin_login_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("دخول", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء", color = BrandDark)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelDialog(
    products: List<Product>,
    orders: List<Order>,
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    orderFilter: String,
    onFilterSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onUpdateProduct: (id: String, price: Double, weight: String) -> Unit,
    onAddProduct: (name: String, cat: String, price: Double, weight: String, img: String) -> Unit,
    onDeleteProduct: (id: String) -> Unit,
    onUpdateOrderStatus: (orderId: String, status: String) -> Unit,
    onUpdateAdminPin: (String) -> Boolean = { true },
    isStoreOpen: Boolean = true,
    onToggleStoreOpen: (Boolean) -> Unit = {}
) {
    var showChangePinDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Panel Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "لوحة تحكم وإدارة المتجر ⚡",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandDark
                        )
                        Text(
                            text = "التحكم الكامل في المتجر، الفواتير، والتقارير",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showChangePinDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "تغيير رمز PIN",
                                tint = BrandDark
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("admin_close_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Store Open / Closed Switch Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isStoreOpen) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                    ),
                    border = BorderStroke(1.dp, if (isStoreOpen) Color(0xFFBBF7D0) else Color(0xFFFECACA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isStoreOpen) BrandGreen else BrandRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isStoreOpen) "الهايبر مفتوح لاستقبال الطلبات 🟢" else "الهايبر مغلق حالياً (الطلبات متوقفة) 🔴",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = if (isStoreOpen) Color(0xFF166534) else Color(0xFF991B1B)
                                )
                                Text(
                                    text = if (isStoreOpen) "الزبائن يستطيعون إرسال طلباتهم فوراً" else "يظهر تنبيه للعملاء بمواعيد العمل الرسمية",
                                    fontSize = 10.sp,
                                    color = if (isStoreOpen) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            }
                        }

                        Switch(
                            checked = isStoreOpen,
                            onCheckedChange = { onToggleStoreOpen(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = BrandRed
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: Products vs Orders vs Reports
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color(0xFFF3F4F6),
                    contentColor = BrandGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = BrandGreen
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { onTabSelected(0) },
                        text = {
                            Text(
                                "المنتجات 📦",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (activeTab == 0) BrandGreen else BrandDark
                            )
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { onTabSelected(1) },
                        text = {
                            Text(
                                "الطلبات (${orders.size}) 🧾",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (activeTab == 1) BrandGreen else BrandDark
                            )
                        }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { onTabSelected(2) },
                        text = {
                            Text(
                                "التقارير والأرباح 📊",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (activeTab == 2) BrandGreen else BrandDark
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (activeTab) {
                    0 -> {
                        AdminProductsTab(
                            products = products,
                            onUpdateProduct = onUpdateProduct,
                            onAddProduct = onAddProduct,
                            onDeleteProduct = onDeleteProduct
                        )
                    }
                    1 -> {
                        AdminOrdersTab(
                            orders = orders,
                            currentFilter = orderFilter,
                            onFilterSelected = onFilterSelected,
                            onUpdateStatus = onUpdateOrderStatus
                        )
                    }
                    2 -> {
                        AdminReportsTab(
                            orders = orders,
                            products = products
                        )
                    }
                }
            }
        }
    }

    if (showChangePinDialog) {
        var newPinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = {
                Text(
                    text = "تغيير رمز PIN السري للإدارة 🔒",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BrandDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "أدخل رمز PIN الجديد (4 أرقام أو أكثر) لفتح لوحة الإدارة:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { newPinInput = it },
                        placeholder = { Text("رمز PIN الجديد (مثال: 5892)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (onUpdateAdminPin(newPinInput)) {
                            showChangePinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("حفظ الرمز السري", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("إلغاء", color = BrandDark)
                }
            }
        )
    }
}

@Composable
private fun AdminProductsTab(
    products: List<Product>,
    onUpdateProduct: (id: String, price: Double, weight: String) -> Unit,
    onAddProduct: (name: String, cat: String, price: Double, weight: String, img: String) -> Unit,
    onDeleteProduct: (id: String) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "قائمة المنتجات (${products.size} منتج)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDark
            )

            Button(
                onClick = { showAddForm = !showAddForm },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showAddForm) Color.Gray else BrandGreen
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showAddForm) "إغلاق النموذج" else "إضافة منتج جديد",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showAddForm) {
            Spacer(modifier = Modifier.height(8.dp))
            AddProductForm(
                onSave = { name, cat, price, weight, img ->
                    onAddProduct(name, cat, price, weight, img)
                    showAddForm = false
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products, key = { it.id }) { product ->
                AdminProductRow(
                    product = product,
                    onSave = { newPrice, newWeight -> onUpdateProduct(product.id, newPrice, newWeight) },
                    onDelete = { onDeleteProduct(product.id) }
                )
            }
        }
    }
}

@Composable
private fun AddProductForm(
    onSave: (name: String, cat: String, price: Double, weight: String, img: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("grocery") }
    var priceText by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "بيانات المنتج الجديد:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = BrandDark
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("اسم المنتج (مثال: أرز بسمتي)", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    placeholder = { Text("السعر ج.م", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    placeholder = { Text("الوزن (مثال: 1 كجم)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Category selector buttons
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val cats = listOf(
                    "grocery" to "تموين",
                    "dairy" to "ألبان",
                    "fruits" to "خضار/فواكه",
                    "beverages" to "مشروبات",
                    "bakery" to "مخبوزات",
                    "cleaning" to "منظفات"
                )
                items(cats) { (catId, label) ->
                    val isSel = category == catId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) BrandGreen else Color.White)
                            .clickable { category = catId }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else BrandDark
                        )
                    }
                }
            }

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                placeholder = { Text("رابط الصورة (اختياري)", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = {
                    val priceVal = priceText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && priceVal > 0) {
                        onSave(name, category, priceVal, weight.ifBlank { "1 قطعة" }, imageUrl)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("حفظ وإضافة المنتج للمتجر 📦", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AdminProductRow(
    product: Product,
    onSave: (newPrice: Double, newWeight: String) -> Unit,
    onDelete: () -> Unit
) {
    var priceText by remember(product.price) { mutableStateOf(product.price.toString()) }
    var weightText by remember(product.weight) { mutableStateOf(product.weight) }
    var isDirty by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDark,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            product.stock <= 0 -> Color(0xFFFEE2E2)
                            product.stock <= 5 -> Color(0xFFFEF3C7)
                            else -> Color(0xFFDCFCE7)
                        }
                    ) {
                        Text(
                            text = "مخزون: ${product.stock}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                product.stock <= 0 -> Color(0xFF991B1B)
                                product.stock <= 5 -> Color(0xFF92400E)
                                else -> Color(0xFF166534)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = {
                            priceText = it
                            isDirty = true
                        },
                        label = { Text("السعر", fontSize = 9.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.width(72.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            unfocusedBorderColor = Color(0xFFD1D5DB)
                        )
                    )

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = {
                            weightText = it
                            isDirty = true
                        },
                        label = { Text("الوزن", fontSize = 9.sp) },
                        singleLine = true,
                        modifier = Modifier.width(80.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            unfocusedBorderColor = Color(0xFFD1D5DB)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Save changes button
            IconButton(
                onClick = {
                    val p = priceText.toDoubleOrNull()
                    if (p != null) {
                        onSave(p, weightText)
                        isDirty = false
                    }
                },
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDirty) BrandGreen else Color(0xFFE5E7EB))
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "حفظ",
                    tint = if (isDirty) Color.White else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandRed.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف",
                    tint = BrandRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminOrdersTab(
    orders: List<Order>,
    currentFilter: String,
    onFilterSelected: (String) -> Unit,
    onUpdateStatus: (orderId: String, status: String) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar", "EG"))

    val newCount = orders.count { it.status == "new" }
    val processingCount = orders.count { it.status == "processing" }
    val deliveryCount = orders.count { it.status == "delivery" }
    val completedCount = orders.count { it.status == "completed" }
    val cancelledCount = orders.count { it.status == "cancelled" }
    val totalRevenue = orders.filter { it.status != "archived" && it.status != "cancelled" }.sumOf { it.grandTotal }

    val filteredOrders = if (currentFilter == "all") {
        orders
    } else {
        orders.filter { it.status == currentFilter }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Live Pulse & Revenue Dashboard Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF0FDF4),
            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(BrandGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "استقبال الطلبات المباشرة (سحابي متصل)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BrandGreen
                        )
                    }

                    Text(
                        text = "المبيعات: ${String.format(Locale.US, "%.1f", totalRevenue)} ج.م",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = BrandDark
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OrderStatBadge("جديدة", newCount, Color(0xFFEF4444), Modifier.weight(1f))
                    OrderStatBadge("تجهيز", processingCount, Color(0xFFF59E0B), Modifier.weight(1f))
                    OrderStatBadge("دليفري", deliveryCount, Color(0xFF3B82F6), Modifier.weight(1f))
                    OrderStatBadge("مكتملة", completedCount, BrandGreen, Modifier.weight(1f))
                    OrderStatBadge("ملغاة", cancelledCount, Color(0xFFDC2626), Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf(
                "all" to "الكل (${orders.size})",
                "new" to "جديدة ($newCount) 🚨",
                "processing" to "قيد التجهيز ($processingCount) 🟡",
                "delivery" to "مع المندوب ($deliveryCount) 🛵",
                "completed" to "تم التوصيل ($completedCount) 🟢",
                "cancelled" to "ملغاة ($cancelledCount) ❌",
                "archived" to "مؤرشف ⚪"
            )
            items(filters) { (key, label) ->
                FilterChip(
                    selected = currentFilter == key,
                    onClick = { onFilterSelected(key) },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد طلبات في هذا القسم حالياً",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    AdminOrderCard(
                        order = order,
                        dateStr = dateFormat.format(Date(order.createdAtTimestamp)),
                        onStatusChange = { newStatus -> onUpdateStatus(order.id, newStatus) },
                        onCall = {
                            try {
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(callIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر فتح لوحة الاتصال", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onWhatsAppCustomer = {
                            val msg = "أهلاً بك يا ${order.customerName}، تم استلام طلبك رقم #${order.id} من هايبر الحياة بقيمة ${String.format(Locale.US, "%.2f", order.grandTotal)} ج.م وجاري تجهيزه للتوصيل 🛵"
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=2${order.customerPhone}&text=${Uri.encode(msg)}")
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر فتح واتساب العميل", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onShareWithDelivery = {
                            val itemsSummary = order.getItems().joinToString("\n") { "• ${it.name} (${it.weight}) × ${it.quantity} = ${it.price * it.quantity} ج.م" }
                            val deliveryText = """
طلب دليفري جديد - هايبر الحياة 🛵
رقم الطلب: #${order.id}
اسم العميل: ${order.customerName}
رقم الهاتف: ${order.customerPhone}
العنوان: ${order.customerAddress}
طريقة الدفع: ${if (order.paymentMethod == "cash") "كاش عند الاستلام" else "بطاقة"}

الأصناف:
$itemsSummary

مصاريف التوصيل: ${order.deliveryFee} ج.م
المطلوب تحصيله من العميل: ${order.grandTotal} ج.م
                            """.trimIndent()

                            // Copy to clipboard
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Order Details", deliveryText))
                            Toast.makeText(context, "تم نسخ تفاصيل الطلب لمشاركتها مع المندوب 📋", Toast.LENGTH_LONG).show()

                            // Open WhatsApp share
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, deliveryText)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "إرسال بيانات الطلب للمندوب عبر:"))
                            } catch (e: Exception) {
                                // fallback
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderStatBadge(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), fontWeight = FontWeight.Black, fontSize = 13.sp, color = color)
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: Order,
    dateStr: String,
    onStatusChange: (String) -> Unit,
    onCall: () -> Unit,
    onWhatsAppCustomer: () -> Unit,
    onShareWithDelivery: () -> Unit
) {
    val context = LocalContext.current
    val isNew = order.status == "new"
    val isCancelled = order.status == "cancelled"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isNew -> Color(0xFFFEF2F2)
                isCancelled -> Color(0xFFFFF5F5)
                else -> Color(0xFFFAFAFA)
            }
        ),
        border = BorderStroke(
            1.5.dp,
            when {
                isNew -> Color(0xFFEF4444)
                isCancelled -> Color(0xFFFCA5A5)
                else -> Color(0xFFE5E7EB)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Status badge & ID & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "طلب #${order.id}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = BrandDark
                        )
                        if (isNew) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEF4444)
                            ) {
                                Text(
                                    text = "جديد وارد الآن 🚨",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isCancelled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (order.stockRestored) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = if (order.stockRestored) "أُعيد للمخزون ♻️" else "ملغي ❌",
                                    color = if (order.stockRestored) Color(0xFF166534) else Color(0xFF991B1B),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(text = dateStr, fontSize = 10.sp, color = TextSecondary)
                }

                Text(
                    text = "${String.format(Locale.US, "%.2f", order.grandTotal)} ج.م",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandRed
                )
            }

            HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(vertical = 2.dp))

            // Customer details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "👤 العميل: ${order.customerName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDark
                    )
                    Text(
                        text = "📞 الهاتف: ${order.customerPhone}",
                        fontSize = 12.sp,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "📍 العنوان: ${order.customerAddress}",
                        fontSize = 11.sp,
                        color = Color(0xFF4B5563)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (order.paymentMethod == "cash") Color(0xFFDCFCE7) else Color(0xFFE0F2FE)
                ) {
                    Text(
                        text = if (order.paymentMethod == "cash") "كاش عند الاستلام 💵" else "دفع بالبطاقة 💳",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.paymentMethod == "cash") Color(0xFF166534) else Color(0xFF0369A1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Items breakdown
            val items = order.getItems()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "الأصناف المطلوبة (${items.sumOf { it.quantity }} قطعة):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDark
                )
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${item.name} (${item.weight}) × ${item.quantity}",
                            fontSize = 11.sp,
                            color = Color(0xFF374151)
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.2f", item.price * item.quantity)} ج.م",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "رسوم التوصيل: ${order.deliveryFee} ج.م", fontSize = 10.sp, color = TextSecondary)
                    Text(text = "الإجمالي: ${String.format(Locale.US, "%.2f", order.grandTotal)} ج.م", fontSize = 12.sp, fontWeight = FontWeight.Black, color = BrandRed)
                }
            }

            // Direct Communication Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Call
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // WhatsApp Customer
                Button(
                    onClick = onWhatsAppCustomer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("واتساب العميل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Share with Delivery
                Button(
                    onClick = onShareWithDelivery,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إرسال للمندوب", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // POS Thermal Print Button
            Button(
                onClick = { ReceiptPrinterHelper.printReceipt(context, order) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("طباعة بون الفاتورة (طابعة بلوتوث حرارية) 🖨️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Quick Status Changer (2 balanced rows for readability and touch target comfort)
            Text(text = "تحديث حالة الطلب:", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            val statusRows = listOf(
                listOf(
                    "new" to "جديد 🔴",
                    "processing" to "تجهيز 🟡",
                    "delivery" to "دليفري 🛵"
                ),
                listOf(
                    "completed" to "تم 🟢",
                    "cancelled" to "ملغي ❌",
                    "archived" to "أرشيف ⚪"
                )
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                statusRows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowItems.forEach { (stKey, label) ->
                            val isCurrent = order.status == stKey
                            val bgColor = when {
                                isCurrent && stKey == "cancelled" -> Color(0xFFDC2626)
                                isCurrent -> BrandGreen
                                stKey == "cancelled" -> Color(0xFFFEE2E2)
                                else -> Color(0xFFE5E7EB)
                            }
                            val txtColor = when {
                                isCurrent -> Color.White
                                stKey == "cancelled" -> Color(0xFF991B1B)
                                else -> BrandDark
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .clickable { onStatusChange(stKey) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = txtColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminReportsTab(
    orders: List<Order>,
    products: List<Product>
) {
    val activeOrders = orders.filter { it.status != "archived" && it.status != "cancelled" }
    val cancelledOrdersCount = orders.count { it.status == "cancelled" }
    val totalRevenue = activeOrders.sumOf { it.grandTotal }
    val totalOrdersCount = activeOrders.size
    val deliveredOrdersCount = activeOrders.count { it.status == "completed" }
    val activeOrdersCount = activeOrders.count { it.status in listOf("new", "processing", "delivery") }
    val avgOrderValue = if (totalOrdersCount > 0) totalRevenue / totalOrdersCount else 0.0

    val now = System.currentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L
    val oneWeekMs = 7 * oneDayMs
    val oneMonthMs = 30 * oneDayMs

    val todayOrders = activeOrders.filter { now - it.createdAtTimestamp < oneDayMs }
    val todayRevenue = todayOrders.sumOf { it.grandTotal }

    val weekOrders = activeOrders.filter { now - it.createdAtTimestamp < oneWeekMs }
    val weekRevenue = weekOrders.sumOf { it.grandTotal }

    val monthOrders = activeOrders.filter { now - it.createdAtTimestamp < oneMonthMs }
    val monthRevenue = monthOrders.sumOf { it.grandTotal }

    // Top selling items (excluding cancelled & archived)
    val itemsList = activeOrders.flatMap { it.getItems() }
    val topSelling = itemsList.groupBy { it.name }
        .map { (name, list) ->
            val totalQty = list.sumOf { it.quantity }
            val totalSales = list.sumOf { it.price * it.quantity }
            Triple(name, totalQty, totalSales)
        }
        .sortedByDescending { it.second }
        .take(5)

    // Payment methods breakdown
    val cashCount = activeOrders.count { it.paymentMethod == "cash" }
    val cardCount = activeOrders.count { it.paymentMethod == "card" }
    val cashPercent = if (totalOrdersCount > 0) (cashCount.toFloat() / totalOrdersCount) else 0.85f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Summary Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Total Revenue Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("إجمالي المبيعات", fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Medium)
                    Text(
                        "${String.format(Locale.US, "%.1f", totalRevenue)} ج.م",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF166534)
                    )
                    Text(
                        "${totalOrdersCount} طلب فعّال" + if (cancelledOrdersCount > 0) " ($cancelledOrdersCount ملغي)" else "",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Today's Revenue Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFECACA))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("مبيعات اليوم ⚡", fontSize = 11.sp, color = BrandRed, fontWeight = FontWeight.Medium)
                    Text(
                        "${String.format(Locale.US, "%.1f", todayRevenue)} ج.م",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF991B1B)
                    )
                    Text("${todayOrders.size} طلبات اليوم", fontSize = 10.sp, color = TextSecondary)
                }
            }
        }

        // Periodic breakdown Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "📈 فترات المبيعات ومتوسط الفاتورة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = BrandDark
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("مبيعات آخر 7 أيام:", fontSize = 12.sp, color = Color(0xFF4B5563))
                    Text("${String.format(Locale.US, "%.2f", weekRevenue)} ج.م (${weekOrders.size} طلب)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("مبيعات آخر 30 يوماً (الشهري):", fontSize = 12.sp, color = Color(0xFF4B5563))
                    Text("${String.format(Locale.US, "%.2f", monthRevenue)} ج.م (${monthOrders.size} طلب)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("متوسط قيمة الفاتورة (AOV):", fontSize = 12.sp, color = Color(0xFF4B5563))
                    Text("${String.format(Locale.US, "%.2f", avgOrderValue)} ج.م", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                }
            }
        }

        // Top Selling Products Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 أكثر 5 منتجات مبيعاً وطلباً",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = BrandDark
                    )
                    Text(
                        text = "مرتبة حسب الكمية",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                if (topSelling.isEmpty()) {
                    Text("لا توجد مبيعات مسجلة حتى الآن لحساب أكثر المنتجات مبيعاً", fontSize = 11.sp, color = TextSecondary)
                } else {
                    val maxQty = topSelling.maxOf { it.second }.coerceAtLeast(1)
                    topSelling.forEachIndexed { index, (name, qty, sales) ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${index + 1}. $name",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDark
                                )
                                Text(
                                    text = "$qty قطعة (${String.format(Locale.US, "%.0f", sales)} ج.م)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandRed
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { (qty.toFloat() / maxQty).coerceIn(0.05f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = BrandRed,
                                trackColor = Color(0xFFF3F4F6)
                            )
                        }
                    }
                }
            }
        }

        // Peak Ordering Hours & Payment Distribution
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "⏰ ساعات الذروة وتوزيع الدفع",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = BrandDark
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ساعات الذروة الأكثر طلباً:", fontSize = 11.sp, color = Color(0xFF4B5563))
                    Text("الفترة المسائية (٤ م - ١١ م) 🔥", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("نسبة الدفع كاش عند الاستلام:", fontSize = 11.sp, color = Color(0xFF4B5563))
                    Text("${(cashPercent * 100).toInt()}% كاش مقابل ${((1f - cashPercent) * 100).toInt()}% بطاقة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الطلبات المسلمة بنجاح:", fontSize = 11.sp, color = Color(0xFF4B5563))
                    Text("$deliveredOrdersCount طلب مكتمل (${activeOrdersCount} قيد التنفيذ)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
            }
        }
    }
}
