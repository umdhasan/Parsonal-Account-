package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CategoryBreakdownCardContent
import com.example.ui.components.WeeklyCashflowBarChart
import com.example.ui.i18n.LedgerFormatters
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.theme.MoneyHeroStyle
import com.example.ui.theme.MoneyLargeStyle
import com.example.ui.viewmodel.LedgerUiState
import com.example.ui.viewmodel.PeriodFilter

@Composable
fun ReportsScreen(
    uiState: LedgerUiState,
    onPeriodChange: (PeriodFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val ledgerColors = LocalLedgerColors.current
    val currency = uiState.preferences.currencySymbol

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "reports_header") {
            Text(
                text = strings.reportsTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        item(key = "reports_period") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    PeriodFilter.WEEK to strings.filterWeek,
                    PeriodFilter.MONTH to strings.filterMonth,
                    PeriodFilter.ALL to strings.filterAll,
                ).forEach { (period, label) ->
                    FilterChip(
                        selected = uiState.periodFilter == period,
                        onClick = { onPeriodChange(period) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
        }

        // Savings Rate Card
        item(key = "savings_rate_card") {
            val rate = uiState.savingsRatePercent
            val positive = rate >= 0
            val accent = if (positive) ledgerColors.income else ledgerColors.expense

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ledgerColors.cardBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = strings.savingsRate,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (positive) strings.savingsHintPositive else strings.savingsHintNegative,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "$rate%",
                            style = MoneyHeroStyle,
                            color = accent,
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (rate.coerceIn(0, 100) / 100f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = accent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = strings.totalIncome,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = LedgerFormatters.formatMoney(uiState.periodIncome, currency),
                                style = MoneyLargeStyle,
                                color = ledgerColors.income,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = strings.totalExpense,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = LedgerFormatters.formatMoney(uiState.periodExpense, currency),
                                style = MoneyLargeStyle,
                                color = ledgerColors.expense,
                            )
                        }
                    }
                }
            }
        }

        // 7-Day Cashflow Bar Chart
        item(key = "weekly_cashflow_chart") {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ledgerColors.cardBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = strings.cashflowLast7Days,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    WeeklyCashflowBarChart(points = uiState.weeklyCashflow)
                }
            }
        }

        // Category Breakdown Donut Chart
        item(key = "category_donut_chart") {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ledgerColors.cardBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = strings.expenseByCategory,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    CategoryBreakdownCardContent(
                        items = uiState.expenseByCategory,
                        currency = currency,
                        language = uiState.language,
                    )
                }
            }
        }

        // Loan Portfolio Overview
        item(key = "loan_portfolio_summary") {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ledgerColors.cardBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.loanSummaryTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${uiState.activeLoansCount} ${strings.activeLoansCount} • ${uiState.settledLoansCount} ${strings.settledLoansCount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = strings.youWillGet,
                                style = MaterialTheme.typography.labelSmall,
                                color = ledgerColors.loanGiven,
                            )
                            Text(
                                text = LedgerFormatters.formatMoney(uiState.totalReceivable, currency),
                                style = MoneyLargeStyle,
                                fontWeight = FontWeight.Bold,
                                color = ledgerColors.loanGiven,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = strings.youWillPay,
                                style = MaterialTheme.typography.labelSmall,
                                color = ledgerColors.loanTaken,
                            )
                            Text(
                                text = LedgerFormatters.formatMoney(uiState.totalPayable, currency),
                                style = MoneyLargeStyle,
                                fontWeight = FontWeight.Bold,
                                color = ledgerColors.loanTaken,
                            )
                        }
                    }
                }
            }
        }
    }
}
