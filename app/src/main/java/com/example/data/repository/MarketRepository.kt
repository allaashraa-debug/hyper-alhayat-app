package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.DefaultProducts
import com.example.data.local.OrderDao
import com.example.data.local.ProductDao
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.util.OrderNotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MarketRepository(
    private val context: Context,
    private val productDao: ProductDao,
    private val orderDao: OrderDao,
    private val scope: CoroutineScope
) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.getString(R.string.firestore_database_id)
    )

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()

    private var isFirstOrdersSnapshot = true
    private val notifiedOrderIds = mutableSetOf<String>()
    private val pendingOrderStatusUpdates = ConcurrentHashMap.newKeySet<String>()
    @Volatile
    private var deletedProductIds: Set<String> = emptySet()

    init {
        OrderNotificationHelper.initAndSubscribe(context)
        syncDeletedProductsFromFirestore()
        syncProductsFromFirestore()
        syncStoreOrdersFromFirestore()
    }

    private fun docToProduct(doc: DocumentSnapshot): Product? {
        return try {
            val data = doc.data ?: return null
            val name = (data["name"] as? String)?.trim().orEmpty()
            if (name.isEmpty()) return null
            val isFlash = (data["isFlashDeal"] as? Boolean)
                ?: (data["flashDeal"] as? Boolean)
                ?: false
            val isAvail = (data["isAvailable"] as? Boolean)
                ?: (data["available"] as? Boolean)
                ?: true
            val stockVal = (data["stock"] as? Number)?.toInt()?.coerceAtLeast(0)
                ?: if (isAvail) 25 else 0
            Product(
                id = doc.id,
                name = name,
                category = (data["category"] as? String) ?: "grocery",
                price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                originalPrice = (data["originalPrice"] as? Number)?.toDouble() ?: 0.0,
                isFlashDeal = isFlash,
                dealBadge = (data["dealBadge"] as? String) ?: "",
                weight = (data["weight"] as? String) ?: "",
                stock = stockVal,
                imageUrl = (data["imageUrl"] as? String) ?: "",
                isAvailable = isAvail && stockVal > 0
            )
        } catch (e: Exception) {
            Log.w("Firestore", "Failed to parse product ${doc.id}: ${e.message}")
            null
        }
    }

    private fun docToOrder(doc: DocumentSnapshot): Order? {
        return try {
            val data = doc.data ?: return null
            Order(
                id = doc.id,
                userId = (data["userId"] as? String) ?: "guest",
                customerName = (data["customerName"] as? String) ?: "",
                customerPhone = (data["customerPhone"] as? String) ?: "",
                customerAddress = (data["customerAddress"] as? String) ?: "",
                paymentMethod = (data["paymentMethod"] as? String) ?: "cash",
                status = (data["status"] as? String) ?: "new",
                itemsJson = (data["itemsJson"] as? String) ?: "",
                subtotal = (data["subtotal"] as? Number)?.toDouble() ?: 0.0,
                deliveryFee = (data["deliveryFee"] as? Number)?.toDouble() ?: 0.0,
                grandTotal = (data["grandTotal"] as? Number)?.toDouble() ?: 0.0,
                stockDeducted = (data["stockDeducted"] as? Boolean) ?: false,
                stockRestored = (data["stockRestored"] as? Boolean) ?: false,
                createdAtTimestamp = (data["createdAtTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.w("Firestore", "Failed to parse order ${doc.id}: ${e.message}")
            null
        }
    }

    private fun syncDeletedProductsFromFirestore() {
        try {
            db.collection("store_settings").document("deleted_products")
                .addSnapshotListener { doc, error ->
                    if (error != null) return@addSnapshotListener
                    try {
                        val ids = (doc?.get("ids") as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                        deletedProductIds = ids
                        if (ids.isNotEmpty()) {
                            scope.launch(Dispatchers.IO) {
                                ids.forEach { delId -> productDao.deleteProductById(delId) }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("Firestore", "Failed to process deleted_products: ${e.message}")
                    }
                }
        } catch (e: Exception) {
            Log.w("Firestore", "Failed to initialize deleted_products listener: ${e.message}")
        }
    }

    private fun syncProductsFromFirestore() {
        try {
            db.collection("products").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("Firestore", "Products sync listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                try {
                    if (snapshot != null && !snapshot.isEmpty) {
                        val deleted = deletedProductIds
                        val cloudProducts = snapshot.documents
                            .mapNotNull { docToProduct(it) }
                            .filter { it.id !in deleted }
                        if (cloudProducts.isNotEmpty()) {
                            scope.launch(Dispatchers.IO) {
                                productDao.insertProducts(cloudProducts)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("Firestore", "Error processing products snapshot: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.w("Firestore", "Failed to initialize product listener: ${e.message}")
        }
    }

    private fun syncStoreOrdersFromFirestore() {
        try {
            db.collection("store_orders").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("Firestore", "Store orders sync listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                try {
                    if (snapshot != null) {
                        val isFirst = isFirstOrdersSnapshot
                        isFirstOrdersSnapshot = false

                        val cloudOrders = snapshot.documents.mapNotNull { docToOrder(it) }

                        if (isFirst) {
                            cloudOrders.forEach { notifiedOrderIds.add(it.id) }
                        } else {
                            // Check for new incoming orders
                            for (dc in snapshot.documentChanges) {
                                if (dc.type == DocumentChange.Type.ADDED) {
                                    val order = docToOrder(dc.document) ?: continue
                                    if (order.id !in notifiedOrderIds && order.status == "new") {
                                        notifiedOrderIds.add(order.id)
                                        OrderNotificationHelper.showNewOrderNotification(
                                            context = context,
                                            orderId = order.id,
                                            customerName = order.customerName,
                                            total = order.grandTotal
                                        )
                                    }
                                }
                            }
                        }

                        if (cloudOrders.isNotEmpty()) {
                            scope.launch(Dispatchers.IO) {
                                for (order in cloudOrders) {
                                    orderDao.insertOrder(order)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("Firestore", "Error processing store_orders snapshot: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.w("Firestore", "Failed to initialize store_orders listener: ${e.message}")
        }
    }

    suspend fun ensureDefaultProducts() = withContext(Dispatchers.IO) {
        val count = productDao.getProductCount()
        if (count == 0) {
            val defaults = DefaultProducts.getList()
            productDao.insertProducts(defaults)
            try {
                val snapshot = db.collection("products").limit(1).get().await()
                if (snapshot.isEmpty) {
                    val batch = db.batch()
                    defaults.forEach { prod ->
                        val docRef = db.collection("products").document(prod.id)
                        batch.set(docRef, prod)
                    }
                    batch.commit().await()
                    Log.d("Firestore", "Seeded ${defaults.size} products to Firestore successfully")
                }
            } catch (e: Exception) {
                Log.w("Firestore", "Could not seed defaults to cloud: ${e.message}")
            }
        }
    }

    suspend fun addProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
        try {
            db.collection("products").document(product.id).set(product).await()
        } catch (e: Exception) {
            Log.w("Firestore", "Failed to sync added product to cloud: ${e.message}")
        }
    }

    suspend fun updateProductPriceAndWeight(productId: String, newPrice: Double, newWeight: String) =
        withContext(Dispatchers.IO) {
            productDao.updatePriceAndWeight(productId, newPrice, newWeight)
            try {
                db.collection("products").document(productId).update(
                    mapOf(
                        "price" to newPrice,
                        "weight" to newWeight
                    )
                ).await()
            } catch (e: Exception) {
                Log.w("Firestore", "Failed to update product in cloud: ${e.message}")
            }
        }

    suspend fun deleteProduct(productId: String) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(productId)
        try {
            db.collection("products").document(productId).delete().await()
        } catch (e: Exception) {
            Log.w("Firestore", "Failed to delete product in cloud: ${e.message}")
        }
    }

    private suspend fun resolveOrderProductQuantities(order: Order): List<Pair<Product, Int>> {
        val items = order.getItems()
        if (items.isEmpty()) return emptyList()
        val grouped = linkedMapOf<String, Pair<Product, Int>>()
        for (item in items) {
            val qty = item.quantity.coerceAtLeast(0)
            if (qty <= 0) continue
            val existingProd = if (item.productId.isNotBlank()) {
                productDao.getProductById(item.productId)
            } else null
            val resolvedProd = existingProd ?: Product(
                id = item.productId.ifBlank { item.name },
                name = item.name,
                price = item.price,
                weight = item.weight,
                stock = 25,
                isAvailable = true
            )
            val key = resolvedProd.id
            val prev = grouped[key]
            grouped[key] = if (prev != null) {
                prev.copy(second = prev.second + qty)
            } else {
                resolvedProd to qty
            }
        }
        return grouped.values.toList()
    }

    private fun productStockPayload(prod: Product, newStock: Int): Map<String, Any> {
        val safeStock = newStock.coerceAtLeast(0)
        return mapOf(
            "id" to prod.id,
            "name" to prod.name,
            "category" to prod.category,
            "price" to prod.price,
            "originalPrice" to prod.originalPrice,
            "isFlashDeal" to prod.isFlashDeal,
            "flashDeal" to prod.isFlashDeal,
            "dealBadge" to prod.dealBadge,
            "weight" to prod.weight,
            "stock" to safeStock,
            "available" to (safeStock > 0),
            "isAvailable" to (safeStock > 0),
            "imageUrl" to prod.imageUrl
        )
    }

    suspend fun insertOrder(order: Order) = withContext(Dispatchers.IO) {
        val currentUserId = Firebase.auth.currentUser?.uid ?: "guest"
        val orderToSave = order.copy(
            userId = currentUserId,
            stockDeducted = true,
            stockRestored = false
        )
        orderDao.insertOrder(orderToSave)

        val aggregated = resolveOrderProductQuantities(orderToSave)

        // Deduct local product stocks immediately
        aggregated.forEach { (prod, qty) ->
            val newLocalStock = (prod.stock - qty).coerceAtLeast(0)
            productDao.updateStock(prod.id, newLocalStock, newLocalStock > 0)
        }

        // 1. Atomically deduct stock & save order to store_orders cloud
        try {
            val orderRef = db.collection("store_orders").document(order.id)
            val prodRefs = aggregated.map { (prod, qty) ->
                Triple(prod, qty, db.collection("products").document(prod.id))
            }
            db.runTransaction { tx ->
                val prodSnapshots = prodRefs.map { (prod, qty, ref) ->
                    val snap = tx.get(ref)
                    Triple(prod, qty, snap to ref)
                }
                prodSnapshots.forEach { (prod, qty, snapAndRef) ->
                    val (snap, ref) = snapAndRef
                    val currentStock = (snap.getLong("stock")?.toInt()) ?: prod.stock
                    val newStock = (currentStock - qty).coerceAtLeast(0)
                    tx.set(ref, productStockPayload(prod, newStock), SetOptions.merge())
                }
                tx.set(orderRef, orderToSave)
                null
            }.await()
            Log.d("Firestore", "Order ${order.id} uploaded and stock deducted atomically")
        } catch (e: Exception) {
            Log.w("Firestore", "Transaction upload failed, falling back to set: ${e.message}")
            try {
                db.collection("store_orders").document(order.id).set(orderToSave).await()
            } catch (e2: Exception) {
                Log.w("Firestore", "Failed to sync order to store_orders: ${e2.message}")
            }
        }

        // 2. Also sync to user's personal orders subcollection if signed in
        val authUser = Firebase.auth.currentUser
        if (authUser != null) {
            try {
                db.collection("users")
                    .document(authUser.uid)
                    .collection("orders")
                    .document(order.id)
                    .set(orderToSave)
                    .await()
            } catch (e: Exception) {
                Log.w("Firestore", "Failed to sync order to user collection: ${e.message}")
            }
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): String = withContext(Dispatchers.IO) {
        if (!pendingOrderStatusUpdates.add(orderId)) {
            return@withContext "جاري معالجة تحديث هذا الطلب بالفعل..."
        }

        try {
            val localOrder = orderDao.getOrderById(orderId)
            if (newStatus == "cancelled" && localOrder?.status == "cancelled" && localOrder.stockRestored) {
                return@withContext "الطلب ملغي بالفعل وتم إرجاع كمياته للمخزون مسبقاً ✅"
            }

            val resolvedItems = if (localOrder != null) resolveOrderProductQuantities(localOrder) else emptyList()
            val orderRef = db.collection("store_orders").document(orderId)

            var finalDeducted = localOrder?.stockDeducted ?: false
            var finalRestored = localOrder?.stockRestored ?: false
            val updatedProductStocks = mutableListOf<Pair<String, Int>>()

            val txAction: String = try {
                db.runTransaction { tx ->
                    val orderSnap = tx.get(orderRef)
                    val serverDeducted = orderSnap.getBoolean("stockDeducted") ?: (localOrder?.stockDeducted ?: false)
                    val serverRestored = orderSnap.getBoolean("stockRestored") ?: (localOrder?.stockRestored ?: false)

                    val prodReads = resolvedItems.map { (prod, qty) ->
                        val ref = db.collection("products").document(prod.id)
                        val snap = tx.get(ref)
                        Triple(prod, qty, snap to ref)
                    }

                    if (newStatus == "cancelled") {
                        if (serverDeducted && !serverRestored && prodReads.isNotEmpty()) {
                            prodReads.forEach { (prod, qty, snapAndRef) ->
                                val (snap, ref) = snapAndRef
                                val baseStock = (snap.getLong("stock")?.toInt()) ?: prod.stock
                                val restoredStock = baseStock.coerceAtLeast(0) + qty
                                tx.set(ref, productStockPayload(prod, restoredStock), SetOptions.merge())
                                updatedProductStocks.add(prod.id to restoredStock)
                            }
                            tx.set(
                                orderRef,
                                mapOf(
                                    "status" to "cancelled",
                                    "stockDeducted" to true,
                                    "stockRestored" to true,
                                    "cancelledAt" to System.currentTimeMillis()
                                ),
                                SetOptions.merge()
                            )
                            finalDeducted = true
                            finalRestored = true
                            "restored"
                        } else {
                            tx.set(
                                orderRef,
                                mapOf(
                                    "status" to "cancelled",
                                    "cancelledAt" to System.currentTimeMillis()
                                ),
                                SetOptions.merge()
                            )
                            finalDeducted = serverDeducted
                            finalRestored = serverRestored
                            if (serverRestored) "already_restored" else "cancelled_no_deduct"
                        }
                    } else {
                        val shouldDeduct = (serverRestored || (newStatus == "completed" && !serverDeducted)) && prodReads.isNotEmpty()
                        if (shouldDeduct) {
                            prodReads.forEach { (prod, qty, snapAndRef) ->
                                val (snap, ref) = snapAndRef
                                val baseStock = (snap.getLong("stock")?.toInt()) ?: prod.stock
                                val deductedStock = (baseStock - qty).coerceAtLeast(0)
                                tx.set(ref, productStockPayload(prod, deductedStock), SetOptions.merge())
                                updatedProductStocks.add(prod.id to deductedStock)
                            }
                            tx.set(
                                orderRef,
                                mapOf(
                                    "status" to newStatus,
                                    "stockDeducted" to true,
                                    "stockRestored" to false
                                ),
                                SetOptions.merge()
                            )
                            finalDeducted = true
                            finalRestored = false
                            "deducted"
                        } else {
                            tx.set(orderRef, mapOf("status" to newStatus), SetOptions.merge())
                            finalDeducted = serverDeducted
                            finalRestored = serverRestored
                            "status_only"
                        }
                    }
                }.await()
            } catch (e: Exception) {
                Log.w("Firestore", "Order status transaction failed: ${e.message}")
                return@withContext "تعذر تحديث حالة الطلب سحابياً، تأكد من الاتصال بالإنترنت"
            }

            // Mirror updates into local Room database immediately
            orderDao.updateOrderStatusAndStockFlags(orderId, newStatus, finalDeducted, finalRestored)
            updatedProductStocks.forEach { (pid, newStock) ->
                productDao.updateStock(pid, newStock, newStock > 0)
            }

            // Update user subcollection if signed in & owned by current user
            val authUser = Firebase.auth.currentUser
            if (authUser != null && (localOrder == null || localOrder.userId == authUser.uid)) {
                try {
                    db.collection("users")
                        .document(authUser.uid)
                        .collection("orders")
                        .document(orderId)
                        .update(
                            mapOf(
                                "status" to newStatus,
                                "stockDeducted" to finalDeducted,
                                "stockRestored" to finalRestored
                            )
                        )
                        .await()
                } catch (e: Exception) {
                    Log.w("Firestore", "Failed to update order status in user collection: ${e.message}")
                }
            }

            when (txAction) {
                "restored" -> "تم إلغاء الطلب وإرجاع جميع الكميات إلى المخزون في السحابة تلقائياً ♻️"
                "already_restored" -> "الطلب ملغي بالفعل وتم إرجاع مخزونه مسبقاً ولن يُكرر الإرجاع ✅"
                "cancelled_no_deduct" -> "تم إلغاء الطلب (لم يكن قد تم خصم مخزونه مسبقاً)"
                "deducted" -> "تم تحديث حالة الطلب وخصم الكميات من المخزون بنجاح 📦"
                else -> "تم تحديث حالة الطلب بنجاح"
            }
        } finally {
            pendingOrderStatusUpdates.remove(orderId)
        }
    }
}
