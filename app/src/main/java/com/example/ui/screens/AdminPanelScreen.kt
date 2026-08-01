package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FirestoreUserData
import com.example.data.PaymentRecord
import com.example.ui.DutyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: DutyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkBg = Color(0xFF080D1A)
    val cardBg = Color(0xFF0F1B33)
    val cyanAccent = Color(0xFF38BDF8)
    val greenSuccess = Color(0xFF10B981)
    val redDanger = Color(0xFFEF4444)
    val amberWarning = Color(0xFFF59E0B)

    val isAdmin = viewModel.isAdmin
    val users by viewModel.adminUsers.collectAsStateWithLifecycle()
    val payments by viewModel.adminPayments.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedUserForEdit by remember { mutableStateOf<FirestoreUserData?>(null) }

    LaunchedEffect(Unit) {
        if (isAdmin) {
            viewModel.loadAdminData()
        }
    }

    if (!isAdmin) {
        // Access Denied Shield
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Panel", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBg)
                )
            },
            containerColor = darkBg
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, redDanger, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(redDanger.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = redDanger, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ACCESS DENIED",
                            color = redDanger,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Admin Panel is strictly restricted to authorized Firebase Administrator:\nrp567082@gmail.com",
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Return to Application", color = Color.White)
                        }
                    }
                }
            }
        }
        return
    }

    // Revenue and Analytics Calculations
    val now = System.currentTimeMillis()
    val activeUsersCount = users.count { it.isSubscribed && it.expiryTimestamp > now && !it.isBlocked }
    val expiredUsersCount = users.size - activeUsersCount
    val totalPaymentsCount = payments.size

    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis

    cal.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonth = cal.timeInMillis

    fun parseAmountInt(amtStr: String): Int {
        val clean = amtStr.replace("₹", "").replace(",", "").trim()
        return clean.toIntOrNull() ?: 99
    }

    val todayRevenue = payments
        .filter { it.purchaseDate >= startOfToday }
        .sumOf { parseAmountInt(it.amount) }

    val monthlyRevenue = payments
        .filter { it.purchaseDate >= startOfMonth }
        .sumOf { parseAmountInt(it.amount) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = cyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Panel",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAdminData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Admin Data", tint = cyanAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBg)
            )
        },
        containerColor = darkBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Metrics Row
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(title = "Total Users", value = "${users.size}", icon = Icons.Default.Group, color = cyanAccent, modifier = Modifier.weight(1f), cardBg = cardBg)
                            MetricCard(title = "Active Subscriptions", value = "$activeUsersCount", icon = Icons.Default.CheckCircle, color = greenSuccess, modifier = Modifier.weight(1f), cardBg = cardBg)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(title = "Expired Subscriptions", value = "$expiredUsersCount", icon = Icons.Default.Block, color = redDanger, modifier = Modifier.weight(1f), cardBg = cardBg)
                            MetricCard(title = "Total Payments", value = "$totalPaymentsCount", icon = Icons.Default.Payments, color = amberWarning, modifier = Modifier.weight(1f), cardBg = cardBg)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(title = "Today's Revenue", value = "₹$todayRevenue", icon = Icons.Default.CurrencyRupee, color = greenSuccess, modifier = Modifier.weight(1f), cardBg = cardBg)
                            MetricCard(title = "Monthly Revenue", value = "₹$monthlyRevenue", icon = Icons.Default.CurrencyRupee, color = cyanAccent, modifier = Modifier.weight(1f), cardBg = cardBg)
                        }
                    }
                }

                // Tab Selector
                item {
                    val tabs = listOf("Users Management", "Subscriptions", "Payment Records")
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = cardBg,
                        contentColor = cyanAccent,
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    height = 3.dp,
                                    color = cyanAccent
                                )
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                // Tab Content
                when (selectedTabIndex) {
                    0 -> { // Users Tab
                        if (users.isEmpty()) {
                            item {
                                Text("No user records loaded from Firestore.", color = Color(0xFF94A3B8), modifier = Modifier.padding(16.dp))
                            }
                        } else {
                            items(users, key = { it.email }) { user ->
                                UserAdminCard(
                                    user = user,
                                    cardBg = cardBg,
                                    greenSuccess = greenSuccess,
                                    redDanger = redDanger,
                                    cyanAccent = cyanAccent,
                                    onToggleBlock = { viewModel.toggleUserBlockAdmin(user.email, user.isBlocked) },
                                    onEditSubscription = { selectedUserForEdit = user }
                                )
                            }
                        }
                    }
                    1 -> { // Subscriptions Summary Tab
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Subscription Tier Breakdown", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    HorizontalDivider(color = Color(0xFF1E293B))
                                    val tiers = users.groupBy { it.planTier }
                                    tiers.forEach { (tier, list) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(tier.uppercase(), color = cyanAccent, fontWeight = FontWeight.Bold)
                                            Text("${list.size} Users", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> { // Payments Tab
                        if (payments.isEmpty()) {
                            item {
                                Text("No payment receipts recorded in Firestore.", color = Color(0xFF94A3B8), modifier = Modifier.padding(16.dp))
                            }
                        } else {
                            items(payments, key = { it.paymentId }) { payment ->
                                AdminPaymentCard(payment = payment, cardBg = cardBg, cyanAccent = cyanAccent, greenSuccess = greenSuccess)
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Subscription Modal Dialog
    selectedUserForEdit?.let { editUser ->
        AlertDialog(
            onDismissRequest = { selectedUserForEdit = null },
            title = { Text("Grant Subscription: ${editUser.name}", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("User Email: ${editUser.email}", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                    Text("Select plan duration to override in Firestore:", color = Color.White, fontSize = 14.sp)

                    val plans = listOf(
                        "1_DAY" to 1L,
                        "1_WEEK" to 7L,
                        "15_DAYS" to 15L,
                        "1_MONTH" to 30L,
                        "3_MONTHS" to 90L,
                        "1_YEAR" to 365L
                    )

                    plans.forEach { (planName, days) ->
                        OutlinedButton(
                            onClick = {
                                viewModel.updateUserSubscriptionAdmin(editUser.email, planName, days)
                                selectedUserForEdit = null
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grant $planName ($days Days)", color = cyanAccent)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedUserForEdit = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = cardBg
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    cardBg: Color
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = modifier.border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun UserAdminCard(
    user: FirestoreUserData,
    cardBg: Color,
    greenSuccess: Color,
    redDanger: Color,
    cyanAccent: Color,
    onToggleBlock: () -> Unit,
    onEditSubscription: () -> Unit
) {
    val isCurrentlyActive = user.isSubscribed && !user.isBlocked
    val expiryDateStr = if (user.expiryTimestamp > 0) SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(user.expiryTimestamp)) else "No Active Plan"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (user.isBlocked) redDanger else Color(0xFF1E293B), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = user.name.ifBlank { "User Driver" }, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = user.email, color = Color(0xFF94A3B8), fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (user.isBlocked) redDanger.copy(alpha = 0.2f) else if (isCurrentlyActive) greenSuccess.copy(alpha = 0.2f) else Color(0xFF334155))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (user.isBlocked) "BLOCKED" else if (isCurrentlyActive) "ACTIVE" else "EXPIRED",
                        color = if (user.isBlocked) redDanger else if (isCurrentlyActive) greenSuccess else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Plan: ${user.planTier.uppercase()}", color = cyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(text = "Expiry: $expiryDateStr", color = Color(0xFFCBD5E1), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onToggleBlock,
                    colors = ButtonDefaults.buttonColors(containerColor = if (user.isBlocked) greenSuccess else redDanger),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(if (user.isBlocked) "UNBLOCK USER" else "BLOCK USER", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onEditSubscription,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = cyanAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cyanAccent),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text("EXTEND PLAN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminPaymentCard(
    payment: PaymentRecord,
    cardBg: Color,
    cyanAccent: Color,
    greenSuccess: Color
) {
    val dateStr = if (payment.purchaseDateFormatted.isNotBlank()) payment.purchaseDateFormatted else SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(payment.purchaseDate))

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = payment.amount, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = payment.paymentId, color = cyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "User: ${payment.userId}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
            Text(text = "Plan: ${payment.planTier.uppercase()} | Date: $dateStr", color = Color(0xFF94A3B8), fontSize = 11.sp)
        }
    }
}
