package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.FinanceDatabase
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import com.example.ui.components.FinanceFormatters
import com.example.util.BudgetNotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MonthlyCashflow(
    val monthLabel: String,
    val income: Double,
    val expense: Double
)

data class BudgetHealth(
    val category: String,
    val spent: Double,
    val limit: Double,
    val percentage: Float,
    val remaining: Double
)

data class CategorySpend(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class BudgetAlert(
    val category: String,
    val spent: Double,
    val limit: Double,
    val excess: Double,
    val percentage: Float,
    val isSevere: Boolean = true
)

data class FinanceUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val savingsGoals: List<SavingsGoalEntity> = emptyList(),
    val recurringBills: List<RecurringBillEntity> = emptyList(),
    val totalNetWorth: Double = 0.0,
    val totalIncomeThisMonth: Double = 0.0,
    val totalExpenseThisMonth: Double = 0.0,
    val netCashflowThisMonth: Double = 0.0,
    val savingsRateThisMonth: Double = 0.0,
    val overallMonthlyBudgetCap: Double = 35000.0,
    val remainingMonthlyBudget: Double = 0.0,
    val budgetCapPercentage: Float = 0.0f,
    val categorySpends: List<CategorySpend> = emptyList(),
    val budgetHealthList: List<BudgetHealth> = emptyList(),
    val budgetAlerts: List<BudgetAlert> = emptyList(),
    val monthlyCashflows: List<MonthlyCashflow> = emptyList(),
    val unpaidBillsTotal: Double = 0.0,
    val upcomingBillsCount: Int = 0,
    val daysUntilReset: Int = 1,
    val isLoading: Boolean = false
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val prefs = application.getSharedPreferences("finance_tracker_prefs", Context.MODE_PRIVATE)

    // Notification Channel setup
    init {
        BudgetNotificationHelper.createNotificationChannel(application)
        val database = FinanceDatabase.getDatabase(application, viewModelScope)
        repository = FinanceRepository(database.financeDao())
    }

    // Real-Time Budget Alert Event Flow (for in-app popups & snackbars)
    private val _budgetAlertEvent = MutableSharedFlow<BudgetAlert>(extraBufferCapacity = 10)
    val budgetAlertEvent: SharedFlow<BudgetAlert> = _budgetAlertEvent.asSharedFlow()

    // Overall Monthly Budget Cap State (Single overall limit)
    val monthlyBudgetCap = MutableStateFlow(prefs.getFloat("monthly_budget_cap", 35000f).toDouble())

    // Theme & Color Palette Selection
    val selectedThemePalette = MutableStateFlow(prefs.getString("theme_palette", "NAVY_SKY") ?: "NAVY_SKY")
    val isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", true))

    // Security & Preferences (100% Offline)
    val isPinEnabled = MutableStateFlow(prefs.getBoolean("is_pin_enabled", false))
    val isBiometricEnabled = MutableStateFlow(prefs.getBoolean("is_biometric_enabled", true))
    val isAppLocked = MutableStateFlow(prefs.getBoolean("is_pin_enabled", false))
    val appPin = MutableStateFlow(prefs.getString("app_pin", "") ?: "")

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedTypeFilter = MutableStateFlow("ALL") // "ALL", "EXPENSE", "INCOME"
    val selectedCategoryFilter = MutableStateFlow("ALL")
    val selectedTimeRangeFilter = MutableStateFlow("THIS_MONTH") // "THIS_MONTH", "THIS_WEEK", "LAST_30_DAYS", "LAST_3_MONTHS", "THIS_YEAR", "ALL_TIME", "CUSTOM"
    val minAmountFilter = MutableStateFlow<Double?>(null)
    val maxAmountFilter = MutableStateFlow<Double?>(null)
    val selectedAmountPreset = MutableStateFlow("ALL") // "ALL", "UNDER_500", "500_2000", "2000_10000", "OVER_10000", "CUSTOM"
    val customStartDateMillis = MutableStateFlow<Long?>(null)
    val customEndDateMillis = MutableStateFlow<Long?>(null)

    // UI state combining all flows
    val uiState: StateFlow<FinanceUiState> = combine(
        combine(repository.allCategories, repository.allTransactions, repository.allAccounts) { categories, transactions, accounts ->
            Triple(categories, transactions, accounts)
        },
        combine(repository.allBudgets, repository.allSavingsGoals, repository.allRecurringBills) { budgets, goals, bills ->
            Triple(budgets, goals, bills)
        },
        monthlyBudgetCap
    ) { (categories, transactions, accounts), (budgets, goals, bills), cap ->
        val effectiveCategories = if (categories.isNotEmpty()) categories else (FinanceCategories.defaultExpenseCategories + FinanceCategories.defaultIncomeCategories)
        computeUiState(effectiveCategories, transactions, accounts, budgets, goals, bills, cap)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanceUiState(isLoading = true)
    )

    // Filtered transactions for the Transactions tab & History Log with keyword, date, and amount filters
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        repository.allTransactions,
        combine(searchQuery, selectedTypeFilter, selectedCategoryFilter, selectedTimeRangeFilter) { q, t, c, tr ->
            listOf(q, t, c, tr)
        },
        combine(minAmountFilter, maxAmountFilter, customStartDateMillis, customEndDateMillis) { minA, maxA, startD, endD ->
            listOf(minA, maxA, startD, endD)
        }
    ) { transactions, textAndRanges, amountsAndDates ->
        val query = (textAndRanges[0] as? String) ?: ""
        val typeFilter = (textAndRanges[1] as? String) ?: "ALL"
        val catFilter = (textAndRanges[2] as? String) ?: "ALL"
        val timeFilter = (textAndRanges[3] as? String) ?: "THIS_MONTH"

        val minAmount = amountsAndDates[0] as? Double
        val maxAmount = amountsAndDates[1] as? Double
        val customStart = amountsAndDates[2] as? Long
        val customEnd = amountsAndDates[3] as? Long

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        val (startTimestamp, endTimestamp) = when (timeFilter) {
            "THIS_MONTH" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to Long.MAX_VALUE
            }
            "THIS_WEEK" -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to Long.MAX_VALUE
            }
            "LAST_30_DAYS" -> (now - (30L * 24 * 60 * 60 * 1000)) to Long.MAX_VALUE
            "LAST_3_MONTHS" -> {
                cal.add(Calendar.MONTH, -3)
                cal.timeInMillis to Long.MAX_VALUE
            }
            "THIS_YEAR" -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to Long.MAX_VALUE
            }
            "CUSTOM" -> {
                val start = customStart?.let { getStartOfDay(it) } ?: 0L
                val end = customEnd?.let { getEndOfDay(it) } ?: Long.MAX_VALUE
                start to end
            }
            else -> 0L to Long.MAX_VALUE // ALL_TIME
        }

        val dateFullFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
        val dateMonthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val dateShortMonthFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
        val dateDayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
        val dateIsoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateSlashFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val dateYearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

        val trimmedQuery = query.trim()

        // Check for smart query operators like "> 500", "< 1000", "500-1000"
        val isGreaterQuery = trimmedQuery.startsWith(">")
        val isLesserQuery = trimmedQuery.startsWith("<")
        val isRangeQuery = (trimmedQuery.contains("-") || trimmedQuery.contains("..")) && !trimmedQuery.contains(Regex("[a-zA-Z]"))

        val parsedMinFromQuery = if (isGreaterQuery) {
            trimmedQuery.removePrefix(">").replace("₹", "").replace(",", "").trim().toDoubleOrNull()
        } else null

        val parsedMaxFromQuery = if (isLesserQuery) {
            trimmedQuery.removePrefix("<").replace("₹", "").replace(",", "").trim().toDoubleOrNull()
        } else null

        val parsedRangeFromQuery = if (isRangeQuery) {
            val delimiter = if (trimmedQuery.contains("..")) ".." else "-"
            val parts = trimmedQuery.split(delimiter).map { it.replace("₹", "").replace(",", "").trim().toDoubleOrNull() }
            if (parts.size == 2 && parts[0] != null && parts[1] != null) {
                parts[0]!! to parts[1]!!
            } else null
        } else null

        transactions.filter { tx ->
            // 1. Search Query / Keyword / Date / Amount matching
            val matchesSearch = if (trimmedQuery.isBlank()) {
                true
            } else if (parsedMinFromQuery != null) {
                tx.amount > parsedMinFromQuery
            } else if (parsedMaxFromQuery != null) {
                tx.amount < parsedMaxFromQuery
            } else if (parsedRangeFromQuery != null) {
                tx.amount >= minOf(parsedRangeFromQuery.first, parsedRangeFromQuery.second) &&
                tx.amount <= maxOf(parsedRangeFromQuery.first, parsedRangeFromQuery.second)
            } else {
                val date = Date(tx.dateMillis)
                val fullDate = dateFullFormat.format(date)
                val monthName = dateMonthFormat.format(date)
                val shortMonthName = dateShortMonthFormat.format(date)
                val dayName = dateDayFormat.format(date)
                val isoDate = dateIsoFormat.format(date)
                val slashDate = dateSlashFormat.format(date)
                val year = dateYearFormat.format(date)

                val amountStr = String.format(Locale.ENGLISH, "%.2f", tx.amount)
                val integerAmountStr = tx.amount.toLong().toString()

                tx.title.contains(trimmedQuery, ignoreCase = true) ||
                tx.category.contains(trimmedQuery, ignoreCase = true) ||
                tx.note.contains(trimmedQuery, ignoreCase = true) ||
                tx.accountName.contains(trimmedQuery, ignoreCase = true) ||
                tx.type.contains(trimmedQuery, ignoreCase = true) ||
                fullDate.contains(trimmedQuery, ignoreCase = true) ||
                monthName.contains(trimmedQuery, ignoreCase = true) ||
                shortMonthName.contains(trimmedQuery, ignoreCase = true) ||
                dayName.contains(trimmedQuery, ignoreCase = true) ||
                isoDate.contains(trimmedQuery, ignoreCase = true) ||
                slashDate.contains(trimmedQuery, ignoreCase = true) ||
                year.contains(trimmedQuery, ignoreCase = true) ||
                amountStr.contains(trimmedQuery) ||
                integerAmountStr == trimmedQuery.replace("₹", "").replace(",", "").trim()
            }

            // 2. Type matching
            val matchesType = when (typeFilter) {
                "EXPENSE" -> tx.type == "EXPENSE"
                "INCOME" -> tx.type == "INCOME"
                else -> true
            }

            // 3. Category matching
            val matchesCategory = catFilter == "ALL" || tx.category.equals(catFilter, ignoreCase = true)

            // 4. Date Range matching
            val matchesTime = tx.dateMillis in startTimestamp..endTimestamp

            // 5. Amount bounds matching
            val matchesMinAmount = minAmount == null || tx.amount >= minAmount
            val matchesMaxAmount = maxAmount == null || tx.amount <= maxAmount

            matchesSearch && matchesType && matchesCategory && matchesTime && matchesMinAmount && matchesMaxAmount
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setAmountPreset(preset: String) {
        selectedAmountPreset.value = preset
        when (preset) {
            "UNDER_500" -> {
                minAmountFilter.value = null
                maxAmountFilter.value = 500.0
            }
            "500_2000" -> {
                minAmountFilter.value = 500.0
                maxAmountFilter.value = 2000.0
            }
            "2000_10000" -> {
                minAmountFilter.value = 2000.0
                maxAmountFilter.value = 10000.0
            }
            "OVER_10000" -> {
                minAmountFilter.value = 10000.0
                maxAmountFilter.value = null
            }
            "ALL" -> {
                minAmountFilter.value = null
                maxAmountFilter.value = null
            }
        }
    }

    fun setCustomAmountBounds(min: Double?, max: Double?) {
        minAmountFilter.value = min
        maxAmountFilter.value = max
        selectedAmountPreset.value = if (min == null && max == null) "ALL" else "CUSTOM"
    }

    fun setCustomDateRange(startMillis: Long?, endMillis: Long?) {
        customStartDateMillis.value = startMillis
        customEndDateMillis.value = endMillis
        selectedTimeRangeFilter.value = "CUSTOM"
    }

    fun setSingleDateFilter(dateMillis: Long) {
        customStartDateMillis.value = dateMillis
        customEndDateMillis.value = dateMillis
        selectedTimeRangeFilter.value = "CUSTOM"
    }

    fun clearAllFilters() {
        searchQuery.value = ""
        selectedTypeFilter.value = "ALL"
        selectedCategoryFilter.value = "ALL"
        selectedTimeRangeFilter.value = "THIS_MONTH"
        minAmountFilter.value = null
        maxAmountFilter.value = null
        selectedAmountPreset.value = "ALL"
        customStartDateMillis.value = null
        customEndDateMillis.value = null
    }

    fun getActiveFilterCount(): Int {
        var count = 0
        if (searchQuery.value.isNotBlank()) count++
        if (selectedTypeFilter.value != "ALL") count++
        if (selectedCategoryFilter.value != "ALL") count++
        if (selectedTimeRangeFilter.value != "THIS_MONTH") count++
        if (minAmountFilter.value != null || maxAmountFilter.value != null || selectedAmountPreset.value != "ALL") count++
        return count
    }

    private fun getStartOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun computeUiState(
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>,
        accounts: List<AccountEntity>,
        budgets: List<BudgetEntity>,
        goals: List<SavingsGoalEntity>,
        bills: List<RecurringBillEntity>,
        cap: Double
    ): FinanceUiState {
        // Calculate Total Net Worth (Checking + Savings + Cash + Investments - Credit Card debt)
        var netWorth = 0.0
        for (acc in accounts) {
            if (acc.type == "CREDIT_CARD") {
                netWorth -= acc.balance
            } else {
                netWorth += acc.balance
            }
        }

        // Current Month Range (Monthly Reset on 1st)
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val monthStart = cal.timeInMillis

        val monthTransactions = transactions.filter { it.dateMillis >= monthStart }

        val incomeThisMonth = monthTransactions
            .filter { it.type == "INCOME" }
            .sumOf { it.amount }

        val expenseThisMonth = monthTransactions
            .filter { it.type == "EXPENSE" }
            .sumOf { it.amount }

        val netCashflow = incomeThisMonth - expenseThisMonth
        val savingsRate = if (incomeThisMonth > 0) {
            ((incomeThisMonth - expenseThisMonth) / incomeThisMonth * 100.0).coerceIn(0.0, 100.0)
        } else 0.0

        // Single Overall Monthly Budget Progress
        val remainingBudget = (cap - expenseThisMonth).coerceAtLeast(0.0)
        val budgetPercentage = if (cap > 0) (expenseThisMonth / cap).toFloat() else 0f

        // Category breakdown for expenses this month
        val categoryExpenses = monthTransactions
            .filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }

        val totalExpensesForBreakdown = if (expenseThisMonth > 0) expenseThisMonth else 1.0
        val categorySpends = categoryExpenses.map { (cat, amt) ->
            CategorySpend(
                category = cat,
                amount = amt,
                percentage = (amt / totalExpensesForBreakdown).toFloat()
            )
        }.sortedByDescending { it.amount }

        // Category Budgets Health & Alerts
        val budgetAlerts = mutableListOf<BudgetAlert>()
        val budgetHealthList = budgets.map { budget ->
            val spent = categoryExpenses[budget.category] ?: 0.0
            val pct = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
            if (pct >= 1.0f && budget.monthlyLimit > 0) {
                budgetAlerts.add(
                    BudgetAlert(
                        category = budget.category,
                        spent = spent,
                        limit = budget.monthlyLimit,
                        excess = spent - budget.monthlyLimit,
                        percentage = pct,
                        isSevere = true
                    )
                )
            }
            BudgetHealth(
                category = budget.category,
                spent = spent,
                limit = budget.monthlyLimit,
                percentage = pct,
                remaining = (budget.monthlyLimit - spent).coerceAtLeast(0.0)
            )
        }

        // Monthly cashflows for historical trend
        val monthlyCashflows = mutableListOf<MonthlyCashflow>()
        val monthFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
        for (i in 5 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -i)
            c.set(Calendar.DAY_OF_MONTH, 1)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            val start = c.timeInMillis

            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            c.set(Calendar.HOUR_OF_DAY, 23)
            c.set(Calendar.MINUTE, 59)
            c.set(Calendar.SECOND, 59)
            val end = c.timeInMillis

            val monthTxs = transactions.filter { it.dateMillis in start..end }
            val inc = monthTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
            val exp = monthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val label = monthFormat.format(Date(start))
            monthlyCashflows.add(MonthlyCashflow(label, inc, exp))
        }

        // Recurring bills metrics
        val unpaidBills = bills.filter { !it.isPaidThisMonth }
        val unpaidBillsTotal = unpaidBills.sumOf { it.amount }

        return FinanceUiState(
            categories = categories,
            transactions = transactions,
            accounts = accounts,
            budgets = budgets,
            savingsGoals = goals,
            recurringBills = bills,
            totalNetWorth = netWorth,
            totalIncomeThisMonth = incomeThisMonth,
            totalExpenseThisMonth = expenseThisMonth,
            netCashflowThisMonth = netCashflow,
            savingsRateThisMonth = savingsRate,
            overallMonthlyBudgetCap = cap,
            remainingMonthlyBudget = remainingBudget,
            budgetCapPercentage = budgetPercentage,
            categorySpends = categorySpends,
            budgetHealthList = budgetHealthList,
            budgetAlerts = budgetAlerts,
            monthlyCashflows = monthlyCashflows,
            unpaidBillsTotal = unpaidBillsTotal,
            upcomingBillsCount = unpaidBills.size,
            daysUntilReset = FinanceFormatters.getDaysUntilMonthReset(),
            isLoading = false
        )
    }

    // --- Action Handlers ---

    private fun checkAndNotifyBudget(category: String, addedAmount: Double) {
        viewModelScope.launch {
            val currentBudgets = repository.allBudgets.firstOrNull() ?: emptyList()
            val budget = currentBudgets.find { it.category.equals(category, ignoreCase = true) } ?: return@launch
            if (budget.monthlyLimit <= 0) return@launch

            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfMonth = cal.timeInMillis

            val allTxs = repository.allTransactions.firstOrNull() ?: emptyList()
            val monthTxs = allTxs.filter { it.dateMillis >= startOfMonth && it.type == "EXPENSE" && it.category.equals(category, ignoreCase = true) }
            val currentSpent = monthTxs.sumOf { it.amount }

            val percentage = (currentSpent / budget.monthlyLimit).toFloat()
            if (percentage >= 1.0f) {
                val excess = currentSpent - budget.monthlyLimit
                val alert = BudgetAlert(
                    category = category,
                    spent = currentSpent,
                    limit = budget.monthlyLimit,
                    excess = excess,
                    percentage = percentage,
                    isSevere = true
                )
                _budgetAlertEvent.emit(alert)
                BudgetNotificationHelper.sendBudgetExceededNotification(
                    getApplication(),
                    category = category,
                    spentAmount = currentSpent,
                    limitAmount = budget.monthlyLimit
                )
            } else if (percentage >= 0.85f) {
                BudgetNotificationHelper.sendBudgetWarningNotification(
                    getApplication(),
                    category = category,
                    spentAmount = currentSpent,
                    limitAmount = budget.monthlyLimit
                )
            }
        }
    }

    fun setThemePalette(paletteId: String) {
        selectedThemePalette.value = paletteId
        prefs.edit().putString("theme_palette", paletteId).apply()
    }

    fun setOverallMonthlyBudget(cap: Double) {
        monthlyBudgetCap.value = cap
        prefs.edit().putFloat("monthly_budget_cap", cap.toFloat()).apply()
    }

    fun toggleDarkMode() {
        val newMode = !isDarkMode.value
        isDarkMode.value = newMode
        prefs.edit().putBoolean("is_dark_mode", newMode).apply()
    }

    fun setPin(pin: String) {
        appPin.value = pin
        isPinEnabled.value = true
        prefs.edit()
            .putString("app_pin", pin)
            .putBoolean("is_pin_enabled", true)
            .apply()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        isBiometricEnabled.value = enabled
        prefs.edit()
            .putBoolean("is_biometric_enabled", enabled)
            .apply()
    }

    fun disablePin() {
        isPinEnabled.value = false
        isAppLocked.value = false
        appPin.value = ""
        prefs.edit()
            .remove("app_pin")
            .putBoolean("is_pin_enabled", false)
            .apply()
    }

    fun lockApp() {
        if (isPinEnabled.value) {
            isAppLocked.value = true
        }
    }

    fun unlockWithBiometric() {
        isAppLocked.value = false
    }

    fun unlockApp(pin: String): Boolean {
        if (pin == appPin.value) {
            isAppLocked.value = false
            return true
        }
        return false
    }

    // Two-Click Instant Quick Logger - dynamically links to existing user account or creates a default
    fun quickLogExpense(amount: Double, title: String, category: String) {
        viewModelScope.launch {
            val accounts = repository.allAccounts.firstOrNull() ?: emptyList()
            val targetAccount = accounts.firstOrNull()
            val accountId = targetAccount?.id ?: 0L
            val accountName = targetAccount?.name ?: "Cash / UPI"

            repository.addTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    type = "EXPENSE",
                    category = category,
                    accountId = accountId,
                    accountName = accountName,
                    dateMillis = System.currentTimeMillis(),
                    note = "Quick Entry"
                )
            )
            checkAndNotifyBudget(category, amount)
        }
    }

    fun addCategory(name: String, type: String, iconKey: String, colorHex: String) {
        viewModelScope.launch {
            repository.addCategory(
                CategoryEntity(
                    name = name.trim(),
                    type = type,
                    iconKey = iconKey,
                    colorHex = colorHex,
                    isDefault = false
                )
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountId: Long,
        accountName: String,
        dateMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    title = title.trim(),
                    amount = amount,
                    type = type,
                    category = category,
                    accountId = accountId,
                    accountName = accountName,
                    dateMillis = dateMillis,
                    note = note.trim()
                )
            )
            if (type == "EXPENSE") {
                checkAndNotifyBudget(category, amount)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addAccount(name: String, type: String, balance: Double, last4: String, colorHex: String) {
        viewModelScope.launch {
            repository.addAccount(
                AccountEntity(
                    name = name.trim(),
                    type = type,
                    balance = balance,
                    accountNumberLast4 = last4.trim(),
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun addBudget(category: String, monthlyLimit: Double) {
        viewModelScope.launch {
            repository.addBudget(
                BudgetEntity(
                    category = category,
                    monthlyLimit = monthlyLimit
                )
            )
        }
    }

    fun updateBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.updateBudget(budget)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun addSavingsGoal(title: String, targetAmount: Double, currentAmount: Double, targetDateMillis: Long, colorHex: String) {
        viewModelScope.launch {
            repository.addSavingsGoal(
                SavingsGoalEntity(
                    title = title.trim(),
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetDateMillis = targetDateMillis,
                    colorHex = colorHex
                )
            )
        }
    }

    fun depositToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            repository.adjustGoalAmount(goalId, amount)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    fun addRecurringBill(title: String, amount: Double, category: String, dueDay: Int) {
        viewModelScope.launch {
            repository.addRecurringBill(
                RecurringBillEntity(
                    title = title.trim(),
                    amount = amount,
                    category = category,
                    dueDayOfMonth = dueDay,
                    isPaidThisMonth = false
                )
            )
        }
    }

    fun toggleBillPaid(bill: RecurringBillEntity) {
        viewModelScope.launch {
            repository.toggleBillPaidStatus(bill.id, bill.isPaidThisMonth)
        }
    }

    fun deleteRecurringBill(bill: RecurringBillEntity) {
        viewModelScope.launch {
            repository.deleteRecurringBill(bill)
        }
    }

    // Reset database to completely fresh start (wiping any dummy or previous test data)
    fun resetToFreshStart() {
        viewModelScope.launch {
            repository.resetAllDataToFreshStart()
        }
    }

    // Generate CSV Data Backup for One-Tap Export
    fun generateCsvExportData(): String {
        val state = uiState.value
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val sb = StringBuilder()

        sb.append("=== FINANCE TRACKER BACKUP EXPORT (INR) ===\n")
        sb.append("Export Date,${dateFormat.format(Date())}\n")
        sb.append("Total Net Worth,${state.totalNetWorth}\n")
        sb.append("Monthly Budget Cap,${state.overallMonthlyBudgetCap}\n")
        sb.append("Current Month Spent,${state.totalExpenseThisMonth}\n")
        sb.append("Current Month Income,${state.totalIncomeThisMonth}\n\n")

        sb.append("--- TRANSACTIONS ---\n")
        sb.append("ID,Date,Title,Type,Category,Amount (INR),Account,Note\n")
        state.transactions.forEach { tx ->
            val cleanTitle = tx.title.replace(",", " ")
            val cleanNote = tx.note.replace(",", " ")
            val dateStr = dateFormat.format(Date(tx.dateMillis))
            sb.append("${tx.id},$dateStr,$cleanTitle,${tx.type},${tx.category},${tx.amount},${tx.accountName},$cleanNote\n")
        }

        sb.append("\n--- ACCOUNTS ---\n")
        sb.append("ID,Name,Type,Balance (INR),Last4\n")
        state.accounts.forEach { acc ->
            sb.append("${acc.id},${acc.name},${acc.type},${acc.balance},${acc.accountNumberLast4}\n")
        }

        return sb.toString()
    }
}
