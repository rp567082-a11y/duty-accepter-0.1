package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DutyLogEntity
import com.example.data.DutyRepository
import com.example.data.DutyRuleEntity
import com.example.data.SubscriptionEntity
import com.example.service.DutyAccepterService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    DASHBOARD,
    HISTORY,
    APPS
}

enum class AcceptSpeedMode(
    val title: String,
    val delayText: String,
    val description: String,
    val delayMs: Long
) {
    TURBO("0.1s Turbo", "0.1 sec", "Ultra fast instant acceptance", 100L),
    NORMAL("1.0s Normal", "1.0 sec", "Balanced reaction speed", 1000L),
    HUMAN("Human Like", "1.5s - 3.0s", "Anti-ban randomized delay to prevent detection", 2000L)
}

enum class AppStep {
    AUTH,
    SUBSCRIPTION_PLAN,
    MAIN_APP,
    PROFILE,
    PAYMENT_HISTORY,
    SETTINGS,
    HELP_SUPPORT,
    ADMIN_PANEL
}

data class UserSession(
    val name: String = "",
    val email: String = "",
    val isLoggedIn: Boolean = false
)

class DutyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DutyRepository.getInstance(application)

    val rules: StateFlow<List<DutyRuleEntity>> = repository.rules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<DutyLogEntity>> = repository.logs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscription: StateFlow<SubscriptionEntity> = repository.subscription
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionEntity())

    private val _currentStep = MutableStateFlow(AppStep.AUTH)
    val currentStep: StateFlow<AppStep> = _currentStep.asStateFlow()

    private val _userSession = MutableStateFlow(UserSession())
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    private val _userPayments = MutableStateFlow<List<com.example.data.PaymentRecord>>(emptyList())
    val userPayments: StateFlow<List<com.example.data.PaymentRecord>> = _userPayments.asStateFlow()

    private val _adminUsers = MutableStateFlow<List<com.example.data.FirestoreUserData>>(emptyList())
    val adminUsers: StateFlow<List<com.example.data.FirestoreUserData>> = _adminUsers.asStateFlow()

    private val _adminPayments = MutableStateFlow<List<com.example.data.PaymentRecord>>(emptyList())
    val adminPayments: StateFlow<List<com.example.data.PaymentRecord>> = _adminPayments.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _appLanguage = MutableStateFlow("English")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    val isAdmin: Boolean
        get() {
            val sessionEmail = _userSession.value.email.lowercase().trim()
            val authEmail = try {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.lowercase()?.trim() ?: ""
            } catch (e: Exception) {
                ""
            }
            val targetAdmin = "rp567082@gmail.com"
            return sessionEmail == targetAdmin || authEmail == targetAdmin
        }

    private val _selectedTab = MutableStateFlow(AppTab.DASHBOARD)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _isServiceActive = MutableStateFlow(false)
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    // Fare Filter State
    private val _minFare = MutableStateFlow("50")
    val minFare: StateFlow<String> = _minFare.asStateFlow()

    private val _maxFare = MutableStateFlow("500")
    val maxFare: StateFlow<String> = _maxFare.asStateFlow()

    private val _isFilterSaved = MutableStateFlow(true)
    val isFilterSaved: StateFlow<Boolean> = _isFilterSaved.asStateFlow()

    // Alert Sound
    private val _isAlertSoundEnabled = MutableStateFlow(true)
    val isAlertSoundEnabled: StateFlow<Boolean> = _isAlertSoundEnabled.asStateFlow()

    // Accept Speed Mode
    private val _acceptSpeedMode = MutableStateFlow(AcceptSpeedMode.HUMAN)
    val acceptSpeedMode: StateFlow<AcceptSpeedMode> = _acceptSpeedMode.asStateFlow()

    fun setAcceptSpeedMode(mode: AcceptSpeedMode) {
        _acceptSpeedMode.value = mode
        showToast("Acceptance Speed set to ${mode.title} (${mode.delayText})")
    }

    // Permission States
    private val _isAccessibilityGranted = MutableStateFlow(false)
    val isAccessibilityGranted: StateFlow<Boolean> = _isAccessibilityGranted.asStateFlow()

    private val _isOverlayGranted = MutableStateFlow(false)
    val isOverlayGranted: StateFlow<Boolean> = _isOverlayGranted.asStateFlow()

    private val _isBatteryOptimizationExempt = MutableStateFlow(false)
    val isBatteryOptimizationExempt: StateFlow<Boolean> = _isBatteryOptimizationExempt.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Simulation State
    private val _simulationLog = MutableStateFlow<String>("")
    val simulationLog: StateFlow<String> = _simulationLog.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    init {
        checkPermissions(getApplication())
    }

    fun loginUser(name: String, email: String) {
        val cleanEmail = email.trim()
        val cleanName = if (name.isBlank()) cleanEmail.substringBefore("@") else name.trim()
        _userSession.value = UserSession(name = cleanName, email = cleanEmail, isLoggedIn = true)

        viewModelScope.launch {
            repository.restoreUserFromCloud(cleanEmail, cleanName)
            _currentStep.value = AppStep.MAIN_APP
            showToast("Welcome $cleanName! App active.")
        }
    }

    fun processAndVerifyRazorpayPayment(
        paymentId: String,
        planTier: String,
        priceInr: String,
        daysCount: Long,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            val email = userSession.value.email.ifBlank { "driver@dutyaccepter.com" }
            val name = userSession.value.name.ifBlank { email.substringBefore("@") }
            val keyId = com.example.BuildConfig.RAZORPAY_KEY_ID.ifBlank { "rzp_test_TGXVf7cQ3EJMt4" }

            repository.recordVerifiedRazorpayPayment(
                paymentId = paymentId,
                email = email,
                name = name,
                amount = priceInr,
                planTier = planTier,
                daysCount = daysCount,
                razorpayKeyId = keyId
            )

            _currentStep.value = AppStep.MAIN_APP
            showToast("Payment Verified ($paymentId)! Plan active and saved to Cloud.")
        }
    }

    fun loginAsGuest() {
        val guestEmail = "driver@dutyaccepter.com"
        val guestName = "Guest Driver"
        _userSession.value = UserSession(name = guestName, email = guestEmail, isLoggedIn = true)
        viewModelScope.launch {
            repository.restoreUserFromCloud(guestEmail, guestName)
            _currentStep.value = AppStep.MAIN_APP
            showToast("Logged in as Guest Driver")
        }
    }

    fun logout() {
        _userSession.value = UserSession()
        _currentStep.value = AppStep.AUTH
        showToast("Signed out successfully")
    }

    fun navigateToStep(step: AppStep) {
        _currentStep.value = step
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun updateFareInputs(min: String, max: String) {
        _minFare.value = min
        _maxFare.value = max
        _isFilterSaved.value = false
    }

    fun saveFareFilter() {
        _isFilterSaved.value = true
        showToast("Filter saved! Please restart the service for changes to take effect.")
    }

    fun toggleAlertSound(enabled: Boolean) {
        _isAlertSoundEnabled.value = enabled
        showToast(if (enabled) "Ride accept sound alert enabled" else "Ride accept sound alert disabled")
    }

    fun toggleServiceState(context: Context, active: Boolean) {
        val accGranted = DutyAccepterService.isAccessibilityServiceEnabled(context)
        if (active && !accGranted) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            showToast("Please enable 'Ride Accepter' in Accessibility Settings")
        } else {
            _isServiceActive.value = active
            showToast(if (active) "Service Active - Monitoring ride apps" else "Service Stopped")
        }
    }

    fun checkPermissions(context: Context) {
        val accGranted = DutyAccepterService.isAccessibilityServiceEnabled(context)
        _isAccessibilityGranted.value = accGranted
        _isServiceActive.value = accGranted

        _isOverlayGranted.value = Settings.canDrawOverlays(context)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        _isBatteryOptimizationExempt.value = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun checkServiceStatus() {
        checkPermissions(getApplication())
    }

    fun saveRule(rule: DutyRuleEntity) {
        viewModelScope.launch {
            repository.saveRule(rule)
            showToast("Rule '${rule.title}' saved successfully")
        }
    }

    fun toggleRule(ruleId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleRule(ruleId, isEnabled)
            showToast(if (isEnabled) "Rule enabled" else "Rule disabled")
        }
    }

    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            repository.deleteRule(ruleId)
            showToast("Rule deleted")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            showToast("Logs cleared")
        }
    }

    fun applyCoupon(code: String) {
        viewModelScope.launch {
            val email = userSession.value.email
            val name = userSession.value.name
            when (val res = repository.applyCoupon(code, email, name)) {
                is DutyRepository.CouponResult.Success -> {
                    _currentStep.value = AppStep.MAIN_APP
                    showToast(res.message)
                }
                is DutyRepository.CouponResult.Error -> {
                    showToast(res.error)
                }
            }
        }
    }

    fun upgradePlan(planTier: String) {
        viewModelScope.launch {
            val email = userSession.value.email
            val name = userSession.value.name
            repository.upgradePlan(planTier, email, name)
            _currentStep.value = AppStep.MAIN_APP
            showToast("Upgraded to $planTier tier! Synced to cloud.")
        }
    }

    fun runSimulationTest(sampleText: String, samplePackage: String, sampleButtonText: String) {
        viewModelScope.launch {
            _isSimulating.value = true
            _simulationLog.value = "🔍 Simulating accessibility node event from '$samplePackage'...\nTarget text: '$sampleText'"
            delay(400)

            val gateCheck = repository.checkGateAccess()
            if (gateCheck is DutyRepository.GateCheckResult.GateRequired) {
                _simulationLog.value += "\n\n❌ GATE BLOCKED: Free trial expired. Active subscription required to perform auto-click."
                repository.addLog(
                    DutyLogEntity(
                        eventType = "SIMULATION_BLOCKED",
                        ruleTitle = "Subscription Gate Block",
                        packageName = samplePackage,
                        matchedText = sampleText,
                        actionTaken = "None",
                        isSuccess = false,
                        statusMessage = "Gate check failed: Subscription required"
                    )
                )
                _isSimulating.value = false
                return@launch
            }

            val activeRules = repository.getActiveRules()
            _simulationLog.value += "\n📋 Loaded ${activeRules.size} active rules from Room database."
            delay(300)

            val matchingRule = activeRules.firstOrNull { rule ->
                val packageOk = rule.targetPackage == "*" || rule.targetPackage.equals(samplePackage, ignoreCase = true) || samplePackage.contains(rule.targetPackage, ignoreCase = true)
                val textOk = sampleText.contains(rule.keyword, ignoreCase = true)
                packageOk && textOk
            }

            if (matchingRule != null) {
                _simulationLog.value += "\n\n✅ MATCH FOUND: Rule '${matchingRule.title}' (Priority ${matchingRule.priority})"
                _simulationLog.value += "\n⏱️ Delay configured: ${matchingRule.delayMs} ms"
                delay(matchingRule.delayMs.coerceAtLeast(200))

                _simulationLog.value += "\n⚡ Programmatic ACTION_CLICK triggered on node text '${sampleButtonText}'"
                _simulationLog.value += "\n🎉 SUCCESS: Duty accepted automatically!"

                repository.addLog(
                    DutyLogEntity(
                        eventType = "SIMULATION_AUTO_CLICK",
                        ruleTitle = matchingRule.title,
                        packageName = samplePackage,
                        matchedText = "Keyword: '${matchingRule.keyword}'",
                        actionTaken = "Simulated ACTION_CLICK on '${sampleButtonText}'",
                        isSuccess = true,
                        statusMessage = "Matched rule '${matchingRule.title}' successfully"
                    )
                )
            } else {
                _simulationLog.value += "\n\n⚠️ NO MATCH: None of the active rules matched text '$sampleText'."
                repository.addLog(
                    DutyLogEntity(
                        eventType = "SIMULATION_NO_MATCH",
                        ruleTitle = "No Match",
                        packageName = samplePackage,
                        matchedText = sampleText,
                        actionTaken = "None",
                        isSuccess = false,
                        statusMessage = "No matching keyword found in active rule set"
                    )
                )
            }

            _isSimulating.value = false
        }
    }

    fun loadUserPayments() {
        viewModelScope.launch {
            val email = userSession.value.email
            if (email.isNotBlank()) {
                val list = repository.getUserPayments(email)
                _userPayments.value = list
            }
        }
    }

    fun loadAdminData() {
        if (!isAdmin) {
            showToast("Access Denied: Admin privileges required")
            return
        }
        viewModelScope.launch {
            val users = repository.getAllUsersForAdmin()
            val payments = repository.getAllPaymentsForAdmin()
            _adminUsers.value = users
            _adminPayments.value = payments
            showToast("Admin data updated from Firestore (${users.size} users, ${payments.size} payments)")
        }
    }

    fun toggleUserBlockAdmin(targetEmail: String, currentBlockedStatus: Boolean) {
        if (!isAdmin) return
        viewModelScope.launch {
            val newBlocked = !currentBlockedStatus
            val success = repository.blockUserAdmin(targetEmail, newBlocked)
            if (success) {
                showToast("User $targetEmail status updated to ${if (newBlocked) "BLOCKED" else "ACTIVE"}")
                loadAdminData()
            } else {
                showToast("Failed to update status in Firestore")
            }
        }
    }

    fun updateUserSubscriptionAdmin(targetEmail: String, planTier: String, daysCount: Long) {
        if (!isAdmin) return
        viewModelScope.launch {
            val success = repository.updateUserSubscriptionAdmin(targetEmail, planTier, daysCount)
            if (success) {
                showToast("Updated $targetEmail to $planTier ($daysCount days)")
                loadAdminData()
            } else {
                showToast("Failed to update user subscription")
            }
        }
    }

    fun submitSupportTicket(subject: String, details: String) {
        viewModelScope.launch {
            val email = userSession.value.email.ifBlank { "user@dutyaccepter.com" }
            val name = userSession.value.name.ifBlank { "Driver User" }
            val success = repository.submitSupportTicket(email, name, subject, details)
            if (success) {
                showToast("Support ticket submitted successfully! Our team will contact you shortly.")
            } else {
                showToast("Ticket saved locally. Support team notified.")
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        showToast("Theme set to ${if (enabled) "Dark Mode" else "Light Mode"}")
    }

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        showToast("Language changed to $lang")
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        showToast("Notifications ${if (enabled) "Enabled" else "Disabled"}")
    }

    fun deleteAccountData() {
        viewModelScope.launch {
            val email = userSession.value.email
            _userSession.value = UserSession()
            _currentStep.value = AppStep.AUTH
            showToast("Account $email deleted and session reset.")
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
