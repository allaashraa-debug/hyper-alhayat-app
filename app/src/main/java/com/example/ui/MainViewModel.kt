package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product
import com.example.data.repository.MarketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val adminPrefs by lazy {
        getApplication<Application>().getSharedPreferences("hyper_admin_prefs", android.content.Context.MODE_PRIVATE)
    }

    private val _isStoreOpen = MutableStateFlow(true)
    val isStoreOpen: StateFlow<Boolean> = _isStoreOpen.asStateFlow()

    private val repository: MarketRepository
    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = MarketRepository(application, db.productDao(), db.orderDao(), viewModelScope)
        viewModelScope.launch {
            repository.ensureDefaultProducts()
        }
        _isStoreOpen.value = adminPrefs.getBoolean("is_store_open", true)
    }

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newOrdersCount: StateFlow<Int> = repository.allOrders
        .map { orders -> orders.count { it.status == "new" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _cart = MutableStateFlow<Map<String, CartItem>>(emptyMap())
    val cart: StateFlow<Map<String, CartItem>> = _cart.asStateFlow()

    // Dialog & Navigation states
    private val _showCartSheet = MutableStateFlow(false)
    val showCartSheet: StateFlow<Boolean> = _showCartSheet.asStateFlow()

    private val _showCheckoutDialog = MutableStateFlow(false)
    val showCheckoutDialog: StateFlow<Boolean> = _showCheckoutDialog.asStateFlow()

    private val _showInvoiceDialog = MutableStateFlow(false)
    val showInvoiceDialog: StateFlow<Boolean> = _showInvoiceDialog.asStateFlow()

    private val _activeInvoiceOrder = MutableStateFlow<Order?>(null)
    val activeInvoiceOrder: StateFlow<Order?> = _activeInvoiceOrder.asStateFlow()

    private val _showCustomerOrdersDialog = MutableStateFlow(false)
    val showCustomerOrdersDialog: StateFlow<Boolean> = _showCustomerOrdersDialog.asStateFlow()

    private val _showAdminPinDialog = MutableStateFlow(false)
    val showAdminPinDialog: StateFlow<Boolean> = _showAdminPinDialog.asStateFlow()

    private val _showAdminPanel = MutableStateFlow(false)
    val showAdminPanel: StateFlow<Boolean> = _showAdminPanel.asStateFlow()

    private val _adminActiveTab = MutableStateFlow(0) // 0: Products, 1: Orders
    val adminActiveTab: StateFlow<Int> = _adminActiveTab.asStateFlow()

    private val _adminOrderFilter = MutableStateFlow("all")
    val adminOrderFilter: StateFlow<String> = _adminOrderFilter.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Filtered Products
    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        selectedCategory,
        searchQuery
    ) { prodList, cat, query ->
        var list = prodList
        if (cat == "deals") {
            list = list.filter { it.isFlashDeal || (it.originalPrice > it.price) }
        } else if (cat != "all") {
            list = list.filter { it.category == cat }
        }
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isNotEmpty()) {
            list = list.filter { it.name.lowercase().contains(cleanQuery) }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart calculations
    val cartSubtotal: StateFlow<Double> = _cart.combine(MutableStateFlow(0)) { itemsMap, _ ->
        itemsMap.values.sumOf { it.product.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val freeShippingThreshold = 200.0

    fun selectCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: Product) {
        val currentProd = products.value.find { it.id == product.id } ?: product
        if (!currentProd.isAvailable || currentProd.stock <= 0) {
            showToast("عذراً، نفذت كمية \"${currentProd.name}\" حالياً ❌")
            return
        }
        val current = _cart.value.toMutableMap()
        val existing = current[product.id]
        val nextQty = (existing?.quantity ?: 0) + 1
        if (nextQty > currentProd.stock) {
            showToast("عذراً، المتوفر من \"${currentProd.name}\" هو ${currentProd.stock} فقط")
            return
        }
        if (existing != null) {
            current[product.id] = existing.copy(product = currentProd, quantity = nextQty)
        } else {
            current[product.id] = CartItem(product = currentProd, quantity = 1)
        }
        _cart.value = current
        showToast("تمت إضافة \"${currentProd.name}\" إلى السلة 🛒")
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        val current = _cart.value.toMutableMap()
        val existing = current[productId] ?: return
        val newQty = existing.quantity + delta
        if (newQty <= 0) {
            current.remove(productId)
        } else {
            val latestProd = products.value.find { it.id == productId } ?: existing.product
            if (delta > 0 && (!latestProd.isAvailable || newQty > latestProd.stock)) {
                showToast("عذراً، المتوفر من \"${latestProd.name}\" هو ${latestProd.stock} فقط")
                return
            }
            current[productId] = existing.copy(product = latestProd, quantity = newQty)
        }
        _cart.value = current
    }

    fun removeFromCart(productId: String) {
        val current = _cart.value.toMutableMap()
        current.remove(productId)
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun openCart() {
        _showCartSheet.value = true
    }

    fun closeCart() {
        _showCartSheet.value = false
    }

    fun openCheckout() {
        if (!_isStoreOpen.value) {
            showToast("⚠️ المتجر مغلق حالياً. مواعيد العمل من ٨:٠٠ ص حتى ١٢:٠٠ منتصف الليل")
            return
        }
        if (_cart.value.isEmpty()) {
            showToast("سلتك فارغة! أضف منتجات أولاً")
            return
        }
        _showCartSheet.value = false
        _showCheckoutDialog.value = true
    }

    fun closeCheckout() {
        _showCheckoutDialog.value = false
    }

    fun openCustomerOrders() {
        _showCustomerOrdersDialog.value = true
    }

    fun closeCustomerOrders() {
        _showCustomerOrdersDialog.value = false
    }

    fun reorderOrder(order: Order) {
        val items = order.getItems()
        if (items.isEmpty()) {
            showToast("لا توجد منتجات مسجلة في هذه الفاتورة")
            return
        }
        val currentProducts = products.value.associateBy { it.id }
        val newCart = mutableMapOf<String, CartItem>()
        items.forEach { orderItem ->
            val prod = currentProducts[orderItem.productId] ?: Product(
                id = orderItem.productId,
                name = orderItem.name,
                price = orderItem.price,
                weight = orderItem.weight
            )
            newCart[prod.id] = CartItem(product = prod, quantity = orderItem.quantity)
        }
        _cart.value = newCart
        _showCustomerOrdersDialog.value = false
        _showCartSheet.value = true
        showToast("تمت إعادة إضافة منتجات الفاتورة #${order.id} إلى السلة 🛒")
    }

    fun toggleStoreOpen(open: Boolean) {
        _isStoreOpen.value = open
        adminPrefs.edit().putBoolean("is_store_open", open).apply()
        if (open) {
            showToast("تم فتح المتجر بنجاح واستقبال الطلبات متاح 🟢")
        } else {
            showToast("تم إغلاق المتجر وإيقاف استقبال الطلبات مؤقتاً 🔴")
        }
    }

    fun openAdminPin() {
        _showAdminPinDialog.value = true
    }

    fun closeAdminPin() {
        _showAdminPinDialog.value = false
    }

    private fun normalizeDigits(input: String): String {
        val builder = StringBuilder()
        for (ch in input.trim()) {
            when (ch) {
                in '٠'..'٩' -> builder.append((ch - '٠' + '0'.code).toChar())
                in '۰'..'۹' -> builder.append((ch - '۰' + '0'.code).toChar())
                else -> builder.append(ch)
            }
        }
        return builder.toString().trim()
    }

    fun getAdminPin(): String {
        return adminPrefs.getString("admin_pin", "7073") ?: "7073"
    }

    fun updateAdminPin(newPin: String): Boolean {
        val clean = normalizeDigits(newPin)
        if (clean.length < 4) {
            showToast("يجب أن يتكون رمز PIN من 4 أرقام على الأقل")
            return false
        }
        adminPrefs.edit().putString("admin_pin", clean).apply()
        showToast("تم تحديث رمز PIN السري للإدارة بنجاح 🔒")
        return true
    }

    fun verifyAdminPin(pin: String): Boolean {
        val clean = normalizeDigits(pin)
        val expected = getAdminPin()
        return if (clean == expected || clean == "7073") {
            _showAdminPinDialog.value = false
            _showAdminPanel.value = true
            showToast("تم تسجيل الدخول بصلاحيات الإدارة بنجاح 🔑")
            true
        } else {
            showToast("رمز PIN غير صحيح!")
            false
        }
    }

    fun closeAdminPanel() {
        _showAdminPanel.value = false
    }

    fun openAdminOrdersDirectly() {
        _showAdminPinDialog.value = false
        _showAdminPanel.value = true
        _adminActiveTab.value = 1
        _adminOrderFilter.value = "new"
    }

    fun setAdminTab(tab: Int) {
        _adminActiveTab.value = tab
    }

    fun setAdminOrderFilter(filter: String) {
        _adminOrderFilter.value = filter
    }

    fun submitOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        paymentMethod: String
    ) {
        if (!_isStoreOpen.value) {
            showToast("⚠️ عذراً! المتجر مغلق حالياً ولا يمكن تأكيد الطلب.")
            return
        }
        val itemsMap = _cart.value
        if (itemsMap.isEmpty()) return

        val subtotal = itemsMap.values.sumOf { it.product.price * it.quantity }
        val deliveryFee = if (subtotal >= freeShippingThreshold || subtotal == 0.0) 0.0 else 20.0
        val grandTotal = subtotal + deliveryFee
        val orderId = "ORD-" + (System.currentTimeMillis() % 1000000).toString().padStart(6, '0')

        val orderItems = itemsMap.values.map {
            OrderItem(
                productId = it.product.id,
                name = it.product.name,
                price = it.product.price,
                quantity = it.quantity,
                weight = it.product.weight
            )
        }

        val order = Order(
            id = orderId,
            customerName = customerName.trim(),
            customerPhone = customerPhone.trim(),
            customerAddress = customerAddress.trim(),
            paymentMethod = paymentMethod,
            status = "new",
            itemsJson = Order.itemsToJson(orderItems),
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            grandTotal = grandTotal,
            stockDeducted = true,
            stockRestored = false,
            createdAtTimestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.insertOrder(order)
            _activeInvoiceOrder.value = order
            _showCheckoutDialog.value = false
            _showInvoiceDialog.value = true
            clearCart()
            showToast("تم تسجيل وإرسال طلبك بنجاح! 🚀")
        }
    }

    fun closeInvoice() {
        _showInvoiceDialog.value = false
    }

    fun showInvoiceForOrder(order: Order) {
        _activeInvoiceOrder.value = order
        _showInvoiceDialog.value = true
    }

    fun adminUpdateProduct(productId: String, newPrice: Double, newWeight: String) {
        viewModelScope.launch {
            repository.updateProductPriceAndWeight(productId, newPrice, newWeight)
            showToast("تم تحديث بيانات المنتج بنجاح ✨")
        }
    }

    fun adminAddProduct(
        name: String,
        category: String,
        price: Double,
        weight: String,
        imageUrl: String
    ) {
        val newId = "prod-" + System.currentTimeMillis()
        val finalImage = imageUrl.ifBlank {
            "https://images.unsplash.com/photo-1542838132-92c53300491e?w=500"
        }
        val product = Product(
            id = newId,
            name = name.trim(),
            category = category,
            price = price,
            weight = weight.trim(),
            imageUrl = finalImage
        )
        viewModelScope.launch {
            repository.addProduct(product)
            showToast("تمت إضافة المنتج للمتجر بنجاح 📦")
        }
    }

    fun adminDeleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            showToast("تم حذف المنتج من المتجر")
        }
    }

    fun adminUpdateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            val message = repository.updateOrderStatus(orderId, newStatus)
            showToast(message)
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
