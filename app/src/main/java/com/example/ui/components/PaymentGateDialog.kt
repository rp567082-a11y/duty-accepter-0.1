package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun PaymentGateDialog(
    onDismiss: () -> Unit,
    onApplyCoupon: (String) -> Unit,
    onSelectPlan: (String) -> Unit
) {
    var couponInput by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("1_YEAR") }

    val cyanAccent = Color(0xFF00D2FF)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1326)),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cyanAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Subscription Gate",
                        tint = cyanAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Duty Accepter Subscription Gate",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Unlock high-speed auto-clicking, unlimited rules, and instant accessibility execution in INR.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Plan Tiers in INR
                PlanOptionCard(
                    title = "1 Year Pass 👑",
                    price = "₹1999",
                    desc = "👑 BEST VALUE (365 days full access)",
                    isRecommended = true,
                    isSelected = selectedPlan == "1_YEAR",
                    onClick = { selectedPlan = "1_YEAR" }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PlanOptionCard(
                    title = "3 Months Pass",
                    price = "₹699",
                    desc = "90 days active auto-click service",
                    isRecommended = false,
                    isSelected = selectedPlan == "3_MONTHS",
                    onClick = { selectedPlan = "3_MONTHS" }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PlanOptionCard(
                    title = "1 Month Pass ⭐",
                    price = "₹299",
                    desc = "⭐ MOST POPULAR (30 days active service)",
                    isRecommended = false,
                    isSelected = selectedPlan == "1_MONTH",
                    onClick = { selectedPlan = "1_MONTH" }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PlanOptionCard(
                    title = "15 Days Pass",
                    price = "₹179",
                    desc = "15 days active auto-click service",
                    isRecommended = false,
                    isSelected = selectedPlan == "15_DAYS",
                    onClick = { selectedPlan = "15_DAYS" }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PlanOptionCard(
                    title = "1 Week Pass",
                    price = "₹99",
                    desc = "7 days active auto-click service",
                    isRecommended = false,
                    isSelected = selectedPlan == "1_WEEK",
                    onClick = { selectedPlan = "1_WEEK" }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PlanOptionCard(
                    title = "1 Day Pass",
                    price = "₹20",
                    desc = "24 hours active auto-click service",
                    isRecommended = false,
                    isSelected = selectedPlan == "1_DAY",
                    onClick = { selectedPlan = "1_DAY" }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Coupon / Promo Code Input
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = "Coupon",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Redeem Promo / Coupon Code", style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = couponInput,
                        onValueChange = { couponInput = it.uppercase() },
                        placeholder = { Text("e.g. VIPFREE, DUTY2026", fontSize = 12.sp, color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cyanAccent,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("coupon_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (couponInput.isNotBlank()) {
                                onApplyCoupon(couponInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A374E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("apply_coupon_button")
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onSelectPlan(selectedPlan) },
                    colors = ButtonDefaults.buttonColors(containerColor = cyanAccent, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("subscribe_now_button")
                ) {
                    Text("PROCEED TO PAYMENT", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Close Gate Dialog", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun PlanOptionCard(
    title: String,
    price: String,
    desc: String,
    isRecommended: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cyanAccent = Color(0xFF00D2FF)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF051D33) else Color(0xFF141E33))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) cyanAccent else Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (isRecommended) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF9500))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("BEST VALUE", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) cyanAccent else Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
