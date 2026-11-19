package com.example

import com.example.data.local.DefaultProducts
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.util.OrderWhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleMarketUnitTest {

    @Test
    fun defaultProductsListContainsSupermarketItems() {
        val defaultList = DefaultProducts.getList()
        assertTrue(defaultList.isNotEmpty())
        assertEquals(16, defaultList.size)
        assertTrue(defaultList.any { it.name.contains("أرز") })
    }

    @Test
    fun orderItemsJsonSerializationAndDeserialization() {
        val items = listOf(
            OrderItem("p1", "أرز الضحى فاخر", 34.0, 2, "1 كجم"),
            OrderItem("p2", "سكر أبيض ناعم", 35.0, 1, "1 كجم")
        )
        val json = Order.itemsToJson(items)
        val order = Order(
            id = "ORD-123456",
            customerName = "أحمد محمد",
            customerPhone = "01012345678",
            customerAddress = "القاهرة، مصر",
            paymentMethod = "cash",
            status = "new",
            itemsJson = json,
            subtotal = 103.0,
            deliveryFee = 20.0,
            grandTotal = 123.0
        )

        val retrievedItems = order.getItems()
        assertEquals(2, retrievedItems.size)
        assertEquals("أرز الضحى فاخر", retrievedItems[0].name)
        assertEquals(2, retrievedItems[0].quantity)

        val waMessage = OrderWhatsAppHelper.formatOrderMessage(order)
        assertTrue(waMessage.contains("ORD-123456"))
        assertTrue(waMessage.contains("أحمد محمد"))
        assertTrue(waMessage.contains("123.00 ج.م"))
    }
}
