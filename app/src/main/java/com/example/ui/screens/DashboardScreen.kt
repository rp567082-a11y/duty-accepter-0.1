package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SubscriptionEntity
import com.example.ui.DutyViewModel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    viewModel: DutyViewModel,
    subscription: SubscriptionEntity,
    onNavigateToSubscription: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val isServiceActive by viewModel.isServiceActive.collectAsStateWithLifecycle()
    val minFare by viewModel.minFare.collectAsStateWithLifecycle()
    val maxFare by viewModel.maxFare.collectAsStateWithLifecycle()
    val isFilterSaved by viewModel.isFilterSaved.collectAsStateWithLifecycle()
    val isAlertSoundEnabled by viewModel.isAlertSoundEnabled.collectAsStateWithLifecycle()
    val acceptSpeedMode by viewModel.acceptSpeedMode.collectAsStateWithLifecycle()

    val isAccessibilityGranted by viewModel.isAccessibilityGranted.collectAsStateWithLifecycle()
    val isOverlayGranted by viewModel.isOverlayGranted.collectAsStateWithLifecycle()
    val isBatteryOptimizationExempt by viewModel.isBatteryOptimizationExempt.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    val darkBg = Color(0xFF080D1A)
    val cardBg = Color(0xFF111C30)
    val cyanAccent = Color(0xFF38BDF8)
    val brightCyan = Color(0xFF00E5FF)
    val greenSuccess = Color(0xFF22C55E)
    val warningYellow = Color(0xFFF59E0B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. SUBSCRIPTION REMAINING TIME CARD (Position #1)
        SubscriptionTimeRemainingCard(
            subscription = subscription,
            onExtendClick = {
                onNavigateToSubscription?.invoke()
            }
        )

        // 2. SERVICE CONTROL SECTION
        Column {
            Text(
                text = "Service Control",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (isServiceActive) Color(0xFF0F382C) else Color(0xFF2C1E23),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Service Status",
                                tint = if (isServiceActive) greenSuccess else Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isServiceActive) "Service Active" else "Service Stopped",
                                color = if (isServiceActive) greenSuccess else Color(0xFFEF4444),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Monitoring ride apps",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = isServiceActive,
                        onCheckedChange = { active ->
                            viewModel.toggleServiceState(context, active)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = greenSuccess,
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }
            }
        }

        // 3. FARE RANGE SECTION
        Column {
            Text(
                text = "Fare Range",
                color = cyanAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Minimum Fare Field
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Minimum ₹",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = minFare,
                                onValueChange = { newMin ->
                                    viewModel.updateFareInputs(newMin, maxFare)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0B132B),
                                    unfocusedContainerColor = Color(0xFF0B132B),
                                    focusedBorderColor = brightCyan,
                                    unfocusedBorderColor = Color(0xFF1E293B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Maximum Fare Field
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Maximum ₹",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = maxFare,
                                onValueChange = { newMax ->
                                    viewModel.updateFareInputs(minFare, newMax)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0B132B),
                                    unfocusedContainerColor = Color(0xFF0B132B),
                                    focusedBorderColor = brightCyan,
                                    unfocusedBorderColor = Color(0xFF1E293B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SAVE FILTER BUTTON (GREEN IF SAVED, BRIGHT CYAN IF UNSAVED)
                    Button(
                        onClick = { viewModel.saveFareFilter() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFilterSaved) Color(0xFF22C55E) else brightCyan,
                            contentColor = if (isFilterSaved) Color.White else Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (isFilterSaved) "✓   Filter Saved   ✓" else "💾   Save Filter",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isFilterSaved) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠️ Unsaved changes",
                            color = warningYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 4. ACCEPTANCE SPEED & DELAY MODE
        Column {
            Text(
                text = "Acceptance Speed & Reaction Delay",
                color = cyanAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.ui.AcceptSpeedMode.values().forEach { mode ->
                            val isSelected = acceptSpeedMode == mode
                            val itemBorderColor = if (isSelected) brightCyan else Color(0xFF1E293B)
                            val itemBgColor = if (isSelected) Color(0xFF0F2B48) else Color(0xFF0B132B)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(itemBgColor)
                                    .border(1.dp, itemBorderColor, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setAcceptSpeedMode(mode) }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = when (mode) {
                                            com.example.ui.AcceptSpeedMode.TURBO -> Icons.Default.FlashOn
                                            com.example.ui.AcceptSpeedMode.NORMAL -> Icons.Default.Timer
                                            com.example.ui.AcceptSpeedMode.HUMAN -> Icons.Default.Security
                                        },
                                        contentDescription = mode.title,
                                        tint = if (isSelected) brightCyan else Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = mode.title,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = mode.delayText,
                                        color = if (isSelected) brightCyan else Color(0xFF64748B),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description Banner for selected speed mode
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0B132B))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (acceptSpeedMode) {
                                    com.example.ui.AcceptSpeedMode.TURBO -> Icons.Default.FlashOn
                                    com.example.ui.AcceptSpeedMode.NORMAL -> Icons.Default.Timer
                                    com.example.ui.AcceptSpeedMode.HUMAN -> Icons.Default.Security
                                },
                                contentDescription = null,
                                tint = when (acceptSpeedMode) {
                                    com.example.ui.AcceptSpeedMode.TURBO -> Color(0xFFEF4444)
                                    com.example.ui.AcceptSpeedMode.NORMAL -> warningYellow
                                    com.example.ui.AcceptSpeedMode.HUMAN -> greenSuccess
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when (acceptSpeedMode) {
                                    com.example.ui.AcceptSpeedMode.TURBO -> "⚡ Instant 0.1s delay: Triggers tap immediately when a ride matches filter."
                                    com.example.ui.AcceptSpeedMode.NORMAL -> "⏱️ Standard 1.0s delay: Normal reaction buffer before accepting."
                                    com.example.ui.AcceptSpeedMode.HUMAN -> "🛡️ Human-Like (1.5s - 3.0s): Randomized natural finger delay to prevent app anti-bot detection."
                                },
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. RIDE ALERT SECTION
        Column {
            Text(
                text = "Ride Alert",
                color = cyanAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF0D3352), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alert Sound",
                                tint = cyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Alert on Accept",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Play sound when ride is accepted",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = isAlertSoundEnabled,
                        onCheckedChange = { viewModel.toggleAlertSound(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = brightCyan,
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }
            }
        }

        // 5. PERMISSIONS SECTION
        Column {
            Text(
                text = "Permissions",
                color = cyanAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Card 1: Accessibility Service
                PermissionItemCard(
                    icon = Icons.Default.AccessibilityNew,
                    iconBgColor = Color(0xFF064E3B),
                    iconTint = Color(0xFF34D399),
                    title = "Accessibility Service",
                    statusText = if (isAccessibilityGranted) "Enabled" else "Disabled - Tap to enable",
                    isGranted = isAccessibilityGranted,
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )

                // Card 2: Overlay Permission
                PermissionItemCard(
                    icon = Icons.Default.Layers,
                    iconBgColor = Color(0xFF4C1D95),
                    iconTint = Color(0xFFF43F5E),
                    title = "Overlay Permission",
                    statusText = if (isOverlayGranted) "Granted" else "Required for floating widget",
                    isGranted = isOverlayGranted,
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )

                // Card 3: Battery Optimization
                PermissionItemCard(
                    icon = Icons.Default.BatteryFull,
                    iconBgColor = Color(0xFF7F1D1D),
                    iconTint = Color(0xFFF87171),
                    title = "Battery Optimization",
                    statusText = if (isBatteryOptimizationExempt) "Unrestricted" else "Tap to exempt",
                    isGranted = isBatteryOptimizationExempt,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(
                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:${context.packageName}")
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    statusText: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111C30))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = statusText,
                        color = if (isGranted) Color(0xFF34D399) else Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Grant Permission",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun SubscriptionTimeRemainingCard(
    subscription: SubscriptionEntity,
    onExtendClick: () -> Unit
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val remainingMillis = (subscription.expiryTimestamp - currentTime).coerceAtLeast(0L)
    val isExpired = remainingMillis <= 0L

    val daysLeft = remainingMillis / (24 * 60 * 60 * 1000L)
    val hoursLeft = (remainingMillis % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L)
    val minsLeft = (remainingMillis % (60 * 60 * 1000L)) / (60 * 1000L)
    val secsLeft = (remainingMillis % (60 * 1000L)) / 1000L

    val formattedPlanTitle = when (subscription.planTier) {
        "1_DAY" -> "1 Day Plan"
        "1_WEEK" -> "1 Week Plan"
        "15_DAYS" -> "15 Days Plan"
        "1_MONTH" -> "1 Month Plan ⭐"
        "3_MONTHS" -> "3 Months Plan"
        "1_YEAR" -> "1 Year VIP Plan 👑"
        "UNLIMITED_VIP" -> "Unlimited VIP Plan"
        "PRO" -> "Pro Plan"
        "FREE_TRIAL" -> "Free Trial"
        else -> subscription.planTier.ifBlank { "Active Plan" }
    }

    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val expiryDateStr = remember(subscription.expiryTimestamp) {
        sdf.format(Date(subscription.expiryTimestamp))
    }

    val cyanAccent = Color(0xFF00E5FF)
    val greenSuccess = Color(0xFF22C55E)
    val redDanger = Color(0xFFEF4444)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subscription Plan Status",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (!isExpired) greenSuccess.copy(alpha = 0.2f) else redDanger.copy(alpha = 0.2f))
                    .border(1.dp, if (!isExpired) greenSuccess else redDanger, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (!isExpired) greenSuccess else redDanger)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (!isExpired) "ACTIVE" else "EXPIRED",
                        color = if (!isExpired) greenSuccess else redDanger,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (!isExpired) {
                            listOf(Color(0xFF0B1B36), Color(0xFF071428))
                        } else {
                            listOf(Color(0xFF2A0D15), Color(0xFF18060B))
                        }
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = if (!isExpired) cyanAccent.copy(alpha = 0.6f) else redDanger.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(18.dp)
                .testTag("dashboard_subscription_remaining_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (!isExpired) cyanAccent.copy(alpha = 0.15f) else redDanger.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = if (!isExpired) cyanAccent else redDanger,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = formattedPlanTitle,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Expires: $expiryDateStr",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Extend / Upgrade button
                    Button(
                        onClick = onExtendClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isExpired) Color(0xFF1E293B) else redDanger
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("dashboard_renew_plan_btn")
                    ) {
                        Text(
                            text = if (!isExpired) "RENEW" else "BUY NOW",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time Remaining Banner Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF040A1A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = "TIME REMAINING",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (!isExpired) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (daysLeft > 0) {
                                        "${daysLeft}d ${hoursLeft}h ${minsLeft}m ${secsLeft}s"
                                    } else {
                                        "${hoursLeft}h ${minsLeft}m ${secsLeft}s"
                                    },
                                    color = cyanAccent,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )

                                Text(
                                    text = "LEFT",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Plan Time Finished • Purchase Plan to Continue",
                                color = redDanger,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

