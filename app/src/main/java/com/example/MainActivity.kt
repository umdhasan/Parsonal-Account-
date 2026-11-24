package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.LoanEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.components.AddEditLoanSheet
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.components.RecordLoanPaymentDialog
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.BengaliStrings
import com.example.ui.i18n.EnglishStrings
import com.example.ui.i18n.LocalStrings
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoansScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SyncSettingsScreen
import com.example.ui.theme.DailyLedgerTheme
import com.example.ui.viewmodel.LedgerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class LedgerTab(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    LEDGER("ledger", Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    LOANS("loans", Icons.Rounded.Handshake, Icons.Outlined.Handshake),
    REPORTS("reports", Icons.Rounded.BarChart, Icons.Outlined.BarChart),
    SYNC("sync", Icons.Rounded.CloudSync, Icons.Outlined.CloudSync),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LedgerViewModel = viewModel()
            DailyLedgerApp(viewModel = viewModel)
        }
    }
}

@Composable
fun DailyLedgerApp(viewModel: LedgerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = if (uiState.language == AppLanguage.BENGALI) BengaliStrings else EnglishStrings
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var currentTab by rememberSaveable { mutableStateOf(LedgerTab.LEDGER) }

    // Modal sheet & dialog states
    var showTransactionSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var defaultTransactionType by remember { mutableStateOf(TransactionType.EXPENSE) }

    var showLoanSheet by remember { mutableStateOf(false) }
    var editingLoan by remember { mutableStateOf<LoanEntity?>(null) }
    var paymentTargetLoan by remember { mutableStateOf<LoanEntity?>(null) }

    // Zero-permission SAF Export / Import launchers for Daily_Income_Expense_Data.json
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching {
                    val json = viewModel.generateLocalBackupJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(json.toByteArray(Charsets.UTF_8))
                        }
                    }
                }.onSuccess {
                    viewModel.notifyMessage(strings.localExportSuccess)
                }.onFailure { err ->
                    viewModel.notifyMessage(err.localizedMessage ?: "Export failed")
                }
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)
                            ?.bufferedReader(Charsets.UTF_8)
                            ?.use { it.readText() }
                            .orEmpty()
                    }
                }.onSuccess { content ->
                    viewModel.importLocalBackupJson(content)
                }.onFailure { err ->
                    viewModel.notifyMessage(err.localizedMessage ?: "Import failed")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle system back on non-home tabs cleanly
    BackHandler(enabled = currentTab != LedgerTab.LEDGER) {
        currentTab = LedgerTab.LEDGER
    }

    DailyLedgerTheme(themeMode = uiState.preferences.themeMode) {
        CompositionLocalProvider(LocalStrings provides strings) {
            val tabLabels = mapOf(
                LedgerTab.LEDGER to strings.navLedger,
                LedgerTab.LOANS to strings.navLoans,
                LedgerTab.REPORTS to strings.navReports,
                LedgerTab.SYNC to strings.navDriveSync,
            )

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 600.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ) {
                            LedgerTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tabLabels[tab],
                                        )
                                    },
                                    label = { Text(tabLabels[tab].orEmpty()) },
                                    modifier = Modifier.testTag("nav_rail_${tab.route}"),
                                )
                            }
                        }
                    }

                    Scaffold(
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.background,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        floatingActionButton = {
                            if (currentTab == LedgerTab.LEDGER || currentTab == LedgerTab.LOANS) {
                                FloatingActionButton(
                                    onClick = {
                                        if (currentTab == LedgerTab.LEDGER) {
                                            editingTransaction = null
                                            defaultTransactionType = TransactionType.EXPENSE
                                            showTransactionSheet = true
                                        } else {
                                            editingLoan = null
                                            showLoanSheet = true
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.testTag("primary_add_fab"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = stringResource(R.string.cd_add_entry),
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            if (!useNavigationRail) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    windowInsets = WindowInsets.navigationBars,
                                    tonalElevation = 2.dp,
                                ) {
                                    LedgerTab.entries.forEach { tab ->
                                        val selected = currentTab == tab
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { currentTab = tab },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tabLabels[tab],
                                                )
                                            },
                                            label = { Text(tabLabels[tab].orEmpty()) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            ),
                                            modifier = Modifier.testTag("nav_bar_${tab.route}"),
                                        )
                                    }
                                }
                            }
                        },
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = 720.dp),
                            ) {
                                when (currentTab) {
                                    LedgerTab.LEDGER -> HomeScreen(
                                        uiState = uiState,
                                        onToggleLanguage = viewModel::toggleLanguage,
                                        onQuickAddIncome = {
                                            editingTransaction = null
                                            defaultTransactionType = TransactionType.INCOME
                                            showTransactionSheet = true
                                        },
                                        onQuickAddExpense = {
                                            editingTransaction = null
                                            defaultTransactionType = TransactionType.EXPENSE
                                            showTransactionSheet = true
                                        },
                                        onQuickAddLoan = {
                                            editingLoan = null
                                            showLoanSheet = true
                                        },
                                        onOpenDriveSync = { currentTab = LedgerTab.SYNC },
                                        onQuickSyncNow = viewModel::pushToGoogleDrive,
                                        onPeriodChange = viewModel::setPeriodFilter,
                                        onTypeFilterChange = viewModel::setTxTypeFilter,
                                        onSearchQueryChange = viewModel::setSearchQuery,
                                        onTransactionClick = { tx ->
                                            editingTransaction = tx
                                            defaultTransactionType = tx.transactionType
                                            showTransactionSheet = true
                                        },
                                    )

                                    LedgerTab.LOANS -> LoansScreen(
                                        uiState = uiState,
                                        onFilterChange = viewModel::setLoanFilter,
                                        onLoanClick = { loan ->
                                            editingLoan = loan
                                            showLoanSheet = true
                                        },
                                        onRecordPaymentClick = { loan ->
                                            paymentTargetLoan = loan
                                        },
                                        onMarkSettledClick = viewModel::markLoanSettled,
                                    )

                                    LedgerTab.REPORTS -> ReportsScreen(
                                        uiState = uiState,
                                        onPeriodChange = viewModel::setPeriodFilter,
                                    )

                                    LedgerTab.SYNC -> SyncSettingsScreen(
                                        uiState = uiState,
                                        onSaveScriptUrl = viewModel::saveScriptUrl,
                                        onPushToDrive = viewModel::pushToGoogleDrive,
                                        onPullFromDrive = viewModel::pullFromGoogleDrive,
                                        onAutoSyncChange = viewModel::setAutoSync,
                                        onExportLocalJson = {
                                            exportJsonLauncher.launch("Daily_Income_Expense_Data.json")
                                        },
                                        onImportLocalJson = {
                                            importJsonLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                        },
                                        onCurrencyChange = viewModel::setCurrency,
                                        onLanguageChange = viewModel::setLanguage,
                                        onThemeModeChange = viewModel::setThemeMode,
                                        onScriptCopied = {
                                            viewModel.notifyMessage(strings.scriptCopiedMsg)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showTransactionSheet) {
                AddEditTransactionSheet(
                    existing = editingTransaction,
                    initialType = defaultTransactionType,
                    currency = uiState.preferences.currencySymbol,
                    language = uiState.language,
                    onDismiss = {
                        showTransactionSheet = false
                        editingTransaction = null
                    },
                    onSave = viewModel::saveTransaction,
                    onDelete = viewModel::deleteTransaction,
                )
            }

            if (showLoanSheet) {
                AddEditLoanSheet(
                    existing = editingLoan,
                    currency = uiState.preferences.currencySymbol,
                    onDismiss = {
                        showLoanSheet = false
                        editingLoan = null
                    },
                    onSave = viewModel::saveLoan,
                    onDelete = viewModel::deleteLoan,
                )
            }

            paymentTargetLoan?.let { target ->
                RecordLoanPaymentDialog(
                    loan = target,
                    currency = uiState.preferences.currencySymbol,
                    onDismiss = { paymentTargetLoan = null },
                    onConfirm = { amount, alsoLogTx ->
                        viewModel.recordLoanPayment(target, amount, alsoLogTx)
                    },
                )
            }
        }
    }
}
