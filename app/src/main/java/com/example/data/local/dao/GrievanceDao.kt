package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GrievanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrievanceDao {
    @Query("SELECT * FROM grievances ORDER BY createdAt DESC")
    fun getAllGrievancesFlow(): Flow<List<GrievanceEntity>>

    @Query("SELECT * FROM grievances WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGrievancesByCitizen(userId: String): Flow<List<GrievanceEntity>>

    @Query("SELECT * FROM grievances WHERE grievanceId = :id LIMIT 1")
    fun getGrievanceByIdFlow(id: String): Flow<GrievanceEntity?>

    @Query("SELECT * FROM grievances WHERE grievanceId = :id LIMIT 1")
    suspend fun getGrievanceById(id: String): GrievanceEntity?

    // Official Dashboard: prioritize Critical -> High -> Medium -> Low, excluding rejected/settled if filtering
    @Query("""
        SELECT * FROM grievances 
        WHERE verificationStatus NOT IN ('REJECTED_CUSTOMER_SERVICE', 'REJECTED_IMAGE_FAIL')
        ORDER BY 
            CASE priority 
                WHEN 'CRITICAL' THEN 1 
                WHEN 'HIGH' THEN 2 
                WHEN 'MEDIUM' THEN 3 
                ELSE 4 
            END ASC,
            deadlineTimestamp ASC
    """)
    fun getOfficialDashboardGrievancesFlow(): Flow<List<GrievanceEntity>>

    // Verification queue
    @Query("SELECT * FROM grievances WHERE verificationStatus = 'PENDING_VERIFICATION' ORDER BY createdAt DESC")
    fun getPendingVerificationGrievancesFlow(): Flow<List<GrievanceEntity>>

    // State Authority escalation queue: unresolved past deadline or escalated warnings
    @Query("""
        SELECT * FROM grievances 
        WHERE verificationStatus = 'ESCALATED_STATE_AUTHORITY' 
           OR (deadlineTimestamp < :currentTimestamp AND verificationStatus NOT IN ('SETTLED', 'REJECTED_CUSTOMER_SERVICE', 'REJECTED_IMAGE_FAIL'))
           OR facedWarnings = 1
        ORDER BY createdAt DESC
    """)
    fun getStateAuthorityEscalationsFlow(currentTimestamp: Long): Flow<List<GrievanceEntity>>

    // Surge cluster check: count reports for same food business within last 7 days
    @Query("""
        SELECT COUNT(*) FROM grievances 
        WHERE foodBusinessName = :businessName 
          AND createdAt >= :sinceTimestamp 
          AND grievanceId != :excludeId
    """)
    suspend fun getRecentComplaintCountForBusiness(businessName: String, sinceTimestamp: Long, excludeId: String): Int

    // Duplicate check: check unresolved grievances with matching hashFingerprint
    @Query("""
        SELECT * FROM grievances 
        WHERE hashFingerprint = :hash 
          AND verificationStatus NOT IN ('SETTLED', 'REJECTED_CUSTOMER_SERVICE', 'REJECTED_IMAGE_FAIL')
          AND grievanceId != :currentId
        LIMIT 1
    """)
    suspend fun findDuplicateUnresolvedGrievance(hash: String, currentId: String): GrievanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrievance(grievance: GrievanceEntity)

    @Update
    suspend fun updateGrievance(grievance: GrievanceEntity)

    @Query("UPDATE grievances SET priority = 'CRITICAL' WHERE grievanceId = :id")
    suspend fun elevatePriorityToCritical(id: String)
}
