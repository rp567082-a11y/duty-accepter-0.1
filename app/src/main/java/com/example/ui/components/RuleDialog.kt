package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.DutyRuleEntity

@Composable
fun RuleDialog(
    initialRule: DutyRuleEntity? = null,
    onDismiss: () -> Unit,
    onSave: (DutyRuleEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialRule?.title ?: "") }
    var keyword by remember { mutableStateOf(initialRule?.keyword ?: "") }
    var targetPackage by remember { mutableStateOf(initialRule?.targetPackage ?: "*") }
    var autoClickText by remember { mutableStateOf(initialRule?.autoClickText ?: "ACCEPT DUTY") }
    var delayMs by remember { mutableStateOf(initialRule?.delayMs?.toFloat() ?: 100f) }
    var priority by remember { mutableStateOf(initialRule?.priority?.toFloat() ?: 2f) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (initialRule == null) "Create Duty Auto-Accept Rule" else "Edit Duty Rule",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rule Name
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Rule Title") },
                    placeholder = { Text("e.g., Express Delivery Rule") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Keyword
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("Target Window Keyword") },
                    placeholder = { Text("e.g., Urgent Duty, New Offer") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_keyword_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Button Text
                OutlinedTextField(
                    value = autoClickText,
                    onValueChange = { autoClickText = it },
                    label = { Text("Button Text to ACTION_CLICK") },
                    placeholder = { Text("e.g., ACCEPT DUTY, CONFIRM") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_autoclick_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Package Name
                OutlinedTextField(
                    value = targetPackage,
                    onValueChange = { targetPackage = it },
                    label = { Text("Target App Package Name (* for All Apps)") },
                    placeholder = { Text("com.delivery.duty or *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_package_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Delay Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-Click Delay:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text("${delayMs.toInt()} ms", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = delayMs,
                    onValueChange = { delayMs = it },
                    valueRange = 0f..1000f,
                    steps = 19,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF0284C7)),
                    modifier = Modifier.testTag("rule_delay_slider")
                )

                // Priority Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rule Priority:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text("P${priority.toInt()}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = priority,
                    onValueChange = { priority = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFBBF24), activeTrackColor = Color(0xFFD97706)),
                    modifier = Modifier.testTag("rule_priority_slider")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && keyword.isNotBlank() && autoClickText.isNotBlank()) {
                                onSave(
                                    DutyRuleEntity(
                                        id = initialRule?.id ?: 0,
                                        title = title.trim(),
                                        keyword = keyword.trim(),
                                        targetPackage = targetPackage.trim().ifEmpty { "*" },
                                        autoClickText = autoClickText.trim(),
                                        delayMs = delayMs.toLong(),
                                        isEnabled = initialRule?.isEnabled ?: true,
                                        priority = priority.toInt()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_rule_button")
                    ) {
                        Text("Save Rule", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
