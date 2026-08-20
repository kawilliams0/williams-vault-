package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.BudgetHealth
import com.example.ui.theme.*

// ----------------------------------------------------
// 1. Overall Monthly Budget Cap & Circular Ring Chart
// ----------------------------------------------------
@Composable
fun OverallBudgetRingCard(
    monthlyCap: Double,
    totalSpent: Double,
    remainingAmount: Double,
    spentPercentage: Float,
    daysUntilReset: Int,
    onEditCap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverBudget = totalSpent > monthlyCap
    val isWarning = spentPercentage >= 0.8f && !isOverBudget
    val animatedProgress by animateFloatAsState(
        targetValue = spentPercentage.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ring_progress"
    )

    val progressColor by animateColorAsState(
        targetValue = when {
            isOverBudget -> CoralRed
            isWarning -> AmberGold
            else -> MintCyan
        },
        animationSpec = tween(500),
        label = "ring_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .pulsingGlow(enabled = isOverBudget || isWarning, minScale = 0.99f, maxScale = 1.01f)
            .testTag("overall_budget_ring_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isOverBudget) CoralRed.copy(alpha = 0.4f) else BorderDarkSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top row: Header & Edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MintCyanContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MintCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MONTHLY SPENDING CAP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = TextSecondary
                        )
                        Text(
                            text = "Resets in $daysUntilReset days on 1st",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextMuted
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onEditCap,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SecondaryDarkContainer,
                        contentColor = LilacPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 32.dp)
                        .bouncyClickable(onClick = onEditCap)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Set Cap", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Content: Circular Progress Ring & Numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOverBudget) "OVER BUDGET BY" else "REMAINING TO SPEND",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = if (isOverBudget) CoralRed else TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AnimatedCurrencyText(
                        targetAmount = if (isOverBudget) (totalSpent - monthlyCap) else remainingAmount,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = if (isOverBudget) CoralRed else MintCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Spent ${FinanceFormatters.formatCurrency(totalSpent)} of ${FinanceFormatters.formatCurrency(monthlyCap)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Circular Progress Ring with Gradient
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = SecondaryDarkContainer
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = Size(diameter, diameter)

                        // Background track
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Progress arc
                        drawArc(
                            color = progressColor,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(spentPercentage * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "used",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. Two-Click Quick Log Bar (On-The-Go Indian Presets)
// ----------------------------------------------------
@Composable
fun QuickExpenseBar(
    onQuickLog: (amount: Double, title: String, category: String) -> Unit,
    onOpenQuickLogModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickPresets = listOf(
        QuickPreset(amount = 50.0, label = "₹50 Chai", category = "Food & Chai", icon = Icons.Default.LocalCafe),
        QuickPreset(amount = 100.0, label = "₹100 Auto/Cab", category = "Transport & Fuel", icon = Icons.Default.DirectionsCar),
        QuickPreset(amount = 250.0, label = "₹250 Lunch", category = "Food & Chai", icon = Icons.Default.Restaurant),
        QuickPreset(amount = 500.0, label = "₹500 Petrol", category = "Transport & Fuel", icon = Icons.Default.LocalGasStation),
        QuickPreset(amount = 1000.0, label = "₹1k Kirana", category = "Groceries & Kirana", icon = Icons.Default.ShoppingCart),
        QuickPreset(amount = 2000.0, label = "₹2k Shopping", category = "Shopping", icon = Icons.Default.ShoppingBag)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "2-Click Quick Log",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            TextButton(
                onClick = onOpenQuickLogModal,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier
                    .defaultMinSize(minHeight = 32.dp)
                    .bouncyClickable(onClick = onOpenQuickLogModal)
            ) {
                Text("+ Custom Quick Log", fontSize = 12.sp, color = LilacPrimary)
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPresets) { preset ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle),
                    modifier = Modifier
                        .bouncyClickable(pressedScale = 0.92f) {
                            onQuickLog(preset.amount, preset.label, preset.category)
                        }
                        .testTag("quick_preset_${preset.label.replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = preset.icon,
                            contentDescription = null,
                            tint = LilacPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

private data class QuickPreset(
    val amount: Double,
    val label: String,
    val category: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// ----------------------------------------------------
// 3. Balance & Net Worth Overview Card
// ----------------------------------------------------
@Composable
fun NetWorthCard(
    netWorth: Double,
    incomeThisMonth: Double,
    expenseThisMonth: Double,
    savingsRate: Double,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("net_worth_card")
    ) {
        // Header: Label & Net Balance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOTAL NET BALANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.4.sp
                    ),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedCurrencyText(
                    targetAmount = netWorth,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp
                    ),
                    color = TextPrimary,
                    modifier = Modifier.testTag("total_net_worth_text")
                )
            }

            // Indian Rupee Symbol Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(SecondaryDarkContainer)
                    .border(1.dp, BorderDarkSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₹",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = LilacPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row: Expense, Income, Savings Pill with Tactile Scale
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Expense Button
            Button(
                onClick = onAddExpense,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .bouncyClickable(onClick = onAddExpense)
                    .testTag("quick_add_expense_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LilacPrimary,
                    contentColor = LilacOnPrimary
                ),
                contentPadding = PaddingValues(4.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "EXPENSE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            }

            // Income Button
            Button(
                onClick = onAddIncome,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .bouncyClickable(onClick = onAddIncome)
                    .testTag("quick_add_income_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryDarkContainer,
                    contentColor = TextPrimary
                ),
                contentPadding = PaddingValues(4.dp)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp), tint = MintCyan)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "INCOME",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            }

            // Savings Rate Pill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SecondaryDarkContainer,
                modifier = Modifier
                    .weight(0.9f)
                    .height(48.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "${savingsRate.toInt()}%",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MintCyan
                    )
                    Text(
                        text = "SAVED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 4. Account Carousel Card
// ----------------------------------------------------
@Composable
fun AccountCard(
    account: AccountEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(account.colorHex))
    } catch (e: Exception) {
        LilacPrimary
    }

    Card(
        modifier = modifier
            .width(200.dp)
            .height(124.dp)
            .bouncyClickable(pressedScale = 0.96f) {}
            .testTag("account_card_${account.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val typeIcon = when (account.type) {
                        "SAVINGS" -> Icons.Default.Savings
                        "CREDIT_CARD" -> Icons.Default.CreditCard
                        "CASH" -> Icons.Default.Payments
                        "INVESTMENT" -> Icons.Default.TrendingUp
                        else -> Icons.Default.AccountBalance
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = account.type,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (account.accountNumberLast4.isNotBlank()) {
                        Text(
                            text = "•••• ${account.accountNumberLast4}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    } else {
                        Text(
                            text = account.type.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Column {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    AnimatedCurrencyText(
                        targetAmount = account.balance,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AccountsCarousel(
    accounts: List<AccountEntity>,
    onAddAccount: () -> Unit,
    onDeleteAccount: (AccountEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("accounts_carousel"),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(accounts, key = { it.id }) { account ->
            AccountCard(
                account = account,
                onDelete = { onDeleteAccount(account) }
            )
        }

        item {
            OutlinedCard(
                onClick = onAddAccount,
                modifier = Modifier
                    .width(120.dp)
                    .height(124.dp)
                    .bouncyClickable(onClick = onAddAccount)
                    .testTag("add_account_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryDarkContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Account",
                            tint = LilacPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "New Account",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 5. Transaction Item Row (₹ INR formatted with bouncy press)
// ----------------------------------------------------
@Composable
fun TransactionItemRow(
    transaction: TransactionEntity,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClickable(pressedScale = 0.98f) { onClick?.invoke() }
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconBadge(
                categoryName = transaction.category,
                size = 46.dp,
                iconSize = 22.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = FinanceFormatters.formatShortDate(transaction.dateMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                if (transaction.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                val isIncome = transaction.type == "INCOME"
                val prefix = if (isIncome) "+" else "-"
                val color = if (isIncome) MintCyan else TextPrimary

                Text(
                    text = prefix + FinanceFormatters.formatCurrency(transaction.amount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = color
                )

                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_tx_${transaction.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete Transaction",
                            tint = TextMuted.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 6. Category Budget Card with Animated Progress & Exceeded Alert Badge
// ----------------------------------------------------
@Composable
fun BudgetProgressCard(
    budgetHealth: BudgetHealth,
    onDelete: () -> Unit,
    onEditBudget: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progress = budgetHealth.percentage.coerceIn(0f, 1.2f)
    val isOverBudget = budgetHealth.spent > budgetHealth.limit
    val isNearBudget = progress >= 0.8f && !isOverBudget

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceAtMost(1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "budget_progress"
    )

    val indicatorColor by animateColorAsState(
        targetValue = when {
            isOverBudget -> CoralRed
            isNearBudget -> AmberGold
            else -> NavySkyPrimary
        },
        label = "budgetColor"
    )

    val cardBorderColor = when {
        isOverBudget -> CoralRed.copy(alpha = 0.75f)
        isNearBudget -> AmberGold.copy(alpha = 0.5f)
        else -> BorderDarkSubtle
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_card_${budgetHealth.category}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget) DarkSurfaceElevated else DarkSurfaceCard
        ),
        border = androidx.compose.foundation.BorderStroke(if (isOverBudget) 1.5.dp else 1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    CategoryIconBadge(
                        categoryName = budgetHealth.category,
                        size = 38.dp,
                        iconSize = 18.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = budgetHealth.category,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            if (isOverBudget) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CoralRedContainer,
                                    contentColor = CoralRed
                                ) {
                                    Text(
                                        text = "EXCEEDED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (isNearBudget) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AmberGold.copy(alpha = 0.15f),
                                    contentColor = AmberGold
                                ) {
                                    Text(
                                        text = "NEARING LIMIT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isOverBudget) "Over limit by ${FinanceFormatters.formatCurrency(budgetHealth.spent - budgetHealth.limit)}"
                            else "${FinanceFormatters.formatCurrency(budgetHealth.remaining)} left",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverBudget) CoralRed else TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onEditBudget != null) {
                        IconButton(
                            onClick = onEditBudget,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Budget",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete Budget",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(3.5.dp)),
                color = indicatorColor,
                trackColor = SecondaryDarkContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${FinanceFormatters.formatCurrency(budgetHealth.spent)} / ${FinanceFormatters.formatCurrency(budgetHealth.limit)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = "${(budgetHealth.percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = indicatorColor
                )
            }
        }
    }
}

// ----------------------------------------------------
// 6B. Category Budget Alert Heads-Up Banner
// ----------------------------------------------------
@Composable
fun CategoryBudgetAlertBanner(
    alerts: List<com.example.ui.BudgetAlert>,
    onAdjustLimits: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (alerts.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("category_budget_alert_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CoralRedContainer.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CoralRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Budget Exceeded Warning",
                            tint = CoralRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Budget Alert (${alerts.size} Exceeded)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Spending in specific categories exceeded limits",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralRed
                        )
                    }
                }

                TextButton(
                    onClick = onAdjustLimits,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 30.dp)
                ) {
                    Text("Adjust", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CoralRed)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            alerts.forEach { alert ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("•", color = CoralRed, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = alert.category,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Exceeded by ${FinanceFormatters.formatCurrency(alert.excess)} (${(alert.percentage * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CoralRed
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 7. Savings Goal Component with Visual Progress Bar & Target Tracking
// ----------------------------------------------------
@Composable
fun SavingsGoalComponent(
    title: String,
    targetAmount: Double,
    currentAmount: Double,
    targetDateMillis: Long = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
    colorHex: String = "#0284C7",
    goalId: Long = 0L,
    onDeposit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (targetAmount > 0) {
        (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val isGoalReached = currentAmount >= targetAmount && targetAmount > 0
    val remainingAmount = (targetAmount - currentAmount).coerceAtLeast(0.0)
    val percentageInt = (progressFraction * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "savings_goal_progress"
    )

    val defaultBarColor = LilacPrimary
    val customGoalColor = remember(colorHex, defaultBarColor) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            defaultBarColor
        }
    }

    val progressBarColor by animateColorAsState(
        targetValue = when {
            isGoalReached -> MintCyan
            progressFraction >= 0.75f -> NavySkyPrimary
            progressFraction >= 0.40f -> AmberGold
            else -> if (colorHex.isNotBlank() && colorHex != "#0284C7") customGoalColor else defaultBarColor
        },
        label = "savings_goal_bar_color"
    )

    // Calculate days remaining until target date
    val daysLeft = remember(targetDateMillis) {
        val now = System.currentTimeMillis()
        val diff = targetDateMillis - now
        (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(if (goalId > 0) "savings_goal_component_$goalId" else "savings_goal_component"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGoalReached) DarkSurfaceElevated else DarkSurfaceCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGoalReached) MintCyan.copy(alpha = 0.6f) else BorderDarkSubtle
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Goal Name, Status Badge & Delete Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isGoalReached) MintCyanContainer else progressBarColor.copy(alpha = 0.18f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isGoalReached) Icons.Default.CheckCircle else Icons.Default.Savings,
                            contentDescription = "Savings Goal Icon",
                            tint = if (isGoalReached) MintCyan else progressBarColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("savings_goal_title")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Status Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    isGoalReached -> MintCyanContainer
                                    progressFraction >= 0.75f -> NavySkyPrimary.copy(alpha = 0.15f)
                                    progressFraction >= 0.40f -> AmberGold.copy(alpha = 0.15f)
                                    else -> SecondaryDarkContainer
                                },
                                contentColor = when {
                                    isGoalReached -> MintCyan
                                    progressFraction >= 0.75f -> NavySkyPrimary
                                    progressFraction >= 0.40f -> AmberGold
                                    else -> TextSecondary
                                }
                            ) {
                                Text(
                                    text = when {
                                        isGoalReached -> "REACHED! 🎉"
                                        progressFraction >= 0.75f -> "ALMOST THERE"
                                        progressFraction >= 0.40f -> "ON TRACK"
                                        else -> "IN PROGRESS"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = when {
                                isGoalReached -> "Target of ${FinanceFormatters.formatCurrency(targetAmount)} fully funded"
                                daysLeft > 1 -> "Target: ${FinanceFormatters.formatDate(targetDateMillis)} • $daysLeft days left"
                                daysLeft == 1 -> "Target: ${FinanceFormatters.formatDate(targetDateMillis)} • 1 day left"
                                daysLeft == 0 -> "Target: ${FinanceFormatters.formatDate(targetDateMillis)} • Due today"
                                else -> "Target: ${FinanceFormatters.formatDate(targetDateMillis)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isGoalReached) MintCyan else TextSecondary
                        )
                    }
                }

                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete Goal",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Highlighted "Remains to be Saved" Metric Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isGoalReached) MintCyanContainer.copy(alpha = 0.35f) else DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isGoalReached) MintCyan.copy(alpha = 0.3f) else BorderDarkSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isGoalReached) "TARGET REACHED" else "REMAINS TO BE SAVED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = if (isGoalReached) MintCyan else TextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isGoalReached) "Goal 100% Accomplished" else "${FinanceFormatters.formatCurrency(remainingAmount)} left",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = if (isGoalReached) MintCyan else CoralRed
                        )
                    }

                    // Progress Percentage Highlight
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = progressBarColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "$percentageInt%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = progressBarColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Progress Indicator Track
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("savings_goal_progress_bar")
            ) {
                // Progress Bar with Gradient Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(SecondaryDarkContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        progressBarColor.copy(alpha = 0.75f),
                                        progressBarColor
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Milestone Labels (0%, 25%, 50%, 75%, 100%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
                    Text("25%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
                    Text("50%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
                    Text("75%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
                    Text("100%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Summary & Add Funds Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${FinanceFormatters.formatCurrency(currentAmount)} saved",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        modifier = Modifier.testTag("savings_goal_saved_amount")
                    )
                    Text(
                        text = "Target: ${FinanceFormatters.formatCurrency(targetAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.testTag("savings_goal_target_amount")
                    )
                }

                if (onDeposit != null) {
                    Button(
                        onClick = onDeposit,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGoalReached) MintCyan else LilacPrimary,
                            contentColor = if (isGoalReached) DarkBackground else LilacOnPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 44.dp)
                            .bouncyClickable(onClick = onDeposit)
                            .testTag(if (goalId > 0) "deposit_goal_$goalId" else "deposit_goal_button")
                    ) {
                        Icon(
                            imageVector = if (isGoalReached) Icons.Default.AddCircle else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGoalReached) "Top Up" else "Add Funds",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsGoalCard(
    goal: SavingsGoalEntity,
    onDeposit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    SavingsGoalComponent(
        title = goal.title,
        targetAmount = goal.targetAmount,
        currentAmount = goal.currentAmount,
        targetDateMillis = goal.targetDateMillis,
        colorHex = goal.colorHex,
        goalId = goal.id,
        onDeposit = onDeposit,
        onDelete = onDelete,
        modifier = modifier
    )
}

// ----------------------------------------------------
// 8. Recurring Bill Item with Checkmark Pop Animation
// ----------------------------------------------------
@Composable
fun RecurringBillItem(
    bill: RecurringBillEntity,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkmarkScale by animateFloatAsState(
        targetValue = if (bill.isPaidThisMonth) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "check_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bill_item_${bill.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (bill.isPaidThisMonth) MintCyanContainer
                        else SecondaryDarkContainer
                    )
                    .border(1.dp, BorderDarkSubtle, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DAY",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = if (bill.isPaidThisMonth) MintCyan else TextSecondary
                    )
                    Text(
                        text = "${bill.dueDayOfMonth}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (bill.isPaidThisMonth) MintCyan else TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bill.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "•", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (bill.isPaidThisMonth) "Paid" else "Due soon",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (bill.isPaidThisMonth) MintCyan else AmberGold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FinanceFormatters.formatCurrency(bill.amount),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePaid,
                        modifier = Modifier
                            .size(34.dp)
                            .scale(checkmarkScale)
                            .bouncyClickable(onClick = onTogglePaid)
                            .testTag("toggle_paid_bill_${bill.id}")
                    ) {
                        Icon(
                            imageVector = if (bill.isPaidThisMonth) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = if (bill.isPaidThisMonth) "Mark Unpaid" else "Mark Paid",
                            tint = if (bill.isPaidThisMonth) MintCyan else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete Bill",
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
