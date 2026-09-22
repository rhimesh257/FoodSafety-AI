package com.example.data.repository

import com.example.data.local.dao.CitizenDao
import com.example.data.local.dao.FoodBusinessDao
import com.example.data.local.dao.GrievanceDao
import com.example.data.local.dao.OfficialDao
import com.example.data.local.dao.WalletDao
import com.example.data.local.entity.CitizenUser
import com.example.data.local.entity.FoodBusinessEntity
import com.example.data.local.entity.GrievanceEntity
import com.example.data.local.entity.OfficialUserEntity
import com.example.data.local.entity.WalletTransactionEntity
import com.example.engine.VerificationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class FoodSafetyRepository(
    private val citizenDao: CitizenDao,
    private val foodBusinessDao: FoodBusinessDao,
    private val officialDao: OfficialDao,
    private val grievanceDao: GrievanceDao,
    private val walletDao: WalletDao
) {

    val currentCitizenFlow: Flow<CitizenUser?> = citizenDao.getFirstCitizenFlow()
    val allGrievancesFlow: Flow<List<GrievanceEntity>> = grievanceDao.getAllGrievancesFlow()
    val officialDashboardFlow: Flow<List<GrievanceEntity>> = grievanceDao.getOfficialDashboardGrievancesFlow()
    val allOfficialsFlow: Flow<List<OfficialUserEntity>> = officialDao.getAllOfficialsFlow()

    suspend fun getCurrentCitizen(): CitizenUser? = withContext(Dispatchers.IO) {
        citizenDao.getFirstCitizen()
    }

    suspend fun registerCitizen(
        fullName: String,
        aadharNumber: String,
        panNumber: String,
        email: String,
        mobileNumber: String,
        permanentAddress: String,
        temporaryAddress: String,
        age: Int
    ): Result<CitizenUser> = withContext(Dispatchers.IO) {
        if (age < 18) {
            return@withContext Result.failure(IllegalArgumentException("Citizen must be 18 years of age or older to register."))
        }
        val citizen = CitizenUser(
            id = "CIT-${(1000..9999).random()}",
            fullName = fullName.trim(),
            aadharNumber = aadharNumber.trim(),
            panNumber = panNumber.trim().uppercase(),
            email = email.trim(),
            mobileNumber = mobileNumber.trim(),
            permanentAddress = permanentAddress.trim(),
            temporaryAddress = temporaryAddress.trim(),
            age = age,
            isOtpVerified = true,
            walletBalanceINR = 0.0
        )
        citizenDao.insertCitizen(citizen)
        Result.success(citizen)
    }

    suspend fun searchFssaiBusiness(query: String): List<FoodBusinessEntity> = withContext(Dispatchers.IO) {
        foodBusinessDao.searchBusinesses(query)
    }

    suspend fun getBusinessByLicense(license: String): FoodBusinessEntity? = withContext(Dispatchers.IO) {
        foodBusinessDao.getBusinessByLicense(license)
    }

    fun getCitizenGrievances(userId: String): Flow<List<GrievanceEntity>> {
        return grievanceDao.getGrievancesByCitizen(userId)
    }

    fun getWalletTransactions(userId: String): Flow<List<WalletTransactionEntity>> {
        return walletDao.getTransactionsForUser(userId)
    }

    fun getStateAuthorityEscalations(): Flow<List<GrievanceEntity>> {
        return grievanceDao.getStateAuthorityEscalationsFlow(System.currentTimeMillis())
    }

    // Citizen files grievance -> runs verification gatekeeper
    suspend fun processAndSubmitGrievance(
        userId: String,
        citizenName: String,
        issueType: String,
        invoiceUploaded: Boolean,
        invoiceUri: String?,
        invoiceOcrStatus: String,
        invoiceDetailsSummary: String?,
        foodBusinessName: String,
        foodBusinessLicense: String,
        keyword: String,
        description: String,
        voiceNoteDurationSec: Int,
        voiceNotePath: String?,
        photosCount: Int,
        photosJson: String,
        userLocationText: String,
        latitude: Double,
        longitude: Double,
        facedWarnings: Boolean,
        facedWarningsDetails: String
    ): GrievanceEntity = withContext(Dispatchers.IO) {
        val grievanceId = "GRV-${(2026..2027).random()}-${(1000..9999).random()}"

        // Step 1: Structural Image Filtering
        val structuralResult = VerificationEngine.performStructuralImageFilter(
            photoAngleLabel = "Primary Evidence",
            brightnessLevel = 0.65f,
            blurMetric = 0.85f,
            isScreenshotAspect = false
        )

        // Step 2: CV Food Object Detection
        val cvResult = VerificationEngine.detectFoodObjects(
            keyword = keyword,
            description = description,
            photoCount = photosCount
        )

        // Step 3: Keyword & Photo Match Score
        val matchScore = VerificationEngine.calculateKeywordPhotoMatch(keyword, description, photosCount)

        // Step 4: FSSAI database check
        val fssaiRecord = foodBusinessDao.getBusinessByName(foodBusinessName) 
            ?: foodBusinessDao.getBusinessByLicense(foodBusinessLicense)
        val hasLicense = fssaiRecord?.hasFssaiLicense ?: (foodBusinessLicense.isNotBlank() && !foodBusinessLicense.contains("UNREGISTERED"))
        val isExpired = fssaiRecord?.isExpired ?: false

        // Step 5: Legal classification under FSSAI Act 2006
        val legalResult = VerificationEngine.scanLegalJurisdiction(
            keyword = keyword,
            description = description,
            hasFssaiLicense = hasLicense,
            isLicenseExpired = isExpired,
            facedWarnings = facedWarnings
        )

        // Step 6: Surge cluster check (multiple complaints in last 7 days)
        val recentCount = grievanceDao.getRecentComplaintCountForBusiness(
            businessName = foodBusinessName,
            sinceTimestamp = System.currentTimeMillis() - (7L * 24 * 3600 * 1000),
            excludeId = grievanceId
        )
        val isSurge = recentCount >= 2

        // Step 7: Deduplication check using hash fingerprint
        val hash = VerificationEngine.generateFingerprint(foodBusinessName, keyword)
        val duplicateMatch = grievanceDao.findDuplicateUnresolvedGrievance(hash, grievanceId)
        val isDuplicate = duplicateMatch != null

        // Step 8: Priority calculation
        var finalPriority = legalResult.calculatedPriority
        if (isSurge && finalPriority == "MEDIUM") {
            finalPriority = "HIGH"
        }
        if (isDuplicate) {
            // Elevate the priority of the original similar grievance in officer dashboard
            duplicateMatch?.let {
                grievanceDao.elevatePriorityToCritical(it.grievanceId)
            }
        }

        // Step 9: Determine Verification Status
        val status = when {
            !structuralResult.isValid -> "REJECTED_IMAGE_FAIL"
            legalResult.isCustomerServiceComplaintOnly -> "REJECTED_CUSTOMER_SERVICE"
            isDuplicate -> "DUPLICATE_MERGED"
            facedWarnings -> "FORWARDED_TO_OFFICIAL" // Fast-forward warnings directly
            else -> "VERIFIED"
        }

        // Step 10: Official Assignment & SLA
        val assignedOfficer = officialDao.getDefaultOfficial()
        val timelineHours = VerificationEngine.getPrescribedTimelineHours(finalPriority)
        val rewardINR = VerificationEngine.calculateRewardAmountINR(finalPriority)

        val grievance = GrievanceEntity(
            grievanceId = grievanceId,
            userId = userId,
            citizenName = citizenName,
            issueType = issueType,
            invoiceUploaded = invoiceUploaded,
            invoiceUri = invoiceUri,
            invoiceOcrStatus = invoiceOcrStatus,
            invoiceDetailsSummary = invoiceDetailsSummary,
            foodBusinessName = foodBusinessName,
            foodBusinessLicenseNumber = if (foodBusinessLicense.isNotBlank()) foodBusinessLicense else (fssaiRecord?.licenseNumber ?: "UNREGISTERED_OPERATOR"),
            keyword = keyword,
            description = description,
            voiceNoteDurationSec = voiceNoteDurationSec,
            voiceNotePath = voiceNotePath,
            photosCount = photosCount,
            photosJson = photosJson,
            userLocationText = userLocationText,
            latitude = latitude,
            longitude = longitude,
            facedWarnings = facedWarnings,
            facedWarningsDetails = facedWarningsDetails,
            verificationStatus = status,
            priority = finalPriority,
            foodLawSection = legalResult.matchedSection,
            isFssaiJurisdiction = legalResult.isUnderFssaiJurisdiction,
            imageQualityPassed = structuralResult.isValid,
            imageQualityScore = structuralResult.clarityScore,
            cvFoodObjectDetected = cvResult.hasFoodObjects,
            cvDetectedLabels = cvResult.detectedLabels.joinToString(", "),
            keywordPhotoMatchScore = matchScore,
            isSurgeCluster = isSurge,
            isDuplicate = isDuplicate,
            duplicateOfGrievanceId = duplicateMatch?.grievanceId,
            hashFingerprint = hash,
            assignedOfficerId = assignedOfficer?.officerId ?: "FSO-DEL-104",
            assignedOfficerName = assignedOfficer?.officerName ?: "Dr. Rajeshwar Sharma",
            officerJurisdiction = assignedOfficer?.jurisdictionZone ?: "Central Delhi Circle",
            prescribedTimelineHours = timelineHours,
            createdAt = System.currentTimeMillis(),
            deadlineTimestamp = System.currentTimeMillis() + (timelineHours * 3600 * 1000L),
            rewardAmountINR = rewardINR,
            rewardStatus = "PENDING",
            cashStatus = "HELD_IN_ESCROW"
        )

        grievanceDao.insertGrievance(grievance)
        grievance
    }

    // Higher Official physical visit & proof upload
    suspend fun submitOfficerPhysicalInspectionProof(
        grievanceId: String,
        actionTaken: String,
        remarks: String,
        proofPhotosCount: Int,
        proofPhotosJson: String
    ): Result<GrievanceEntity> = withContext(Dispatchers.IO) {
        val grievance = grievanceDao.getGrievanceById(grievanceId)
            ?: return@withContext Result.failure(IllegalArgumentException("Grievance not found"))

        // Run independent anti-corruption validation check
        val validation = VerificationEngine.validateOfficerInspectionProof(
            photosCount = proofPhotosCount,
            actionTaken = actionTaken,
            remarks = remarks
        )

        if (!validation.isValidProof) {
            return@withContext Result.failure(IllegalStateException(validation.failureReason ?: "Proof validation failed"))
        }

        val updated = grievance.copy(
            officerVisited = true,
            officerVisitTimestamp = System.currentTimeMillis(),
            officerProofPhotosCount = proofPhotosCount,
            officerProofPhotosJson = proofPhotosJson,
            officerActionTaken = actionTaken,
            officerRemarks = remarks,
            officerProofValidationPassed = true,
            officerProofValidationScore = validation.qualityScore,
            verificationStatus = "OFFICIAL_RESOLVED_PENDING_AUDIT",
            supervisoryAuditStatus = "PENDING"
        )
        grievanceDao.updateGrievance(updated)
        Result.success(updated)
    }

    // Dual-ended supervisory human verification & citizen settlement
    suspend fun auditOfficialProofAndSettle(
        grievanceId: String,
        isApproved: Boolean,
        auditNotes: String
    ): Result<GrievanceEntity> = withContext(Dispatchers.IO) {
        val grievance = grievanceDao.getGrievanceById(grievanceId)
            ?: return@withContext Result.failure(IllegalArgumentException("Grievance not found"))

        if (!isApproved) {
            // Revert into official dashboard with supervisory feedback
            val reverted = grievance.copy(
                verificationStatus = "FORWARDED_TO_OFFICIAL",
                supervisoryAuditStatus = "REVERTED_TO_OFFICER",
                supervisoryAuditNotes = auditNotes,
                auditTimestamp = System.currentTimeMillis()
            )
            grievanceDao.updateGrievance(reverted)
            return@withContext Result.success(reverted)
        }

        // Approved! Mark settled and credit citizen reward to wallet
        val settled = grievance.copy(
            verificationStatus = "SETTLED",
            supervisoryAuditStatus = "APPROVED",
            supervisoryAuditNotes = auditNotes,
            auditTimestamp = System.currentTimeMillis(),
            rewardStatus = "CREDITED",
            cashStatus = "RELEASED"
        )
        grievanceDao.updateGrievance(settled)

        // Credit to Citizen Wallet & Record Transaction
        citizenDao.creditWallet(settled.userId, settled.rewardAmountINR)
        walletDao.insertTransaction(
            WalletTransactionEntity(
                userId = settled.userId,
                grievanceId = settled.grievanceId,
                amount = settled.rewardAmountINR,
                type = "CREDIT_REWARD",
                title = "Statutory Reward Credited",
                subtitle = "Grievance #${settled.grievanceId} verified and settled (${settled.priority} Priority)",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(settled)
    }

    // Citizen converts wallet balance into INR to bank account
    suspend fun withdrawWalletBalanceToBank(
        userId: String,
        amount: Double,
        accountNumber: String,
        accountHolderName: String,
        ifscCode: String,
        bankName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val citizen = citizenDao.getCitizenById(userId)
            ?: return@withContext Result.failure(IllegalArgumentException("Citizen not found"))

        if (amount <= 0 || citizen.walletBalanceINR < amount) {
            return@withContext Result.failure(IllegalArgumentException("Insufficient wallet balance for withdrawal."))
        }

        if (accountNumber.length < 9 || ifscCode.length != 11) {
            return@withContext Result.failure(IllegalArgumentException("Invalid bank account or IFSC code format."))
        }

        val rows = citizenDao.debitWallet(userId, amount)
        if (rows > 0) {
            val masked = "•••• " + accountNumber.takeLast(4)
            walletDao.insertTransaction(
                WalletTransactionEntity(
                    userId = userId,
                    grievanceId = "WITHDRAWAL-${System.currentTimeMillis() % 10000}",
                    amount = amount,
                    type = "WITHDRAWAL_INR",
                    title = "Converted to INR - Bank Payout",
                    subtitle = "$bankName ($masked, IFSC: ${ifscCode.uppercase()})",
                    bankAccountMasked = masked,
                    ifscCode = ifscCode.uppercase(),
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Debit transaction could not be completed."))
        }
    }
}
