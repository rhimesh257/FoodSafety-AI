package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fssai_businesses")
data class FoodBusinessEntity(
    @PrimaryKey val licenseNumber: String, // 14-digit FSSAI number
    val businessName: String,
    val category: String, // "Restaurant", "Cloud Kitchen", "Sweet Shop & Dairy", "Street Food Vendor"
    val address: String,
    val licenseExpiryDate: String,
    val isExpired: Boolean,
    val hasFssaiLicense: Boolean,
    val lastLabInspectionDate: String,
    val hygieneRatingScore: Int, // 1 to 5
    val pastViolationsCount: Int,
    val designatedOfficerId: String
)
