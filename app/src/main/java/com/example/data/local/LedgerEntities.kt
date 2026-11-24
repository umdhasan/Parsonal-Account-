package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class TransactionType {
    INCOME,
    EXPENSE;

    companion object {
        fun fromString(raw: String?): TransactionType = when (raw?.trim()?.uppercase()) {
            "INCOME", "IN", "CREDIT", "আয়" -> INCOME
            else -> EXPENSE
        }
    }
}

enum class LoanType {
    GIVEN,   // Receivable / পাওনা
    TAKEN;   // Payable / দেনা

    companion object {
        fun fromString(raw: String?): LoanType = when (raw?.trim()?.uppercase()) {
            "GIVEN", "LENT", "RECEIVABLE", "PAONA", "পাওনা" -> GIVEN
            else -> TAKEN
        }
    }
}

enum class LoanStatus {
    ACTIVE,
    SETTLED;

    companion object {
        fun fromString(raw: String?): LoanStatus = when (raw?.trim()?.uppercase()) {
            "SETTLED", "PAID", "CLOSED", "COMPLETED", "পরিশোধিত" -> SETTLED
            else -> ACTIVE
        }
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: String = TransactionType.EXPENSE.name,
    val amount: Double = 0.0,
    val category: String = "Other",
    val note: String = "",
    val date: String = "",
    val paymentMethod: String = "Cash",
    val timestamp: Long = System.currentTimeMillis(),
) {
    val transactionType: TransactionType
        get() = TransactionType.fromString(type)
}

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val personName: String = "",
    val type: String = LoanType.GIVEN.name,
    val amount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val phone: String = "",
    val note: String = "",
    val date: String = "",
    val dueDate: String = "",
    val status: String = LoanStatus.ACTIVE.name,
    val timestamp: Long = System.currentTimeMillis(),
) {
    val loanType: LoanType
        get() = LoanType.fromString(type)

    val loanStatus: LoanStatus
        get() = if (paidAmount >= amount && amount > 0.0) {
            LoanStatus.SETTLED
        } else {
            LoanStatus.fromString(status)
        }

    val remainingAmount: Double
        get() = (amount - paidAmount).coerceAtLeast(0.0)

    val progressFraction: Float
        get() = if (amount <= 0.0) 0f else (paidAmount / amount).toFloat().coerceIn(0f, 1f)
}
