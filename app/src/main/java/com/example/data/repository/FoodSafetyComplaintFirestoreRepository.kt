package com.example.data.repository

import android.util.Log
import com.example.data.local.entity.GrievanceEntity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Data model representing a Food Safety Complaint stored in Cloud Firestore.
 * Default parameter values ensure Firestore can deserialize documents using toObject().
 */
data class FoodSafetyComplaint(
    val complaintId: String = "",
    val userId: String = "",
    val citizenName: String = "",
    val issueType: String = "OFFLINE", // "ONLINE" or "OFFLINE"
    val foodBusinessName: String = "",
    val foodBusinessLicenseNumber: String = "",
    val keyword: String = "",
    val description: String = "",
    val photosCount: Int = 0,
    val userLocationText: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val priority: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val verificationStatus: String = "PENDING_VERIFICATION",
    val foodLawSection: String = "",
    val isFssaiJurisdiction: Boolean = true,
    val facedWarnings: Boolean = false,
    val facedWarningsDetails: String = "",
    val assignedOfficerId: String = "",
    val rewardAmountINR: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val deadlineTimestamp: Long = 0L,
    val invoiceUploaded: Boolean = false,
    val invoiceSummary: String? = null
)

/**
 * Basic Repository class that initializes Firebase Firestore for storing
 * and managing food safety complaints.
 *
 * @param firestore The [FirebaseFirestore] instance, defaults to [FirebaseFirestore.getInstance].
 */
class FoodSafetyComplaintFirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FoodSafetyFirestoreRepo"
        const val COMPLAINTS_COLLECTION = "food_safety_complaints"
    }

    private val complaintsCollection: CollectionReference
        get() = firestore.collection(COMPLAINTS_COLLECTION)

    /**
     * Stores a new food safety complaint document in Firestore.
     * Uses [complaint.complaintId] as the document ID if present, otherwise auto-generates one.
     */
    suspend fun saveComplaint(complaint: FoodSafetyComplaint): Result<String> {
        return try {
            val docRef = if (complaint.complaintId.isNotBlank()) {
                complaintsCollection.document(complaint.complaintId)
            } else {
                complaintsCollection.document()
            }

            val finalComplaint = if (complaint.complaintId.isBlank()) {
                complaint.copy(complaintId = docRef.id)
            } else {
                complaint
            }

            docRef.set(finalComplaint).await()
            Log.d(TAG, "Complaint successfully saved to Firestore with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save complaint to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Convenience method to sync an existing local [GrievanceEntity] to Firestore.
     */
    suspend fun saveFromGrievanceEntity(entity: GrievanceEntity): Result<String> {
        val complaint = FoodSafetyComplaint(
            complaintId = entity.grievanceId,
            userId = entity.userId,
            citizenName = entity.citizenName,
            issueType = entity.issueType,
            foodBusinessName = entity.foodBusinessName,
            foodBusinessLicenseNumber = entity.foodBusinessLicenseNumber,
            keyword = entity.keyword,
            description = entity.description,
            photosCount = entity.photosCount,
            userLocationText = entity.userLocationText,
            latitude = entity.latitude,
            longitude = entity.longitude,
            priority = entity.priority,
            verificationStatus = entity.verificationStatus,
            foodLawSection = entity.foodLawSection,
            isFssaiJurisdiction = entity.isFssaiJurisdiction,
            facedWarnings = entity.facedWarnings,
            facedWarningsDetails = entity.facedWarningsDetails,
            assignedOfficerId = entity.assignedOfficerId,
            rewardAmountINR = entity.rewardAmountINR,
            createdAt = entity.createdAt,
            deadlineTimestamp = entity.deadlineTimestamp,
            invoiceUploaded = entity.invoiceUploaded,
            invoiceSummary = entity.invoiceDetailsSummary
        )
        return saveComplaint(complaint)
    }

    /**
     * Fetches a single complaint by its unique ID.
     */
    suspend fun getComplaintById(complaintId: String): Result<FoodSafetyComplaint?> {
        return try {
            val snapshot = complaintsCollection.document(complaintId).get().await()
            if (snapshot.exists()) {
                val complaint = snapshot.toObject(FoodSafetyComplaint::class.java)
                Result.success(complaint)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching complaint: $complaintId", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time Flow observing all complaints ordered by creation timestamp descending.
     */
    fun observeAllComplaints(): Flow<List<FoodSafetyComplaint>> = callbackFlow {
        val listenerRegistration = complaintsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to complaints collection", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(FoodSafetyComplaint::class.java) }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Real-time Flow observing complaints submitted by a specific citizen.
     */
    fun observeComplaintsByCitizen(userId: String): Flow<List<FoodSafetyComplaint>> = callbackFlow {
        val listenerRegistration = complaintsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to citizen complaints", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(FoodSafetyComplaint::class.java) }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Updates the status and verification details of an existing complaint document.
     */
    suspend fun updateComplaintStatus(
        complaintId: String,
        newStatus: String,
        officerId: String? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "verificationStatus" to newStatus
            )
            if (officerId != null) {
                updates["assignedOfficerId"] = officerId
            }
            complaintsCollection.document(complaintId).update(updates).await()
            Log.d(TAG, "Complaint $complaintId status updated to $newStatus")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update complaint status", e)
            Result.failure(e)
        }
    }
}
