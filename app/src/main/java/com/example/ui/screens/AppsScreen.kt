package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DutyRuleEntity
import com.example.ui.DutyViewModel

data class MonitoredApp(
    val name: String,
    val packageName: String,
    val subtitle: String? = null,
    val isEnabled: Boolean = true,
    val bgBgColor: Color,
    val contentColor: Color = Color.White,
    val letterText: String? = null,
    val iconVector: ImageVector? = null
)

@Composable
fun AppsScreen(
    rules: List<DutyRuleEntity>,
    viewModel: DutyViewModel,
    modifier: Modifier = Modifier
) {
    val darkBg = Color(0xFF080D1A)
    val cardBg = Color(0xFF0F1B33)
    val greenSuccess = Color(0xFF22C55E)

    var rideApps by remember {
        mutableStateOf(
            listOf(
                MonitoredApp("Ola", "com.olacabs.driver", subtitle = "Standard Mode", bgBgColor = Color(0xFFEAB308), contentColor = Color.Black, letterText = "O"),
                MonitoredApp("Uber", "com.ubercab.driver", subtitle = "Standard Mode", bgBgColor = Color(0xFF27272A), contentColor = Color.White, letterText = "U"),
                MonitoredApp("Rapido", "com.rapido.captain", subtitle = "Standard Mode", bgBgColor = Color(0xFFEF4444), iconVector = Icons.Default.FlashOn),
                MonitoredApp("Namma Yatri", "in.nammayatri.driver", bgBgColor = Color(0xFF16A34A), iconVector = Icons.Default.LocalTaxi),
                MonitoredApp("Zomato", "com.zomato.delivery", bgBgColor = Color(0xFFDC2626), iconVector = Icons.Default.Restaurant),
                MonitoredApp("Swiggy", "com.swiggy.delivery", bgBgColor = Color(0xFFF97316), iconVector = Icons.Default.ShoppingBag),
                MonitoredApp("Dunzo", "com.dunzo.partner", bgBgColor = Color(0xFF22C55E), iconVector = Icons.Default.DirectionsRun),
                MonitoredApp("Blinkit", "com.grofers.delivery", bgBgColor = Color(0xFFEAB308), contentColor = Color.Black, iconVector = Icons.Default.FlashOn),
                MonitoredApp("Zepto", "com.zepto.partner", bgBgColor = Color(0xFF9333EA), iconVector = Icons.Default.Schedule),
                MonitoredApp("BigBasket", "com.bigbasket.partner", bgBgColor = Color(0xFF15803D), iconVector = Icons.Default.ShoppingBasket),
                MonitoredApp("Porter", "com.porter.driver", bgBgColor = Color(0xFFEA580C), iconVector = Icons.Default.LocalShipping),
                MonitoredApp("Instamart", "com.swiggy.instamart", bgBgColor = Color(0xFFC2410C), iconVector = Icons.Default.ShoppingBag)
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP SUBTITLE
        item {
            Text(
                text = "Apps monitored by the service for ride detection",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // MONITORED APPS CARD CONTAINER
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    rideApps.forEachIndexed { index, app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    rideApps = rideApps.toMutableList().also {
                                        it[index] = app.copy(isEnabled = !app.isEnabled)
                                    }
                                    viewModel.showToast("${app.name} monitoring ${if (!app.isEnabled) "enabled" else "disabled"}")
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // App Icon Square
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(app.bgBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (app.letterText != null) {
                                        Text(
                                            text = app.letterText,
                                            color = app.contentColor,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else if (app.iconVector != null) {
                                        Icon(
                                            imageVector = app.iconVector,
                                            contentDescription = app.name,
                                            tint = app.contentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = app.name,
                                            tint = app.contentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = app.name,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (app.subtitle != null) {
                                        Text(
                                            text = app.subtitle,
                                            color = Color(0xFF64748B),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            // Active Green Check Circle
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = if (app.isEnabled) greenSuccess else Color(0xFF334155),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        if (index < rideApps.lastIndex) {
                            HorizontalDivider(
                                color = Color(0xFF1E293B).copy(alpha = 0.5f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
