package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET price = :newPrice, weight = :newWeight WHERE id = :productId")
    suspend fun updatePriceAndWeight(productId: String, newPrice: Double, newWeight: String)

    @Query("UPDATE products SET stock = :newStock, isAvailable = :isAvailable WHERE id = :productId")
    suspend fun updateStock(productId: String, newStock: Int, isAvailable: Boolean)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}
