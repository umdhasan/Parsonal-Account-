package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.CategoryCatalog
import com.example.ui.i18n.LedgerFormatters
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.theme.MoneyMediumStyle
import com.example.ui.theme.MoneySmallStyle
import com.example.ui.viewmodel.CategoryBreakdownItem
import com.example.ui.viewmodel.DailyCashflowPoint

@Composable
fun WeeklyCashflowBarChart(
    points: List<DailyCashflowPoint>,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val ledgerColors = LocalLedgerColors.current
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    val hasAnyValue = points.any { it.income > 0.0 || it.expense > 0.0 }
    if (!hasAnyValue) {
        EmptyChartNotice(text = strings.noChartData, modifier = modifier)
        return
    }

    val animProgress = remember(points) { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        )
    }

    val maxValue = points.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1.0) ?: 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChartLegendDot(color = ledgerColors.income, label = strings.typeIncome)
            ChartLegendDot(color = ledgerColors.expense, label = strings.typeExpense)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp),
        ) {
            val chartHeight = size.height
            val slotWidth = size.width / points.size.coerceAtLeast(1)
            val barWidth = (slotWidth * 0.27f).coerceIn(8.dp.toPx(), 18.dp.toPx())
            val barGap = 4.dp.toPx()

            // Horizontal dashed-like subtle reference lines
            for (step in 0..3) {
                val y = chartHeight * (step / 3f)
                drawLine(
                    color = gridColor.copy(alpha = 0.55f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val progress = animProgress.value
            points.forEachIndexed { idx, point ->
                val centerX = slotWidth * idx + slotWidth / 2f
                val incomeHeight = ((point.income / maxValue).toFloat() * (chartHeight - 8.dp.toPx()) * progress)
                val expenseHeight = ((point.expense / maxValue).toFloat() * (chartHeight - 8.dp.toPx()) * progress)

                val incomeLeft = centerX - barWidth - barGap / 2f
                val expenseLeft = centerX + barGap / 2f
                val corner = CornerRadius(6.dp.toPx(), 6.dp.toPx())

                if (incomeHeight > 1f) {
                    drawRoundRect(
                        color = ledgerColors.income,
                        topLeft = Offset(incomeLeft, chartHeight - incomeHeight),
                        size = Size(barWidth, incomeHeight),
                        cornerRadius = corner,
                    )
                } else {
                    drawRoundRect(
                        color = gridColor.copy(alpha = 0.45f),
                        topLeft = Offset(incomeLeft, chartHeight - 3.dp.toPx()),
                        size = Size(barWidth, 3.dp.toPx()),
                        cornerRadius = corner,
                    )
                }

                if (expenseHeight > 1f) {
                    drawRoundRect(
                        color = ledgerColors.expense,
                        topLeft = Offset(expenseLeft, chartHeight - expenseHeight),
                        size = Size(barWidth, expenseHeight),
                        cornerRadius = corner,
                    )
                } else {
                    drawRoundRect(
                        color = gridColor.copy(alpha = 0.45f),
                        topLeft = Offset(expenseLeft, chartHeight - 3.dp.toPx()),
                        size = Size(barWidth, 3.dp.toPx()),
                        cornerRadius = corner,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            points.forEach { pt ->
                Text(
                    text = pt.shortLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun CategoryBreakdownCardContent(
    items: List<CategoryBreakdownItem>,
    currency: String,
    language: AppLanguage,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    if (items.isEmpty()) {
        EmptyChartNotice(text = strings.noChartData, modifier = modifier)
        return
    }

    val total = items.sumOf { it.amount }
    val animProgress = remember(items) { Animatable(0f) }
    LaunchedEffect(items) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        )
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val palette = LedgerCategoryVisuals.chartPalette

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val strokeWidth = 22.dp.toPx()
                val inset = strokeWidth / 2f
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(inset, inset)

                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth),
                )

                var startAngle = -90f
                val progress = animProgress.value
                items.forEachIndexed { idx, item ->
                    val sweep = (item.percentage * 360f * progress).coerceAtLeast(2f)
                    drawArc(
                        color = palette[idx % palette.size],
                        startAngle = startAngle,
                        sweepAngle = (sweep - 2.5f).coerceAtLeast(1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    )
                    startAngle += sweep
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = strings.totalExpense,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = LedgerFormatters.formatMoney(total, currency),
                    style = MoneyMediumStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.take(6).forEachIndexed { idx, item ->
                val sliceColor = palette[idx % palette.size]
                val pct = (item.percentage * 100f).toInt()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(sliceColor),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = CategoryCatalog.localizeCategory(item.category, language),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "$pct%",
                            style = MoneySmallStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = LedgerFormatters.formatMoney(item.amount, currency),
                            style = MoneySmallStyle,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = item.percentage.coerceIn(0.03f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(sliceColor),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyChartNotice(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
