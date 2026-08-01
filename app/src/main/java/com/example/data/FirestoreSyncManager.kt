package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class PaymentRecord(
    val paymentId: String = "",
    val userId: String = "",
    val userName: String = "",
    val amount: String = "",
    val planTier: String = "",
    val paymentStatus: String = "VERIFIED_SUCCESS",
    val purchaseDate: Long = System.currentTimeMillis(),
    val purchaseDateFormatted: String = "",
    val expiryTimestamp: Long = 0L,
    val expiryDateFormatted: String = "",
    val gateway: String = "Razorpay",
    val razorpayKeyId: String = ""
)

data class FirestoreUserData(
    val email: String = "",
    val name: String = "",
    val planTier: String = "1_WEEK",
    val isSubscribed: Boolean = false,
    val isBlocked: Boolean = false,
    val expiryTimestamp: Long = 0L,
    val maxRulesAllowed: Int = 20,
    val lastPaymentId: String = "",
    val lastPaymentAmount: String = "",
    val lastPaymentStatus: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

object FirestoreSyncManager {
    private const val TAG = "FirestoreSyncManager"
    private const val USERS_COLLECTION = "duty_accepter_users"
    private const val PAYMENTS_COLLECTION = "duty_accepter_payments"

    private val db: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseFirestore failed to initialize: ${e.message}")
            null
        }

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAuth failed to initialize: ${e.message}")
            null
        }

    suspend fun saveVerifiedPaymentAndSubscription(
        paymentRecord: PaymentRecord,
        email: String,
        name: String,
        subscription: SubscriptionEntity
    ): Boolean {
        if (email.isBlank()) return false
        val firestore = db ?: return false
        val userDocId = sanitizeEmail(email)

        val paymentData = hashMapOf(
            "paymentId" to paymentRecord.paymentId,
            "userId" to email,
            "userName" to name,
            "amount" to paymentRecord.amount,
            "planTier" to paymentRecord.planTier,
            "paymentStatus" to "VERIFIED_SUCCESS",
            "purchaseDate" to paymentRecord.purchaseDate,
            "purchaseDateFormatted" to paymentRecord.purchaseDateFormatted,
            "expiryTimestamp" to paymentRecord.expiryTimestamp,
            "expiryDateFormatted" to paymentRecord.expiryDateFormatted,
            "gateway" to paymentRecord.gateway,
            "razorpayKeyId" to paymentRecord.razorpayKeyId,
            "timestamp" to System.currentTimeMillis()
        )

        val userData = hashMapOf(
            "email" to email,
            "name" to name,
            "planTier" to subscription.planTier,
            "isSubscribed" to subscription.isSubscribed,
            "expiryTimestamp" to subscription.expiryTimestamp,
            "maxRulesAllowed" to subscription.maxRulesAllowed,
            "usedCoupon" to (subscription.usedCoupon ?: ""),
            "lastPaymentId" to paymentRecord.paymentId,
            "lastPaymentAmount" to paymentRecord.amount,
            "lastPaymentStatus" to "VERIFIED_SUCCESS",
            "lastPaymentDate" to paymentRecord.purchaseDate,
            "lastUpdated" to System.currentTimeMillis()
        )

        return try {
            // Save Payment Transaction Receipt
            firestore.collection(PAYMENTS_COLLECTION)
                .document(paymentRecord.paymentId)
                .set(paymentData, SetOptions.merge())
                .await()

            // Update User Subscription Doc
            firestore.collection(USERS_COLLECTION)
                .document(userDocId)
                .set(userData, SetOptions.merge())
                .await()

            Log.d(TAG, "Verified payment ${paymentRecord.paymentId} and subscription saved to Firestore for $email")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save verified payment to Firestore: ${e.message}")
            false
        }
    }

    suspend fun saveUserAndSubscription(
        email: String,
        name: String,
        subscription: SubscriptionEntity
    ): Boolean {
        if (email.isBlank()) return false
        val firestore = db ?: return false
        val docId = sanitizeEmail(email)

        val userData = hashMapOf(
            "email" to email,
            "name" to name,
            "planTier" to subscription.planTier,
            "isSubscribed" to subscription.isSubscribed,
            "expiryTimestamp" to subscription.expiryTimestamp,
            "maxRulesAllowed" to subscription.maxRulesAllowed,
            "usedCoupon" to (subscription.usedCoupon ?: ""),
            "lastUpdated" to System.currentTimeMillis()
        )

        return try {
            firestore.collection(USERS_COLLECTION)
                .document(docId)
                .set(userData, SetOptions.merge())
                .await()
            Log.d(TAG, "User profile and subscription saved to Firestore for $email")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user data to Firestore: ${e.message}")
            false
        }
    }

    suspend fun fetchUserAndSubscription(email: String): FirestoreUserData? {
        if (email.isBlank()) return null
        val firestore = db ?: return null
        val docId = sanitizeEmail(email)

        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(docId)
                .get()
                .await()

            if (snapshot.exists()) {
                val fetchedEmail = snapshot.getString("email") ?: email
                val name = snapshot.getString("name") ?: ""
                val planTier = snapshot.getString("planTier") ?: "1_WEEK"
                val isSubscribed = snapshot.getBoolean("isSubscribed") ?: false
                val isBlocked = snapshot.getBoolean("isBlocked") ?: false
                val expiryTimestamp = snapshot.getLong("expiryTimestamp") ?: 0L
                val maxRulesAllowed = snapshot.getLong("maxRulesAllowed")?.toInt() ?: 20
                val lastPaymentId = snapshot.getString("lastPaymentId") ?: ""
                val lastPaymentAmount = snapshot.getString("lastPaymentAmount") ?: ""
                val lastPaymentStatus = snapshot.getString("lastPaymentStatus") ?: ""

                val now = System.currentTimeMillis()
                val isActuallyActive = isSubscribed && expiryTimestamp > now && !isBlocked

                Log.d(TAG, "Fetched cloud user profile for $email: planTier=$planTier, expiry=$expiryTimestamp, isActive=$isActuallyActive, isBlocked=$isBlocked")
                FirestoreUserData(
                    email = fetchedEmail,
                    name = name,
                    planTier = planTier,
                    isSubscribed = isActuallyActive,
                    isBlocked = isBlocked,
                    expiryTimestamp = expiryTimestamp,
                    maxRulesAllowed = maxRulesAllowed,
                    lastPaymentId = lastPaymentId,
                    lastPaymentAmount = lastPaymentAmount,
                    lastPaymentStatus = lastPaymentStatus
                )
            } else {
                Log.d(TAG, "No existing cloud user document for $email")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch cloud user profile: ${e.message}")
            null
        }
    }

    suspend fun fetchUserPayments(email: String): List<PaymentRecord> {
        if (email.isBlank()) return emptyList()
        val firestore = db ?: return emptyList()

        return try {
            val querySnapshot = firestore.collection(PAYMENTS_COLLECTION)
                .whereEqualTo("userId", email)
                .get()
                .await()

            querySnapshot.documents.map { doc ->
                PaymentRecord(
                    paymentId = doc.getString("paymentId") ?: doc.id,
                    userId = doc.getString("userId") ?: email,
                    userName = doc.getString("userName") ?: "",
                    amount = doc.getString("amount") ?: "₹0",
                    planTier = doc.getString("planTier") ?: "PRO",
                    paymentStatus = doc.getString("paymentStatus") ?: "VERIFIED_SUCCESS",
                    purchaseDate = doc.getLong("purchaseDate") ?: System.currentTimeMillis(),
                    purchaseDateFormatted = doc.getString("purchaseDateFormatted") ?: "",
                    expiryTimestamp = doc.getLong("expiryTimestamp") ?: 0L,
                    expiryDateFormatted = doc.getString("expiryDateFormatted") ?: "",
                    gateway = doc.getString("gateway") ?: "Razorpay",
                    razorpayKeyId = doc.getString("razorpayKeyId") ?: ""
                )
            }.sortedByDescending { it.purchaseDate }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user payments: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchAllUsersForAdmin(): List<FirestoreUserData> {
        val firestore = db ?: return emptyList()
        return try {
            val querySnapshot = firestore.collection(USERS_COLLECTION)
                .get()
                .await()

            querySnapshot.documents.map { doc ->
                val email = doc.getString("email") ?: doc.id
                val name = doc.getString("name") ?: ""
                val planTier = doc.getString("planTier") ?: "1_WEEK"
                val isSubscribed = doc.getBoolean("isSubscribed") ?: false
                val isBlocked = doc.getBoolean("isBlocked") ?: false
                val expiryTimestamp = doc.getLong("expiryTimestamp") ?: 0L
                val maxRulesAllowed = doc.getLong("maxRulesAllowed")?.toInt() ?: 20
                val lastPaymentId = doc.getString("lastPaymentId") ?: ""
                val lastPaymentAmount = doc.getString("lastPaymentAmount") ?: ""
                val lastPaymentStatus = doc.getString("lastPaymentStatus") ?: ""

                FirestoreUserData(
                    email = email,
                    name = name,
                    planTier = planTier,
                    isSubscribed = isSubscribed && expiryTimestamp > System.currentTimeMillis() && !isBlocked,
                    isBlocked = isBlocked,
                    expiryTimestamp = expiryTimestamp,
                    maxRulesAllowed = maxRulesAllowed,
                    lastPaymentId = lastPaymentId,
                    lastPaymentAmount = lastPaymentAmount,
                    lastPaymentStatus = lastPaymentStatus
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch all users for admin: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchAllPaymentsForAdmin(): List<PaymentRecord> {
        val firestore = db ?: return emptyList()
        return try {
            val querySnapshot = firestore.collection(PAYMENTS_COLLECTION)
                .get()
                .await()

            querySnapshot.documents.map { doc ->
                PaymentRecord(
                    paymentId = doc.getString("paymentId") ?: doc.id,
                    userId = doc.getString("userId") ?: "",
                    userName = doc.getString("userName") ?: "",
                    amount = doc.getString("amount") ?: "₹0",
                    planTier = doc.getString("planTier") ?: "PRO",
                    paymentStatus = doc.getString("paymentStatus") ?: "VERIFIED_SUCCESS",
                    purchaseDate = doc.getLong("purchaseDate") ?: System.currentTimeMillis(),
                    purchaseDateFormatted = doc.getString("purchaseDateFormatted") ?: "",
                    expiryTimestamp = doc.getLong("expiryTimestamp") ?: 0L,
                    expiryDateFormatted = doc.getString("expiryDateFormatted") ?: "",
                    gateway = doc.getString("gateway") ?: "Razorpay",
                    razorpayKeyId = doc.getString("razorpayKeyId") ?: ""
                )
            }.sortedByDescending { it.purchaseDate }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch all payments for admin: ${e.message}")
            emptyList()
        }
    }

    suspend fun setUserBlockedStatusAdmin(targetEmail: String, isBlocked: Boolean): Boolean {
        if (targetEmail.isBlank()) return false
        val firestore = db ?: return false
        val docId = sanitizeEmail(targetEmail)

        return try {
            firestore.collection(USERS_COLLECTION)
                .document(docId)
                .set(mapOf("isBlocked" to isBlocked, "lastUpdated" to System.currentTimeMillis()), SetOptions.merge())
                .await()
            Log.d(TAG, "Updated block status for $targetEmail to $isBlocked")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set block status: ${e.message}")
            false
        }
    }

    suspend fun updateUserSubscriptionAdmin(targetEmail: String, planTier: String, daysCount: Long): Boolean {
        if (targetEmail.isBlank()) return false
        val firestore = db ?: return false
        val docId = sanitizeEmail(targetEmail)
        val now = System.currentTimeMillis()
        val expiryTimestamp = now + (daysCount * 24 * 3600 * 1000L)

        val updates = hashMapOf(
            "planTier" to planTier,
            "isSubscribed" to true,
            "isBlocked" to false,
            "expiryTimestamp" to expiryTimestamp,
            "lastUpdated" to now
        )

        return try {
            firestore.collection(USERS_COLLECTION)
                .document(docId)
                .set(updates, SetOptions.merge())
                .await()
            Log.d(TAG, "Updated user subscription admin for $targetEmail")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user sub admin: ${e.message}")
            false
        }
    }

    suspend fun submitSupportTicket(email: String, name: String, subject: String, details: String): Boolean {
        if (email.isBlank() || details.isBlank()) return false
        val firestore = db ?: return false
        val ticketId = "ticket_" + System.currentTimeMillis()

        val ticketData = hashMapOf(
            "ticketId" to ticketId,
            "email" to email,
            "name" to name,
            "subject" to subject,
            "details" to details,
            "status" to "OPEN",
            "createdAt" to System.currentTimeMillis()
        )

        return try {
            firestore.collection("duty_accepter_support_tickets")
                .document(ticketId)
                .set(ticketData)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit support ticket: ${e.message}")
            false
        }
    }

    private fun sanitizeEmail(email: String): String {
        return email.lowercase().replace(".", "_dot_").replace("@", "_at_")
    }
}
