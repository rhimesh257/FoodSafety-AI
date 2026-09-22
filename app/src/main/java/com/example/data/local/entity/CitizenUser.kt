package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citizens")
data class CitizenUser(
    @PrimaryKey val id: String, // e.g. "CIT-8821"
    val fullName: String,
    val aadharNumber: String, // formatted/masked 12 digits
    val panNumber: String,    // 10 chars
    val email: String,
    val mobileNumber: String,
    val permanentAddress: String,
    val temporaryAddress: String,
    val age: Int,
    val isOtpVerified: Boolean = true,
    val walletBalanceINR: Double = 0.0
)
