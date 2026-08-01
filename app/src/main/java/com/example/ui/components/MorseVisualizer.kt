package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBgDark
import com.example.ui.theme.DarkBorder

@Composable
fun MorseVisualizer(
    morseText: String,
    decodedText: String,
    isPlaying: Boolean,
    currentProgress: Int,
    onPlayAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CodeBgDark)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "MORSE SIGNAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val lightColor by animateColorAsState(
                targetValue = if (isPlaying) Color(0xFFFACC15) else Color(0xFF374151),
                label = "MorseSignalLight"
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(lightColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPlaying) "SIGNALING" else "READY",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPlaying) Color(0xFFFACC15) else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Morse output text area
        Text(
            text = if (morseText.isEmpty()) "Morse signal dots (.) and dashes (-) will display here" else morseText,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                color = if (morseText.isEmpty()) Color.DarkGray else Color(0xFFFDE047),
                fontSize = 18.sp,
                letterSpacing = 2.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
                .padding(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Decoded Plain text counterpart
        Text(
            text = "Decoded Plain Text: $decodedText",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4ADE80),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onPlayAudio,
            enabled = morseText.isNotEmpty() && !isPlaying,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD97706),
                disabledContainerColor = Color(0xFF374151)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("play_morse_audio_button")
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Play Morse Code Audio"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isPlaying) "Playing Morse Tone..." else "Play Morse Audio Signal",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
