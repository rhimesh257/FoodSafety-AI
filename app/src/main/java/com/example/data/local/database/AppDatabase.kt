package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CitizenUser::class,
        FoodBusinessEntity::class,
        OfficialUserEntity::class,
        GrievanceEntity::class,
        WalletTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun citizenDao(): CitizenDao
    abstract fun foodBusinessDao(): FoodBusinessDao
    abstract fun officialDao(): OfficialDao
    abstract fun grievanceDao(): GrievanceDao
    abstract fun walletDao(): WalletDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "foodsafe_pipeline.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedInitialData(database)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(db: AppDatabase) {
            // Seed Default Officials
            val officials = listOf(
                OfficialUserEntity(
                    officerId = "FSO-DEL-104",
                    officerName = "Dr. Rajeshwar Sharma",
                    designation = "Food Safety Officer (FSO)",
                    jurisdictionZone = "Central Delhi & Connaught Place Circle",
                    contactNumber = "+91 98110 44219",
                    activeInspectionsCount = 3
                ),
                OfficialUserEntity(
                    officerId = "DO-DEL-002",
                    officerName = "Smt. Vandana Rao, IAS",
                    designation = "Designated Officer (DO)",
                    jurisdictionZone = "National Capital Region Food Safety Administration",
                    contactNumber = "+91 98711 55620",
                    activeInspectionsCount = 7
                ),
                OfficialUserEntity(
                    officerId = "COMM-NAT-001",
                    officerName = "Dr. Arun Singhal, FSSAI",
                    designation = "State Food Safety Commissioner",
                    jurisdictionZone = "State Appellate & Enforcement Commission",
                    contactNumber = "+91 11 2323 6975",
                    activeInspectionsCount = 14
                )
            )
            db.officialDao().insertOfficials(officials)

            // Seed FSSAI Registered & Flagged Food Businesses
            val businesses = listOf(
                FoodBusinessEntity(
                    licenseNumber = "10021011000452",
                    businessName = "Spice Garden Fine Dine",
                    category = "Restaurant & Banquet",
                    address = "Plot 14, Outer Ring Road, Connaught Place, New Delhi",
                    licenseExpiryDate = "2027-11-30",
                    isExpired = false,
                    hasFssaiLicense = true,
                    lastLabInspectionDate = "2026-03-15 (Satisfactory)",
                    hygieneRatingScore = 4,
                    pastViolationsCount = 0,
                    designatedOfficerId = "FSO-DEL-104"
                ),
                FoodBusinessEntity(
                    licenseNumber = "13319002000819",
                    businessName = "Royal Biryani & Cloud Kitchens",
                    category = "Cloud Kitchen",
                    address = "Shop 4, Basement, Hauz Khas Village, New Delhi",
                    licenseExpiryDate = "2025-01-10", // Expired renewal!
                    isExpired = true,
                    hasFssaiLicense = true,
                    lastLabInspectionDate = "2025-08-10 (Notice Issued for Oil Reuse)",
                    hygieneRatingScore = 2,
                    pastViolationsCount = 2,
                    designatedOfficerId = "FSO-DEL-104"
                ),
                FoodBusinessEntity(
                    licenseNumber = "UNREGISTERED_003",
                    businessName = "Street Tadka & Chaat Hub",
                    category = "Street Food Eatery",
                    address = "Near Metro Gate 2, Laxmi Nagar, New Delhi",
                    licenseExpiryDate = "N/A",
                    isExpired = true,
                    hasFssaiLicense = false, // No FSSAI license!
                    lastLabInspectionDate = "Never Inspected",
                    hygieneRatingScore = 1,
                    pastViolationsCount = 1,
                    designatedOfficerId = "FSO-DEL-104"
                ),
                FoodBusinessEntity(
                    licenseNumber = "10018042001590",
                    businessName = "Evergreen Sweet & Dairy Works",
                    category = "Dairy & Sweet Manufacturing",
                    address = "Main Market, Sector 18, Noida / Delhi NCR",
                    licenseExpiryDate = "2028-04-20",
                    isExpired = false,
                    hasFssaiLicense = true,
                    lastLabInspectionDate = "2026-06-02 (Passed adulteration tests)",
                    hygieneRatingScore = 5,
                    pastViolationsCount = 0,
                    designatedOfficerId = "DO-DEL-002"
                )
            )
            db.foodBusinessDao().insertBusinesses(businesses)

            // Seed Default Verified Citizen User
            val citizen = CitizenUser(
                id = "CIT-9021",
                fullName = "Rohan Verma",
                aadharNumber = "XXXX-XXXX-8921",
                panNumber = "BQWPV4489J",
                email = "rohan.verma@citizenmail.in",
                mobileNumber = "+91 98765 43210",
                permanentAddress = "H-42, Shanti Kunj, South Extension Part II, New Delhi - 110049",
                temporaryAddress = "Flat 304, Green Palms Residency, Saket, New Delhi - 110017",
                age = 26,
                isOtpVerified = true,
                walletBalanceINR = 1500.0
            )
            db.citizenDao().insertCitizen(citizen)

            // Seed an initial settled grievance with reward history and an active inspection grievance
            val settledTime = System.currentTimeMillis() - 86400000L * 4
            val settledGrievance = GrievanceEntity(
                grievanceId = "GRV-2026-1042",
                userId = "CIT-9021",
                citizenName = "Rohan Verma",
                issueType = "ONLINE",
                invoiceUploaded = true,
                invoiceUri = "content://sample/invoice_swiggy_4812.pdf",
                invoiceOcrStatus = "VALID",
                invoiceDetailsSummary = "Zomato Order #8921: Paneer Butter Masala & Garlic Naan (Total ₹490)",
                foodBusinessName = "Royal Biryani & Cloud Kitchens",
                foodBusinessLicenseNumber = "13319002000819",
                keyword = "Severely stale paneer with fungal growth and rancid odor",
                description = "Received contaminated dish with visible green mold on cottage cheese cubes. Sour foul smell upon opening container.",
                voiceNoteDurationSec = 14,
                voiceNotePath = "simulated_audio_note_1042.m4a",
                photosCount = 3,
                photosJson = """[{"angle":"Container Seal and Batch Label","passed":true},{"angle":"Close-up Macro of Contaminated Cheese","passed":true},{"angle":"Invoice and Tamper Proof Bag","passed":true}]""",
                userLocationText = "Saket, South Delhi (Lat: 28.5245, Lng: 77.2066)",
                latitude = 28.5245,
                longitude = 77.2066,
                facedWarnings = false,
                facedWarningsDetails = "",
                verificationStatus = "SETTLED",
                priority = "CRITICAL",
                foodLawSection = "FSSAI Act 2006: Sec 59(i) Unsafe & Decomposed Food",
                isFssaiJurisdiction = true,
                imageQualityPassed = true,
                imageQualityScore = 96.5f,
                cvFoodObjectDetected = true,
                cvDetectedLabels = "food, curry_bowl, cheese_mold, dining_table",
                keywordPhotoMatchScore = 94.0f,
                isSurgeCluster = true,
                hashFingerprint = "FSSAI_HASH_ROYAL_BIRYANI_STALE_PANEER",
                assignedOfficerId = "FSO-DEL-104",
                assignedOfficerName = "Dr. Rajeshwar Sharma",
                officerJurisdiction = "Central Delhi & Connaught Place Circle",
                prescribedTimelineHours = 48,
                createdAt = settledTime,
                deadlineTimestamp = settledTime + (48 * 3600 * 1000L),
                officerVisited = true,
                officerVisitTimestamp = settledTime + 18 * 3600 * 1000L,
                officerProofPhotosCount = 3,
                officerProofPhotosJson = """[{"angle":"Site Inspection Seal & Premises","desc":"Found refrigeration unit broken at 22°C"},{"angle":"Improvement Notice & Seizure Memo","desc":"Form VA Seizure of 18kg spoiled paneer"},{"angle":"Compounding Penalty Fine Receipt","desc":"₹25,000 spot fine collected under Sec 58"}]""",
                officerActionTaken = "SEIZURE_AND_SEALING_SEC38",
                officerRemarks = "Raid completed. Storage freezers found non-operational. Issued statutory improvement notice and seized 18kg decomposed dairy stock.",
                officerProofValidationPassed = true,
                officerProofValidationScore = 98.2f,
                supervisoryAuditStatus = "APPROVED",
                supervisoryAuditNotes = "Supervisory dual-verification confirmed by Designated Officer. Evidence package meets high accountability standard.",
                auditTimestamp = settledTime + 24 * 3600 * 1000L,
                rewardAmountINR = 1500.0,
                rewardStatus = "CREDITED",
                cashStatus = "RELEASED"
            )
            db.grievanceDao().insertGrievance(settledGrievance)

            // Seed initial wallet transaction
            db.walletDao().insertTransaction(
                WalletTransactionEntity(
                    userId = "CIT-9021",
                    grievanceId = "GRV-2026-1042",
                    amount = 1500.0,
                    type = "CREDIT_REWARD",
                    title = "Citizen Reward - Grievance Settled",
                    subtitle = "Grievance #GRV-2026-1042 (Critical Priority: Sec 59 Unsafe Food)",
                    timestamp = settledTime + 24 * 3600 * 1000L
                )
            )
        }
    }
}
