package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FoodBusinessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodBusinessDao {
    @Query("SELECT * FROM fssai_businesses")
    fun getAllBusinessesFlow(): Flow<List<FoodBusinessEntity>>

    @Query("SELECT * FROM fssai_businesses WHERE businessName LIKE '%' || :query || '%' OR licenseNumber LIKE '%' || :query || '%'")
    suspend fun searchBusinesses(query: String): List<FoodBusinessEntity>

    @Query("SELECT * FROM fssai_businesses WHERE licenseNumber = :licenseNumber LIMIT 1")
    suspend fun getBusinessByLicense(licenseNumber: String): FoodBusinessEntity?

    @Query("SELECT * FROM fssai_businesses WHERE businessName LIKE '%' || :name || '%' LIMIT 1")
    suspend fun getBusinessByName(name: String): FoodBusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinesses(businesses: List<FoodBusinessEntity>)
}
