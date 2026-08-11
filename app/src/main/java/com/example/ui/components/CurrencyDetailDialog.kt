package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.CurrencyConverter
import java.util.Locale
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDetailDialog(
    initialBaseCurrency: String,
    initialTargetCurrency: String = "PYG",
    initialTimeframe: String = "7D",
    onDismiss: () -> Unit,
    onTimeframeChange: (String) -> Unit = {}
) {
    var baseCurrency by remember { mutableStateOf(initialBaseCurrency) }
    var targetCurrency by remember { mutableStateOf(initialTargetCurrency) }
    var selectedTimeframe by remember { mutableStateOf(initialTimeframe) }

    var baseExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    val timeframes = listOf("1D", "3D", "5D", "7D", "1M", "1Y", "5Y", "ALL")

    // Current exchange rate
    val currentRate = CurrencyConverter.convert(1.0, baseCurrency, targetCurrency)

    // Generate historical points for the chart based on timeframe and current rate
    val (points, timeframeLabel) = remember(baseCurrency, targetCurrency, selectedTimeframe, currentRate) {
        CurrencyConverter.generateHistoricalRates(baseCurrency, targetCurrency, currentRate, selectedTimeframe)
    }

    val firstRate = points.firstOrNull() ?: currentRate
    val lastRate = points.lastOrNull() ?: currentRate
    val diff = lastRate - firstRate
    val percentChange = if (firstRate > 0) (diff / firstRate) * 100.0 else 0.0

    val isUp = diff >= 0
    val trendColor = if (isUp) Color(0xFF059669) else Color(0xFFDC2626)
    val trendBg = if (isUp) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)

    val minRate = points.minOrNull() ?: currentRate
    val maxRate = points.maxOrNull() ?: currentRate
    val avgRate = if (points.isNotEmpty()) points.average() else currentRate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = baseCurrency.take(1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$baseCurrency / $targetCurrency",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Histórico e Variação de Câmbio",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Parity Switchers Header
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Base Dropdown
                        ExposedDropdownMenuBox(
                            expanded = baseExpanded,
                            onExpandedChange = { baseExpanded = !baseExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = baseCurrency,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { Text("Base", fontSize = 10.sp) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = baseExpanded,
                                onDismissRequest = { baseExpanded = false }
                            ) {
                                CurrencyConverter.supportedCurrencies.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr, maxLines = 1) },
                                        onClick = {
                                            baseCurrency = curr
                                            baseExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                val temp = baseCurrency
                                baseCurrency = targetCurrency
                                targetCurrency = temp
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SyncAlt,
                                contentDescription = "Inverter Paridade",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Target Dropdown
                        ExposedDropdownMenuBox(
                            expanded = targetExpanded,
                            onExpandedChange = { targetExpanded = !targetExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = targetCurrency,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { Text("Paridade", fontSize = 10.sp) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = targetExpanded,
                                onDismissRequest = { targetExpanded = false }
                            ) {
                                CurrencyConverter.supportedCurrencies.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr, maxLines = 1) },
                                        onClick = {
                                            targetCurrency = curr
                                            targetExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Rate Banner
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = trendBg.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "1 $baseCurrency =",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyConverter.format(currentRate, targetCurrency),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = trendColor,
                                modifier = Modifier.size(16.dp)
                            )
                            val sign = if (percentChange >= 0) "+" else ""
                            Text(
                                text = "$sign${String.format(Locale.US, "%.2f", percentChange)}% ($timeframeLabel)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = trendColor
                            )
                        }

                        // Explanation message
                        val explanation = if (targetCurrency.uppercase() == "PYG") {
                            if (isUp) "O Guaraní desvalorizou em relação ao $baseCurrency no período."
                            else "O Guaraní valorizou em relação ao $baseCurrency no período."
                        } else {
                            if (isUp) "$baseCurrency valorizou em relação ao $targetCurrency."
                            else "$baseCurrency desvalorizou em relação ao $targetCurrency."
                        }
                        Text(
                            text = explanation,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Timeframe Chips Selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(timeframes) { tf ->
                        val selected = selectedTimeframe == tf
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedTimeframe = tf
                                onTimeframeChange(tf)
                            },
                            label = { Text(tf, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                // Line Chart Canvas
                InteractiveCurrencyChart(
                    points = points,
                    lineColor = trendColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                // Stats Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        title = "Mínima",
                        value = CurrencyConverter.format(minRate, targetCurrency),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Média",
                        value = CurrencyConverter.format(avgRate, targetCurrency),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Máxima",
                        value = CurrencyConverter.format(maxRate, targetCurrency),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("close_currency_detail_btn")
            ) {
                Text("Fechar", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InteractiveCurrencyChart(
    points: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animateProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "chart_anim"
    )

    LaunchedEffect(points) {
        animationPlayed = false
        animationPlayed = true
    }

    val min = points.minOrNull() ?: 1.0
    val max = points.maxOrNull() ?: 2.0
    val range = (max - min).coerceAtLeast(0.0001)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                if (points.size < 2) return@Canvas

                val stepX = width / (points.size - 1)
                val path = Path()
                val fillPath = Path()

                points.forEachIndexed { index, rate ->
                    val x = index * stepX
                    val normalizedY = ((rate - min) / range).toFloat()
                    val y = height - (normalizedY * height * animateProgress)

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }

                fillPath.lineTo(width, height)
                fillPath.close()

                // Draw background gradient under line
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent)
                    )
                )

                // Draw line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw last point dot
                val lastX = (points.size - 1) * stepX
                val lastVal = points.last()
                val lastNormalizedY = ((lastVal - min) / range).toFloat()
                val lastY = height - (lastNormalizedY * height * animateProgress)

                drawCircle(
                    color = lineColor,
                    radius = 5.dp.toPx(),
                    center = Offset(lastX, lastY)
                )
            }
        }
    }
}


