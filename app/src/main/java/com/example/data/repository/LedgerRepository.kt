package com.example.data.repository

import com.example.data.local.AppPreferences
import com.example.data.local.LedgerDao
import com.example.data.local.LoanEntity
import com.example.data.local.LoanStatus
import com.example.data.local.LoanType
import com.example.data.local.PreferencesRepository
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.remote.AppsScriptSyncService
import com.example.data.remote.DriveLedgerJsonCodec
import com.example.data.remote.DriveLedgerPayload
import com.example.data.remote.SyncResult
import com.example.ui.i18n.LedgerFormatters
import kotlinx.coroutines.flow.Flow

enum class ImportStrategy {
    MERGE,
    REPLACE,
}

class LedgerRepository(
    private val dao: LedgerDao,
    private val prefs: PreferencesRepository,
    private val syncService: AppsScriptSyncService,
) {
    val transactions: Flow<List<TransactionEntity>> = dao.observeTransactions()
    val loans: Flow<List<LoanEntity>> = dao.observeLoans()
    val preferences: Flow<AppPreferences> = prefs.preferencesFlow

    suspend fun saveTransaction(tx: TransactionEntity) {
        dao.upsertTransaction(tx)
        prefs.markPendingChanges()
        tryAutoPush()
    }

    suspend fun deleteTransaction(id: String) {
        dao.deleteTransactionById(id)
        prefs.markPendingChanges()
        tryAutoPush()
    }

    suspend fun saveLoan(loan: LoanEntity) {
        val normalized = loan.copy(
            status = if (loan.paidAmount >= loan.amount && loan.amount > 0.0) {
                LoanStatus.SETTLED.name
            } else {
                loan.status
            },
        )
        dao.upsertLoan(normalized)
        prefs.markPendingChanges()
        tryAutoPush()
    }

    suspend fun deleteLoan(id: String) {
        dao.deleteLoanById(id)
        prefs.markPendingChanges()
        tryAutoPush()
    }

    suspend fun recordLoanPayment(
        loan: LoanEntity,
        paymentAmount: Double,
        alsoLogTransaction: Boolean,
    ) {
        val newPaid = (loan.paidAmount + paymentAmount).coerceAtMost(loan.amount)
        val newStatus = if (newPaid >= loan.amount) LoanStatus.SETTLED.name else LoanStatus.ACTIVE.name
        dao.upsertLoan(
            loan.copy(
                paidAmount = newPaid,
                status = newStatus,
            ),
        )
        if (alsoLogTransaction && paymentAmount > 0.0) {
            val txType = if (loan.loanType == LoanType.GIVEN) {
                TransactionType.INCOME
            } else {
                TransactionType.EXPENSE
            }
            val category = if (loan.loanType == LoanType.GIVEN) "Loan Return" else "Loan Paid"
            dao.upsertTransaction(
                TransactionEntity(
                    type = txType.name,
                    amount = paymentAmount,
                    category = category,
                    note = "${loan.personName} (${if (loan.loanType == LoanType.GIVEN) "Loan return" else "Loan payment"})",
                    date = LedgerFormatters.todayIso(),
                    paymentMethod = "Cash",
                ),
            )
        }
        prefs.markPendingChanges()
        tryAutoPush()
    }

    suspend fun markLoanSettled(loan: LoanEntity) {
        dao.upsertLoan(
            loan.copy(
                paidAmount = loan.amount,
                status = LoanStatus.SETTLED.name,
            ),
        )
        prefs.markPendingChanges()
        tryAutoPush()
    }

    suspend fun pushToGoogleDrive(): SyncResult<Pair<Int, Int>> {
        val currentPrefs = prefs.getPreferencesOnce()
        if (currentPrefs.scriptUrl.isBlank()) {
            return SyncResult.Error("MISSING_URL")
        }
        val txList = dao.getAllTransactionsOnce()
        val loanList = dao.getAllLoansOnce()
        val result = syncService.saveToDrive(
            scriptUrl = currentPrefs.scriptUrl,
            payload = DriveLedgerPayload(transactions = txList, loans = loanList),
        )
        return when (result) {
            is SyncResult.Success -> {
                prefs.markSynced()
                SyncResult.Success(txList.size to loanList.size)
            }
            is SyncResult.Error -> result
        }
    }

    suspend fun pullFromGoogleDrive(strategy: ImportStrategy): SyncResult<Pair<Int, Int>> {
        val currentPrefs = prefs.getPreferencesOnce()
        if (currentPrefs.scriptUrl.isBlank()) {
            return SyncResult.Error("MISSING_URL")
        }
        return when (val result = syncService.importFromDrive(currentPrefs.scriptUrl)) {
            is SyncResult.Success -> {
                val payload = result.data
                when (strategy) {
                    ImportStrategy.MERGE -> dao.mergeData(payload.transactions, payload.loans)
                    ImportStrategy.REPLACE -> dao.replaceAllData(payload.transactions, payload.loans)
                }
                prefs.markSynced()
                SyncResult.Success(payload.transactions.size to payload.loans.size)
            }
            is SyncResult.Error -> result
        }
    }

    suspend fun exportLocalJson(): String {
        val txList = dao.getAllTransactionsOnce()
        val loanList = dao.getAllLoansOnce()
        return DriveLedgerJsonCodec.encode(DriveLedgerPayload(txList, loanList))
    }

    suspend fun importLocalJson(rawJson: String, strategy: ImportStrategy): Pair<Int, Int> {
        val payload = DriveLedgerJsonCodec.decode(rawJson)
        when (strategy) {
            ImportStrategy.MERGE -> dao.mergeData(payload.transactions, payload.loans)
            ImportStrategy.REPLACE -> dao.replaceAllData(payload.transactions, payload.loans)
        }
        prefs.markPendingChanges()
        return payload.transactions.size to payload.loans.size
    }

    suspend fun saveScriptUrl(url: String) = prefs.saveScriptUrl(url)
    suspend fun setAutoSync(enabled: Boolean) = prefs.setAutoSync(enabled)
    suspend fun setCurrency(symbol: String) = prefs.setCurrency(symbol)
    suspend fun setLanguage(code: String) = prefs.setLanguage(code)
    suspend fun setThemeMode(mode: String) = prefs.setThemeMode(mode)

    private suspend fun tryAutoPush() {
        val currentPrefs = prefs.getPreferencesOnce()
        if (currentPrefs.autoSync && AppsScriptSyncService.isValidAppsScriptUrl(currentPrefs.scriptUrl)) {
            val txList = dao.getAllTransactionsOnce()
            val loanList = dao.getAllLoansOnce()
            val res = syncService.saveToDrive(
                currentPrefs.scriptUrl,
                DriveLedgerPayload(txList, loanList),
            )
            if (res is SyncResult.Success) {
                prefs.markSynced()
            }
        }
    }
}
