package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grievances")
data class GrievanceEntity(
    @PrimaryKey val grievanceId: String, // e.g., "GRV-2026-7821"
    val userId: String,
    val citizenName: String,
    val issueType: String, // "ONLINE" or "OFFLINE"
    
    // Online order invoice fields
    val invoiceUploaded: Boolean = false,
    val invoiceUri: String? = null,
    val invoiceOcrStatus: String = "NONE", // "VALID", "UNCLEAR_UNDER_REVIEW", "NOT_REQUIRED"
    val invoiceDetailsSummary: String? = null,
    
    // Citizen input
    val foodBusinessName: String,
    val foodBusinessLicenseNumber: String,
    val keyword: String,
    val description: String,
    val voiceNoteDurationSec: Int = 0,
    val voiceNotePath: String? = null,
    val photosCount: Int, // 2 to 15
    val photosJson: String, // JSON array of simulated captured angle images
    val userLocationText: String,
    val latitude: Double,
    val longitude: Double,
    val facedWarnings: Boolean, // "have you faced any warnings from food business or higher officials?"
    val facedWarningsDetails: String = "",
    
    // Verification state & gatekeeper pipeline checks
    val verificationStatus: String, // "PENDING_VERIFICATION", "VERIFIED", "REJECTED_CUSTOMER_SERVICE", "REJECTED_IMAGE_FAIL", "DUPLICATE_MERGED", "FORWARDED_TO_OFFICIAL", "INSPECTION_IN_PROGRESS", "OFFICIAL_RESOLVED_PENDING_AUDIT", "SETTLED", "ESCALATED_STATE_AUTHORITY"
    val priority: String, // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val foodLawSection: String, // e.g. "FSSAI Act 2006: Sec 59 Unsafe Food"
    val isFssaiJurisdiction: Boolean,
    val imageQualityPassed: Boolean,
    val imageQualityScore: Float, // 0..100
    val cvFoodObjectDetected: Boolean,
    val cvDetectedLabels: String, // e.g. "food, kitchen, contaminated_soup, plate"
    val keywordPhotoMatchScore: Float, // 0..100
    val isSurgeCluster: Boolean, // multiple distinct reports in short window
    val isDuplicate: Boolean = false,
    val duplicateOfGrievanceId: String? = null,
    val hashFingerprint: String,
    
    // Official assignment & SLA
    val assignedOfficerId: String,
    val assignedOfficerName: String,
    val officerJurisdiction: String,
    val prescribedTimelineHours: Int, // e.g. 48 for Critical, 120 for High
    val createdAt: Long = System.currentTimeMillis(),
    val deadlineTimestamp: Long,
    
    // Higher Official physical visit & proof upload
    val officerVisited: Boolean = false,
    val officerVisitTimestamp: Long? = null,
    val officerProofPhotosCount: Int = 0,
    val officerProofPhotosJson: String = "[]",
    val officerActionTaken: String? = null, // "IMPROVEMENT_NOTICE_SEC32", "SEIZURE_AND_SEALING_SEC38", "SAMPLES_SENT_TO_FOOD_LAB", "COMPOUNDING_FINE_COLLECTED", "IMMEDIATE_CLOSURE"
    val officerRemarks: String? = null,
    val officerProofValidationPassed: Boolean = false,
    val officerProofValidationScore: Float = 0f,
    
    // Dual-ended supervisory human verification
    val supervisoryAuditStatus: String = "PENDING", // "PENDING", "APPROVED", "REVERTED_TO_OFFICER"
    val supervisoryAuditNotes: String? = null,
    val auditTimestamp: Long? = null,
    
    // Settlement & Reward
    val rewardAmountINR: Double = 0.0,
    val rewardStatus: String = "PENDING", // "PENDING", "CREDITED", "TRANSFERRED"
    val cashStatus: String = "HELD_IN_ESCROW" // "HELD_IN_ESCROW", "RELEASED"
)
