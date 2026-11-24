package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.components.LedgerCategoryVisuals
import com.example.ui.i18n.CategoryCatalog
import com.example.ui.i18n.LedgerFormatters
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.GoldLightAccent
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.theme.MoneyHeroStyle
import com.example.ui.theme.MoneyLargeStyle
import com.example.ui.theme.MoneyMediumStyle
import com.example.ui.theme.MoneySmallStyle
import com.example.ui.viewmodel.LedgerUiState
import com.example.ui.viewmodel.PeriodFilter
import com.example.ui.viewmodel.TxTypeFilter

@Composable
fun HomeScreen(
    uiState: LedgerUiState,
    onToggleLanguage: () -> Unit,
    onQuickAddIncome: () -> Unit,
    onQuickAddExpense: () -> Unit,
    onQuickAddLoan: () -> Unit,
    onOpenDriveSync: () -> Unit,
    onQuickSyncNow: () -> Unit,
    onPeriodChange: (PeriodFilter) -> Unit,
    onTypeFilterChange: (TxTypeFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val ledgerColors = LocalLedgerColors.current
    val currency = uiState.preferences.currencySymbol
    val hasUrl = uiState.preferences.scriptUrl.isNotBlank()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Editorial Top Bar
        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.appTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = strings.appSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Drive Sync Status Pill
                    Surface(
                        onClick = { if (hasUrl) onQuickSyncNow() else onOpenDriveSync() },
                        shape = RoundedCornerShape(50),
                        color = when {
                            !hasUrl -> MaterialTheme.colorScheme.surfaceContainerHigh
                            uiState.preferences.hasPendingChanges -> ledgerColors.loanTakenContainer
                            else -> ledgerColors.incomeContainer
                        },
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("header_sync_pill"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (uiState.syncStatus.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(15.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            } else {
                                Icon(
                                    imageVector = when {
                                        !hasUrl -> Icons.Rounded.CloudOff
                                        uiState.preferences.hasPendingChanges -> Icons.Rounded.CloudUpload
                                        else -> Icons.Rounded.CloudDone
                                    },
                                    contentDescription = stringResource(R.string.cd_sync_now),
                                    tint = when {
                                        !hasUrl -> MaterialTheme.colorScheme.onSurfaceVariant
                                        uiState.preferences.hasPendingChanges -> ledgerColors.loanTaken
                                        else -> ledgerColors.income
                                    },
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                            Text(
                                text = when {
                                    !hasUrl -> strings.driveSync
                                    uiState.preferences.hasPendingChanges -> strings.driveSync
                                    else -> strings.syncedBadge
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = when {
                                    !hasUrl -> MaterialTheme.colorScheme.onSurfaceVariant
                                    uiState.preferences.hasPendingChanges -> ledgerColors.loanTaken
                                    else -> ledgerColors.income
                                },
                            )
                        }
                    }

                    // Instant EN / বাং toggle
                    Surface(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("language_toggle_button"),
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = uiState.language.badge,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }

        // Signature Editorial Hero Card
        item(key = "hero_balance") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ledgerColors.heroStart,
                                ledgerColors.heroMid,
                                ledgerColors.heroEnd,
                            ),
                        ),
                    ),
            ) {
                // Subtle geometric ledger rings drawn behind
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = size.height * 0.72f,
                        center = Offset(size.width * 0.92f, size.height * 0.14f),
                    )
                    drawCircle(
                        color = GoldLightAccent.copy(alpha = 0.14f),
                        radius = size.height * 0.55f,
                        center = Offset(size.width * 0.92f, size.height * 0.14f),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.04f),
                        radius = size.height * 0.45f,
                        center = Offset(size.width * 0.08f, size.height * 1.05f),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = strings.netBalance,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.78f),
                            )
                            Text(
                                text = LedgerFormatters.formatMoney(uiState.allTimeBalance, currency),
                                style = MoneyHeroStyle,
                                color = Color.White,
                                modifier = Modifier.testTag("hero_net_balance_text"),
                            )
                        }

                        // Net Loan badge
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.13f),
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.End,
                            ) {
                                Text(
                                    text = strings.netLoanPosition,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldLightAccent,
                                )
                                Text(
                                    text = LedgerFormatters.formatMoney(
                                        uiState.netLoanPosition,
                                        currency,
                                        signed = true,
                                    ),
                                    style = MoneySmallStyle,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }

                    // Income vs Expense glass split
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        HeroMetricGlassTile(
                            label = strings.totalIncome,
                            amount = LedgerFormatters.formatMoney(uiState.periodIncome, currency),
                            icon = Icons.Rounded.ArrowDownward,
                            accentColor = Color(0xFF6EF0BD),
                            modifier = Modifier.weight(1f),
                        )
                        HeroMetricGlassTile(
                            label = strings.totalExpense,
                            amount = LedgerFormatters.formatMoney(uiState.periodExpense, currency),
                            icon = Icons.Rounded.ArrowUpward,
                            accentColor = Color(0xFFFF9B94),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // Quick Actions Row
        item(key = "quick_actions") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickActionTile(
                    label = strings.addIncome,
                    icon = Icons.Rounded.Add,
                    containerColor = ledgerColors.incomeContainer,
                    contentColor = ledgerColors.income,
                    onClick = onQuickAddIncome,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_income_button"),
                )
                QuickActionTile(
                    label = strings.addExpense,
                    icon = Icons.Rounded.ArrowUpward,
                    containerColor = ledgerColors.expenseContainer,
                    contentColor = ledgerColors.expense,
                    onClick = onQuickAddExpense,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_expense_button"),
                )
                QuickActionTile(
                    label = strings.addLoan,
                    icon = Icons.Rounded.Handshake,
                    containerColor = ledgerColors.loanGivenContainer,
                    contentColor = ledgerColors.loanGiven,
                    onClick = onQuickAddLoan,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_loan_button"),
                )
                QuickActionTile(
                    label = strings.driveSync,
                    icon = Icons.Rounded.CloudSync,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onOpenDriveSync,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_drive_sync_button"),
                )
            }
        }

        // Period Filter Chips
        item(key = "period_filters") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val periodOptions = listOf(
                    PeriodFilter.TODAY to strings.filterToday,
                    PeriodFilter.WEEK to strings.filterWeek,
                    PeriodFilter.MONTH to strings.filterMonth,
                    PeriodFilter.ALL to strings.filterAll,
                )
                periodOptions.forEach { (period, label) ->
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

        // Search + Type filter
        item(key = "search_and_type") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(strings.searchPlaceholder) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.cd_close),
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_search_input"),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${strings.dailyTransactions} (${uiState.filteredTransactions.size} ${strings.entriesCount})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            TxTypeFilter.ALL to strings.typeAll,
                            TxTypeFilter.INCOME to strings.typeIncome,
                            TxTypeFilter.EXPENSE to strings.typeExpense,
                        ).forEach { (typeFilter, label) ->
                            val selected = uiState.txTypeFilter == typeFilter
                            Surface(
                                onClick = { onTypeFilterChange(typeFilter) },
                                shape = RoundedCornerShape(50),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                ),
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Empty State or Date-Grouped Ledger List
        if (uiState.groupedTransactions.isEmpty()) {
            item(key = "empty_transactions") {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, ledgerColors.cardBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp),
                            )
                        }
                        Text(
                            text = strings.noTransactionsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = strings.noTransactionsBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            uiState.groupedTransactions.forEach { (dateKey, dayTransactions) ->
                val dayNet = dayTransactions.sumOf {
                    if (it.transactionType == TransactionType.INCOME) it.amount else -it.amount
                }

                item(key = "date_header_$dateKey") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = LedgerFormatters.friendlyDateHeader(dateKey, strings),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = LedgerFormatters.formatMoney(dayNet, currency, signed = true),
                            style = MoneySmallStyle,
                            fontWeight = FontWeight.SemiBold,
                            color = if (dayNet >= 0) ledgerColors.income else ledgerColors.expense,
                        )
                    }
                }

                items(
                    items = dayTransactions,
                    key = { it.id },
                ) { tx ->
                    TransactionRowCard(
                        transaction = tx,
                        currency = currency,
                        language = uiState.language,
                        onClick = { onTransactionClick(tx) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricGlassTile(
    label: String,
    amount: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.11f),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.76f),
                    maxLines = 1,
                )
                Text(
                    text = amount,
                    style = MoneyLargeStyle,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        modifier = modifier.height(74.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TransactionRowCard(
    transaction: TransactionEntity,
    currency: String,
    language: com.example.ui.i18n.AppLanguage,
    onClick: () -> Unit,
) {
    val ledgerColors = LocalLedgerColors.current
    val isIncome = transaction.transactionType == TransactionType.INCOME
    val tint = if (isIncome) ledgerColors.income else ledgerColors.expense
    val containerTint = if (isIncome) ledgerColors.incomeContainer else ledgerColors.expenseContainer

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ledgerColors.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("transaction_card_${transaction.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(containerTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LedgerCategoryVisuals.iconForCategory(transaction.category),
                    contentDescription = transaction.category,
                    tint = tint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = CategoryCatalog.localizeCategory(transaction.category, language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = buildString {
                    if (transaction.note.isNotBlank()) {
                        append(transaction.note)
                        append(" • ")
                    }
                    append(CategoryCatalog.localizePaymentMethod(transaction.paymentMethod, language))
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = LedgerFormatters.formatMoney(
                    amount = if (isIncome) transaction.amount else -transaction.amount,
                    currency = currency,
                    signed = true,
                ),
                style = MoneyMediumStyle,
                color = tint,
            )
        }
    }
}
