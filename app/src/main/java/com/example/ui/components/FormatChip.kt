package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.IndigoPrimary
import com.example.util.FormatType

@Composable
fun FormatSelectorRow(
    selectedFormat: FormatType,
    onFormatSelected: (FormatType) -> Unit,
    availableFormats: List<FormatType> = FormatType.values().toList(),
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(availableFormats) { format ->
            val isSelected = format == selectedFormat
            val isAsItIs = format == FormatType.AS_IT_IS

            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        when {
                            isAsItIs && isSelected -> Color(0xFF10B981)
                            isAsItIs -> Color(0xFF064E3B)
                            isSelected -> IndigoPrimary
                            else -> Color(0xFF1F2937)
                        }
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = when {
                            isAsItIs -> Color(0xFF34D399)
                            isSelected -> Color(0xFF818CF8)
                            else -> DarkBorder
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onFormatSelected(format) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("format_chip_${format.name}")
            ) {
                Text(
                    text = if (isAsItIs) "✨ ${format.displayName}" else format.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        isAsItIs -> Color.White
                        isSelected -> Color.White
                        else -> Color(0xFFD1D5DB)
                    },
                    fontWeight = if (isSelected || isAsItIs) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}
