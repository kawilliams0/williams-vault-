package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.BudgetAlert
import com.example.ui.FinanceUiState
import com.example.ui.FinanceViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.util.ApkExportHelper
import com.example.util.BiometricAuthManager
import com.example.util.CsvExportHelper
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent silent unhandled crashes from closing the app abruptly
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("MainActivity", "Uncaught exception caught safely: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val selectedThemePalette by viewModel.selectedThemePalette.collectAsStateWithLifecycle()
            val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
            val appPin by viewModel.appPin.collectAsStateWithLifecycle()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Request Notification Permission on Android 13+ (Tiramisu)
            val context = LocalContext.current
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* Permission result handled */ }

            LaunchedEffect(Unit) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } catch (e: Throwable) {
                    Log.e("MainActivity", "Notification permission request safe catch: ${e.message}")
                }
            }

            fun promptBiometricUnlock() {
                try {
                    if (BiometricAuthManager.isBiometricAvailable(this@MainActivity)) {
                        BiometricAuthManager.showBiometricPrompt(
                            activity = this@MainActivity,
                            title = "Unlock Williams Vault",
                            subtitle = "Verify fingerprint or face to protect your financial data",
                            negativeButtonText = "Use PIN",
                            onSuccess = {
                                viewModel.unlockWithBiometric()
                            }
                        )
                    }
                } catch (e: Throwable) {
                    Log.e("MainActivity", "Biometric unlock prompt safe catch: ${e.message}")
                }
            }

            // Automatically trigger biometric unlock on startup if app lock is active
            LaunchedEffect(isAppLocked, isBiometricEnabled) {
                if (isAppLocked && isBiometricEnabled) {
                    delay(300) // Small stabilization delay to ensure Activity FragmentManager is ready
                    promptBiometricUnlock()
                }
            }

            FinanceTheme(darkTheme = isDarkMode, paletteId = selectedThemePalette) {
                if (isAppLocked) {
                    BackHandler {
                        // Minimize app safely instead of crashing or exiting unexpectedly
                        moveTaskToBack(true)
                    }
                    PinLockScreen(
                        savedPin = appPin,
                        onUnlockSuccess = { viewModel.unlockWithBiometric() },
                        onBiometricClick = { promptBiometricUnlock() },
                        isBiometricAvailable = BiometricAuthManager.isBiometricAvailable(this@MainActivity)
                    )
                } else {
                    MainApp(
                        viewModel = viewModel,
                        isDarkMode = isDarkMode,
                        selectedThemePalette = selectedThemePalette,
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onExportTransactionsCsv = { transactions, isFiltered ->
                            exportTransactionsCsv(this, transactions, isFiltered)
                        },
                        onExportFullBackupCsv = {
                            exportFullBackupCsv(this, uiState)
                        },
                        onDownloadPdf = {
                            downloadFinancePdf(this, uiState)
                        },
                        onDownloadApk = {
                            downloadAppApk(this)
                        }
                    )
                }
            }
        }
    }

    private fun downloadAppApk(context: Context) {
        ApkExportHelper.downloadOrShareApk(context)
    }

    private fun downloadFinancePdf(context: Context, uiState: FinanceUiState) {
        try {
            val pdfFile = PdfReportGenerator.generateFinancePdf(context, uiState)
            if (pdfFile != null && pdfFile.exists()) {
                Toast.makeText(context, "PDF Statement generated successfully!", Toast.LENGTH_SHORT).show()
                PdfReportGenerator.openOrSharePdf(context, pdfFile)
            } else {
                Toast.makeText(context, "Could not generate PDF statement", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Throwable) {
            Toast.makeText(context, "PDF error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportTransactionsCsv(
        context: Context,
        transactions: List<TransactionEntity>,
        isFiltered: Boolean = false
    ) {
        try {
            val prefix = if (isFiltered) "filtered_transactions" else "transactions_backup"
            val csvFile = CsvExportHelper.generateTransactionsCsv(context, transactions, prefix)
            if (csvFile != null && csvFile.exists()) {
                val countMsg = "${transactions.size} transactions exported to CSV"
                Toast.makeText(context, countMsg, Toast.LENGTH_SHORT).show()
                CsvExportHelper.openOrShareCsv(context, csvFile, "Save or Share Transactions CSV")
            } else {
                Toast.makeText(context, "Could not generate CSV file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Throwable) {
            Toast.makeText(context, "CSV export error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportFullBackupCsv(context: Context, uiState: FinanceUiState) {
        try {
            val csvFile = CsvExportHelper.generateFullBackupCsv(context, uiState)
            if (csvFile != null && csvFile.exists()) {
                Toast.makeText(context, "Full financial backup CSV generated!", Toast.LENGTH_SHORT).show()
                CsvExportHelper.openOrShareCsv(context, csvFile, "Save or Share Full Financial Backup")
            } else {
                Toast.makeText(context, "Could not generate full backup CSV", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Throwable) {
            Toast.makeText(context, "Backup export error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

sealed class DeleteConfirmationTarget {
    data class Account(val account: AccountEntity) : DeleteConfirmationTarget()
    data class Transaction(val transaction: TransactionEntity) : DeleteConfirmationTarget()
    data class Budget(val budget: BudgetEntity) : DeleteConfirmationTarget()
    data class Goal(val goal: SavingsGoalEntity) : DeleteConfirmationTarget()
    data class Bill(val bill: RecurringBillEntity) : DeleteConfirmationTarget()
    data class Category(val category: CategoryEntity) : DeleteConfirmationTarget()
}

@Composable
fun MainApp(
    viewModel: FinanceViewModel,
    isDarkMode: Boolean,
    selectedThemePalette: String,
    onToggleDarkMode: () -> Unit,
    onExportTransactionsCsv: (List<TransactionEntity>, Boolean) -> Unit,
    onExportFullBackupCsv: () -> Unit,
    onDownloadPdf: () -> Unit,
    onDownloadApk: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val isPinEnabled by viewModel.isPinEnabled.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val appPin by viewModel.appPin.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog & Sheet States
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var initialTransactionType by remember { mutableStateOf("EXPENSE") }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetEntity?>(null) }
    var activeBudgetAlert by remember { mutableStateOf<BudgetAlert?>(null) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var depositGoalTarget by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var showCategoryManagementSheet by remember { mutableStateOf(false) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var showSetMonthlyCapDialog by remember { mutableStateOf(false) }
    var showQuickLogTwoClickModal by remember { mutableStateOf(false) }
    var showExportOptionsModal by remember { mutableStateOf(false) }
    var showSecuritySettingsDialog by remember { mutableStateOf(false) }
    var popFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // Listen to real-time budget exceeded notification events
    LaunchedEffect(viewModel) {
        viewModel.budgetAlertEvent.collect { alert ->
            activeBudgetAlert = alert
        }
    }

    // Deletion confirmation state
    var itemToDelete by remember { mutableStateOf<DeleteConfirmationTarget?>(null) }

    val isAnyModalOrSubtabActive = showAddTransactionSheet ||
            showCategoryManagementSheet ||
            showQuickLogTwoClickModal ||
            showExportOptionsModal ||
            showSecuritySettingsDialog ||
            showAddAccountDialog ||
            showAddBudgetDialog ||
            editingBudget != null ||
            activeBudgetAlert != null ||
            showAddGoalDialog ||
            depositGoalTarget != null ||
            showAddBillDialog ||
            showCreateCategoryDialog ||
            showSetMonthlyCapDialog ||
            itemToDelete != null ||
            selectedTab != 0

    BackHandler(enabled = isAnyModalOrSubtabActive) {
        when {
            showAddTransactionSheet -> showAddTransactionSheet = false
            showCategoryManagementSheet -> showCategoryManagementSheet = false
            showQuickLogTwoClickModal -> showQuickLogTwoClickModal = false
            showExportOptionsModal -> showExportOptionsModal = false
            showSecuritySettingsDialog -> showSecuritySettingsDialog = false
            showAddAccountDialog -> showAddAccountDialog = false
            showAddBudgetDialog -> showAddBudgetDialog = false
            editingBudget != null -> editingBudget = null
            activeBudgetAlert != null -> activeBudgetAlert = null
            showAddGoalDialog -> showAddGoalDialog = false
            depositGoalTarget != null -> depositGoalTarget = null
            showAddBillDialog -> showAddBillDialog = false
            showCreateCategoryDialog -> showCreateCategoryDialog = false
            showSetMonthlyCapDialog -> showSetMonthlyCapDialog = false
            itemToDelete != null -> itemToDelete = null
            selectedTab != 0 -> selectedTab = 0
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("main_scaffold"),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.testTag("main_snackbar_host")
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = DarkSurfaceElevated,
                    contentColor = TextPrimary,
                    actionColor = LilacPrimary,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    initialTransactionType = "EXPENSE"
                    showAddTransactionSheet = true
                },
                shape = CircleShape,
                containerColor = LilacPrimary,
                contentColor = LilacOnPrimary,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .bouncyClickable(pressedScale = 0.88f) {
                        initialTransactionType = "EXPENSE"
                        showAddTransactionSheet = true
                    }
                    .testTag("main_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(26.dp))
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                containerColor = DarkSurface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            if (selectedTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Overview"
                        )
                    },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LilacActivePillContent,
                        selectedTextColor = TextPrimary,
                        indicatorColor = LilacActivePill,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_item_overview")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            if (selectedTab == 1) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                            contentDescription = "Transactions"
                        )
                    },
                    label = { Text("Activity") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LilacActivePillContent,
                        selectedTextColor = TextPrimary,
                        indicatorColor = LilacActivePill,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_item_transactions")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            if (selectedTab == 2) Icons.Filled.PieChart else Icons.Outlined.PieChart,
                            contentDescription = "Budgets"
                        )
                    },
                    label = { Text("Budgets") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LilacActivePillContent,
                        selectedTextColor = TextPrimary,
                        indicatorColor = LilacActivePill,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_item_budgets")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            if (selectedTab == 3) Icons.Filled.AutoGraph else Icons.Outlined.AutoGraph,
                            contentDescription = "Insights"
                        )
                    },
                    label = { Text("Stats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LilacActivePillContent,
                        selectedTextColor = TextPrimary,
                        indicatorColor = LilacActivePill,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_item_insights")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { width -> width / 3 } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { width -> -width / 3 } + fadeOut(tween(180)))
                    } else {
                        (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { width -> -width / 3 } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { width -> width / 3 } + fadeOut(tween(180)))
                    }
                },
                label = "screen_tab_transition"
            ) { tab ->
                when (tab) {
                    0 -> DashboardScreen(
                        uiState = uiState,
                        isDarkMode = isDarkMode,
                        isPinEnabled = isPinEnabled,
                        onToggleDarkMode = onToggleDarkMode,
                        onOpenSettings = { showSecuritySettingsDialog = true },
                        onLockApp = {
                            if (isPinEnabled) {
                                viewModel.lockApp()
                            } else {
                                showSecuritySettingsDialog = true
                            }
                        },
                        onExportData = { showExportOptionsModal = true },
                        onEditMonthlyCap = { showSetMonthlyCapDialog = true },
                        onQuickLog = { amount, title, category ->
                            viewModel.quickLogExpense(amount, title, category)
                            popFeedbackMessage = "Logged ${FinanceFormatters.formatCurrency(amount)} for $title"
                        },
                        onOpenQuickLogModal = { showQuickLogTwoClickModal = true },
                        onNavigateToTransactions = { selectedTab = 1 },
                        onNavigateToGoals = { selectedTab = 2 },
                        onAddGoal = { showAddGoalDialog = true },
                        onDepositGoal = { depositGoalTarget = it },
                        onDeleteGoal = { itemToDelete = DeleteConfirmationTarget.Goal(it) },
                        onAddExpense = {
                            initialTransactionType = "EXPENSE"
                            showAddTransactionSheet = true
                        },
                        onAddIncome = {
                            initialTransactionType = "INCOME"
                            showAddTransactionSheet = true
                        },
                        onAddAccount = { showAddAccountDialog = true },
                        onDeleteAccount = { itemToDelete = DeleteConfirmationTarget.Account(it) },
                        onDeleteTransaction = { itemToDelete = DeleteConfirmationTarget.Transaction(it) }
                    )
                    1 -> TransactionsScreen(
                        viewModel = viewModel,
                        categories = uiState.categories,
                        filteredTransactions = filteredTransactions,
                        onAddTransaction = {
                            initialTransactionType = "EXPENSE"
                            showAddTransactionSheet = true
                        },
                        onManageCategories = { showCategoryManagementSheet = true },
                        onDeleteTransaction = { itemToDelete = DeleteConfirmationTarget.Transaction(it) },
                        onDownloadPdf = onDownloadPdf,
                        onExportCsv = { onExportTransactionsCsv(uiState.transactions, false) },
                        onExportFilteredCsv = { onExportTransactionsCsv(it, true) }
                    )
                    2 -> BudgetsAndGoalsScreen(
                        uiState = uiState,
                        onAddBudget = { showAddBudgetDialog = true },
                        onDeleteBudget = { itemToDelete = DeleteConfirmationTarget.Budget(it) },
                        onEditBudget = { editingBudget = it },
                        onAddGoal = { showAddGoalDialog = true },
                        onDepositGoal = { depositGoalTarget = it },
                        onDeleteGoal = { itemToDelete = DeleteConfirmationTarget.Goal(it) }
                    )
                    3 -> AnalyticsAndBillsScreen(
                        uiState = uiState,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = onToggleDarkMode,
                        onAddBill = { showAddBillDialog = true },
                        onToggleBillPaid = {
                            viewModel.toggleBillPaid(it)
                            val status = if (!it.isPaidThisMonth) "Marked as Paid!" else "Marked as Unpaid"
                            popFeedbackMessage = "${it.title}: $status"
                        },
                        onDeleteBill = { itemToDelete = DeleteConfirmationTarget.Bill(it) }
                    )
                }
            }

            // Quick Micro-interaction Toast Popup
            popFeedbackMessage?.let { msg ->
                QuickActionPopFeedback(
                    visible = true,
                    text = msg,
                    onFinished = { popFeedbackMessage = null }
                )
            }
        }

        // --- Bottom Sheet & Dialog Modals ---

        if (showSetMonthlyCapDialog) {
            SetMonthlyCapDialog(
                currentCap = uiState.overallMonthlyBudgetCap,
                onDismiss = { showSetMonthlyCapDialog = false },
                onConfirm = { newCap ->
                    viewModel.setOverallMonthlyBudget(newCap)
                    showSetMonthlyCapDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Monthly spending cap set to ${FinanceFormatters.formatCurrency(newCap)}.")
                    }
                }
            )
        }

        if (showQuickLogTwoClickModal) {
            QuickLogTwoClickModal(
                categories = uiState.categories,
                onDismiss = { showQuickLogTwoClickModal = false },
                onLogExpense = { amount, title, category ->
                    viewModel.quickLogExpense(amount, title, category)
                    scope.launch {
                        snackbarHostState.showSnackbar("Logged ${FinanceFormatters.formatCurrency(amount)} for $title.")
                    }
                }
            )
        }

        if (showAddTransactionSheet) {
            AddTransactionBottomSheet(
                accounts = uiState.accounts,
                categories = uiState.categories,
                initialType = initialTransactionType,
                onDismiss = { showAddTransactionSheet = false },
                onAddNewCategory = { showCreateCategoryDialog = true },
                onSave = { title, amount, type, category, accountId, accountName, dateMillis, note ->
                    viewModel.addTransaction(
                        title = title,
                        amount = amount,
                        type = type,
                        category = category,
                        accountId = accountId,
                        accountName = accountName,
                        dateMillis = dateMillis,
                        note = note
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar("Transaction \"$title\" recorded in $category.")
                    }
                }
            )
        }

        if (showCategoryManagementSheet) {
            CategoryManagementSheet(
                categories = uiState.categories,
                onDismiss = { showCategoryManagementSheet = false },
                onAddNewCategory = { showCreateCategoryDialog = true },
                onDeleteCategory = { itemToDelete = DeleteConfirmationTarget.Category(it) }
            )
        }

        if (showExportOptionsModal) {
            ExportOptionsModal(
                onDismiss = { showExportOptionsModal = false },
                onDownloadPdf = onDownloadPdf,
                onExportTransactionsCsv = { onExportTransactionsCsv(uiState.transactions, false) },
                onExportFullBackupCsv = onExportFullBackupCsv,
                onDownloadApk = onDownloadApk
            )
        }

        if (showSecuritySettingsDialog) {
            SecuritySettingsDialog(
                isPinEnabled = isPinEnabled,
                currentPin = appPin,
                isBiometricEnabled = isBiometricEnabled,
                onToggleBiometric = { enabled ->
                    viewModel.setBiometricEnabled(enabled)
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                selectedThemePalette = selectedThemePalette,
                onSelectThemePalette = { paletteId ->
                    viewModel.setThemePalette(paletteId)
                },
                onResetData = {
                    viewModel.resetToFreshStart()
                    scope.launch {
                        snackbarHostState.showSnackbar("All data cleared. Started fresh with ₹0.")
                    }
                },
                onDownloadApk = onDownloadApk,
                onDismiss = { showSecuritySettingsDialog = false },
                onSetPin = { newPin ->
                    viewModel.setPin(newPin)
                    scope.launch {
                        snackbarHostState.showSnackbar("4-digit PIN security activated!")
                    }
                },
                onDisablePin = {
                    viewModel.disablePin()
                    scope.launch {
                        snackbarHostState.showSnackbar("PIN protection disabled.")
                    }
                },
                onLockNow = {
                    viewModel.lockApp()
                }
            )
        }

        if (showCreateCategoryDialog) {
            CreateCategoryDialog(
                initialType = initialTransactionType,
                onDismiss = { showCreateCategoryDialog = false },
                onSave = { name, type, iconKey, colorHex ->
                    viewModel.addCategory(name, type, iconKey, colorHex)
                    scope.launch {
                        snackbarHostState.showSnackbar("Category \"$name\" created.")
                    }
                }
            )
        }

        if (showAddAccountDialog) {
            AddAccountDialog(
                onDismiss = { showAddAccountDialog = false },
                onSave = { name, type, balance, last4, colorHex ->
                    viewModel.addAccount(name, type, balance, last4, colorHex)
                    scope.launch {
                        snackbarHostState.showSnackbar("Account \"$name\" created.")
                    }
                }
            )
        }

        if (showAddBudgetDialog) {
            AddBudgetDialog(
                categories = uiState.categories,
                onDismiss = { showAddBudgetDialog = false },
                onSave = { category, limit ->
                    viewModel.addBudget(category, limit)
                    scope.launch {
                        snackbarHostState.showSnackbar("Budget set for $category.")
                    }
                }
            )
        }

        editingBudget?.let { budget ->
            AddBudgetDialog(
                categories = uiState.categories,
                initialCategory = budget.category,
                initialLimit = budget.monthlyLimit,
                onDismiss = { editingBudget = null },
                onSave = { category, limit ->
                    viewModel.updateBudget(budget.copy(category = category, monthlyLimit = limit))
                    editingBudget = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Budget updated for $category (${FinanceFormatters.formatCurrency(limit)}).")
                    }
                }
            )
        }

        activeBudgetAlert?.let { alert ->
            BudgetExceededDialog(
                alert = alert,
                onDismiss = { activeBudgetAlert = null },
                onAdjustBudget = {
                    val existingBudget = uiState.budgets.find { it.category.equals(alert.category, ignoreCase = true) }
                    if (existingBudget != null) {
                        editingBudget = existingBudget
                    } else {
                        showAddBudgetDialog = true
                    }
                }
            )
        }

        if (showAddGoalDialog) {
            AddSavingsGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onSave = { title, target, current, targetDate, colorHex ->
                    viewModel.addSavingsGoal(title, target, current, targetDate, colorHex)
                    scope.launch {
                        snackbarHostState.showSnackbar("Savings goal \"$title\" created.")
                    }
                }
            )
        }

        depositGoalTarget?.let { goal ->
            DepositGoalDialog(
                goal = goal,
                onDismiss = { depositGoalTarget = null },
                onDeposit = { amount ->
                    viewModel.depositToGoal(goal.id, amount)
                    depositGoalTarget = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Deposited ${FinanceFormatters.formatCurrency(amount)} into ${goal.title}.")
                    }
                }
            )
        }

        if (showAddBillDialog) {
            AddRecurringBillDialog(
                categories = uiState.categories,
                onDismiss = { showAddBillDialog = false },
                onSave = { title, amount, category, dueDay ->
                    viewModel.addRecurringBill(title, amount, category, dueDay)
                    scope.launch {
                        snackbarHostState.showSnackbar("Subscription \"$title\" added.")
                    }
                }
            )
        }

        // Deletion Confirmation Dialog
        itemToDelete?.let { target ->
            val (dialogTitle, dialogMessage, confirmAction) = when (target) {
                is DeleteConfirmationTarget.Account -> Triple(
                    "Delete Account?",
                    "Are you sure you want to delete \"${target.account.name}\"? This action cannot be undone.",
                    { viewModel.deleteAccount(target.account) }
                )
                is DeleteConfirmationTarget.Transaction -> Triple(
                    "Delete Transaction?",
                    "Are you sure you want to delete \"${target.transaction.title}\" (${FinanceFormatters.formatCurrency(target.transaction.amount)})?",
                    { viewModel.deleteTransaction(target.transaction) }
                )
                is DeleteConfirmationTarget.Budget -> Triple(
                    "Delete Budget?",
                    "Are you sure you want to remove the budget for \"${target.budget.category}\"?",
                    { viewModel.deleteBudget(target.budget) }
                )
                is DeleteConfirmationTarget.Goal -> Triple(
                    "Delete Savings Goal?",
                    "Are you sure you want to delete \"${target.goal.title}\"?",
                    { viewModel.deleteSavingsGoal(target.goal) }
                )
                is DeleteConfirmationTarget.Bill -> Triple(
                    "Delete Subscription?",
                    "Are you sure you want to remove \"${target.bill.title}\"?",
                    { viewModel.deleteRecurringBill(target.bill) }
                )
                is DeleteConfirmationTarget.Category -> Triple(
                    "Delete Category?",
                    "Are you sure you want to delete custom category \"${target.category.name}\"?",
                    { viewModel.deleteCategory(target.category) }
                )
            }

            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                containerColor = DarkSurface,
                title = { Text(dialogTitle, fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = { Text(dialogMessage, color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            confirmAction()
                            itemToDelete = null
                            scope.launch {
                                snackbarHostState.showSnackbar("Item successfully deleted.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralRed,
                            contentColor = CoralRedContainer
                        )
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
