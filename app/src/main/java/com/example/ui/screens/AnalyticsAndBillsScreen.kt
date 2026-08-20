package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecurringBillEntity
import com.example.ui.FinanceUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AnalyticsAndBillsScreen(
    uiState: FinanceUiState,
    isDarkMode: Boolean = true,
    onToggleDarkMode: () -> Unit = {},
    onAddBill: () -> Unit,
    onToggleBillPaid: (RecurringBillEntity) -> Unit,
    onDeleteBill: (RecurringBillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Analytics, 1 = Bills

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("analytics_and_bills_screen"),
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
                    text = "Insights & Bills",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp
                    ),
                    color = TextPrimary
                )

                if (selectedSection == 1) {
                    Button(
                        onClick = onAddBill,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LilacPrimary,
                            contentColor = LilacOnPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_bill_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Bill", fontWeight = FontWeight.Bold)
                    }
                } else {
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.testTag("toggle_theme_header_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = AmberGold
                        )
                    }
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
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = SecondaryDarkContainer,
                        activeContentColor = LilacPrimary,
                        inactiveContainerColor = DarkSurface,
                        inactiveContentColor = TextSecondary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analytics", fontWeight = FontWeight.SemiBold)
                    }
                }
                SegmentedButton(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = SecondaryDarkContainer,
                        activeContentColor = LilacPrimary,
                        inactiveContainerColor = DarkSurface,
                        inactiveContentColor = TextSecondary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.EventRepeat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subscriptions", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedSection == 0) {
            // --- Analytics Tab Content ---

            // Key Metrics Highlights
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Metric 1: Net Cashflow
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "NET CASH FLOW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.8.sp
                                ),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = FinanceFormatters.formatCurrency(uiState.netCashflowThisMonth),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (uiState.netCashflowThisMonth >= 0) MintCyan else CoralRed
                            )
                        }
                    }

                    // Metric 2: Savings Rate
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "SAVINGS RATE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.8.sp
                                ),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.savingsRateThisMonth.toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = LilacPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Donut Chart
            item {
                CategoryDonutChart(
                    categorySpends = uiState.categorySpends,
                    totalExpense = uiState.totalExpenseThisMonth
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Month-Over-Month Total Spending Trend Line Chart
            item {
                MonthOverMonthSpendingLineChart(
                    monthlyCashflows = uiState.monthlyCashflows
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Monthly Bar Chart
            item {
                MonthlyCashflowBarChart(
                    monthlyCashflows = uiState.monthlyCashflows
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // App Appearance & Navy Theme Mode Switcher
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("theme_switcher_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SecondaryDarkContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                        contentDescription = null,
                                        tint = LilacPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Appearance & Theme",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (isDarkMode) "Midnight Navy Blue (Dark)" else "Ice Navy & Pure White (Light)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Segmented Light / Dark Toggle Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Dark Mode Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDarkMode) LilacPrimary else DarkSurfaceElevated,
                                contentColor = if (isDarkMode) LilacOnPrimary else TextSecondary,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { if (!isDarkMode) onToggleDarkMode() }
                                    .testTag("theme_select_dark")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isDarkMode) LilacOnPrimary else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Dark Navy",
                                        fontSize = 13.sp,
                                        fontWeight = if (isDarkMode) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }

                            // Light Mode Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (!isDarkMode) LilacPrimary else DarkSurfaceElevated,
                                contentColor = if (!isDarkMode) LilacOnPrimary else TextSecondary,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { if (isDarkMode) onToggleDarkMode() }
                                    .testTag("theme_select_light")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (!isDarkMode) LilacOnPrimary else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Light Navy",
                                        fontSize = 13.sp,
                                        fontWeight = if (!isDarkMode) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            // --- Recurring Bills Tab Content ---
            val totalMonthlyBills = uiState.recurringBills.sumOf { it.amount }

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
                            text = "COMMITTED MONTHLY SUBSCRIPTIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
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
                                text = FinanceFormatters.formatCurrency(totalMonthlyBills),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "${uiState.upcomingBillsCount} unpaid",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (uiState.upcomingBillsCount > 0) AmberGold else MintCyan
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.recurringBills.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.EventRepeat,
                        title = "No Recurring Bills Added",
                        subtitle = "Track subscriptions like Netflix, Gym, Internet, or Rent.",
                        actionButtonText = "Add First Bill",
                        onActionClick = onAddBill
                    )
                }
            } else {
                items(uiState.recurringBills, key = { it.id }) { bill ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                        RecurringBillItem(
                            bill = bill,
                            onTogglePaid = { onToggleBillPaid(bill) },
                            onDelete = { onDeleteBill(bill) }
                        )
                    }
                }
            }
        }
    }
}
