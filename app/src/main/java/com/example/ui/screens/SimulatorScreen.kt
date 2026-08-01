package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DutyViewModel

data class SimulationPreset(
    val name: String,
    val packageName: String,
    val windowText: String,
    val buttonText: String
)

@Composable
fun SimulatorScreen(
    simulationLog: String,
    isSimulating: Boolean,
    viewModel: DutyViewModel,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        SimulationPreset(
            name = "🚚 Express Order",
            packageName = "com.delivery.duty",
            windowText = "New Express Order #804 Available! High priority dispatch.",
            buttonText = "ACCEPT DUTY"
        ),
        SimulationPreset(
            name = "🚑 Urgent Shift",
            packageName = "com.hospital.duty",
            windowText = "Urgent Duty Available for Night Shift Emergency Ward.",
            buttonText = "CONFIRM SHIFT"
        ),
        SimulationPreset(
            name = "🚕 Taxi Dispatch",
            packageName = "com.taxi.driver",
            windowText = "New Trip Offer: Airport Terminal 2 -> Downtown Center ($28.50)",
            buttonText = "ACCEPT RIDE"
        )
    )

    var packageNameInput by remember { mutableStateOf(presets[0].packageName) }
    var windowTextInput by remember { mutableStateOf(presets[0].windowText) }
    var buttonTextInput by remember { mutableStateOf(presets[0].buttonText) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ACCESSIBILITY NODE EVENT SIMULATOR",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Presets row
            LazyRow(contentPadding = PaddingValues(horizontal = 2.dp)) {
                items(presets) { preset ->
                    Surface(
                        onClick = {
                            packageNameInput = preset.packageName
                            windowTextInput = preset.windowText
                            buttonTextInput = preset.buttonText
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("simulator_preset_${preset.name}")
                    ) {
                        Text(
                            text = preset.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF93C5FD),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mock Target App Window Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = "Target App",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MOCK TARGET WINDOW NODE",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "EVENT SOURCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = packageNameInput,
                        onValueChange = { packageNameInput = it },
                        label = { Text("Simulated App Package") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sim_package_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = windowTextInput,
                        onValueChange = { windowTextInput = it },
                        label = { Text("Simulated Window Text Payload") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sim_window_text_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = buttonTextInput,
                        onValueChange = { buttonTextInput = it },
                        label = { Text("Simulated Target Button Text") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sim_button_text_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Fire Event Action Button
        item {
            Button(
                onClick = {
                    viewModel.runSimulationTest(
                        sampleText = windowTextInput,
                        samplePackage = packageNameInput,
                        sampleButtonText = buttonTextInput
                    )
                },
                enabled = !isSimulating,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("fire_simulation_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Fire Event")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSimulating) "Evaluating Rules..." else "Simulate Window Event & Trigger Auto-Click",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Simulation Console Output Card
        item {
            Text(
                text = "SIMULATION EXECUTION CONSOLE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (simulationLog.isEmpty()) "Tap button above to simulate an incoming duty offer event and test matching rules." else simulationLog,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = if (simulationLog.isEmpty()) Color.DarkGray else Color(0xFF34D399)
                        )
                    )
                }
            }
        }
    }
}
