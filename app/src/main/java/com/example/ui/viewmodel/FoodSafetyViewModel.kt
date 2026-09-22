package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.CitizenUser
import com.example.data.local.entity.FoodBusinessEntity
import com.example.data.local.entity.GrievanceEntity
import com.example.data.local.entity.OfficialUserEntity
import com.example.data.local.entity.WalletTransactionEntity
import com.example.data.repository.FoodSafetyRepository
import com.example.engine.VerificationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CapturedPhotoItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val angleLabel: String,
    val bitmap: android.graphics.Bitmap? = null,
    val clarityScore: Float = 88.0f,
    val brightnessScore: Float = 65.0f,
    val isPitchBlack: Boolean = false,
    val isBlurry: Boolean = false,
    val passed: Boolean = true,
    val statusFeedback: String = "Quality Passed"
)

class FoodSafetyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FoodSafetyRepository(
        citizenDao = db.citizenDao(),
        foodBusinessDao = db.foodBusinessDao(),
        officialDao = db.officialDao(),
        grievanceDao = db.grievanceDao(),
        walletDao = db.walletDao()
    )

    // Logged in Citizen State
    val currentCitizen: StateFlow<CitizenUser?> = repository.currentCitizenFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Grievances Lists
    val allGrievances: StateFlow<List<GrievanceEntity>> = repository.allGrievancesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val officialDashboardGrievances: StateFlow<List<GrievanceEntity>> = repository.officialDashboardFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stateAuthorityEscalations: StateFlow<List<GrievanceEntity>> = repository.getStateAuthorityEscalations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val officialsList: StateFlow<List<OfficialUserEntity>> = repository.allOfficialsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wallet Transactions
    private val _walletTransactions = MutableStateFlow<List<WalletTransactionEntity>>(emptyList())
    val walletTransactions: StateFlow<List<WalletTransactionEntity>> = _walletTransactions.asStateFlow()

    // Active Navigation / Tab state
    // "CITIZEN_HOME", "NEW_GRIEVANCE", "VERIFICATION_DETAILS", "OFFICIAL_DASHBOARD", "STATE_AUTHORITY", "WALLET"
    private val _currentScreen = MutableStateFlow("CITIZEN_HOME")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Selected Grievance for Detailed Inspection / Verification View
    private val _selectedGrievance = MutableStateFlow<GrievanceEntity?>(null)
    val selectedGrievance: StateFlow<GrievanceEntity?> = _selectedGrievance.asStateFlow()

    // UI Feedback Banner
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // FSSAI Business Lookup results
    private val _fssaiSearchResults = MutableStateFlow<List<FoodBusinessEntity>>(emptyList())
    val fssaiSearchResults: StateFlow<List<FoodBusinessEntity>> = _fssaiSearchResults.asStateFlow()

    init {
        // Automatically monitor transactions when citizen user is loaded
        viewModelScope.launch {
            currentCitizen.collect { citizen ->
                if (citizen != null) {
                    repository.getWalletTransactions(citizen.id).collect { txs ->
                        _walletTransactions.value = txs
                    }
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun selectGrievance(grievance: GrievanceEntity) {
        _selectedGrievance.value = grievance
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showMessage(msg: String) {
        _uiMessage.value = msg
    }

    // Citizen Registration with Age >= 18 check
    fun registerCitizen(
        name: String,
        aadhar: String,
        pan: String,
        email: String,
        mobile: String,
        permAddress: String,
        tempAddress: String,
        age: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.registerCitizen(
                fullName = name,
                aadharNumber = aadhar,
                panNumber = pan,
                email = email,
                mobileNumber = mobile,
                permanentAddress = permAddress,
                temporaryAddress = tempAddress,
                age = age
            )
            result.onSuccess {
                _uiMessage.value = "Registration successful! Welcome to FoodSafe Citizen Pipeline."
                onSuccess()
            }.onFailure { err ->
                _uiMessage.value = err.message ?: "Registration failed."
            }
        }
    }

    // FSSAI live database search
    fun searchFssaiBusinesses(query: String) {
        viewModelScope.launch {
            if (query.trim().length >= 2) {
                _fssaiSearchResults.value = repository.searchFssaiBusiness(query)
            } else {
                _fssaiSearchResults.value = emptyList()
            }
        }
    }

    // Submit Grievance through the Verification Engine
    fun submitGrievance(
        issueType: String,
        invoiceUploaded: Boolean,
        invoiceUri: String?,
        invoiceOcrStatus: String,
        invoiceSummary: String?,
        foodBusinessName: String,
        foodBusinessLicense: String,
        keyword: String,
        description: String,
        voiceNoteDurationSec: Int,
        voiceNotePath: String?,
        photos: List<CapturedPhotoItem>,
        userLocation: String,
        latitude: Double,
        longitude: Double,
        facedWarnings: Boolean,
        facedWarningsDetails: String,
        onSubmitted: (GrievanceEntity) -> Unit
    ) {
        val citizen = currentCitizen.value ?: return
        if (photos.size < 2) {
            _uiMessage.value = "Minimum 2 authentic photos from different angles are required."
            return
        }

        viewModelScope.launch {
            val photosJson = buildString {
                append("[")
                photos.forEachIndexed { i, p ->
                    append("""{"angle":"${p.angleLabel}","passed":${p.passed},"clarity":${p.clarityScore}}""")
                    if (i < photos.size - 1) append(",")
                }
                append("]")
            }

            val submitted = repository.processAndSubmitGrievance(
                userId = citizen.id,
                citizenName = citizen.fullName,
                issueType = issueType,
                invoiceUploaded = invoiceUploaded,
                invoiceUri = invoiceUri,
                invoiceOcrStatus = invoiceOcrStatus,
                invoiceDetailsSummary = invoiceSummary,
                foodBusinessName = foodBusinessName,
                foodBusinessLicense = foodBusinessLicense,
                keyword = keyword,
                description = description,
                voiceNoteDurationSec = voiceNoteDurationSec,
                voiceNotePath = voiceNotePath,
                photosCount = photos.size,
                photosJson = photosJson,
                userLocationText = userLocation,
                latitude = latitude,
                longitude = longitude,
                facedWarnings = facedWarnings,
                facedWarningsDetails = facedWarningsDetails
            )

            _selectedGrievance.value = submitted
            _uiMessage.value = "Grievance #${submitted.grievanceId} processed by verification gatekeeper."
            onSubmitted(submitted)
        }
    }

    // Official Action: Log physical visit & upload proofs
    fun submitOfficialInspection(
        grievanceId: String,
        actionTaken: String,
        remarks: String,
        proofPhotosCount: Int,
        proofsList: List<String>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val proofsJson = proofsList.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
            val result = repository.submitOfficerPhysicalInspectionProof(
                grievanceId = grievanceId,
                actionTaken = actionTaken,
                remarks = remarks,
                proofPhotosCount = proofPhotosCount,
                proofPhotosJson = proofsJson
            )
            result.onSuccess { updated ->
                _selectedGrievance.value = updated
                _uiMessage.value = "Physical visit proof submitted and validated. Sent for supervisory audit."
                onSuccess()
            }.onFailure { err ->
                _uiMessage.value = "Proof Rejection: ${err.message}"
            }
        }
    }

    // Supervisory Dual-Verification Audit & Citizen Settlement
    fun performSupervisoryAudit(
        grievanceId: String,
        isApproved: Boolean,
        auditNotes: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.auditOfficialProofAndSettle(
                grievanceId = grievanceId,
                isApproved = isApproved,
                auditNotes = auditNotes
            )
            result.onSuccess { updated ->
                _selectedGrievance.value = updated
                _uiMessage.value = if (isApproved) {
                    "Supervisory verification approved! Grievance settled & ₹${updated.rewardAmountINR.toInt()} credited to citizen wallet."
                } else {
                    "Supervisory verification failed. Reverted to officer dashboard for remediation."
                }
                onComplete()
            }.onFailure { err ->
                _uiMessage.value = "Audit error: ${err.message}"
            }
        }
    }

    // Citizen Wallet Bank Transfer / Conversion to INR
    fun withdrawWalletBalance(
        amount: Double,
        accountNumber: String,
        accountHolderName: String,
        ifscCode: String,
        bankName: String,
        onSuccess: () -> Unit
    ) {
        val citizen = currentCitizen.value ?: return
        viewModelScope.launch {
            val result = repository.withdrawWalletBalanceToBank(
                userId = citizen.id,
                amount = amount,
                accountNumber = accountNumber,
                accountHolderName = accountHolderName,
                ifscCode = ifscCode,
                bankName = bankName
            )
            result.onSuccess {
                _uiMessage.value = "₹${amount.toInt()} transferred to bank account (IFSC: ${ifscCode.uppercase()})."
                onSuccess()
            }.onFailure { err ->
                _uiMessage.value = "Transfer error: ${err.message}"
            }
        }
    }
}
