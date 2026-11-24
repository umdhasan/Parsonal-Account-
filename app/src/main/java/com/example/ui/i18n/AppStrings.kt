package com.example.ui.i18n

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppLanguage(val code: String, val badge: String) {
    ENGLISH("en", "EN"),
    BENGALI("bn", "বাং");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code == code } ?: BENGALI
    }
}

@Immutable
data class LedgerStrings(
    val appTitle: String,
    val appSubtitle: String,
    // Bottom nav
    val navLedger: String,
    val navLoans: String,
    val navReports: String,
    val navDriveSync: String,
    // Hero card
    val netBalance: String,
    val totalIncome: String,
    val totalExpense: String,
    val netLoanPosition: String,
    // Quick actions
    val addIncome: String,
    val addExpense: String,
    val addLoan: String,
    val driveSync: String,
    // Filters
    val filterToday: String,
    val filterWeek: String,
    val filterMonth: String,
    val filterAll: String,
    val typeAll: String,
    val typeIncome: String,
    val typeExpense: String,
    val searchPlaceholder: String,
    val dailyTransactions: String,
    val entriesCount: String,
    val noTransactionsTitle: String,
    val noTransactionsBody: String,
    // Transaction Sheet
    val newTransactionTitle: String,
    val editTransactionTitle: String,
    val amountLabel: String,
    val categoryLabel: String,
    val paymentMethodLabel: String,
    val dateLabel: String,
    val noteLabel: String,
    val notePlaceholder: String,
    val saveAction: String,
    val updateAction: String,
    val deleteAction: String,
    val cancelAction: String,
    val amountError: String,
    // Loans
    val loansTitle: String,
    val youWillGet: String,
    val youWillPay: String,
    val loanGiven: String,
    val loanTaken: String,
    val loanActive: String,
    val loanSettled: String,
    val newLoanTitle: String,
    val editLoanTitle: String,
    val personNameLabel: String,
    val personNameError: String,
    val phoneLabel: String,
    val totalLoanAmount: String,
    val alreadyPaidAmount: String,
    val remainingLabel: String,
    val paidLabel: String,
    val dueDateLabel: String,
    val overdueTag: String,
    val recordPayment: String,
    val markSettled: String,
    val paymentAmountLabel: String,
    val alsoLogTransaction: String,
    val noLoansTitle: String,
    val noLoansBody: String,
    // Reports
    val reportsTitle: String,
    val cashflowLast7Days: String,
    val expenseByCategory: String,
    val savingsRate: String,
    val savingsHintPositive: String,
    val savingsHintNegative: String,
    val loanSummaryTitle: String,
    val activeLoansCount: String,
    val settledLoansCount: String,
    val noChartData: String,
    // Drive Sync & Settings
    val syncTitle: String,
    val syncSubtitle: String,
    val scriptUrlLabel: String,
    val scriptUrlPlaceholder: String,
    val saveUrlAction: String,
    val urlSavedMsg: String,
    val urlInvalidMsg: String,
    val connectedBadge: String,
    val notConfiguredBadge: String,
    val pendingChangesBadge: String,
    val syncedBadge: String,
    val lastSyncedPrefix: String,
    val neverSynced: String,
    val saveToDriveTitle: String,
    val saveToDriveDesc: String,
    val importFromDriveTitle: String,
    val importFromDriveDesc: String,
    val autoSyncTitle: String,
    val autoSyncDesc: String,
    val importModeTitle: String,
    val importModeBody: String,
    val mergeOption: String,
    val replaceOption: String,
    val appsScriptGuideTitle: String,
    val appsScriptGuideSubtitle: String,
    val copyScriptAction: String,
    val scriptCopiedMsg: String,
    val setupSteps: List<String>,
    val localBackupTitle: String,
    val exportJsonAction: String,
    val importJsonAction: String,
    val preferencesTitle: String,
    val currencyLabel: String,
    val languageLabel: String,
    val themeLabel: String,
    val themeSystem: String,
    val themeLight: String,
    val themeDark: String,
    // Status messages
    val syncingNow: String,
    val saveSuccessMsg: String,
    val importSuccessMsg: String,
    val missingUrlError: String,
    val localExportSuccess: String,
    val localImportSuccess: String,
    val transactionSavedMsg: String,
    val transactionDeletedMsg: String,
    val loanSavedMsg: String,
    val loanDeletedMsg: String,
    val paymentRecordedMsg: String,
    val todayLabel: String,
    val yesterdayLabel: String,
)

val EnglishStrings = LedgerStrings(
    appTitle = "Daily Ledger",
    appSubtitle = "Income, Expense & Loans",
    navLedger = "Ledger",
    navLoans = "Loans",
    navReports = "Reports",
    navDriveSync = "Drive Sync",
    netBalance = "Net Balance",
    totalIncome = "Income",
    totalExpense = "Expense",
    netLoanPosition = "Net Loans",
    addIncome = "Income",
    addExpense = "Expense",
    addLoan = "Loan",
    driveSync = "Sync",
    filterToday = "Today",
    filterWeek = "7 Days",
    filterMonth = "This Month",
    filterAll = "All Time",
    typeAll = "All",
    typeIncome = "Income",
    typeExpense = "Expense",
    searchPlaceholder = "Search notes, categories...",
    dailyTransactions = "Transactions",
    entriesCount = "entries",
    noTransactionsTitle = "Your ledger is clean",
    noTransactionsBody = "Tap + Income or + Expense to record your first daily entry, or import from Google Drive.",
    newTransactionTitle = "New Entry",
    editTransactionTitle = "Edit Entry",
    amountLabel = "Amount",
    categoryLabel = "Category",
    paymentMethodLabel = "Payment Method",
    dateLabel = "Date (YYYY-MM-DD)",
    noteLabel = "Note / Description",
    notePlaceholder = "e.g., Groceries, Rickshaw, Salary",
    saveAction = "Save",
    updateAction = "Update",
    deleteAction = "Delete",
    cancelAction = "Cancel",
    amountError = "Enter a valid amount greater than 0",
    loansTitle = "Loans & Dues",
    youWillGet = "Receivable (Given)",
    youWillPay = "Payable (Taken)",
    loanGiven = "Given",
    loanTaken = "Taken",
    loanActive = "Active",
    loanSettled = "Settled",
    newLoanTitle = "New Loan Record",
    editLoanTitle = "Edit Loan Record",
    personNameLabel = "Person / Party Name",
    personNameError = "Please enter a name",
    phoneLabel = "Phone (Optional)",
    totalLoanAmount = "Loan Amount",
    alreadyPaidAmount = "Paid So Far",
    remainingLabel = "Remaining",
    paidLabel = "Paid",
    dueDateLabel = "Due Date (Optional, YYYY-MM-DD)",
    overdueTag = "Overdue",
    recordPayment = "Record Payment",
    markSettled = "Mark Settled",
    paymentAmountLabel = "Payment Amount",
    alsoLogTransaction = "Also record in Income/Expense ledger",
    noLoansTitle = "No loan records yet",
    noLoansBody = "Track money lent to friends or borrowed dues so nothing is ever forgotten.",
    reportsTitle = "Financial Insights",
    cashflowLast7Days = "7-Day Cashflow",
    expenseByCategory = "Expenses by Category",
    savingsRate = "Savings Rate",
    savingsHintPositive = "Healthy surplus retained from your recorded income.",
    savingsHintNegative = "Expenses exceed income in this period.",
    loanSummaryTitle = "Loan Portfolio",
    activeLoansCount = "Active Records",
    settledLoansCount = "Settled",
    noChartData = "Add income or expense entries to unlock charts.",
    syncTitle = "Google Drive Sync",
    syncSubtitle = "Daily_Income_Expense_Data.json",
    scriptUrlLabel = "Apps Script Web App URL",
    scriptUrlPlaceholder = "https://script.google.com/macros/s/.../exec",
    saveUrlAction = "Save URL",
    urlSavedMsg = "Apps Script URL saved",
    urlInvalidMsg = "URL must start with https://script.google.com/",
    connectedBadge = "URL Configured",
    notConfiguredBadge = "URL Not Set",
    pendingChangesBadge = "Unsynced Changes",
    syncedBadge = "Synced",
    lastSyncedPrefix = "Last synced:",
    neverSynced = "Not synced yet",
    saveToDriveTitle = "Save to Google Drive",
    saveToDriveDesc = "Upload all local transactions & loans directly to Drive (POST)",
    importFromDriveTitle = "Import from Google Drive",
    importFromDriveDesc = "Download Daily_Income_Expense_Data.json into this app (GET)",
    autoSyncTitle = "Auto-Save to Drive",
    autoSyncDesc = "Automatically push JSON after adding, editing, or deleting entries",
    importModeTitle = "Import Data from Drive",
    importModeBody = "Choose how to combine cloud records with your local data:",
    mergeOption = "Merge (Keep Local + Add Cloud)",
    replaceOption = "Replace All Local Data",
    appsScriptGuideTitle = "Google Apps Script Code & Guide",
    appsScriptGuideSubtitle = "Deploy your Drive JSON bridge in 4 steps",
    copyScriptAction = "Copy Script",
    scriptCopiedMsg = "Apps Script copied to clipboard",
    setupSteps = listOf(
        "1. Open script.google.com and create a New Project.",
        "2. Paste the Apps Script code below and save.",
        "3. Click Deploy -> New deployment -> Web app.",
        "4. Execute as 'Me', Who has access: 'Anyone' -> Deploy and paste the /exec URL above.",
    ),
    localBackupTitle = "Offline JSON File Backup",
    exportJsonAction = "Export .json",
    importJsonAction = "Import .json",
    preferencesTitle = "Preferences",
    currencyLabel = "Currency",
    languageLabel = "Language",
    themeLabel = "Appearance",
    themeSystem = "System",
    themeLight = "Light",
    themeDark = "Dark",
    syncingNow = "Connecting to Google Drive...",
    saveSuccessMsg = "Saved directly to Google Drive!",
    importSuccessMsg = "Imported from Google Drive!",
    missingUrlError = "Please paste & save your Apps Script Web App URL first.",
    localExportSuccess = "Exported Daily_Income_Expense_Data.json",
    localImportSuccess = "Imported records from JSON file",
    transactionSavedMsg = "Entry saved",
    transactionDeletedMsg = "Entry deleted",
    loanSavedMsg = "Loan record saved",
    loanDeletedMsg = "Loan record deleted",
    paymentRecordedMsg = "Repayment recorded",
    todayLabel = "Today",
    yesterdayLabel = "Yesterday",
)

val BengaliStrings = LedgerStrings(
    appTitle = "Daily Ledger",
    appSubtitle = "দৈনিক আয়-ব্যয় ও ঋণ হিসাব",
    navLedger = "হিসাব",
    navLoans = "ঋণ",
    navReports = "রিপোর্ট",
    navDriveSync = "ড্রাইভ সিঙ্ক",
    netBalance = "বর্তমান ব্যালেন্স",
    totalIncome = "মোট আয়",
    totalExpense = "মোট ব্যয়",
    netLoanPosition = "নিট ঋণ",
    addIncome = "আয় যোগ",
    addExpense = "ব্যয় যোগ",
    addLoan = "ঋণ যোগ",
    driveSync = "সিঙ্ক",
    filterToday = "আজ",
    filterWeek = "৭ দিন",
    filterMonth = "এই মাস",
    filterAll = "সব সময়",
    typeAll = "সব",
    typeIncome = "আয়",
    typeExpense = "ব্যয়",
    searchPlaceholder = "নোট বা ক্যাটাগরি খুঁজুন...",
    dailyTransactions = "লেনদেন তালিকা",
    entriesCount = "টি এন্ট্রি",
    noTransactionsTitle = "কোনো লেনদেন নেই",
    noTransactionsBody = "নতুন আয় বা ব্যয় যোগ করুন, অথবা Google Drive থেকে পুরনো ডাটা ইম্পোর্ট করুন।",
    newTransactionTitle = "নতুন লেনদেন",
    editTransactionTitle = "লেনদেন সম্পাদনা",
    amountLabel = "টাকার পরিমাণ",
    categoryLabel = "ক্যাটাগরি",
    paymentMethodLabel = "মাধ্যম",
    dateLabel = "তারিখ (YYYY-MM-DD)",
    noteLabel = "বিবরণ / নোট",
    notePlaceholder = "যেমন: বাজার, ভাড়া, বেতন",
    saveAction = "সেভ করুন",
    updateAction = "আপডেট",
    deleteAction = "মুছুন",
    cancelAction = "বাতিল",
    amountError = "সঠিক টাকার পরিমাণ লিখুন",
    loansTitle = "ঋণ ও বাকি হিসাব",
    youWillGet = "পাওনা (ধার দেওয়া)",
    youWillPay = "দেনা (ধার নেওয়া)",
    loanGiven = "ধার দেওয়া",
    loanTaken = "ধার নেওয়া",
    loanActive = "বাকি আছে",
    loanSettled = "পরিশোধিত",
    newLoanTitle = "নতুন ঋণ যোগ",
    editLoanTitle = "ঋণ সম্পাদনা",
    personNameLabel = "ব্যক্তির নাম",
    personNameError = "ব্যক্তির নাম লিখুন",
    phoneLabel = "মোবাইল নম্বর (ঐচ্ছিক)",
    totalLoanAmount = "মোট ঋণের পরিমাণ",
    alreadyPaidAmount = "ইতোমধ্যে পরিশোধিত",
    remainingLabel = "বাকি",
    paidLabel = "পরিশোধ",
    dueDateLabel = "পরিশোধের তারিখ (ঐচ্ছিক)",
    overdueTag = "সময় পার",
    recordPayment = "পরিশোধ যোগ",
    markSettled = "পরিশোধিত করুন",
    paymentAmountLabel = "পরিশোধের পরিমাণ",
    alsoLogTransaction = "আয়/ব্যয় হিসেবে লেনদেনেও যোগ করুন",
    noLoansTitle = "কোনো ঋণের হিসাব নেই",
    noLoansBody = "কাউকে ধার দিলে বা কারো থেকে ধার নিলে এখানে সহজেই লিখে রাখুন।",
    reportsTitle = "আর্থিক বিশ্লেষণ",
    cashflowLast7Days = "শেষ ৭ দিনের আয়-ব্যয়",
    expenseByCategory = "খাত অনুযায়ী ব্যয়",
    savingsRate = "সঞ্চয়ের হার",
    savingsHintPositive = "আপনার আয়ের তুলনায় ভালো সঞ্চয় হচ্ছে।",
    savingsHintNegative = "এই সময়ে আয়ের চেয়ে ব্যয় বেশি হয়েছে।",
    loanSummaryTitle = "ঋণের সারসংক্ষেপ",
    activeLoansCount = "চলমান ঋণ",
    settledLoansCount = "পরিশোধিত",
    noChartData = "চার্ট দেখতে আয় বা ব্যয়ের এন্ট্রি যোগ করুন।",
    syncTitle = "Google Drive সিঙ্ক",
    syncSubtitle = "Daily_Income_Expense_Data.json",
    scriptUrlLabel = "Apps Script ওয়েব অ্যাপ URL",
    scriptUrlPlaceholder = "https://script.google.com/macros/s/.../exec",
    saveUrlAction = "URL সেভ করুন",
    urlSavedMsg = "Apps Script URL সেভ হয়েছে",
    urlInvalidMsg = "URL অবশ্যই https://script.google.com/ দিয়ে শুরু হতে হবে",
    connectedBadge = "URL যুক্ত আছে",
    notConfiguredBadge = "URL সেট নেই",
    pendingChangesBadge = "অসিঙ্কড পরিবর্তন",
    syncedBadge = "সিঙ্কড",
    lastSyncedPrefix = "সর্বশেষ সিঙ্ক:",
    neverSynced = "এখনো সিঙ্ক হয়নি",
    saveToDriveTitle = "Google Drive-এ সেভ করুন",
    saveToDriveDesc = "অ্যাপ থেকে সরাসরি Google Drive-এ ডাটা সেভ করুন (POST)",
    importFromDriveTitle = "Google Drive থেকে ইম্পোর্ট",
    importFromDriveDesc = "Google Drive থেকে ডাটা অ্যাপে ইম্পোর্ট করুন (GET)",
    autoSyncTitle = "অটো-সেভ (Google Drive)",
    autoSyncDesc = "প্রতিটি পরিবর্তনের পর স্বয়ংক্রিয়ভাবে Drive-এ ব্যাকআপ হবে",
    importModeTitle = "ডাটা ইম্পোর্ট পদ্ধতি",
    importModeBody = "Drive-এর ডাটা আপনার ফোনের ডাটার সাথে কীভাবে যুক্ত করবেন?",
    mergeOption = "মার্জ করুন (ফোন + ড্রাইভ উভয়ই থাকবে)",
    replaceOption = "রিপ্লেস করুন (ড্রাইভের ডাটা দিয়ে প্রতিস্থাপন)",
    appsScriptGuideTitle = "Apps Script কোড ও সেটআপ গাইড",
    appsScriptGuideSubtitle = "৪ ধাপে Google Drive ব্যাকআপ চালু করুন",
    copyScriptAction = "কোড কপি করুন",
    scriptCopiedMsg = "Apps Script কোড কপি হয়েছে",
    setupSteps = listOf(
        "১. script.google.com ওপেন করে New Project তৈরি করুন।",
        "২. নিচের Apps Script কোড পেস্ট করে Save করুন।",
        "৩. Deploy -> New deployment -> Web app সিলেক্ট করুন।",
        "৪. Execute as 'Me', Who has access: 'Anyone' দিয়ে Deploy করে /exec লিংক উপরে দিন।",
    ),
    localBackupTitle = "অফলাইন JSON ফাইল ব্যাকআপ",
    exportJsonAction = "JSON এক্সপোর্ট",
    importJsonAction = "JSON ইম্পোর্ট",
    preferencesTitle = "সেটিংস",
    currencyLabel = "মুদ্রা",
    languageLabel = "ভাষা",
    themeLabel = "থিম",
    themeSystem = "সিস্টেম",
    themeLight = "লাইট",
    themeDark = "ডার্ক",
    syncingNow = "Google Drive-এর সাথে যুক্ত হচ্ছে...",
    saveSuccessMsg = "Google Drive-এ সফলভাবে সেভ হয়েছে!",
    importSuccessMsg = "Google Drive থেকে ডাটা ইম্পোর্ট হয়েছে!",
    missingUrlError = "প্রথমে আপনার Apps Script Web App URL পেস্ট করে সেভ করুন।",
    localExportSuccess = "Daily_Income_Expense_Data.json সেভ হয়েছে",
    localImportSuccess = "JSON ফাইল থেকে ডাটা ইম্পোর্ট হয়েছে",
    transactionSavedMsg = "লেনদেন সেভ হয়েছে",
    transactionDeletedMsg = "লেনদেন মুছে ফেলা হয়েছে",
    loanSavedMsg = "ঋণের তথ্য সেভ হয়েছে",
    loanDeletedMsg = "ঋণের তথ্য মুছে ফেলা হয়েছে",
    paymentRecordedMsg = "পরিশোধ যুক্ত হয়েছে",
    todayLabel = "আজ",
    yesterdayLabel = "গতকাল",
)

val LocalStrings = staticCompositionLocalOf { BengaliStrings }

object CategoryCatalog {
    val incomeCategories = listOf(
        "Salary" to "বেতন",
        "Business" to "ব্যবসা",
        "Freelance" to "ফ্রিল্যান্স",
        "Bonus" to "বোনাস",
        "Loan Return" to "ঋণ ফেরত",
        "Gift" to "উপহার",
        "Other" to "অন্যান্য",
    )

    val expenseCategories = listOf(
        "Food" to "খাবার ও বাজার",
        "Transport" to "যাতায়াত",
        "Bills" to "বিল ও রিচার্জ",
        "Rent" to "বাসা ভাড়া",
        "Shopping" to "কেনাকাটা",
        "Health" to "চিকিৎসা",
        "Family" to "পরিবার",
        "Education" to "পড়াশোনা",
        "Loan Paid" to "ঋণ পরিশোধ",
        "Other" to "অন্যান্য",
    )

    val paymentMethods = listOf(
        "Cash" to "নগদ",
        "bKash / Mobile" to "বিকাশ / মোবাইল",
        "Bank" to "ব্যাংক",
        "Card" to "কার্ড",
    )

    fun localizeCategory(key: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return key
        return (incomeCategories + expenseCategories)
            .firstOrNull { it.first.equals(key, ignoreCase = true) }
            ?.second ?: key
    }

    fun localizePaymentMethod(key: String, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) return key
        return paymentMethods
            .firstOrNull { it.first.equals(key, ignoreCase = true) }
            ?.second ?: key
    }
}

object LedgerFormatters {
    private val wholeFormatter = DecimalFormat("#,##0")
    private val decimalFormatter = DecimalFormat("#,##0.##")

    fun formatMoney(amount: Double, currency: String, signed: Boolean = false): String {
        val absVal = kotlin.math.abs(amount)
        val formatted = if (absVal % 1.0 == 0.0) {
            wholeFormatter.format(absVal)
        } else {
            decimalFormatter.format(absVal)
        }
        val sign = when {
            amount < 0 -> "-"
            signed && amount > 0 -> "+"
            else -> ""
        }
        return "$sign$currency$formatted"
    }

    fun todayIso(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun formatSyncTime(epochMs: Long): String {
        if (epochMs <= 0L) return ""
        return SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(epochMs))
    }

    fun friendlyDateHeader(isoDate: String, strings: LedgerStrings): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = sdf.format(Date())
        val yesterday = sdf.format(Date(System.currentTimeMillis() - 86_400_000L))
        return when (isoDate) {
            today -> "${strings.todayLabel} • $isoDate"
            yesterday -> "${strings.yesterdayLabel} • $isoDate"
            else -> runCatching {
                val parsed = sdf.parse(isoDate)
                if (parsed != null) {
                    SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(parsed)
                } else isoDate
            }.getOrDefault(isoDate)
        }
    }
}

const val USER_APPS_SCRIPT_TEMPLATE = """const FILE_NAME = 'Daily_Income_Expense_Data.json';

function getOrCreateFile() {
  const files = DriveApp.getFilesByName(FILE_NAME);
  if (files.hasNext()) {
    return files.next();
  }
  const initialData = JSON.stringify({ transactions: [], loans: [] });
  return DriveApp.createFile(FILE_NAME, initialData, MimeType.PLAIN_TEXT);
}

// Google Drive থেকে ডাটা অ্যাপে ইম্পোর্ট করার জন্য
function doGet() {
  const file = getOrCreateFile();
  const content = file.getBlob().getDataAsString();
  return ContentService.createTextOutput(content)
    .setMimeType(ContentService.MimeType.JSON);
}

// অ্যাপ থেকে সরাসরি Google Drive-এ ডাটা সেভ করার জন্য
function doPost(e) {
  const file = getOrCreateFile();
  const data = e.postData.contents;
  file.setContent(data);
  return ContentService.createTextOutput(JSON.stringify({ status: 'success' }))
    .setMimeType(ContentService.MimeType.JSON);
}"""
