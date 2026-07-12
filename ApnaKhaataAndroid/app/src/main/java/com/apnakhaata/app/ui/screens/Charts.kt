package com.apnakhaata.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apnakhaata.app.ui.theme.categoryColor
import java.text.NumberFormat
import java.util.Locale

fun formatInr(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
    nf.maximumFractionDigits = 0
    return "₹" + nf.format(amount)
}

@Composable
fun CategoryPieChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum()
    if (total <= 0.0) return
    val entries = data.entries.sortedByDescending { it.value }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            var startAngle = -90f
            entries.forEach { (cat, value) ->
                val sweep = (value / total * 360f).toFloat()
                drawArc(
                    color = categoryColor(cat),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true
                )
                startAngle += sweep
            }
            // Donut hole
            drawCircle(
                color = Color(0xFFFAF6EE),
                radius = size.minDimension / 4.2f
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            entries.take(6).forEach { (cat, value) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(categoryColor(cat), CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text(
                        text = formatInr(value),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
