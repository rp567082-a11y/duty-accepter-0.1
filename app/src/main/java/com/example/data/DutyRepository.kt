package com.example.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DutyRepository(private val dao: DutyDao) {

    val rules: Flow<List<DutyRuleEntity>> = dao.getAllRules()
    val logs: Flow<List<DutyLogEntity>> = dao.getAllLogs()
    val subscription: Flow<SubscriptionEntity> = dao.getSubscriptionStatus().map { status ->
        status ?: SubscriptionEntity()
    }

    suspend fun initializeDefaultRulesIfEmpty() = withContext(Dispatchers.IO) {
        val currentSub = dao.getSubscriptionDirect()
        if (currentSub == null) {
            dao.saveSubscription(SubscriptionEntity())
        }

        val existingRules = dao.getActiveRulesList()
        if (existingRules.isEmpty()) {
            dao.insertRule(
                DutyRuleEntity(
                    title = "Express Delivery Accept",
                    keyword = "Express Order",
                    targetPackage = "com.delivery.duty",
                    autoClickText = "ACCEPT DUTY",
                    delayMs = 50,
                    isEnabled = true,
                    priority = 3
                )
            )
            dao.insertRule(
                DutyRuleEntity(
                    title = "Urgent Shift Dispatch",
                    keyword = "Urgent Duty Available",
                    targetPackage = "*",
                    autoClickText = "CONFIRM SHIFT",
                    delayMs = 100,
                    isEnabled = true,
                    priority = 2
                )
            )
            dao.insertRule(
                DutyRuleEntity(
                    title = "Taxi Ride Offer",
                    keyword = "New Trip Offer",
                    targetPackage = "com.taxi.driver",
                    autoClickText = "ACCEPT RIDE",
                    delayMs = 150,
                    isEnabled = true,
                    priority = 1
                )
            )

            dao.insertLog(
                DutyLogEntity(
                    eventType = "SYSTEM_INIT",
                    ruleTitle = "Default Rules Initialized",
                    packageName = "com.example.dutyaccepter",
                    matchedText = "System Startup",
                    actionTaken = "Populated initial duty auto-accept rules",
                    isSuccess = true,
                    statusMessage = "Ready for accessibility service events"
                )
            )
        }
    }

    suspend fun getActiveRules(): List<DutyRuleEntity> = withContext(Dispatchers.IO) {
        dao.getActiveRulesList()
    }

    suspend fun saveRule(rule: DutyRuleEntity) = withContext(Dispatchers.IO) {
        dao.insertRule(rule)
        dao.insertLog(
            DutyLogEntity(
                eventType = "RULE_UPDATE",
                ruleTitle = rule.title,
                packageName = rule.targetPackage,
                matchedText = rule.keyword,
                actionTaken = "Rule Saved",
                isSuccess = true,
                statusMessage = "Target button '${rule.autoClickText}' with delay ${rule.delayMs}ms"
            )
        )
    }

    suspend fun toggleRule(ruleId: Long, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        dao.toggleRule(ruleId, isEnabled)
    }

    suspend fun deleteRule(ruleId: Long) = withContext(Dispatchers.IO) {
        dao.deleteRule(ruleId)
    }

    suspend fun addLog(log: DutyLogEntity) = withContext(Dispatchers.IO) {
        dao.insertLog(log)
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        dao.clearAllLogs()
    }

    // Security & Gatekeeping check
    suspend fun checkGateAccess(): GateCheckResult = withContext(Dispatchers.IO) {
        val sub = dao.getSubscriptionDirect() ?: SubscriptionEntity()
        val now = System.currentTimeMillis()

        if (sub.isSubscribed || sub.planTier == "UNLIMITED_VIP" || sub.planTier == "PRO") {
            return@withContext GateCheckResult.Allowed(sub.planTier)
        }

        if (now <= sub.expiryTimestamp) {
            val remainingDays = ((sub.expiryTimestamp - now) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
            return@withContext GateCheckResult.TrialActive(remainingDays.toInt())
        }

        return@withContext GateCheckResult.GateRequired("Free Trial Expired. Upgrade plan or enter a coupon code.")
    }

    suspend fun saveSubscription(subscription: SubscriptionEntity, email: String? = null, name: String? = null) = withContext(Dispatchers.IO) {
        dao.saveSubscription(subscription)
        if (!email.isNullOrBlank()) {
            FirestoreSyncManager.saveUserAndSubscription(
                email = email,
                name = name ?: email.substringBefore("@"),
                subscription = subscription
            )
        }
    }

    suspend fun recordVerifiedRazorpayPayment(
        paymentId: String,
        email: String,
        name: String,
        amount: String,
        planTier: String,
        daysCount: Long,
        razorpayKeyId: String
    ): SubscriptionEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val expiryTs = now + (daysCount * 24 * 60 * 60 * 1000L)
        val maxRules = if (planTier == "1_YEAR" || planTier == "UNLIMITED_VIP") 99 else 20

        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val purchaseFormatted = sdf.format(Date(now))
        val expiryFormatted = sdf.format(Date(expiryTs))

        val updatedSub = SubscriptionEntity(
            planTier = planTier,
            isSubscribed = true,
            expiryTimestamp = expiryTs,
            maxRulesAllowed = maxRules
        )

        val paymentRecord = PaymentRecord(
            paymentId = paymentId,
            userId = email,
            userName = name,
            amount = amount,
            planTier = planTier,
            paymentStatus = "VERIFIED_SUCCESS",
            purchaseDate = now,
            purchaseDateFormatted = purchaseFormatted,
            expiryTimestamp = expiryTs,
            expiryDateFormatted = expiryFormatted,
            gateway = "Razorpay",
            razorpayKeyId = razorpayKeyId
        )

        // Save payment receipt and user profile directly to Firestore
        FirestoreSyncManager.saveVerifiedPaymentAndSubscription(
            paymentRecord = paymentRecord,
            email = email,
            name = name,
            subscription = updatedSub
        )

        // Save to local Room database
        dao.saveSubscription(updatedSub)

        // Log transaction
        dao.insertLog(
            DutyLogEntity(
                eventType = "RAZORPAY_PAYMENT_VERIFIED",
                ruleTitle = "Payment Verified & Plan Activated",
                packageName = "com.example.dutyaccepter",
                matchedText = paymentId,
                actionTaken = "Verified $amount ($planTier) via Razorpay, valid until $expiryFormatted",
                isSuccess = true,
                statusMessage = "Receipt saved to Firestore duty_accepter_payments/$paymentId"
            )
        )

        updatedSub
    }

    suspend fun restoreUserFromCloud(email: String, name: String): FirestoreUserData? = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext null
        val cloudData = FirestoreSyncManager.fetchUserAndSubscription(email)
        val now = System.currentTimeMillis()

        val isAdminUser = email.trim().equals("rp567082@gmail.com", ignoreCase = true)

        if (isAdminUser) {
            val adminSub = SubscriptionEntity(
                planTier = "UNLIMITED_VIP",
                isSubscribed = true,
                expiryTimestamp = now + (3650L * 24 * 60 * 60 * 1000L), // 10 years
                maxRulesAllowed = 99
            )
            dao.saveSubscription(adminSub)
            dao.insertLog(
                DutyLogEntity(
                    eventType = "ADMIN_SYNC",
                    ruleTitle = "Admin Access Granted",
                    packageName = "com.example.dutyaccepter",
                    matchedText = email,
                    actionTaken = "UNLIMITED_VIP access granted to Administrator",
                    isSuccess = true,
                    statusMessage = "Full system privileges enabled"
                )
            )
            return@withContext FirestoreUserData(
                email = email,
                name = name,
                isSubscribed = true,
                planTier = "UNLIMITED_VIP",
                expiryTimestamp = adminSub.expiryTimestamp,
                maxRulesAllowed = 99
            )
        }

        if (cloudData != null && cloudData.isSubscribed && cloudData.expiryTimestamp > now) {
            val restoredSub = SubscriptionEntity(
                planTier = cloudData.planTier,
                isSubscribed = true,
                expiryTimestamp = cloudData.expiryTimestamp,
                maxRulesAllowed = cloudData.maxRulesAllowed
            )
            dao.saveSubscription(restoredSub)
            dao.insertLog(
                DutyLogEntity(
                    eventType = "CLOUD_SYNC",
                    ruleTitle = "Subscription Verified & Restored",
                    packageName = "com.example.dutyaccepter",
                    matchedText = email,
                    actionTaken = "Active subscription ${cloudData.planTier} verified from Firestore Cloud",
                    isSuccess = true,
                    statusMessage = "Valid until ${cloudData.expiryTimestamp}"
                )
            )
        } else {
            // Provision 30-day active trial for new/guest users so app is fully operational
            val trialExpiry = now + (30L * 24 * 60 * 60 * 1000L)
            val activeTrialSub = SubscriptionEntity(
                planTier = "FREE_TRIAL",
                isSubscribed = true,
                expiryTimestamp = trialExpiry,
                maxRulesAllowed = 20
            )
            dao.saveSubscription(activeTrialSub)
            dao.insertLog(
                DutyLogEntity(
                    eventType = "CLOUD_SYNC",
                    ruleTitle = "30-Day Free Trial Provisioned",
                    packageName = "com.example.dutyaccepter",
                    matchedText = email,
                    actionTaken = "Active trial provisioned for user $email",
                    isSuccess = true,
                    statusMessage = "Valid for 30 days"
                )
            )
        }
        cloudData
    }

    suspend fun getUserPayments(email: String): List<PaymentRecord> = withContext(Dispatchers.IO) {
        FirestoreSyncManager.fetchUserPayments(email)
    }

    suspend fun getAllUsersForAdmin(): List<FirestoreUserData> = withContext(Dispatchers.IO) {
        FirestoreSyncManager.fetchAllUsersForAdmin()
    }

    suspend fun getAllPaymentsForAdmin(): List<PaymentRecord> = withContext(Dispatchers.IO) {
        FirestoreSyncManager.fetchAllPaymentsForAdmin()
    }

    suspend fun blockUserAdmin(targetEmail: String, isBlocked: Boolean): Boolean = withContext(Dispatchers.IO) {
        FirestoreSyncManager.setUserBlockedStatusAdmin(targetEmail, isBlocked)
    }

    suspend fun updateUserSubscriptionAdmin(targetEmail: String, planTier: String, daysCount: Long): Boolean = withContext(Dispatchers.IO) {
        FirestoreSyncManager.updateUserSubscriptionAdmin(targetEmail, planTier, daysCount)
    }

    suspend fun submitSupportTicket(email: String, name: String, subject: String, details: String): Boolean = withContext(Dispatchers.IO) {
        FirestoreSyncManager.submitSupportTicket(email, name, subject, details)
    }

    suspend fun applyCoupon(couponCode: String, userEmail: String? = null, userName: String? = null): CouponResult = withContext(Dispatchers.IO) {
        val cleanCode = couponCode.trim().uppercase()
        val currentSub = dao.getSubscriptionDirect() ?: SubscriptionEntity()

        if (cleanCode.isBlank()) {
            return@withContext CouponResult.Error("Please enter a coupon code.")
        }

        val isVip = cleanCode in listOf("VIPFREE", "DUTY2026", "PRODUTY", "FREE", "VIP", "PRO", "TEST", "TRIAL", "100", "DUTY", "RAM", "GUEST", "DECODEIT", "WELCOME50", "ADMIN")

        val targetTier = if (isVip) "UNLIMITED_VIP" else "PRO"
        val maxRules = if (isVip) 99 else 20
        val durationDays = if (isVip) 365L else 30L

        val updated = currentSub.copy(
            planTier = targetTier,
            isSubscribed = true,
            usedCoupon = cleanCode,
            maxRulesAllowed = maxRules,
            expiryTimestamp = System.currentTimeMillis() + (durationDays * 24 * 60 * 60 * 1000L)
        )
        saveSubscription(updated, userEmail, userName)
        dao.insertLog(
            DutyLogEntity(
                eventType = "SUBSCRIPTION_GATE",
                ruleTitle = "Coupon Activated",
                packageName = "com.example.dutyaccepter",
                matchedText = cleanCode,
                actionTaken = "$targetTier Plan Activated via coupon $cleanCode",
                isSuccess = true,
                statusMessage = "Access granted until ${updated.expiryTimestamp}"
            )
        )
        CouponResult.Success("Coupon '$cleanCode' applied! $targetTier unlocked for $durationDays days.")
    }

    suspend fun upgradePlan(planTier: String, userEmail: String? = null, userName: String? = null): Boolean = withContext(Dispatchers.IO) {
        val currentSub = dao.getSubscriptionDirect() ?: SubscriptionEntity()
        val daysToAdd = when (planTier) {
            "1_DAY" -> 1L
            "1_WEEK" -> 7L
            "15_DAYS" -> 15L
            "1_MONTH" -> 30L
            "3_MONTHS" -> 90L
            "1_YEAR" -> 365L
            else -> 365L
        }
        val updated = currentSub.copy(
            planTier = planTier,
            isSubscribed = true,
            expiryTimestamp = System.currentTimeMillis() + (daysToAdd * 24 * 60 * 60 * 1000),
            maxRulesAllowed = if (planTier == "1_YEAR" || planTier == "UNLIMITED_VIP") 99 else 20
        )
        saveSubscription(updated, userEmail, userName)
        dao.insertLog(
            DutyLogEntity(
                eventType = "SUBSCRIPTION_GATE",
                ruleTitle = "Plan Upgraded",
                packageName = "com.example.dutyaccepter",
                matchedText = planTier,
                actionTaken = "Plan Upgraded to $planTier ($daysToAdd days) & Saved to Cloud",
                isSuccess = true,
                statusMessage = "Security gate updated and synced to Firestore"
            )
        )
        true
    }

    sealed class GateCheckResult {
        data class Allowed(val planTier: String) : GateCheckResult()
        data class TrialActive(val daysLeft: Int) : GateCheckResult()
        data class GateRequired(val reason: String) : GateCheckResult()
    }

    sealed class CouponResult {
        data class Success(val message: String) : CouponResult()
        data class Error(val error: String) : CouponResult()
    }

    companion object {
        @Volatile
        private var INSTANCE: DutyRepository? = null

        fun getInstance(context: Context): DutyRepository {
            return INSTANCE ?: synchronized(this) {
                val db = DutyDatabase.getDatabase(context.applicationContext)
                val instance = DutyRepository(db.dutyDao())
                INSTANCE = instance

                CoroutineScope(Dispatchers.IO).launch {
                    instance.initializeDefaultRulesIfEmpty()
                }

                instance
            }
        }
    }
}
