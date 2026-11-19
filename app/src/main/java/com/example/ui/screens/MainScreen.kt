package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvailableCategories
import com.example.ui.MainViewModel
import com.example.ui.components.AdminPanelDialog
import com.example.ui.components.AdminPinDialog
import com.example.ui.components.CartBottomSheet
import com.example.ui.components.CheckoutDialog
import com.example.ui.components.CustomerOrdersDialog
import com.example.ui.components.HeroBanner
import com.example.ui.components.InvoiceDialog
import com.example.ui.components.ProductCard
import com.example.ui.components.StoreFooter
import com.example.ui.components.TopAnnouncementBar
import androidx.compose.material.icons.filled.NotificationsActive
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BrandDark
import com.example.ui.theme.BrandRed
import com.example.ui.theme.BrandRedDark
import com.example.ui.theme.BrandYellowBright
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.filteredProducts.collectAsState()
    val allRawProducts by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val newOrdersCount by viewModel.newOrdersCount.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()

    val showCartSheet by viewModel.showCartSheet.collectAsState()
    val showCheckoutDialog by viewModel.showCheckoutDialog.collectAsState()
    val showInvoiceDialog by viewModel.showInvoiceDialog.collectAsState()
    val activeInvoiceOrder by viewModel.activeInvoiceOrder.collectAsState()
    val showCustomerOrdersDialog by viewModel.showCustomerOrdersDialog.collectAsState()
    val showAdminPinDialog by viewModel.showAdminPinDialog.collectAsState()
    val showAdminPanel by viewModel.showAdminPanel.collectAsState()
    val adminActiveTab by viewModel.adminActiveTab.collectAsState()
    val adminOrderFilter by viewModel.adminOrderFilter.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isStoreOpen by viewModel.isStoreOpen.collectAsState()

    // Handle Toast
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val totalCartCount = cart.values.sumOf { it.quantity }

    var currentUser by remember { mutableStateOf(com.example.util.GoogleSignInHelper.getCurrentUser()) }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { androidx.credentials.CredentialManager.create(context) }

    DisposableEffect(Unit) {
        val authListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        com.google.firebase.Firebase.auth.addAuthStateListener(authListener)
        onDispose {
            com.google.firebase.Firebase.auth.removeAuthStateListener(authListener)
        }
    }

    // Request Notification permission for Android 13+ (API 33+)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { /* handled */ }

        LaunchedEffect(Unit) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                // Announcement Bar with Cloud Auth & Live Orders Badge
                TopAnnouncementBar(
                    currentUser = currentUser,
                    newOrdersCount = newOrdersCount,
                    onOpenMyOrders = { viewModel.openCustomerOrders() },
                    onOpenAdmin = { viewModel.openAdminPin() },
                    onSignInClicked = {
                        if (currentUser == null) {
                            com.example.util.GoogleSignInHelper.onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = { user ->
                                    viewModel.showToast("أهلاً بك يا ${user.displayName ?: "عميلنا العزيز"} ☁️")
                                },
                                onAuthError = { err ->
                                    viewModel.showToast(err)
                                },
                                scope = coroutineScope
                            )
                        } else {
                            viewModel.showToast("مسجل بحساب: ${currentUser?.email}")
                        }
                    }
                )

                // Store Closed Alert Banner (when store is toggled closed by admin)
                if (!isStoreOpen) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_closed_banner"),
                        color = Color(0xFF7F1D1D) // Deep Maroon Alert
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🌙 المتجر مغلق حالياً • مواعيد استقبال الطلبات يومياً من ٨:٠٠ ص حتى ١٢:٠٠ منتصف الليل • يمكنك تصفح المنتجات وسنستقبل طلباتكم فور الفتح",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Live Admin Orders Alert Banner
                if (newOrdersCount > 0) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openAdminPin() }
                            .testTag("admin_live_new_orders_alert"),
                        color = Color(0xFFDC2626) // Vivid Alert Red
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = BrandYellowBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🚨 تنبيه للإدارة: لديك $newOrdersCount طلبات شراء جديدة واردة!",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "معاينة الطلبات ⬅",
                                color = BrandYellowBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Main Nav Header
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo and Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BrandRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "هايبر الحياة",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = BrandDark
                                    )
                                    Text(
                                        text = "Hyper Al-Hayat Market",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Cart Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BrandRed)
                                    .clickable { viewModel.openCart() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("cart_header_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "السلة",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "السلة",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color.White,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "$totalCartCount",
                                            color = BrandRed,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = {
                                Text(
                                    text = "ابحث عن أرز، زيت، سكر، جبنة، فواكه...",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "بحث",
                                    tint = BrandRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "مسح",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("main_search_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9FAFB),
                                unfocusedContainerColor = Color(0xFFF9FAFB),
                                focusedBorderColor = BrandRed,
                                unfocusedBorderColor = Color(0xFFE5E7EB)
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Floating Cart Bottom Bar when cart has items
            AnimatedVisibility(
                visible = totalCartCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$totalCartCount منتج في السلة",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.2f", subtotal)} ج.م",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandRed
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(BrandRed)
                                .clickable { viewModel.openCart() }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .testTag("floating_cart_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "عرض السلة وإتمام الطلب",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hero Banner with Rotating Daily Discount Offers Showcase & 1-Click Deals shortcut
            item(span = { GridItemSpan(2) }) {
                HeroBanner(
                    onOpenDeals = { viewModel.selectCategory("deals") },
                    products = allRawProducts,
                    cart = cart,
                    onAddToCart = { viewModel.addToCart(it) }
                )
            }

            // Categories Row Title
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "أقسام السوبرماركت",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandDark,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Categories horizontal scrolling row
            item(span = { GridItemSpan(2) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AvailableCategories, key = { it.id }) { cat ->
                        val isSelected = selectedCategory == cat.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) BrandRed else Color.White)
                                .clickable { viewModel.selectCategory(cat.id) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("cat_chip_${cat.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = cat.iconEmoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = cat.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else BrandDark
                                )
                            }
                        }
                    }
                }
            }

            // Section Header & Product Count
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeCatTitle = AvailableCategories.find { it.id == selectedCategory }?.title ?: "المنتجات"
                    Text(
                        text = "$activeCatTitle المتاحة",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandDark
                    )

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = "عرض ${products.size} منتج",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Empty state if search or category yields 0 products
            if (products.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لا توجد منتجات مطابقة للبحث حالياً",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandDark
                            )
                            Text(
                                text = "جرب البحث بكلمات أخرى أو اختر قسماً مختلفاً",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                // Product Grid items
                items(products, key = { it.id }) { product ->
                    val currentQty = cart[product.id]?.quantity ?: 0
                    ProductCard(
                        product = product,
                        currentCartQuantity = currentQty,
                        onAddToCart = { viewModel.addToCart(product) },
                        onIncreaseQty = { viewModel.updateCartQuantity(product.id, 1) },
                        onDecreaseQty = { viewModel.updateCartQuantity(product.id, -1) }
                    )
                }
            }

            // Footer with full details, address, branches, location, and social links
            item(span = { GridItemSpan(2) }) {
                StoreFooter()
            }
        }
    }

    // Cart Bottom Sheet
    if (showCartSheet) {
        CartBottomSheet(
            cartItems = cart.values.toList(),
            subtotal = subtotal,
            onDismiss = { viewModel.closeCart() },
            onIncreaseQty = { viewModel.updateCartQuantity(it, 1) },
            onDecreaseQty = { viewModel.updateCartQuantity(it, -1) },
            onRemoveItem = { viewModel.removeFromCart(it) },
            onCheckout = { viewModel.openCheckout() }
        )
    }

    // Checkout Dialog
    if (showCheckoutDialog) {
        CheckoutDialog(
            subtotal = subtotal,
            onDismiss = { viewModel.closeCheckout() },
            onSubmitOrder = { name, phone, addr, pay ->
                viewModel.submitOrder(name, phone, addr, pay)
            }
        )
    }

    // Invoice Success Dialog
    if (showInvoiceDialog && activeInvoiceOrder != null) {
        InvoiceDialog(
            order = activeInvoiceOrder!!,
            onDismiss = { viewModel.closeInvoice() }
        )
    }

    // Customer Past Orders Dialog
    if (showCustomerOrdersDialog) {
        CustomerOrdersDialog(
            orders = allOrders,
            onDismiss = { viewModel.closeCustomerOrders() },
            onViewOrderInvoice = { order ->
                viewModel.closeCustomerOrders()
                viewModel.showInvoiceForOrder(order)
            },
            onReorderOrder = { order ->
                viewModel.reorderOrder(order)
            }
        )
    }

    // Admin PIN Dialog
    if (showAdminPinDialog) {
        AdminPinDialog(
            onDismiss = { viewModel.closeAdminPin() },
            onVerifyPin = { pin -> viewModel.verifyAdminPin(pin) }
        )
    }

    // Admin Panel Dialog
    if (showAdminPanel) {
        AdminPanelDialog(
            products = allRawProducts,
            orders = allOrders,
            activeTab = adminActiveTab,
            onTabSelected = { viewModel.setAdminTab(it) },
            orderFilter = adminOrderFilter,
            onFilterSelected = { viewModel.setAdminOrderFilter(it) },
            onDismiss = { viewModel.closeAdminPanel() },
            onUpdateProduct = { id, p, w -> viewModel.adminUpdateProduct(id, p, w) },
            onAddProduct = { n, c, p, w, img -> viewModel.adminAddProduct(n, c, p, w, img) },
            onDeleteProduct = { viewModel.adminDeleteProduct(it) },
            onUpdateOrderStatus = { id, st -> viewModel.adminUpdateOrderStatus(id, st) },
            onUpdateAdminPin = { viewModel.updateAdminPin(it) },
            isStoreOpen = isStoreOpen,
            onToggleStoreOpen = { viewModel.toggleStoreOpen(it) }
        )
    }
}
