package com.example.data.remote

import com.example.data.local.LoanEntity
import com.example.data.local.LoanStatus
import com.example.data.local.LoanType
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.i18n.LedgerFormatters
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class DriveLedgerPayload(
    val transactions: List<TransactionEntity>,
    val loans: List<LoanEntity>,
)

/**
 * Serializes and deserializes the exact JSON format used by Daily_Income_Expense_Data.json:
 * { "transactions": [...], "loans": [...] }
 * Resilient to pre-existing Drive JSON files with optional or alternate field names.
 */
object DriveLedgerJsonCodec {

    fun encode(payload: DriveLedgerPayload): String {
        val root = JSONObject()

        val txArray = JSONArray()
        for (tx in payload.transactions) {
            txArray.put(
                JSONObject().apply {
                    put("id", tx.id)
                    put("type", tx.type)
                    put("amount", tx.amount)
                    put("category", tx.category)
                    put("note", tx.note)
                    put("date", tx.date)
                    put("paymentMethod", tx.paymentMethod)
                    put("timestamp", tx.timestamp)
                },
            )
        }

        val loanArray = JSONArray()
        for (loan in payload.loans) {
            loanArray.put(
                JSONObject().apply {
                    put("id", loan.id)
                    put("personName", loan.personName)
                    put("type", loan.type)
                    put("amount", loan.amount)
                    put("paidAmount", loan.paidAmount)
                    put("phone", loan.phone)
                    put("note", loan.note)
                    put("date", loan.date)
                    put("dueDate", loan.dueDate)
                    put("status", loan.status)
                    put("timestamp", loan.timestamp)
                },
            )
        }

        root.put("transactions", txArray)
        root.put("loans", loanArray)
        return root.toString(2)
    }

    fun decode(rawJson: String): DriveLedgerPayload {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) {
            return DriveLedgerPayload(emptyList(), emptyList())
        }
        val root = JSONObject(trimmed)

        val transactions = mutableListOf<TransactionEntity>()
        val txArray = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until txArray.length()) {
            val obj = txArray.optJSONObject(i) ?: continue
            val id = obj.optNonBlankString("id") ?: UUID.randomUUID().toString()
            val rawType = obj.optNonBlankString("type") ?: "EXPENSE"
            val amount = obj.optFlexibleDouble("amount")
            val category = obj.optNonBlankString("category") ?: "Other"
            val note = obj.optNonBlankString("note")
                ?: obj.optNonBlankString("description")
                ?: obj.optNonBlankString("title")
                ?: ""
            val date = obj.optNonBlankString("date") ?: LedgerFormatters.todayIso()
            val paymentMethod = obj.optNonBlankString("paymentMethod")
                ?: obj.optNonBlankString("method")
                ?: "Cash"
            val timestamp = obj.optLong("timestamp", System.currentTimeMillis())

            transactions.add(
                TransactionEntity(
                    id = id,
                    type = TransactionType.fromString(rawType).name,
                    amount = amount,
                    category = category,
                    note = note,
                    date = date,
                    paymentMethod = paymentMethod,
                    timestamp = if (timestamp > 0L) timestamp else System.currentTimeMillis(),
                ),
            )
        }

        val loans = mutableListOf<LoanEntity>()
        val loanArray = root.optJSONArray("loans") ?: JSONArray()
        for (i in 0 until loanArray.length()) {
            val obj = loanArray.optJSONObject(i) ?: continue
            val id = obj.optNonBlankString("id") ?: UUID.randomUUID().toString()
            val personName = obj.optNonBlankString("personName")
                ?: obj.optNonBlankString("person")
                ?: obj.optNonBlankString("name")
                ?: "Unknown"
            val rawType = obj.optNonBlankString("type") ?: "GIVEN"
            val amount = obj.optFlexibleDouble("amount")
            val paidAmount = obj.optFlexibleDouble("paidAmount")
                .takeIf { it > 0.0 } ?: obj.optFlexibleDouble("paid")
            val phone = obj.optNonBlankString("phone") ?: ""
            val note = obj.optNonBlankString("note")
                ?: obj.optNonBlankString("description")
                ?: ""
            val date = obj.optNonBlankString("date") ?: LedgerFormatters.todayIso()
            val dueDate = obj.optNonBlankString("dueDate") ?: ""
            val rawStatus = obj.optNonBlankString("status")
            val status = if (paidAmount >= amount && amount > 0.0) {
                LoanStatus.SETTLED.name
            } else {
                LoanStatus.fromString(rawStatus).name
            }
            val timestamp = obj.optLong("timestamp", System.currentTimeMillis())

            loans.add(
                LoanEntity(
                    id = id,
                    personName = personName,
                    type = LoanType.fromString(rawType).name,
                    amount = amount,
                    paidAmount = paidAmount.coerceAtMost(amount.coerceAtLeast(paidAmount)),
                    phone = phone,
                    note = note,
                    date = date,
                    dueDate = dueDate,
                    status = status,
                    timestamp = if (timestamp > 0L) timestamp else System.currentTimeMillis(),
                ),
            )
        }

        return DriveLedgerPayload(transactions = transactions, loans = loans)
    }

    private fun JSONObject.optNonBlankString(key: String): String? {
        if (!has(key) || isNull(key)) return null
        val value = opt(key)?.toString()?.trim()
        return value?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
    }

    private fun JSONObject.optFlexibleDouble(key: String): Double {
        if (!has(key) || isNull(key)) return 0.0
        val raw = opt(key)
        return when (raw) {
            is Number -> raw.toDouble()
            is String -> raw.replace(",", "").trim().toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}
