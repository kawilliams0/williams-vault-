package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.SavingsGoalEntity
import com.example.ui.FinanceUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun BudgetsAndGoalsScreen(
    uiState: FinanceUiState,
    onAddBudget: () -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit,
    onEditBudget: ((BudgetEntity) -> Unit)? = null,
    onAddGoal: () -> Unit,
    onDepositGoal: (SavingsGoalEntity) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Budgets, 1 = Goals

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("budgets_and_goals_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budgets & Goals",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp
                    ),
                    color = TextPrimary
                )

                Button(
                    onClick = {
                        if (selectedTab == 0) onAddBudget() else onAddGoal()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LilacPrimary,
                        contentColor = LilacOnPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_budget_or_goal_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (selectedTab == 0) "Budget" else "Goal", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Segmented Switcher
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = SecondaryDarkContainer,
                        activeContentColor = LilacPrimary,
                        inactiveContainerColor = DarkSurface,
                        inactiveContentColor = TextSecondary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.PieChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Category Budgets", fontWeight = FontWeight.SemiBold)
                    }
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = SecondaryDarkContainer,
                        activeContentColor = LilacPrimary,
                        inactiveContainerColor = DarkSurface,
                        inactiveContentColor = TextSecondary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Savings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Savings Goals", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedTab == 0) {
            // --- Budgets Tab Content ---

            // Active Exceeded Budget Alert Banner
            if (uiState.budgetAlerts.isNotEmpty()) {
                item {
                    CategoryBudgetAlertBanner(
                        alerts = uiState.budgetAlerts,
                        onAdjustLimits = onAddBudget
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            val totalBudgeted = uiState.budgets.sumOf { it.monthlyLimit }
            val totalSpentOnBudgets = uiState.budgetHealthList.sumOf { it.spent }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "MONTHLY SPENDING CAP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.2.sp
                            ),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = FinanceFormatters.formatCurrency(totalSpentOnBudgets),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "of ${FinanceFormatters.formatCurrency(totalBudgeted)} limit",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.budgetHealthList.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.PieChart,
                        title = "No Category Budgets Set",
                        subtitle = "Create spending limits to stay on track this month.",
                        actionButtonText = "Set First Budget",
                        onActionClick = onAddBudget
                    )
                }
            } else {
                items(uiState.budgetHealthList, key = { it.category }) { health ->
                    val budgetEntity = uiState.budgets.find { it.category == health.category }
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                        BudgetProgressCard(
                            budgetHealth = health,
                            onDelete = {
                                if (budgetEntity != null) onDeleteBudget(budgetEntity)
                            },
                            onEditBudget = if (budgetEntity != null && onEditBudget != null) {
                                { onEditBudget(budgetEntity) }
                            } else null
                        )
                    }
                }
            }
        } else {
            // --- Savings Goals Tab Content ---
            val totalSaved = uiState.savingsGoals.sumOf { it.currentAmount }
            val totalTarget = uiState.savingsGoals.sumOf { it.targetAmount }
            val totalRemaining = (totalTarget - totalSaved).coerceAtLeast(0.0)
            val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL GOALS ACCUMULATED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = MintCyan
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MintCyanContainer
                            ) {
                                Text(
                                    text = "${(overallProgress * 100).toInt()}% Saved",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MintCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = FinanceFormatters.formatCurrency(totalSaved),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "of ${FinanceFormatters.formatCurrency(totalTarget)} target",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Visual Progress Indicator for All Goals
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MintCyan,
                            trackColor = SecondaryDarkContainer
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (totalRemaining > 0) "${FinanceFormatters.formatCurrency(totalRemaining)} remaining to reach targets" else "All savings goals achieved!",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (totalRemaining > 0) NavySkyPrimary else MintCyan
                            )
                            Text(
                                text = "${uiState.savingsGoals.size} ${if (uiState.savingsGoals.size == 1) "Goal" else "Goals"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.savingsGoals.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.Savings,
                        title = "No Savings Goals Yet",
                        subtitle = "Plan for a trip, emergency fund, or major purchase.",
                        actionButtonText = "Create Goal",
                        onActionClick = onAddGoal
                    )
                }
            } else {
                items(uiState.savingsGoals, key = { it.id }) { goal ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                        SavingsGoalCard(
                            goal = goal,
                            onDeposit = { onDepositGoal(goal) },
                            onDelete = { onDeleteGoal(goal) }
                        )
                    }
                }
            }
        }
    }
}
