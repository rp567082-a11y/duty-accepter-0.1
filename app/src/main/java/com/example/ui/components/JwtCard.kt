package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBgDark
import com.example.ui.theme.DarkBorder
import com.example.util.JwtParsed

@Composable
fun JwtCardInspector(
    jwt: JwtParsed,
    modifier: Modifier = Modifier
) {
    if (!jwt.isValid) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Invalid JWT Token",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Ensure the token follows the standard three-part format: Header.Payload.Signature",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CodeBgDark)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Status Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF818CF8).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "JWT PARSER",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA5B4FC),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = jwt.algorithm,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF22D3EE),
                    fontFamily = FontFamily.Monospace
                )
            }

            if (jwt.expiresAt.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (jwt.isExpired) Color(0xFF991B1B) else Color(0xFF065F46)
                ) {
                    Text(
                        text = if (jwt.isExpired) "EXPIRED" else "VALID",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expiration detail bar
        if (jwt.expiresAt.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Expires At", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(jwt.expiresAt, style = MaterialTheme.typography.bodySmall, color = Color.White, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Status", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = jwt.remainingTimeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (jwt.isExpired) Color(0xFFF87171) else Color(0xFF34D399),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Section 1: HEADER
        JwtSectionHeader(title = "1. HEADER", subtitle = "Algorithm & Token Type", color = Color(0xFFF43F5E))
        SelectionContainer {
            Text(
                text = jwt.headerJson,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFFB7185),
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section 2: PAYLOAD
        JwtSectionHeader(title = "2. PAYLOAD CLAIMS", subtitle = "Data & Subject", color = Color(0xFFA855F7))
        SelectionContainer {
            Text(
                text = jwt.payloadJson,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFC084FC),
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section 3: SIGNATURE
        JwtSectionHeader(title = "3. SIGNATURE", subtitle = "HMAC / RSA Verification", color = Color(0xFF0EA5E9))
        SelectionContainer {
            Text(
                text = jwt.signature,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            )
        }
    }
}

@Composable
private fun JwtSectionHeader(title: String, subtitle: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}
