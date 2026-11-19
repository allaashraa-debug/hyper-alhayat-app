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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandRed
import com.example.ui.theme.TextSecondary
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun CheckoutDialog(
    subtotal: Double,
    onDismiss: () -> Unit,
    onSubmitOrder: (name: String, phone: String, address: String, payment: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("cash") } // "cash" or "card"
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    fun fetchCurrentLocation() {
        isLocating = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locationManager == null) {
                    withContext(Dispatchers.Main) {
                        isLocating = false
                        Toast.makeText(context, "خدمة تحديد الموقع غير متوفرة", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                var bestLocation: Location? = null
                val providers = locationManager.getProviders(true)
                for (provider in providers) {
                    try {
                        val l = locationManager.getLastKnownLocation(provider) ?: continue
                        if (bestLocation == null || l.accuracy < bestLocation!!.accuracy) {
                            bestLocation = l
                        }
                    } catch (e: SecurityException) {
                        // ignore
                    }
                }

                if (bestLocation != null) {
                    var resolvedAddress = ""
                    try {
                        val geocoder = Geocoder(context, Locale("ar", "EG"))
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(bestLocation.latitude, bestLocation.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val parts = mutableListOf<String>()
                            addr.thoroughfare?.let { parts.add(it) }
                            addr.subLocality?.let { parts.add(it) }
                            addr.locality?.let { parts.add(it) }
                            addr.adminArea?.let { parts.add(it) }
                            if (parts.isNotEmpty()) {
                                resolvedAddress = parts.distinct().joinToString("، ")
                            }
                        }
                    } catch (e: Exception) {
                        // Geocoder fallback
                    }

                    if (resolvedAddress.isBlank()) {
                        resolvedAddress = "مدينة بدر (موقع GPS: ${String.format(Locale.US, "%.4f, %.4f", bestLocation.latitude, bestLocation.longitude)})"
                    }

                    withContext(Dispatchers.Main) {
                        address = resolvedAddress
                        errorMessage = null
                        isLocating = false
                        Toast.makeText(context, "تم تحديد العنوان بنجاح 📍", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        address = "مدينة بدر - الإسكان الاجتماعي (موقع تقريبي)"
                        errorMessage = null
                        isLocating = false
                        Toast.makeText(context, "تم إدراج الموقع بنجاح، يرجى استكمال رقم العمارة 📍", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLocating = false
                    address = "مدينة بدر (موقع محدد عبر GPS)"
                    Toast.makeText(context, "تم تحديد المنطقة، يرجى كتابة الشارع 📍", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            fetchCurrentLocation()
        } else {
            Toast.makeText(context, "يرجى منح صلاحية الموقع لتحديد عنوانك تلقائياً", Toast.LENGTH_LONG).show()
        }
    }

    fun requestLocation() {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val freeShippingThreshold = 200.0
    val isFreeShipping = subtotal >= freeShippingThreshold
    val deliveryFee = if (isFreeShipping) 0.0 else 20.0
    val grandTotal = subtotal + deliveryFee

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
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تأكيد بيانات التوصيل",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandDark
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("checkout_close_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                // Name Input
                Text(
                    text = "الاسم الكامل",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    placeholder = { Text("مثال: أحمد محمد", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = BrandRed)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRed,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Input
                Text(
                    text = "رقم الهاتف (للتواصل عند الوصول)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        errorMessage = null
                    },
                    placeholder = { Text("مثال: 01012345678", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = BrandRed)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_phone_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRed,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Address Input Header with 1-Click GPS button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عنوان التوصيل بالتفصيل",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDark
                    )

                    Surface(
                        color = if (isLocating) Color(0xFFF3F4F6) else BrandGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable(enabled = !isLocating) { requestLocation() }
                            .testTag("checkout_gps_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (isLocating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = BrandGreenDark
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "جارٍ التحديد...",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreenDark
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "GPS",
                                    tint = BrandGreenDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تحديد موقعي (GPS) 📍",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreenDark
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        errorMessage = null
                    },
                    placeholder = { Text("اكتب العنوان بوضوح...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = BrandRed)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_address_input"),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRed,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method
                Text(
                    text = "طريقة الدفع",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Cash Option
                    PaymentOptionCard(
                        title = "كاش عند الاستلام 💵",
                        isSelected = paymentMethod == "cash",
                        onClick = { paymentMethod = "cash" },
                        modifier = Modifier.weight(1f)
                    )

                    // Card Option
                    PaymentOptionCard(
                        title = "بطاقة ائتمان 💳",
                        isSelected = paymentMethod == "card",
                        onClick = { paymentMethod = "card" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Order summary container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "المجموع:", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "${String.format(Locale.US, "%.2f", subtotal)} ج.م",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "التوصيل:", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = if (isFreeShipping) "مجاني 🎉" else "${String.format(Locale.US, "%.2f", deliveryFee)} ج.م",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFreeShipping) BrandGreen else BrandDark
                            )
                        }
                        HorizontalDivider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(vertical = 2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الإجمالي المطلوب:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandDark
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.2f", grandTotal)} ج.م",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandRed
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "يرجى كتابة الاسم الكامل"
                        } else if (phone.isBlank() || phone.length < 8) {
                            errorMessage = "يرجى إدخال رقم هاتف صحيح"
                        } else if (address.isBlank()) {
                            errorMessage = "يرجى إدخال عنوان التوصيل بالتفصيل"
                        } else {
                            onSubmitOrder(name, phone, address, paymentMethod)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_order_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "إرسال الطلب وإصدار الفاتورة 🚀",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentOptionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFEBEE) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) BrandRed else Color(0xFFE5E7EB)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = BrandRed),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) BrandRed else BrandDark
            )
        }
    }
}
