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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubscriptionEntity
import com.example.ui.DutyViewModel
import com.example.ui.components.PaymentGateDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubscriptionScreenGate(
    subscription: SubscriptionEntity,
    activeRulesCount: Int,
    viewModel: DutyViewModel,
    modifier: Modifier = Modifier
) {
    var showGateDialog by remember { mutableStateOf(false) }
    var couponInput by remember { mutableStateOf("") }

    val sdf = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val now = System.currentTimeMillis()
    val isExpired = now > subscription.expiryTimestamp && !subscription.isSubscribed

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Status Card
        item {
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = if (subscription.planTier == "UNLIMITED_VIP") Color(0xFFFBBF24) else Color(0xFF6366F1),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Gate Status",
                                tint = if (subscription.planTier == "UNLIMITED_VIP") Color(0xFFFBBF24) else Color(0xFF6366F1),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SECURITY & SUBSCRIPTION GATE",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (subscription.planTier == "UNLIMITED_VIP") Color(0xFFF59E0B)
                                    else if (subscription.isSubscribed) Color(0xFF10B981)
                                    else Color(0xFF6366F1)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = subscription.planTier,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (subscription.isSubscribed || subscription.planTier == "UNLIMITED_VIP") "Active Subscription Status: UNLOCKED"
                        else if (!isExpired) "Free Trial Period Active"
                        else "Free Trial Expired (Gate Active)",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isExpired) Color(0xFFEF4444) else Color(0xFF34D399),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Access Expiry Date: ${sdf.format(Date(subscription.expiryTimestamp))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    if (subscription.usedCoupon != null) {
                        Text(
                            text = "Active Coupon Applied: ${subscription.usedCoupon}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rules Usage Bar
                    Text(
                        text = "Rule Allocation: $activeRulesCount / ${subscription.maxRulesAllowed} Rules",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (activeRulesCount.toFloat() / subscription.maxRulesAllowed).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF6366F1),
                        trackColor = Color(0xFF1E293B),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showGateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_subscription_gate_dialog")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Upgrade")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Plan & Subscription Gate", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Coupon Activation Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = "Coupon",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REDEEM PROMO / COUPON CODE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter a valid access code to unlock VIP or Pro tier instantly (e.g., DUTY2026, VIPFREE).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = couponInput,
                            onValueChange = { couponInput = it.uppercase() },
                            placeholder = { Text("Code: DUTY2026 / VIPFREE", fontSize = 12.sp, color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFBBF24),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gate_screen_coupon_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (couponInput.isNotBlank()) {
                                    viewModel.applyCoupon(couponInput)
                                    couponInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("gate_screen_apply_coupon_btn")
                        ) {
                            Text("Redeem", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Gate Features List
        item {
            Text(
                text = "SECURITY GATE FEATURE CHECKLIST",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            GateFeatureCheckRow("Instant Accessibility Service ACTION_CLICK Execution", true)
            GateFeatureCheckRow("Unlimited Custom Window Keyword & App Matching Rules", subscription.planTier == "UNLIMITED_VIP")
            GateFeatureCheckRow("Sub-100ms Ultra-Low Delay Triggering", true)
            GateFeatureCheckRow("Audit Execution Logs & Historical Logging", true)
            GateFeatureCheckRow("Priority Rule Evaluation (P1-P5)", true)
        }
    }

    if (showGateDialog) {
        PaymentGateDialog(
            onDismiss = { showGateDialog = false },
            onApplyCoupon = { code ->
                viewModel.applyCoupon(code)
                showGateDialog = false
            },
            onSelectPlan = { plan ->
                viewModel.upgradePlan(plan)
                showGateDialog = false
            }
        )
    }
}

@Composable
private fun GateFeatureCheckRow(title: String, isUnlocked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = null,
            tint = if (isUnlocked) Color(0xFF10B981) else Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUnlocked) Color.White else Color.Gray,
            fontWeight = if (isUnlocked) FontWeight.Medium else FontWeight.Normal
        )
    }
}
