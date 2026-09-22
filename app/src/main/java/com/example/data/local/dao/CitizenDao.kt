package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CitizenUser
import kotlinx.coroutines.flow.Flow

@Dao
interface CitizenDao {
    @Query("SELECT * FROM citizens WHERE id = :userId LIMIT 1")
    fun getCitizenFlow(userId: String): Flow<CitizenUser?>

    @Query("SELECT * FROM citizens WHERE id = :userId LIMIT 1")
    suspend fun getCitizenById(userId: String): CitizenUser?

    @Query("SELECT * FROM citizens LIMIT 1")
    fun getFirstCitizenFlow(): Flow<CitizenUser?>

    @Query("SELECT * FROM citizens LIMIT 1")
    suspend fun getFirstCitizen(): CitizenUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCitizen(citizen: CitizenUser)

    @Update
    suspend fun updateCitizen(citizen: CitizenUser)

    @Query("UPDATE citizens SET walletBalanceINR = walletBalanceINR + :amount WHERE id = :userId")
    suspend fun creditWallet(userId: String, amount: Double)

    @Query("UPDATE citizens SET walletBalanceINR = walletBalanceINR - :amount WHERE id = :userId AND walletBalanceINR >= :amount")
    suspend fun debitWallet(userId: String, amount: Double): Int
}
