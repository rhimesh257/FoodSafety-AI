package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "officials")
data class OfficialUserEntity(
    @PrimaryKey val officerId: String,
    val officerName: String,
    val designation: String, // "Food Safety Officer (FSO)", "Designated Officer (DO)", "State Food Commissioner"
    val jurisdictionZone: String,
    val contactNumber: String,
    val activeInspectionsCount: Int = 0
)
