package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val grievanceId: String,
    val amount: Double,
    val type: String, // "CREDIT_REWARD", "WITHDRAWAL_INR"
    val title: String,
    val subtitle: String,
    val bankAccountMasked: String? = null,
    val ifscCode: String? = null,
    val status: String = "SUCCESS", // "SUCCESS", "PROCESSING"
    val timestamp: Long = System.currentTimeMillis()
)
