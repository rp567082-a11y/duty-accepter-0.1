package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.ui.AppStep
import com.example.ui.AppTab
import com.example.ui.DutyViewModel
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HelpSupportScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PaymentHistoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubscriptionPlanScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DecodeItTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DutyViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DecodeItTheme(darkTheme = true) {
                val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
                val userSession by viewModel.userSession.collectAsStateWithLifecycle()
                val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
                val rules by viewModel.rules.collectAsStateWithLifecycle()
                val logs by viewModel.logs.collectAsStateWithLifecycle()
                val subscription by viewModel.subscription.collectAsStateWithLifecycle()
                val isServiceActive by viewModel.isServiceActive.collectAsStateWithLifecycle()
                val simulationLog by viewModel.simulationLog.collectAsStateWithLifecycle()
                val isSimulating by viewModel.isSimulating.collectAsStateWithLifecycle()
                val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    viewModel.checkPermissions(this@MainActivity)
                    val secStatus = com.example.util.SecurityCheckUtils.checkSecurityStatus(this@MainActivity)
                    if (!secStatus.isSignatureValid) {
                        viewModel.showToast("App integrity verification failed: Untrusted APK signature")
                    } else if (secStatus.isRooted) {
                        viewModel.showToast("Security Warning: Rooted environment detected")
                    } else if (secStatus.isDebuggerConnected) {
                        viewModel.showToast("Security Notice: Active debugger detected")
                    } else if (secStatus.isFridaDetected) {
                        viewModel.showToast("Security Alert: Dynamic hook tool (Frida) detected!")
                    } else if (secStatus.isXposedDetected) {
                        viewModel.showToast("Security Alert: Framework modification (Xposed) detected!")
                    }
                }

                LaunchedEffect(toastMessage) {
                    toastMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearToast()
                    }
                }

                // Automatic Subscription Expiration Monitor & Gating
                LaunchedEffect(subscription, currentStep) {
                    while (true) {
                        val now = System.currentTimeMillis()
                        val isExpired = subscription.expiryTimestamp > 0L && now >= subscription.expiryTimestamp
                        if (isExpired && (currentStep == AppStep.MAIN_APP || currentStep == AppStep.PROFILE)) {
                            viewModel.showToast("Subscription plan expired! Redirecting to plan purchase...")
                            viewModel.navigateToStep(AppStep.SUBSCRIPTION_PLAN)
                        }
                        kotlinx.coroutines.delay(10000L)
                    }
                }

                Crossfade(targetState = currentStep, label = "StepCrossfade") { step ->
                    when (step) {
                        AppStep.AUTH -> {
                            AuthScreen(
                                onLoginSuccess = { name, email ->
                                    viewModel.loginUser(name, email)
                                },
                                onGuestLogin = {
                                    viewModel.loginAsGuest()
                                }
                            )
                        }

                        AppStep.SUBSCRIPTION_PLAN -> {
                            SubscriptionPlanScreen(
                                currentSubscription = subscription,
                                userName = userSession.name.ifBlank { "User" },
                                onPlanSelected = { plan ->
                                    viewModel.upgradePlan(plan)
                                },
                                onApplyCoupon = { code ->
                                    viewModel.applyCoupon(code)
                                },
                                onVerifiedPayment = { paymentId, planTier, priceInr, daysCount, paymentMethod ->
                                    viewModel.processAndVerifyRazorpayPayment(
                                        paymentId = paymentId,
                                        planTier = planTier,
                                        priceInr = priceInr,
                                        daysCount = daysCount,
                                        paymentMethod = paymentMethod
                                    )
                                },
                                onProceedToApp = {
                                    val now = System.currentTimeMillis()
                                    if (subscription.expiryTimestamp > now) {
                                        viewModel.navigateToStep(AppStep.MAIN_APP)
                                    } else {
                                        viewModel.showToast("Active plan required. Please select a plan or enter a valid coupon.")
                                    }
                                }
                            )
                        }

                        AppStep.MAIN_APP -> {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                topBar = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF080D1A))
                                    ) {
                                        // Header Row
                                        TopAppBar(
                                            title = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Round Logo Badge with DA Duty Accepter image
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF0B1B36))
                                                            .border(1.dp, Color(0xFF00E5FF), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Image(
                                                            painter = painterResource(id = R.drawable.img_app_logo_1784727011307),
                                                            contentDescription = "DA Duty Accepter Logo",
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(CircleShape)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = "DA Duty Accepter",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 19.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            },
                                            actions = {
                                                // User Profile Account Icon
                                                IconButton(
                                                    onClick = {
                                                        viewModel.navigateToStep(AppStep.PROFILE)
                                                    },
                                                    modifier = Modifier.testTag("app_profile_btn")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountCircle,
                                                        contentDescription = "Profile & Account",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            },
                                            colors = TopAppBarDefaults.topAppBarColors(
                                                containerColor = Color(0xFF080D1A),
                                                titleContentColor = Color.White
                                            )
                                        )

                                        // Top Navigation Tabs Bar
                                        val tabs = listOf(
                                            AppTab.DASHBOARD to ("Dashboard" to Icons.Default.GridView),
                                            AppTab.HISTORY to ("History" to Icons.Default.History),
                                            AppTab.APPS to ("Apps" to Icons.Default.Apps)
                                        )

                                        val selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)

                                        TabRow(
                                            selectedTabIndex = selectedTabIndex,
                                            containerColor = Color(0xFF080D1A),
                                            contentColor = Color(0xFF38BDF8),
                                            indicator = { tabPositions ->
                                                if (selectedTabIndex < tabPositions.size) {
                                                    TabRowDefaults.SecondaryIndicator(
                                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                                        height = 3.dp,
                                                        color = Color(0xFF00E5FF)
                                                    )
                                                }
                                            },
                                            divider = {}
                                        ) {
                                            tabs.forEachIndexed { index, (tabEnum, pair) ->
                                                val (title, icon) = pair
                                                val isSelected = selectedTab == tabEnum

                                                Tab(
                                                    selected = isSelected,
                                                    onClick = { viewModel.selectTab(tabEnum) },
                                                    text = {
                                                        Text(
                                                            text = title,
                                                            fontSize = 14.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                                        )
                                                    },
                                                    icon = {
                                                        Icon(
                                                            imageVector = icon,
                                                            contentDescription = title,
                                                            tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    },
                                                    modifier = Modifier.testTag("tab_${title.lowercase()}")
                                                )
                                            }
                                        }
                                    }
                                },
                                snackbarHost = { SnackbarHost(snackbarHostState) },
                                containerColor = Color(0xFF080D1A)
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    Crossfade(targetState = selectedTab, label = "TabCrossfade") { tab ->
                                        when (tab) {
                                            AppTab.DASHBOARD -> DashboardScreen(
                                                viewModel = viewModel,
                                                subscription = subscription,
                                                onNavigateToSubscription = {
                                                    viewModel.navigateToStep(AppStep.SUBSCRIPTION_PLAN)
                                                }
                                            )
                                            AppTab.HISTORY -> HistoryScreen(
                                                logs = logs,
                                                viewModel = viewModel
                                            )
                                            AppTab.APPS -> AppsScreen(
                                                rules = rules,
                                                viewModel = viewModel
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AppStep.PROFILE -> {
                            ProfileScreen(
                                userSession = userSession,
                                subscription = subscription,
                                viewModel = viewModel,
                                onBack = {
                                    viewModel.navigateToStep(AppStep.MAIN_APP)
                                }
                            )
                        }

                        AppStep.PAYMENT_HISTORY -> {
                            PaymentHistoryScreen(
                                viewModel = viewModel,
                                onBack = {
                                    viewModel.navigateToStep(AppStep.PROFILE)
                                }
                            )
                        }

                        AppStep.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = {
                                    viewModel.navigateToStep(AppStep.PROFILE)
                                }
                            )
                        }

                        AppStep.HELP_SUPPORT -> {
                            HelpSupportScreen(
                                viewModel = viewModel,
                                onBack = {
                                    viewModel.navigateToStep(AppStep.PROFILE)
                                }
                            )
                        }

                        AppStep.ADMIN_PANEL -> {
                            AdminPanelScreen(
                                viewModel = viewModel,
                                onBack = {
                                    viewModel.navigateToStep(AppStep.PROFILE)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
