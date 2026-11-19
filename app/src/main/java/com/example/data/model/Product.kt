package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@Entity(tableName = "products")
@IgnoreExtraProperties
data class Product(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val category: String = "grocery", // "grocery", "dairy", "fruits", "beverages", "bakery", "cleaning", "deals"
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val isFlashDeal: Boolean = false,
    val dealBadge: String = "",
    val weight: String = "",
    val stock: Int = 25,
    val imageUrl: String = "",
    val isAvailable: Boolean = true
)

data class ProductCategory(
    val id: String,
    val title: String,
    val iconEmoji: String
)

val AvailableCategories = listOf(
    ProductCategory("all", "كل المنتجات", "🛒"),
    ProductCategory("deals", "عروض التوفير ⚡", "🔥"),
    ProductCategory("fruits", "خضار وفواكه", "🍎"),
    ProductCategory("dairy", "ألبان وأجبان", "🧀"),
    ProductCategory("grocery", "تموين وزيوت", "🌾"),
    ProductCategory("beverages", "عصائر ومشروبات", "🧃"),
    ProductCategory("bakery", "مخبوزات", "🍞"),
    ProductCategory("cleaning", "منظفات", "🧹")
)
