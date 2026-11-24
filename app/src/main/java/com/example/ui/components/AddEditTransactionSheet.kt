package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.CategoryCatalog
import com.example.ui.i18n.LedgerFormatters
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalLedgerColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    existing: TransactionEntity?,
    initialType: TransactionType,
    currency: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: ((String) -> Unit)? = null,
) {
    val strings = LocalStrings.current
    val ledgerColors = LocalLedgerColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember(existing, initialType) {
        mutableStateOf(existing?.transactionType ?: initialType)
    }
    var amountText by remember(existing) {
        mutableStateOf(
            existing?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                ?: "",
        )
    }
    var category by remember(existing, type) {
        mutableStateOf(
            existing?.category ?: if (type == TransactionType.INCOME) "Salary" else "Food",
        )
    }
    var paymentMethod by remember(existing) {
        mutableStateOf(existing?.paymentMethod?.ifBlank { "Cash" } ?: "Cash")
    }
    var date by remember(existing) {
        mutableStateOf(existing?.date?.ifBlank { LedgerFormatters.todayIso() } ?: LedgerFormatters.todayIso())
    }
    var note by remember(existing) { mutableStateOf(existing?.note ?: "") }
    var showAmountError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = if (type == TransactionType.INCOME) {
        CategoryCatalog.incomeCategories
    } else {
        CategoryCatalog.expenseCategories
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (existing == null) strings.newTransactionTitle else strings.editTransactionTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (existing != null && onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete(existing.id)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("delete_transaction_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.cd_delete),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            // Segmented Income / Expense selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val isIncome = type == TransactionType.INCOME
                Surface(
                    onClick = {
                        type = TransactionType.INCOME
                        if (CategoryCatalog.incomeCategories.none { it.first == category }) {
                            category = "Salary"
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isIncome) ledgerColors.incomeContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = if (isIncome) 1.5.dp else 1.dp,
                        color = if (isIncome) ledgerColors.income else MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("tx_type_income_tab"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = if (isIncome) ledgerColors.income else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.typeIncome,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isIncome) ledgerColors.income else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    onClick = {
                        type = TransactionType.EXPENSE
                        if (CategoryCatalog.expenseCategories.none { it.first == category }) {
                            category = "Food"
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = if (!isIncome) ledgerColors.expenseContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = if (!isIncome) 1.5.dp else 1.dp,
                        color = if (!isIncome) ledgerColors.expense else MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("tx_type_expense_tab"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            tint = if (!isIncome) ledgerColors.expense else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.typeExpense,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (!isIncome) ledgerColors.expense else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Amount input
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    val sanitized = input.filter { it.isDigit() || it == '.' }
                    if (sanitized.count { it == '.' } <= 1) {
                        amountText = sanitized
                        showAmountError = false
                    }
                },
                label = { Text("${strings.amountLabel} ($currency)") },
                prefix = {
                    Text(
                        text = "$currency ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (type == TransactionType.INCOME) ledgerColors.income else ledgerColors.expense,
                    )
                },
                isError = showAmountError,
                supportingText = if (showAmountError) {
                    { Text(strings.amountError) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_amount_input"),
            )

            // Categories FlowRow
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.categoryLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    categories.forEach { (key, bn) ->
                        val selected = category.equals(key, ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = { category = key },
                            label = {
                                Text(if (language == AppLanguage.BENGALI) bn else key)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = LedgerCategoryVisuals.iconForCategory(key),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }

            // Payment Method
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.paymentMethodLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CategoryCatalog.paymentMethods.forEach { (key, bn) ->
                        val selected = paymentMethod.equals(key, ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = { paymentMethod = key },
                            label = {
                                Text(if (language == AppLanguage.BENGALI) bn else key)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = LedgerCategoryVisuals.iconForPaymentMethod(key),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                }
            }

            // Date input with DatePicker trigger
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text(strings.dateLabel) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = strings.dateLabel,
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_date_input"),
            )

            // Note input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(strings.noteLabel) },
                placeholder = { Text(strings.notePlaceholder) },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_note_input"),
            )

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Text(strings.cancelAction)
                }
                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull()
                        if (parsedAmount == null || parsedAmount <= 0.0) {
                            showAmountError = true
                            return@Button
                        }
                        val entity = TransactionEntity(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            type = type.name,
                            amount = parsedAmount,
                            category = category,
                            note = note.trim(),
                            date = date.trim().ifBlank { LedgerFormatters.todayIso() },
                            paymentMethod = paymentMethod,
                            timestamp = existing?.timestamp ?: System.currentTimeMillis(),
                        )
                        onSave(entity)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(52.dp)
                        .testTag("save_transaction_button"),
                ) {
                    Text(
                        text = if (existing == null) strings.saveAction else strings.updateAction,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            date = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(strings.saveAction)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(strings.cancelAction)
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
