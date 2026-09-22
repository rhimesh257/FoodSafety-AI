package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.OfficialUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficialDao {
    @Query("SELECT * FROM officials")
    fun getAllOfficialsFlow(): Flow<List<OfficialUserEntity>>

    @Query("SELECT * FROM officials WHERE officerId = :id LIMIT 1")
    suspend fun getOfficialById(id: String): OfficialUserEntity?

    @Query("SELECT * FROM officials WHERE jurisdictionZone = :zone LIMIT 1")
    suspend fun getOfficialByZone(zone: String): OfficialUserEntity?

    @Query("SELECT * FROM officials LIMIT 1")
    suspend fun getDefaultOfficial(): OfficialUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfficials(officials: List<OfficialUserEntity>)
}
