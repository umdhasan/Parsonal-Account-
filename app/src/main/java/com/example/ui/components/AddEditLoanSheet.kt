package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CallMade
import androidx.compose.material.icons.rounded.CallReceived
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.data.local.LoanEntity
import com.example.data.local.LoanStatus
import com.example.data.local.LoanType
import com.example.ui.i18n.LedgerFormatters
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.LocalLedgerColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLoanSheet(
    existing: LoanEntity?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (LoanEntity) -> Unit,
    onDelete: ((String) -> Unit)? = null,
) {
    val strings = LocalStrings.current
    val ledgerColors = LocalLedgerColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember(existing) { mutableStateOf(existing?.loanType ?: LoanType.GIVEN) }
    var personName by remember(existing) { mutableStateOf(existing?.personName ?: "") }
    var phone by remember(existing) { mutableStateOf(existing?.phone ?: "") }
    var amountText by remember(existing) {
        mutableStateOf(
            existing?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                ?: "",
        )
    }
    var paidText by remember(existing) {
        mutableStateOf(
            existing?.paidAmount?.takeIf { it > 0.0 }?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: "",
        )
    }
    var date by remember(existing) {
        mutableStateOf(existing?.date?.ifBlank { LedgerFormatters.todayIso() } ?: LedgerFormatters.todayIso())
    }
    var dueDate by remember(existing) { mutableStateOf(existing?.dueDate ?: "") }
    var note by remember(existing) { mutableStateOf(existing?.note ?: "") }

    var showNameError by remember { mutableStateOf(false) }
    var showAmountError by remember { mutableStateOf(false) }
    var pickingDueDate by remember { mutableStateOf<Boolean?>(null) }

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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (existing == null) strings.newLoanTitle else strings.editLoanTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (existing != null && onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete(existing.id)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("delete_loan_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.cd_delete),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            // Given vs Taken toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val isGiven = type == LoanType.GIVEN
                Surface(
                    onClick = { type = LoanType.GIVEN },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isGiven) ledgerColors.loanGivenContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = if (isGiven) 1.5.dp else 1.dp,
                        color = if (isGiven) ledgerColors.loanGiven else MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("loan_type_given_tab"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CallMade,
                            contentDescription = null,
                            tint = if (isGiven) ledgerColors.loanGiven else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.loanGiven,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isGiven) ledgerColors.loanGiven else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    onClick = { type = LoanType.TAKEN },
                    shape = RoundedCornerShape(14.dp),
                    color = if (!isGiven) ledgerColors.loanTakenContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = if (!isGiven) 1.5.dp else 1.dp,
                        color = if (!isGiven) ledgerColors.loanTaken else MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("loan_type_taken_tab"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CallReceived,
                            contentDescription = null,
                            tint = if (!isGiven) ledgerColors.loanTaken else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.loanTaken,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (!isGiven) ledgerColors.loanTaken else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            OutlinedTextField(
                value = personName,
                onValueChange = {
                    personName = it
                    showNameError = false
                },
                label = { Text(strings.personNameLabel) },
                isError = showNameError,
                supportingText = if (showNameError) {
                    { Text(strings.personNameError) }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_person_input"),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() || it == '.' }
                        if (clean.count { it == '.' } <= 1) {
                            amountText = clean
                            showAmountError = false
                        }
                    },
                    label = { Text("${strings.totalLoanAmount} ($currency)") },
                    isError = showAmountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("loan_amount_input"),
                )

                OutlinedTextField(
                    value = paidText,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() || it == '.' }
                        if (clean.count { it == '.' } <= 1) paidText = clean
                    },
                    label = { Text("${strings.alreadyPaidAmount} ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(strings.phoneLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(strings.dateLabel) },
                    trailingIcon = {
                        IconButton(onClick = { pickingDueDate = false }) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = strings.dateLabel)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text(strings.dueDateLabel) },
                    trailingIcon = {
                        IconButton(onClick = { pickingDueDate = true }) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = strings.dueDateLabel)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(strings.noteLabel) },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )

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
                        val trimmedName = personName.trim()
                        val amount = amountText.toDoubleOrNull()
                        if (trimmedName.isEmpty()) {
                            showNameError = true
                            return@Button
                        }
                        if (amount == null || amount <= 0.0) {
                            showAmountError = true
                            return@Button
                        }
                        val paid = (paidText.toDoubleOrNull() ?: 0.0).coerceIn(0.0, amount)
                        val status = if (paid >= amount) LoanStatus.SETTLED else LoanStatus.ACTIVE
                        onSave(
                            LoanEntity(
                                id = existing?.id ?: UUID.randomUUID().toString(),
                                personName = trimmedName,
                                type = type.name,
                                amount = amount,
                                paidAmount = paid,
                                phone = phone.trim(),
                                note = note.trim(),
                                date = date.trim().ifBlank { LedgerFormatters.todayIso() },
                                dueDate = dueDate.trim(),
                                status = status.name,
                                timestamp = existing?.timestamp ?: System.currentTimeMillis(),
                            ),
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(52.dp)
                        .testTag("save_loan_button"),
                ) {
                    Text(if (existing == null) strings.saveAction else strings.updateAction)
                }
            }
        }
    }

    if (pickingDueDate != null) {
        val isDue = pickingDueDate == true
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { pickingDueDate = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            val formatted = sdf.format(Date(millis))
                            if (isDue) dueDate = formatted else date = formatted
                        }
                        pickingDueDate = null
                    },
                ) {
                    Text(strings.saveAction)
                }
            },
            dismissButton = {
                TextButton(onClick = { pickingDueDate = null }) {
                    Text(strings.cancelAction)
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
fun RecordLoanPaymentDialog(
    loan: LoanEntity,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, alsoLogTransaction: Boolean) -> Unit,
) {
    val strings = LocalStrings.current
    val remaining = loan.remainingAmount
    var paymentText by remember(loan) {
        mutableStateOf(
            if (remaining % 1.0 == 0.0) remaining.toLong().toString() else remaining.toString(),
        )
    }
    var alsoLogTx by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${strings.recordPayment} • ${loan.personName}",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${strings.remainingLabel}: ${LedgerFormatters.formatMoney(remaining, currency)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                OutlinedTextField(
                    value = paymentText,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() || it == '.' }
                        if (clean.count { it == '.' } <= 1) {
                            paymentText = clean
                            showError = false
                        }
                    },
                    label = { Text("${strings.paymentAmountLabel} ($currency)") },
                    isError = showError,
                    supportingText = if (showError) {
                        { Text(strings.amountError) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("loan_payment_amount_input"),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = alsoLogTx,
                        onCheckedChange = { alsoLogTx = it },
                    )
                    Text(
                        text = strings.alsoLogTransaction,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = paymentText.toDoubleOrNull()
                    if (value == null || value <= 0.0) {
                        showError = true
                        return@Button
                    }
                    onConfirm(value, alsoLogTx)
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_loan_payment_button"),
            ) {
                Text(strings.saveAction)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelAction)
            }
        },
    )
}
