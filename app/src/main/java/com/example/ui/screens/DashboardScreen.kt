package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.ui.FinanceUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    uiState: FinanceUiState,
    isDarkMode: Boolean,
    isPinEnabled: Boolean,
    onToggleDarkMode: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onLockApp: () -> Unit,
    onExportData: () -> Unit,
    onEditMonthlyCap: () -> Unit,
    onQuickLog: (amount: Double, title: String, category: String) -> Unit,
    onOpenQuickLogModal: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToGoals: () -> Unit = {},
    onAddGoal: () -> Unit = {},
    onDepositGoal: (SavingsGoalEntity) -> Unit = {},
    onDeleteGoal: (SavingsGoalEntity) -> Unit = {},
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onAddAccount: () -> Unit,
    onDeleteAccount: (AccountEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // App Top Navigation Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LilacPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = LilacOnPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Williams Vault",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${FinanceFormatters.formatCurrentMonth()} • Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = MintCyan
                        )
                    }
                }

                // Header Utility Action Icons
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Export CSV Backup Icon
                    IconButton(
                        onClick = onExportData,
                        modifier = Modifier.testTag("export_backup_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Export Backup",
                            tint = LilacPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Settings & Color Theme Palette Icon
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("open_settings_palette_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Color Themes & Settings",
                            tint = LilacPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Dark / Light Theme Toggle Icon
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.testTag("toggle_dark_mode_icon")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = AmberGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Lock App / Security Icon
                    IconButton(
                        onClick = onLockApp,
                        modifier = Modifier.testTag("lock_app_icon")
                    ) {
                        Icon(
                            imageVector = if (isPinEnabled) Icons.Filled.Lock else Icons.Outlined.Lock,
                            contentDescription = "Lock App",
                            tint = if (isPinEnabled) MintCyan else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 1. Overall Monthly Spending Cap & Ring Progress Gauge
        item {
            OverallBudgetRingCard(
                monthlyCap = uiState.overallMonthlyBudgetCap,
                totalSpent = uiState.totalExpenseThisMonth,
                remainingAmount = uiState.remainingMonthlyBudget,
                spentPercentage = uiState.budgetCapPercentage,
                daysUntilReset = uiState.daysUntilReset,
                onEditCap = onEditMonthlyCap
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Active Exceeded Category Alerts Banner
        if (uiState.budgetAlerts.isNotEmpty()) {
            item {
                CategoryBudgetAlertBanner(
                    alerts = uiState.budgetAlerts,
                    onAdjustLimits = onEditMonthlyCap
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 2. Two-Click Quick Log Bar
        item {
            QuickExpenseBar(
                onQuickLog = onQuickLog,
                onOpenQuickLogModal = onOpenQuickLogModal
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Balance & Net Worth Overview
        item {
            NetWorthCard(
                netWorth = uiState.totalNetWorth,
                incomeThisMonth = uiState.totalIncomeThisMonth,
                expenseThisMonth = uiState.totalExpenseThisMonth,
                savingsRate = uiState.savingsRateThisMonth,
                onAddExpense = onAddExpense,
                onAddIncome = onAddIncome
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. Accounts & Cards Carousel Section
        item {
            SectionHeader(
                title = "My Accounts & UPI Wallets",
                actionText = "+ Add",
                onActionClick = onAddAccount
            )
            AccountsCarousel(
                accounts = uiState.accounts,
                onAddAccount = onAddAccount,
                onDeleteAccount = onDeleteAccount
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 5. Savings Goals Showcase Section
        item {
            SectionHeader(
                title = "Savings Goals",
                actionText = if (uiState.savingsGoals.isNotEmpty()) "View All" else "+ Goal",
                onActionClick = {
                    if (uiState.savingsGoals.isNotEmpty()) onNavigateToGoals() else onAddGoal()
                }
            )
            if (uiState.savingsGoals.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(LilacContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = LilacPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Set a Custom Savings Goal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track your target progress with interactive milestone bars",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddGoal,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LilacPrimary,
                                contentColor = LilacOnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("dashboard_add_first_goal_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Savings Goal", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.savingsGoals.take(2).forEach { goal ->
                        SavingsGoalComponent(
                            title = goal.title,
                            targetAmount = goal.targetAmount,
                            currentAmount = goal.currentAmount,
                            targetDateMillis = goal.targetDateMillis,
                            colorHex = goal.colorHex,
                            goalId = goal.id,
                            onDeposit = { onDepositGoal(goal) },
                            onDelete = { onDeleteGoal(goal) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 6. Category Breakdown Donut Chart
        if (uiState.categorySpends.isNotEmpty()) {
            item {
                CategoryDonutChart(
                    categorySpends = uiState.categorySpends,
                    totalExpense = uiState.totalExpenseThisMonth
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 7. Simple History Log (Recent Transactions)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = LilacPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Simple History Log",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                        TextButton(
                            onClick = onNavigateToTransactions,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = LilacPrimary
                                )
                            )
                        }
                    }

                    if (uiState.transactions.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Outlined.ReceiptLong,
                            title = "No Transactions Yet",
                            subtitle = "Use the 2-Click Quick Log above or tap Add Expense.",
                            actionButtonText = "Add Expense",
                            onActionClick = onAddExpense
                        )
                    } else {
                        val recentTransactions = uiState.transactions.take(6)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentTransactions.forEach { tx ->
                                TransactionItemRow(
                                    transaction = tx,
                                    onDelete = { onDeleteTransaction(tx) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
