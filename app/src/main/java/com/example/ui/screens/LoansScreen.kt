package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallMade
import androidx.compose.material.icons.rounded.CallReceived
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.LoanEntity
import com.example.data.local.LoanStatus
import com.example.data.local.LoanType
import com.example.ui.i18n.LedgerFormatters
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.theme.MoneyLargeStyle
import com.example.ui.theme.MoneyMediumStyle
import com.example.ui.theme.MoneySmallStyle
import com.example.ui.viewmodel.LedgerUiState
import com.example.ui.viewmodel.LoanFilter

@Composable
fun LoansScreen(
    uiState: LedgerUiState,
    onFilterChange: (LoanFilter) -> Unit,
    onLoanClick: (LoanEntity) -> Unit,
    onRecordPaymentClick: (LoanEntity) -> Unit,
    onMarkSettledClick: (LoanEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val ledgerColors = LocalLedgerColors.current
    val currency = uiState.preferences.currencySymbol
    val today = LedgerFormatters.todayIso()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "loans_header") {
            Text(
                text = strings.loansTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Split Receivable vs Payable Cards
        item(key = "loans_summary_split") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LoanSummaryCard(
                    title = strings.youWillGet,
                    amount = LedgerFormatters.formatMoney(uiState.totalReceivable, currency),
                    icon = Icons.Rounded.CallMade,
                    accent = ledgerColors.loanGiven,
                    container = ledgerColors.loanGivenContainer,
                    modifier = Modifier.weight(1f),
                )
                LoanSummaryCard(
                    title = strings.youWillPay,
                    amount = LedgerFormatters.formatMoney(uiState.totalPayable, currency),
                    icon = Icons.Rounded.CallReceived,
                    accent = ledgerColors.loanTaken,
                    container = ledgerColors.loanTakenContainer,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Loan Filter Chips
        item(key = "loan_filters") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    LoanFilter.ALL to strings.typeAll,
                    LoanFilter.GIVEN to strings.loanGiven,
                    LoanFilter.TAKEN to strings.loanTaken,
                    LoanFilter.SETTLED to strings.loanSettled,
                ).forEach { (filter, label) ->
                    FilterChip(
                        selected = uiState.loanFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
        }

        if (uiState.filteredLoans.isEmpty()) {
            item(key = "empty_loans") {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, ledgerColors.cardBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 38.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(ledgerColors.loanGivenContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Handshake,
                                contentDescription = null,
                                tint = ledgerColors.loanGiven,
                                modifier = Modifier.size(30.dp),
                            )
                        }
                        Text(
                            text = strings.noLoansTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = strings.noLoansBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            items(
                items = uiState.filteredLoans,
                key = { it.id },
            ) { loan ->
                LoanRecordCard(
                    loan = loan,
                    currency = currency,
                    todayIso = today,
                    onClick = { onLoanClick(loan) },
                    onRecordPayment = { onRecordPaymentClick(loan) },
                    onMarkSettled = { onMarkSettledClick(loan) },
                )
            }
        }
    }
}

@Composable
private fun LoanSummaryCard(
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    container: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = container,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = amount,
                style = MoneyLargeStyle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LoanRecordCard(
    loan: LoanEntity,
    currency: String,
    todayIso: String,
    onClick: () -> Unit,
    onRecordPayment: () -> Unit,
    onMarkSettled: () -> Unit,
) {
    val strings = LocalStrings.current
    val ledgerColors = LocalLedgerColors.current
    val isGiven = loan.loanType == LoanType.GIVEN
    val isSettled = loan.loanStatus == LoanStatus.SETTLED
    val isOverdue = !isSettled && loan.dueDate.isNotBlank() && loan.dueDate < todayIso
    val accent = when {
        isSettled -> ledgerColors.income
        isGiven -> ledgerColors.loanGiven
        else -> ledgerColors.loanTaken
    }
    val containerTint = when {
        isSettled -> ledgerColors.incomeContainer
        isGiven -> ledgerColors.loanGivenContainer
        else -> ledgerColors.loanTakenContainer
    }
    val initials = loan.personName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ledgerColors.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("loan_card_${loan.id}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(containerTint),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = loan.personName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = containerTint,
                        ) {
                            Text(
                                text = if (isSettled) strings.loanSettled
                                else if (isGiven) strings.loanGiven
                                else strings.loanTaken,
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                        if (isOverdue) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ledgerColors.expenseContainer,
                            ) {
                                Text(
                                    text = strings.overdueTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ledgerColors.expense,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = loan.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (loan.dueDate.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Event,
                                    contentDescription = null,
                                    tint = if (isOverdue) ledgerColors.expense else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = loan.dueDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isOverdue) ledgerColors.expense else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = LedgerFormatters.formatMoney(loan.remainingAmount, currency),
                        style = MoneyMediumStyle,
                        color = accent,
                    )
                    Text(
                        text = "/ ${LedgerFormatters.formatMoney(loan.amount, currency)}",
                        style = MoneySmallStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { loan.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${strings.paidLabel}: ${LedgerFormatters.formatMoney(loan.paidAmount, currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${(loan.progressFraction * 100).toInt()}%",
                        style = MoneySmallStyle,
                        color = accent,
                    )
                }
            }

            if (loan.note.isNotBlank() || loan.phone.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (loan.phone.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = loan.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (loan.note.isNotBlank()) {
                        Text(
                            text = loan.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (!isSettled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FilledTonalButton(
                        onClick = onRecordPayment,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = containerTint,
                            contentColor = accent,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("record_payment_button_${loan.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.recordPayment,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                        )
                    }

                    OutlinedButton(
                        onClick = onMarkSettled,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("settle_loan_button_${loan.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.markSettled,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
