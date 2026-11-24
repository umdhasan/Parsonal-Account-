package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.local.LedgerDatabase
import com.example.data.local.LoanEntity
import com.example.data.local.LoanStatus
import com.example.data.local.LoanType
import com.example.data.local.PreferencesRepository
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.remote.AppsScriptSyncService
import com.example.data.remote.SyncResult
import com.example.data.repository.ImportStrategy
import com.example.data.repository.LedgerRepository
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.BengaliStrings
import com.example.ui.i18n.EnglishStrings
import com.example.ui.i18n.LedgerFormatters
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PeriodFilter {
    TODAY,
    WEEK,
    MONTH,
    ALL,
}

enum class TxTypeFilter {
    ALL,
    INCOME,
    EXPENSE,
}

enum class LoanFilter {
    ALL,
    GIVEN,
    TAKEN,
    SETTLED,
}

data class DailyCashflowPoint(
    val isoDate: String,
    val shortLabel: String,
    val income: Double,
    val expense: Double,
)

data class CategoryBreakdownItem(
    val category: String,
    val amount: Double,
    val percentage: Float,
)

data class SyncUiStatus(
    val isSyncing: Boolean = false,
    val lastError: String? = null,
)

data class LedgerUiState(
    val preferences: AppPreferences = AppPreferences(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val groupedTransactions: Map<String, List<TransactionEntity>> = emptyMap(),
    val allLoans: List<LoanEntity> = emptyList(),
    val filteredLoans: List<LoanEntity> = emptyList(),
    val periodFilter: PeriodFilter = PeriodFilter.MONTH,
    val txTypeFilter: TxTypeFilter = TxTypeFilter.ALL,
    val searchQuery: String = "",
    val loanFilter: LoanFilter = LoanFilter.ALL,
    // Derived financial metrics (for active period)
    val periodIncome: Double = 0.0,
    val periodExpense: Double = 0.0,
    val allTimeBalance: Double = 0.0,
    val totalReceivable: Double = 0.0,
    val totalPayable: Double = 0.0,
    val activeLoansCount: Int = 0,
    val settledLoansCount: Int = 0,
    val weeklyCashflow: List<DailyCashflowPoint> = emptyList(),
    val expenseByCategory: List<CategoryBreakdownItem> = emptyList(),
    val syncStatus: SyncUiStatus = SyncUiStatus(),
) {
    val language: AppLanguage
        get() = AppLanguage.fromCode(preferences.languageCode)

    val netLoanPosition: Double
        get() = totalReceivable - totalPayable

    val savingsRatePercent: Int
        get() = if (periodIncome <= 0.0) {
            0
        } else {
            (((periodIncome - periodExpense) / periodIncome) * 100.0).toInt().coerceIn(-99, 100)
        }
}

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LedgerRepository

    init {
        val db = LedgerDatabase.getInstance(application)
        val prefs = PreferencesRepository(application)
        val syncService = AppsScriptSyncService()
        repository = LedgerRepository(db.ledgerDao(), prefs, syncService)
    }

    private val periodFilter = MutableStateFlow(PeriodFilter.MONTH)
    private val txTypeFilter = MutableStateFlow(TxTypeFilter.ALL)
    private val searchQuery = MutableStateFlow("")
    private val loanFilter = MutableStateFlow(LoanFilter.ALL)
    private val syncStatus = MutableStateFlow(SyncUiStatus())

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    private val filterState = combine(
        periodFilter,
        txTypeFilter,
        searchQuery,
        loanFilter,
        syncStatus,
    ) { period, type, query, loanF, sync ->
        FilterBundle(period, type, query, loanF, sync)
    }

    val uiState: StateFlow<LedgerUiState> = combine(
        repository.preferences,
        repository.transactions,
        repository.loans,
        filterState,
    ) { prefs, transactions, loans, filters ->
        val today = LedgerFormatters.todayIso()
        val weekCutoff = daysAgoIso(6)
        val monthPrefix = today.take(7) // YYYY-MM

        val periodMatchedTx = transactions.filter { tx ->
            when (filters.period) {
                PeriodFilter.TODAY -> tx.date == today
                PeriodFilter.WEEK -> tx.date >= weekCutoff
                PeriodFilter.MONTH -> tx.date.startsWith(monthPrefix)
                PeriodFilter.ALL -> true
            }
        }

        val periodIncome = periodMatchedTx
            .filter { it.transactionType == TransactionType.INCOME }
            .sumOf { it.amount }
        val periodExpense = periodMatchedTx
            .filter { it.transactionType == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val allTimeIncome = transactions
            .filter { it.transactionType == TransactionType.INCOME }
            .sumOf { it.amount }
        val allTimeExpense = transactions
            .filter { it.transactionType == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val q = filters.query.trim().lowercase()
        val filteredTx = periodMatchedTx.filter { tx ->
            val matchesType = when (filters.type) {
                TxTypeFilter.ALL -> true
                TxTypeFilter.INCOME -> tx.transactionType == TransactionType.INCOME
                TxTypeFilter.EXPENSE -> tx.transactionType == TransactionType.EXPENSE
            }
            val matchesQuery = q.isEmpty() ||
                tx.category.lowercase().contains(q) ||
                tx.note.lowercase().contains(q) ||
                tx.paymentMethod.lowercase().contains(q) ||
                tx.date.contains(q)
            matchesType && matchesQuery
        }

        val groupedTx = filteredTx.groupBy { it.date.ifBlank { today } }

        val activeLoans = loans.filter { it.loanStatus == LoanStatus.ACTIVE }
        val settledLoans = loans.filter { it.loanStatus == LoanStatus.SETTLED }

        val totalReceivable = activeLoans
            .filter { it.loanType == LoanType.GIVEN }
            .sumOf { it.remainingAmount }
        val totalPayable = activeLoans
            .filter { it.loanType == LoanType.TAKEN }
            .sumOf { it.remainingAmount }

        val filteredLoans = loans.filter { loan ->
            when (filters.loanFilter) {
                LoanFilter.ALL -> true
                LoanFilter.GIVEN -> loan.loanType == LoanType.GIVEN && loan.loanStatus == LoanStatus.ACTIVE
                LoanFilter.TAKEN -> loan.loanType == LoanType.TAKEN && loan.loanStatus == LoanStatus.ACTIVE
                LoanFilter.SETTLED -> loan.loanStatus == LoanStatus.SETTLED
            }
        }

        val weeklyCashflow = buildLast7DaysSeries(transactions)
        val expenseBreakdown = buildExpenseBreakdown(periodMatchedTx)

        LedgerUiState(
            preferences = prefs,
            allTransactions = transactions,
            filteredTransactions = filteredTx,
            groupedTransactions = groupedTx,
            allLoans = loans,
            filteredLoans = filteredLoans,
            periodFilter = filters.period,
            txTypeFilter = filters.type,
            searchQuery = filters.query,
            loanFilter = filters.loanFilter,
            periodIncome = periodIncome,
            periodExpense = periodExpense,
            allTimeBalance = allTimeIncome - allTimeExpense,
            totalReceivable = totalReceivable,
            totalPayable = totalPayable,
            activeLoansCount = activeLoans.size,
            settledLoansCount = settledLoans.size,
            weeklyCashflow = weeklyCashflow,
            expenseByCategory = expenseBreakdown,
            syncStatus = filters.sync,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LedgerUiState(),
    )

    private fun currentStrings() =
        if (uiState.value.language == AppLanguage.BENGALI) BengaliStrings else EnglishStrings

    fun setPeriodFilter(filter: PeriodFilter) {
        periodFilter.value = filter
    }

    fun setTxTypeFilter(filter: TxTypeFilter) {
        txTypeFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setLoanFilter(filter: LoanFilter) {
        loanFilter.value = filter
    }

    fun saveTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.saveTransaction(tx)
            _snackbarMessages.emit(currentStrings().transactionSavedMsg)
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            _snackbarMessages.emit(currentStrings().transactionDeletedMsg)
        }
    }

    fun saveLoan(loan: LoanEntity) {
        viewModelScope.launch {
            repository.saveLoan(loan)
            _snackbarMessages.emit(currentStrings().loanSavedMsg)
        }
    }

    fun deleteLoan(id: String) {
        viewModelScope.launch {
            repository.deleteLoan(id)
            _snackbarMessages.emit(currentStrings().loanDeletedMsg)
        }
    }

    fun recordLoanPayment(loan: LoanEntity, amount: Double, alsoLogTransaction: Boolean) {
        viewModelScope.launch {
            repository.recordLoanPayment(loan, amount, alsoLogTransaction)
            _snackbarMessages.emit(currentStrings().paymentRecordedMsg)
        }
    }

    fun markLoanSettled(loan: LoanEntity) {
        viewModelScope.launch {
            repository.markLoanSettled(loan)
            _snackbarMessages.emit(currentStrings().paymentRecordedMsg)
        }
    }

    fun saveScriptUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isNotEmpty() && !AppsScriptSyncService.isValidAppsScriptUrl(trimmed)) {
            viewModelScope.launch { _snackbarMessages.emit(currentStrings().urlInvalidMsg) }
            return false
        }
        viewModelScope.launch {
            repository.saveScriptUrl(trimmed)
            syncStatus.value = syncStatus.value.copy(lastError = null)
            _snackbarMessages.emit(currentStrings().urlSavedMsg)
        }
        return true
    }

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoSync(enabled) }
    }

    fun setCurrency(symbol: String) {
        viewModelScope.launch { repository.setCurrency(symbol) }
    }

    fun toggleLanguage() {
        val next = if (uiState.value.language == AppLanguage.BENGALI) {
            AppLanguage.ENGLISH.code
        } else {
            AppLanguage.BENGALI.code
        }
        viewModelScope.launch { repository.setLanguage(next) }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch { repository.setLanguage(code) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun pushToGoogleDrive() {
        if (syncStatus.value.isSyncing) return
        viewModelScope.launch {
            syncStatus.value = SyncUiStatus(isSyncing = true, lastError = null)
            when (val result = repository.pushToGoogleDrive()) {
                is SyncResult.Success -> {
                    syncStatus.value = SyncUiStatus(isSyncing = false, lastError = null)
                    val (txCount, loanCount) = result.data
                    _snackbarMessages.emit(
                        "${currentStrings().saveSuccessMsg} ($txCount tx, $loanCount loans)",
                    )
                }
                is SyncResult.Error -> {
                    val msg = if (result.message == "MISSING_URL") {
                        currentStrings().missingUrlError
                    } else {
                        result.message
                    }
                    syncStatus.value = SyncUiStatus(isSyncing = false, lastError = msg)
                    _snackbarMessages.emit(msg)
                }
            }
        }
    }

    fun pullFromGoogleDrive(strategy: ImportStrategy) {
        if (syncStatus.value.isSyncing) return
        viewModelScope.launch {
            syncStatus.value = SyncUiStatus(isSyncing = true, lastError = null)
            when (val result = repository.pullFromGoogleDrive(strategy)) {
                is SyncResult.Success -> {
                    syncStatus.value = SyncUiStatus(isSyncing = false, lastError = null)
                    val (txCount, loanCount) = result.data
                    _snackbarMessages.emit(
                        "${currentStrings().importSuccessMsg} ($txCount tx, $loanCount loans)",
                    )
                }
                is SyncResult.Error -> {
                    val msg = if (result.message == "MISSING_URL") {
                        currentStrings().missingUrlError
                    } else {
                        result.message
                    }
                    syncStatus.value = SyncUiStatus(isSyncing = false, lastError = msg)
                    _snackbarMessages.emit(msg)
                }
            }
        }
    }

    suspend fun generateLocalBackupJson(): String = repository.exportLocalJson()

    fun importLocalBackupJson(rawJson: String, strategy: ImportStrategy = ImportStrategy.MERGE) {
        viewModelScope.launch {
            runCatching { repository.importLocalJson(rawJson, strategy) }
                .onSuccess { (txCount, loanCount) ->
                    _snackbarMessages.emit(
                        "${currentStrings().localImportSuccess} ($txCount tx, $loanCount loans)",
                    )
                }
                .onFailure { err ->
                    _snackbarMessages.emit("JSON Error: ${err.localizedMessage ?: "Invalid file"}")
                }
        }
    }

    fun notifyMessage(msg: String) {
        viewModelScope.launch { _snackbarMessages.emit(msg) }
    }

    private fun daysAgoIso(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    private fun buildLast7DaysSeries(transactions: List<TransactionEntity>): List<DailyCashflowPoint> {
        val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val labelFmt = SimpleDateFormat("EEE", Locale.getDefault())
        val byDate = transactions.groupBy { it.date }

        return (6 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
            val dateObj: Date = cal.time
            val iso = isoFmt.format(dateObj)
            val label = labelFmt.format(dateObj).take(3)
            val dayTxs = byDate[iso].orEmpty()
            DailyCashflowPoint(
                isoDate = iso,
                shortLabel = label,
                income = dayTxs.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount },
                expense = dayTxs.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount },
            )
        }
    }

    private fun buildExpenseBreakdown(transactions: List<TransactionEntity>): List<CategoryBreakdownItem> {
        val expenses = transactions.filter { it.transactionType == TransactionType.EXPENSE }
        val total = expenses.sumOf { it.amount }
        if (total <= 0.0) return emptyList()
        return expenses
            .groupBy { it.category.ifBlank { "Other" } }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                CategoryBreakdownItem(
                    category = cat,
                    amount = sum,
                    percentage = (sum / total).toFloat().coerceIn(0f, 1f),
                )
            }
            .sortedByDescending { it.amount }
    }

    private data class FilterBundle(
        val period: PeriodFilter,
        val type: TxTypeFilter,
        val query: String,
        val loanFilter: LoanFilter,
        val sync: SyncUiStatus,
    )
}
