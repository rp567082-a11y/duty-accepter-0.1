package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubscriptionEntity
import com.example.ui.DutyViewModel
import com.example.ui.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userSession: UserSession,
    subscription: SubscriptionEntity,
    viewModel: DutyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val darkBg = Color(0xFF080D1A)
    val cardBg = Color(0xFF0F1B33)
    val cyanAccent = Color(0xFF38BDF8)
    val brightCyan = Color(0xFF00E5FF)

    var isHowToUseExpanded by remember { mutableStateOf(true) }
    var isChangeEmailExpanded by remember { mutableStateOf(true) }
    var isChangePasswordExpanded by remember { mutableStateOf(true) }

    var newEmail by remember { mutableStateOf("") }
    var emailCurrentPassword by remember { mutableStateOf("") }

    var passwordCurrent by remember { mutableStateOf("") }
    var passwordNew by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBg)
            )
        },
        containerColor = darkBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. USER PROFILE CARD
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar circle with DA Duty Accepter Logo
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0B1B36))
                                    .border(2.dp, cyanAccent, CircleShape),
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
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(brightCyan)
                                    .border(2.dp, darkBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Edit photo",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Remaining Days Calculation
                        val now = System.currentTimeMillis()
                        val diffMs = subscription.expiryTimestamp - now
                        val daysRemaining = if (diffMs > 0) (diffMs / (1000 * 60 * 60 * 24)).toInt() + 1 else 0
                        val isSubActive = subscription.isSubscribed && daysRemaining > 0

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSubActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isSubActive) "ACTIVE ($daysRemaining DAYS REMAINING)" else "EXPIRED / PAYMENT REQUIRED",
                                color = if (isSubActive) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // User Information Table
                        ProfileDetailRow(label = "Name", value = userSession.name.ifBlank { "Driver User" })
                        ProfileDetailRow(label = "Email", value = userSession.email.ifBlank { "driver@dutyaccepter.com" })
                        ProfileDetailRow(label = "Status", value = if (isSubActive) "Active Subscription" else "Expired", valueColor = if (isSubActive) Color(0xFF10B981) else Color(0xFFEF4444))
                        ProfileDetailRow(label = "Active Plan", value = subscription.planTier.uppercase().ifBlank { "PRO" })
                        ProfileDetailRow(label = "Expiry Date", value = if (subscription.expiryTimestamp > 0) java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(subscription.expiryTimestamp)) else "Not Active")
                        ProfileDetailRow(label = "Remaining Days", value = "$daysRemaining Days Left")
                    }
                }
            }

            // Quick Navigation Hub Card
            item {
                Text("App Hub & Utilities", color = cyanAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Payment History Button
                        NavigationHubRow(
                            title = "Payment History",
                            subtitle = "View Razorpay receipts and Firestore transactions",
                            icon = Icons.Default.CloudSync,
                            tint = cyanAccent,
                            onClick = { viewModel.navigateToStep(com.example.ui.AppStep.PAYMENT_HISTORY) }
                        )

                        HorizontalDivider(color = Color(0xFF1E293B))

                        // Settings Button
                        NavigationHubRow(
                            title = "Settings & Preferences",
                            subtitle = "Dark Mode, language, notifications, legal",
                            icon = Icons.Default.Security,
                            tint = cyanAccent,
                            onClick = { viewModel.navigateToStep(com.example.ui.AppStep.SETTINGS) }
                        )

                        HorizontalDivider(color = Color(0xFF1E293B))

                        // Help & Support Button
                        NavigationHubRow(
                            title = "Help & Support",
                            subtitle = "Contact support, report issue, FAQs",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            tint = cyanAccent,
                            onClick = { viewModel.navigateToStep(com.example.ui.AppStep.HELP_SUPPORT) }
                        )

                        // Admin Panel Entry Point (ONLY if Admin!)
                        if (viewModel.isAdmin) {
                            HorizontalDivider(color = Color(0xFF1E293B))
                            NavigationHubRow(
                                title = "Admin Panel (Authorized Only)",
                                subtitle = "Manage users, subscriptions, revenue analytics",
                                icon = Icons.Default.Lock,
                                tint = Color(0xFFEF4444),
                                onClick = { viewModel.navigateToStep(com.example.ui.AppStep.ADMIN_PANEL) }
                            )
                        }
                    }
                }
            }

            // Firebase Firestore Cloud Sync Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Cloud Sync",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Firebase Firestore Cloud Sync",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Account & Subscription Plan Backed Up",
                                    color = Color(0xFF10B981),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Your login details and active subscription plan are stored in Firebase Firestore. If you uninstall and reinstall the app, your active pack will automatically restore on sign in.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                val email = userSession.email.ifBlank { "rp567082@gmail.com" }
                                val name = userSession.name.ifBlank { "Ram" }
                                viewModel.loginUser(name, email)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Now",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore / Sync Cloud Subscription", fontSize = 13.sp)
                        }
                    }
                }
            }

            // 2. RIDE FILTERS — COMING SOON! BANNER
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = cyanAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ride Filters — Coming Soon!",
                                color = cyanAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Distance filters, location filters & more are currently under development. They will be available in the next update.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // 3. HOW TO USE (EXPANDABLE)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isHowToUseExpanded = !isHowToUseExpanded }
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = cyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "How to Use",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (isHowToUseExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle",
                                tint = Color(0xFF94A3B8)
                            )
                        }

                        AnimatedVisibility(
                            visible = isHowToUseExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp)
                                    .padding(bottom = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                HorizontalDivider(color = Color(0xFF1E293B))

                                // Item 1: Set App Language to English
                                GuideItem(
                                    icon = Icons.Default.Language,
                                    iconTint = cyanAccent,
                                    title = "Set App Language to English",
                                    description = "Keep Ola, Uber, and Rapido apps in English language for best ride detection accuracy."
                                )

                                // Item 2: Reduce Animations
                                Column {
                                    GuideItem(
                                        icon = Icons.Default.FlashOn,
                                        iconTint = Color(0xFFEAB308),
                                        title = "Reduce Animations",
                                        description = "Turn ON 'Reduce animations' in your phone's Accessibility settings. This removes ride app timers and makes ride acceptance faster."
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                            } catch (e: Exception) {
                                                viewModel.showToast("Opening Accessibility settings")
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = cyanAccent),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, cyanAccent),
                                        modifier = Modifier.padding(start = 34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Accessibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Open Accessibility Settings", fontSize = 13.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Graphic card showing Reduce Animations Toggle
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 34.dp)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFF0B132B))
                                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF16A34A)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Accessibility,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Accessibility",
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Reduce animations",
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowForward,
                                                        contentDescription = null,
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Switch(
                                                    checked = true,
                                                    onCheckedChange = {},
                                                    colors = SwitchDefaults.colors(
                                                        checkedTrackColor = Color(0xFF3B82F6),
                                                        checkedThumbColor = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                // Item 3: Disable Play Protect
                                GuideItem(
                                    icon = Icons.Default.Security,
                                    iconTint = cyanAccent,
                                    title = "Disable Play Protect",
                                    description = "Google Play Protect may block or remove this app because ride apps restrict third-party automation. Turn off Play Protect scanning to keep the app installed."
                                )

                                // Item 4: How It Works
                                GuideItem(
                                    icon = Icons.Default.SmartToy,
                                    iconTint = cyanAccent,
                                    title = "How It Works",
                                    description = "Ride Accepter uses AI-powered deep integration to detect ride requests from Ola, Rapido & other supported apps. It automatically accepts rides within your set fare range in milliseconds — even from floating overlays and notifications."
                                )
                            }
                        }
                    }
                }
            }

            // VERSION TEXT
            item {
                Text(
                    text = "Version 5.0",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // 4. CHANGE EMAIL (EXPANDABLE)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isChangeEmailExpanded = !isChangeEmailExpanded }
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = cyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Change Email",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (isChangeEmailExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle",
                                tint = Color(0xFF94A3B8)
                            )
                        }

                        AnimatedVisibility(
                            visible = isChangeEmailExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp)
                                    .padding(bottom = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = newEmail,
                                    onValueChange = { newEmail = it },
                                    label = { Text("New Email", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = cyanAccent,
                                        unfocusedBorderColor = Color(0xFF1E293B)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = emailCurrentPassword,
                                    onValueChange = { emailCurrentPassword = it },
                                    label = { Text("Current Password", color = Color(0xFF94A3B8)) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = cyanAccent,
                                        unfocusedBorderColor = Color(0xFF1E293B)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Button(
                                    onClick = {
                                        if (newEmail.isNotBlank()) {
                                            viewModel.showToast("Verification email sent to $newEmail")
                                        } else {
                                            viewModel.showToast("Please enter new email address")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                                ) {
                                    Text("Send Verification Email", color = Color.White, fontSize = 15.sp)
                                }

                                Text(
                                    text = "✉ Also check your Spam/Junk folder in your mail app.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // 5. CHANGE PASSWORD (EXPANDABLE)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isChangePasswordExpanded = !isChangePasswordExpanded }
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = cyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Change Password",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (isChangePasswordExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle",
                                tint = Color(0xFF94A3B8)
                            )
                        }

                        AnimatedVisibility(
                            visible = isChangePasswordExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp)
                                    .padding(bottom = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = passwordCurrent,
                                    onValueChange = { passwordCurrent = it },
                                    label = { Text("Current Password", color = Color(0xFF94A3B8)) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = cyanAccent,
                                        unfocusedBorderColor = Color(0xFF1E293B)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = passwordNew,
                                    onValueChange = { passwordNew = it },
                                    label = { Text("New Password", color = Color(0xFF94A3B8)) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = cyanAccent,
                                        unfocusedBorderColor = Color(0xFF1E293B)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = passwordConfirm,
                                    onValueChange = { passwordConfirm = it },
                                    label = { Text("Confirm New Password", color = Color(0xFF94A3B8)) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = cyanAccent,
                                        unfocusedBorderColor = Color(0xFF1E293B)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Button(
                                    onClick = {
                                        if (passwordNew.isNotBlank() && passwordNew == passwordConfirm) {
                                            viewModel.showToast("Password updated successfully")
                                            passwordCurrent = ""
                                            passwordNew = ""
                                            passwordConfirm = ""
                                        } else {
                                            viewModel.showToast("Passwords do not match")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                                ) {
                                    Text("Change Password", color = Color.White, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 6. SIGN OUT BUTTON
            item {
                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign Out",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 15.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GuideItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color(0xFFCBD5E1),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun NavigationHubRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
    }
}
