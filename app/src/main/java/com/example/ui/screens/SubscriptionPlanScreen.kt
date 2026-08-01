package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubscriptionEntity

data class RidePlanItem(
    val id: String,
    val title: String,
    val daysText: String,
    val priceInr: String,
    val badgeText: String? = null,
    val isBestValue: Boolean = false
)

@Composable
fun SubscriptionPlanScreen(
    currentSubscription: SubscriptionEntity,
    userName: String,
    onPlanSelected: (planTier: String) -> Unit,
    onApplyCoupon: (code: String) -> Unit,
    onProceedToApp: () -> Unit,
    onVerifiedPayment: (paymentId: String, planTier: String, priceInr: String, daysCount: Long, paymentMethod: String) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val plans = remember {
        listOf(
            RidePlanItem("1_DAY", "1 Day", "24 Hours", "₹20"),
            RidePlanItem("1_WEEK", "1 Week", "7 Days", "₹99"),
            RidePlanItem("15_DAYS", "15 Days", "15 Days", "₹179"),
            RidePlanItem("1_MONTH", "1 Month", "30 Days", "₹299", badgeText = "⭐ MOST POPULAR"),
            RidePlanItem("3_MONTHS", "3 Months", "90 Days", "₹699"),
            RidePlanItem("1_YEAR", "1 Year", "365 Days", "₹1999", badgeText = "👑 BEST VALUE", isBestValue = true)
        )
    }

    var selectedPlanId by remember { mutableStateOf("1_YEAR") }
    var couponCodeInput by remember { mutableStateOf("") }
    var couponAppliedMsg by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // Cyan Brand Theme Colors
    val cyanAccent = Color(0xFF00D2FF)
    val darkBackground = Color(0xFF040814)
    val cardBackground = Color(0xFF0B1326)
    val selectedCardBackground = Color(0xFF051D33)
    val cardBorderUnselected = Color(0xFF141E33)
    val orangeBadgeColor = Color(0xFFFF9500)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        darkBackground,
                        Color(0xFF02050E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            val isPlanActive = currentSubscription.expiryTimestamp > System.currentTimeMillis()

            if (isPlanActive) {
                // Active Plan Card with Quick Entry
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F291E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFF22C55E), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE SUBSCRIPTION",
                                color = Color(0xFF22C55E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Your plan is currently active",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onProceedToApp,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_app_dashboard_btn")
                        ) {
                            Text("OPEN APP", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "EXTEND SUBSCRIPTION",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Select a plan to add more days to your active account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            } else {
                // PLAN EXPIRED Header
                Text(
                    text = "PLAN EXPIRED",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    fontSize = 28.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Purchase a plan to continue using Duty Accepter",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Plan Cards List
            plans.forEach { plan ->
                val isSelected = selectedPlanId == plan.id

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) selectedCardBackground else cardBackground)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) cyanAccent else cardBorderUnselected,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedPlanId = plan.id }
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                        .testTag("plan_card_${plan.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Left Circular Icon
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) cyanAccent else Color(0xFF131D33)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF040814),
                                        modifier = Modifier.size(26.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = "Calendar",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Middle Info
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = plan.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    )

                                    if (plan.badgeText != null) {
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (plan.isBestValue) orangeBadgeColor else Color(0xFF38BDF8))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = plan.badgeText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Black,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = plan.daysText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Right Price
                        Text(
                            text = plan.priceInr,
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (isSelected) cyanAccent else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Coupon Code Card Section (Matching screenshot design)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorderUnselected, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE11D48).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = "Coupon Icon",
                                tint = Color(0xFFFB7185),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Have a coupon code?",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = couponCodeInput,
                            onValueChange = { couponCodeInput = it.uppercase() },
                            placeholder = {
                                Text(
                                    "Enter code",
                                    fontSize = 14.sp,
                                    color = Color(0xFF475569)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cyanAccent,
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedContainerColor = Color(0xFF070E1E),
                                unfocusedContainerColor = Color(0xFF070E1E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("plan_screen_coupon_input")
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                if (couponCodeInput.isNotBlank()) {
                                    onApplyCoupon(couponCodeInput)
                                    couponAppliedMsg = "Code '$couponCodeInput' Applied!"
                                    couponCodeInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A374E)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("plan_screen_apply_coupon_btn")
                        ) {
                            Text(
                                "APPLY",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    if (couponAppliedMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = couponAppliedMsg!!,
                            color = cyanAccent,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main CTA: PROCEED TO PAYMENT Button (Bright Cyan, matching screenshot)
            var showPaymentGatewayModal by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    showPaymentGatewayModal = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = cyanAccent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("activate_plan_proceed_button")
            ) {
                Text(
                    text = "PROCEED TO PAYMENT",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary CTA: Proceed to Dashboard / Free Trial
            Button(
                onClick = {
                    onApplyCoupon("FREE_TRIAL")
                    onProceedToApp()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("start_free_trial_dashboard_button")
            ) {
                Text(
                    text = "START 30-DAY FREE TRIAL & GO TO DASHBOARD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }

            if (showPaymentGatewayModal) {
                val selectedPlanObj = plans.firstOrNull { it.id == selectedPlanId } ?: plans.first()
                val daysCount = when (selectedPlanId) {
                    "1_DAY" -> 1L
                    "1_WEEK" -> 7L
                    "15_DAYS" -> 15L
                    "1_MONTH" -> 30L
                    "3_MONTHS" -> 90L
                    "1_YEAR" -> 365L
                    else -> 365L
                }

                RazorpayPaymentGatewayDialog(
                    planTitle = selectedPlanObj.title,
                    planPrice = selectedPlanObj.priceInr,
                    daysText = selectedPlanObj.daysText,
                    onDismiss = { showPaymentGatewayModal = false },
                    onPaymentSuccess = { paymentId, paymentMethod ->
                        showPaymentGatewayModal = false
                        onVerifiedPayment(paymentId, selectedPlanId, selectedPlanObj.priceInr, daysCount, paymentMethod)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RazorpayPaymentGatewayDialog(
    planTitle: String,
    planPrice: String,
    daysText: String,
    onDismiss: () -> Unit,
    onPaymentSuccess: (paymentId: String, paymentMethod: String) -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var selectedPaymentMode by remember { mutableStateOf("UPI_GPAY") } // UPI_GPAY, UPI_PHONEPE, UPI_PAYTM, CARD
    val cyanAccent = Color(0xFF00D2FF)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1326)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Razorpay Gateway branding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0C2340)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "R",
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Razorpay Gateway",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Secured 256-Bit SSL",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = com.example.BuildConfig.RAZORPAY_KEY_ID.ifBlank { "rzp_test_TGXVf7cQ3EJMt4" }.take(10) + "...",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Order summary card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF040814)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Duty Accepter $planTitle",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Duration: $daysText",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = planPrice,
                            color = cyanAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Select Payment Method (UPI / Card)",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Options List
                val paymentOptions = listOf(
                    Triple("UPI_GPAY", "Google Pay UPI", "Fastest Instant UPI Settlement"),
                    Triple("UPI_PHONEPE", "PhonePe UPI", "Direct Bank Transfer"),
                    Triple("UPI_PAYTM", "Paytm UPI / Wallet", "Instant Balance"),
                    Triple("CARD", "Credit / Debit Card / NetBanking", "Visa, MasterCard, RuPay")
                )

                paymentOptions.forEach { (id, title, desc) ->
                    val isOptSelected = selectedPaymentMode == id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isOptSelected) Color(0xFF051D33) else Color(0xFF040814))
                            .border(
                                width = if (isOptSelected) 1.5.dp else 1.dp,
                                color = if (isOptSelected) cyanAccent else Color(0xFF1E293B),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedPaymentMode = id }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isOptSelected) cyanAccent else Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isOptSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = desc,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Pay Button
                Button(
                    onClick = {
                        isProcessing = true
                        val paymentId = "pay_rzp_" + System.currentTimeMillis().toString().takeLast(8) + (1000..9999).random()
                        onPaymentSuccess(paymentId, selectedPaymentMode)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cyanAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isProcessing) "Processing Payment..." else "PAY $planPrice VIA RAZORPAY",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel Transaction", color = Color.Gray, fontSize = 13.sp)
                }
            }
        }
    }
}

