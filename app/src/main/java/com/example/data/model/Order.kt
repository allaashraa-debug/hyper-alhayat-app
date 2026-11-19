package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import org.json.JSONArray

data class CartItem(
    val product: Product = Product(),
    val quantity: Int = 1
)

data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val weight: String = ""
)

@Entity(tableName = "orders")
@IgnoreExtraProperties
data class Order(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val paymentMethod: String = "cash", // "cash" or "card"
    val status: String = "new", // "new", "processing", "delivery", "completed", "cancelled", "archived"
    val itemsJson: String = "",
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val grandTotal: Double = 0.0,
    val stockDeducted: Boolean = false,
    val stockRestored: Boolean = false,
    val createdAtTimestamp: Long = System.currentTimeMillis()
) {
    fun getItems(): List<OrderItem> {
        val result = mutableListOf<OrderItem>()
        if (itemsJson.isBlank()) return result

        try {
            val trimmed = itemsJson.trim()
            // Check JSON array format (used by Web store)
            if (trimmed.startsWith("[")) {
                val arr = JSONArray(trimmed)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val name = obj.optString("name", "").trim()
                    if (name.isNotEmpty()) {
                        result.add(
                            OrderItem(
                                productId = obj.optString("productId", obj.optString("id", "")),
                                name = name,
                                price = obj.optDouble("price", 0.0),
                                quantity = obj.optInt("quantity", obj.optInt("qty", 1)),
                                weight = obj.optString("weight", "")
                            )
                        )
                    }
                }
            } else if (trimmed.contains("~")) {
                // Delimiter format: id~name~price~qty~weight
                val lines = trimmed.split(";;")
                for (line in lines) {
                    val parts = line.split("~")
                    if (parts.size >= 5) {
                        result.add(
                            OrderItem(
                                productId = parts[0],
                                name = parts[1],
                                price = parts[2].toDoubleOrNull() ?: 0.0,
                                quantity = parts[3].toIntOrNull() ?: 1,
                                weight = parts[4]
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore malformed itemsJson
        }
        return result
    }

    companion object {
        fun itemsToJson(items: List<OrderItem>): String {
            return items.joinToString(";;") { item ->
                "${item.productId}~${item.name}~${item.price}~${item.quantity}~${item.weight}"
            }
        }
    }
}
