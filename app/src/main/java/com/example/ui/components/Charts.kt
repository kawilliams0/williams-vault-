package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinanceCategories
import com.example.ui.CategorySpend
import com.example.ui.MonthlyCashflow
import com.example.ui.theme.*
import java.util.Locale

private val ElegantPalette = listOf(
    NavySkyPrimary,           // Vibrant Sky Blue
    NavyRoyalPrimary,         // Royal Navy Blue
    DarkMintCyan,             // Crisp Mint Cyan
    SecondaryLavender,        // Ice Blue
    DarkAmberGold,            // Golden Amber
    DarkCoralRed,             // Rose Coral Red
    Color(0xFF67E8F9),        // Luminous Cyan
    Color(0xFF818CF8),        // Indigo Blue
    Color(0xFFCBD5E1),        // Slate Ice
    Color(0xFF38BDF8)         // Bright Ice Sky
)

/**
 * High-performance native Jetpack Compose Data Visualization component
 * displaying a comprehensive breakdown of monthly expenses by category.
 * Supports multiple interactive visualization modes:
 *  1. Radial Donut Chart with animated sweep & slice tap inspection HUD.
 *  2. Ranked Horizontal Distribution Bars with animated progress & gradients.
 *  3. Detailed Category Matrix with breakdown metrics.
 */
@Composable
fun CategoryExpenseBreakdownVisualization(
    categorySpends: List<CategorySpend>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    if (categorySpends.isEmpty() || totalExpense <= 0.0) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Category Expense Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No expenses recorded this month yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        return
    }

    var selectedVisualizationMode by remember { mutableIntStateOf(0) } // 0: Donut, 1: Bars, 2: Detail
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(0f) }

    val topCategory = remember(categorySpends) {
        categorySpends.maxByOrNull { it.amount }
    }
    val averagePerCategory = remember(categorySpends, totalExpense) {
        if (categorySpends.isNotEmpty()) totalExpense / categorySpends.size else 0.0
    }

    LaunchedEffect(categorySpends, selectedVisualizationMode) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("category_expense_breakdown_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row with Visualization Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Expenses",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Breakdown by Category",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                // Mode toggle pills
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SecondaryDarkContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { selectedVisualizationMode = 0 },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedVisualizationMode == 0) LilacPrimary else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DonutLarge,
                                contentDescription = "Donut View",
                                tint = if (selectedVisualizationMode == 0) LilacOnPrimary else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { selectedVisualizationMode = 1 },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedVisualizationMode == 1) LilacPrimary else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BarChart,
                                contentDescription = "Bar View",
                                tint = if (selectedVisualizationMode == 1) LilacOnPrimary else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { selectedVisualizationMode = 2 },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedVisualizationMode == 2) LilacPrimary else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FormatListBulleted,
                                contentDescription = "List View",
                                tint = if (selectedVisualizationMode == 2) LilacOnPrimary else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Stats Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Category chip
                topCategory?.let { top ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SecondaryDarkContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text("TOP SPEND", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold), color = TextSecondary)
                            Text(top.category, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = LilacPrimary, maxLines = 1)
                        }
                    }
                }

                // Average / Category chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SecondaryDarkContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text("AVG / CATEGORY", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold), color = TextSecondary)
                        Text(FinanceFormatters.formatCurrency(averagePerCategory), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MintCyan, maxLines = 1)
                    }
                }

                // Active Categories Count
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SecondaryDarkContainer,
                    modifier = Modifier.weight(0.7f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text("CATEGORIES", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold), color = TextSecondary)
                        Text("${categorySpends.size} Active", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Animated Visual Mode Content
            AnimatedContent(
                targetState = selectedVisualizationMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "breakdown_mode_anim"
            ) { mode ->
                when (mode) {
                    0 -> {
                        // --- MODE 0: Interactive Radial Donut Chart ---
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .size(190.dp)
                                        .padding(8.dp)
                                ) {
                                    val strokeWidth = 28.dp.toPx()
                                    val diameter = size.minDimension - strokeWidth
                                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                                    val arcSize = Size(diameter, diameter)

                                    var startAngle = -90f
                                    categorySpends.forEachIndexed { index, item ->
                                        val sweepAngle = item.percentage * 360f * animationProgress.value
                                        val color = ElegantPalette[index % ElegantPalette.size]
                                        val isSelected = selectedIndex == index

                                        drawArc(
                                            color = color,
                                            startAngle = startAngle,
                                            sweepAngle = (sweepAngle - 2.5f).coerceAtLeast(1f),
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = arcSize,
                                            style = Stroke(
                                                width = if (isSelected) strokeWidth + 8f else strokeWidth,
                                                cap = StrokeCap.Round
                                            )
                                        )
                                        startAngle += sweepAngle
                                    }
                                }

                                // Interactive Center Label HUD
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                ) {
                                    val activeItem = selectedIndex?.let { categorySpends.getOrNull(it) }
                                    if (activeItem != null) {
                                        val activeCategory = remember(activeItem.category) { FinanceCategories.getCategory(activeItem.category) }
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(activeCategory.color.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = activeCategory.icon,
                                                contentDescription = null,
                                                tint = activeCategory.color,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = activeItem.category,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = activeCategory.color,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        AnimatedCurrencyText(
                                            targetAmount = activeItem.amount,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = SecondaryDarkContainer,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = "${(activeItem.percentage * 100).toInt()}% of total",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                color = activeCategory.color,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(SecondaryDarkContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DonutLarge,
                                                contentDescription = null,
                                                tint = NavySkyPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "TOTAL SPENT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 1.sp
                                            ),
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        AnimatedCurrencyText(
                                            targetAmount = totalExpense,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Interactive Category Legend with Intuitive Category Icons
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                categorySpends.take(6).forEachIndexed { index, item ->
                                    val isSelected = selectedIndex == index
                                    val catItem = remember(item.category) { FinanceCategories.getCategory(item.category) }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) SecondaryDarkContainer else Color.Transparent)
                                            .bouncyClickable {
                                                selectedIndex = if (selectedIndex == index) null else index
                                            }
                                            .padding(vertical = 6.dp, horizontal = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(catItem.color.copy(alpha = 0.18f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = catItem.icon,
                                                    contentDescription = item.category,
                                                    tint = catItem.color,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = item.category,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = TextPrimary
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = FinanceFormatters.formatCurrency(item.amount),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${(item.percentage * 100).toInt()}%",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) catItem.color else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // --- MODE 1: Horizontal Distribution Progress Bars ---
                        val maxCategoryAmount = categorySpends.maxOfOrNull { it.amount } ?: 1.0
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            categorySpends.forEachIndexed { index, item ->
                                val color = ElegantPalette[index % ElegantPalette.size]
                                val barRatio = (item.amount / maxCategoryAmount).toFloat()
                                val categoryItem = FinanceCategories.getCategory(item.category)

                                val animatedBarWidth by animateFloatAsState(
                                    targetValue = barRatio * animationProgress.value,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "cat_bar_$index"
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SecondaryDarkContainer.copy(alpha = 0.5f))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(color.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = categoryItem.icon,
                                                    contentDescription = null,
                                                    tint = color,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = item.category,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = TextPrimary
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = FinanceFormatters.formatCurrency(item.amount),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = color.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "${(item.percentage * 100).toInt()}%",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = color,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Animated Horizontal Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DarkBackground)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction = animatedBarWidth.coerceIn(0.02f, 1f))
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(color, color.copy(alpha = 0.7f))
                                                    )
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // --- MODE 2: Detailed Matrix Grid ---
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            categorySpends.forEachIndexed { index, item ->
                                val color = ElegantPalette[index % ElegantPalette.size]
                                val categoryItem = FinanceCategories.getCategory(item.category)

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = DarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(color.copy(alpha = 0.18f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = categoryItem.icon,
                                                    contentDescription = null,
                                                    tint = color,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = item.category,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Rank #${index + 1} • ${(item.percentage * 100).toInt()}% share",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = FinanceFormatters.formatCurrency(item.amount),
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${(item.percentage * 100).toInt()}% of budget",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MintCyan
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Backwards compatibility wrapper for CategoryDonutChart.
 */
@Composable
fun CategoryDonutChart(
    categorySpends: List<CategorySpend>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    CategoryExpenseBreakdownVisualization(
        categorySpends = categorySpends,
        totalExpense = totalExpense,
        modifier = modifier
    )
}

@Composable
fun MonthlyCashflowBarChart(
    monthlyCashflows: List<MonthlyCashflow>,
    modifier: Modifier = Modifier
) {
    if (monthlyCashflows.isEmpty()) return

    val maxAmount = monthlyCashflows.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0
    val effectiveMax = if (maxAmount <= 0) 1.0 else maxAmount
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("monthly_cashflow_chart_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cash Flow Trend",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MintCyan)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Income", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LilacPrimary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Expense", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }

            // Interactive Tooltip if month selected
            AnimatedVisibility(
                visible = selectedMonthIndex != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedMonthIndex?.let { idx ->
                    monthlyCashflows.getOrNull(idx)?.let { selectedItem ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SecondaryDarkContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedItem.monthLabel}:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "In: ${FinanceFormatters.formatCurrency(selectedItem.income)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MintCyan
                                )
                                Text(
                                    text = "Out: ${FinanceFormatters.formatCurrency(selectedItem.expense)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CoralRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bars Row with Spring Animation on Bar Heights
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                monthlyCashflows.forEachIndexed { index, item ->
                    val isSelected = selectedMonthIndex == index
                    val targetIncomeRatio = (item.income / effectiveMax).toFloat().coerceIn(0.06f, 1f)
                    val targetExpenseRatio = (item.expense / effectiveMax).toFloat().coerceIn(0.06f, 1f)

                    val animatedIncomeRatio by animateFloatAsState(
                        targetValue = targetIncomeRatio,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "income_bar_ratio_$index"
                    )

                    val animatedExpenseRatio by animateFloatAsState(
                        targetValue = targetExpenseRatio,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "expense_bar_ratio_$index"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .bouncyClickable {
                                selectedMonthIndex = if (selectedMonthIndex == index) null else index
                            }
                            .padding(horizontal = 4.dp)
                    ) {
                        // The Two Bars with Rounded Caps & Gradient
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Income Bar
                            Box(
                                modifier = Modifier
                                    .width(if (isSelected) 14.dp else 11.dp)
                                    .fillMaxHeight(fraction = animatedIncomeRatio)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(MintCyan, Color(0xFF007A7A))
                                        )
                                    )
                            )
                            // Expense Bar
                            Box(
                                modifier = Modifier
                                    .width(if (isSelected) 14.dp else 11.dp)
                                    .fillMaxHeight(fraction = animatedExpenseRatio)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(LilacPrimary, Color(0xFF4F378B))
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Month Label
                        Text(
                            text = item.monthLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) LilacPrimary else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-craft Month-Over-Month Total Spending Trend Line Chart
 * Displays monthly historical spend evolution using smooth cubic Bézier curves,
 * luminous gradient fills, interactive inspection tooltips, and month-over-month delta indicators.
 */
@Composable
fun MonthOverMonthSpendingLineChart(
    monthlyCashflows: List<MonthlyCashflow>,
    modifier: Modifier = Modifier
) {
    if (monthlyCashflows.isEmpty()) return

    var selectedIndex by remember { mutableStateOf<Int?>(monthlyCashflows.lastIndex) }
    var showDualTrend by remember { mutableStateOf(false) }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(monthlyCashflows, showDualTrend) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // Month-over-month delta calculation
    val latestMonth = monthlyCashflows.lastOrNull()
    val prevMonth = if (monthlyCashflows.size >= 2) monthlyCashflows[monthlyCashflows.size - 2] else null

    val momDeltaPercent = if (latestMonth != null && prevMonth != null && prevMonth.expense > 0) {
        ((latestMonth.expense - prevMonth.expense) / prevMonth.expense * 100.0)
    } else 0.0

    val averageMonthlyExpense = remember(monthlyCashflows) {
        if (monthlyCashflows.isNotEmpty()) monthlyCashflows.map { it.expense }.average() else 0.0
    }

    val maxAmount = remember(monthlyCashflows, showDualTrend) {
        val maxVal = if (showDualTrend) {
            monthlyCashflows.maxOfOrNull { maxOf(it.expense, it.income) } ?: 1.0
        } else {
            monthlyCashflows.maxOfOrNull { it.expense } ?: 1.0
        }
        if (maxVal <= 0.0) 1.0 else maxVal * 1.15 // Add 15% headroom for aesthetic curve peak
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("mom_spending_line_chart_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Title & Dual Trend Toggle Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Spending Trend",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // MoM Trend Indicator Badge
                        if (prevMonth != null) {
                            val isHigherExpense = momDeltaPercent > 0
                            val badgeBg = if (isHigherExpense) CoralRed.copy(alpha = 0.15f) else MintCyan.copy(alpha = 0.15f)
                            val badgeColor = if (isHigherExpense) CoralRed else MintCyan
                            val icon = if (isHigherExpense) Icons.Default.TrendingUp else Icons.Default.TrendingDown

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeBg,
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${if (isHigherExpense) "+" else ""}${String.format(Locale.ENGLISH, "%.1f", momDeltaPercent)}% MoM",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = badgeColor
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "Month-over-month total expenditure",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                // Dual / Single Trend Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SecondaryDarkContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle),
                    modifier = Modifier.clickable { showDualTrend = !showDualTrend }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (showDualTrend) Icons.Outlined.Timeline else Icons.Outlined.ShowChart,
                            contentDescription = "Toggle Cashflow Overlay",
                            tint = NavySkyPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showDualTrend) "Dual Flow" else "Expense",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = NavySkyPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Month Inspection Banner
            val inspectedMonth = selectedIndex?.let { monthlyCashflows.getOrNull(it) } ?: latestMonth
            if (inspectedMonth != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${inspectedMonth.monthLabel} Total Spend",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = FinanceFormatters.formatCurrency(inspectedMonth.expense),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (showDualTrend) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Income", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(
                                        text = "+${FinanceFormatters.formatCurrency(inspectedMonth.income)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MintCyan
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("6-Mo Avg", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    text = FinanceFormatters.formatCompactCurrency(averageMonthlyExpense),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = LilacPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Interactive Smooth Spline Canvas Line Chart ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val lineColor = NavySkyPrimary
                val incomeLineColor = MintCyan
                val gridColor = BorderDarkSubtle.copy(alpha = 0.6f)
                val nodeBgColor = DarkSurfaceElevated

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    val count = monthlyCashflows.size
                    if (count < 2) return@Canvas

                    val stepX = size.width / (count - 1).toFloat()
                    val chartHeight = size.height - 24.dp.toPx()
                    val progress = animationProgress.value

                    // Draw 3 horizontal dashed reference baseline gridlines
                    val gridLines = listOf(0f, 0.5f, 1f)
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    gridLines.forEach { ratio ->
                        val y = chartHeight * (1f - ratio) + 8.dp.toPx()
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = pathEffect
                        )
                    }

                    // Compute Point Coordinates for Expense Curve
                    val expensePoints = monthlyCashflows.mapIndexed { idx, item ->
                        val x = idx * stepX
                        val ratio = (item.expense / maxAmount).toFloat().coerceIn(0f, 1f)
                        val y = chartHeight * (1f - ratio * progress) + 8.dp.toPx()
                        Offset(x, y)
                    }

                    // Draw Income Curve if Dual Trend is enabled
                    if (showDualTrend) {
                        val incomePoints = monthlyCashflows.mapIndexed { idx, item ->
                            val x = idx * stepX
                            val ratio = (item.income / maxAmount).toFloat().coerceIn(0f, 1f)
                            val y = chartHeight * (1f - ratio * progress) + 8.dp.toPx()
                            Offset(x, y)
                        }

                        val incomePath = Path().apply {
                            moveTo(incomePoints[0].x, incomePoints[0].y)
                            for (i in 0 until count - 1) {
                                val p0 = incomePoints[i]
                                val p1 = incomePoints[i + 1]
                                val controlX1 = (p0.x + p1.x) / 2f
                                val controlY1 = p0.y
                                val controlX2 = (p0.x + p1.x) / 2f
                                val controlY2 = p1.y
                                cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                            }
                        }

                        drawPath(
                            path = incomePath,
                            color = incomeLineColor.copy(alpha = 0.8f),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // Build Smooth Cubic Bézier Spline Path for Expense
                    val strokePath = Path().apply {
                        moveTo(expensePoints[0].x, expensePoints[0].y)
                        for (i in 0 until count - 1) {
                            val p0 = expensePoints[i]
                            val p1 = expensePoints[i + 1]
                            val controlX1 = (p0.x + p1.x) / 2f
                            val controlY1 = p0.y
                            val controlX2 = (p0.x + p1.x) / 2f
                            val controlY2 = p1.y
                            cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                        }
                    }

                    // Build Closed Area Gradient Fill Path
                    val fillPath = Path().apply {
                        addPath(strokePath)
                        lineTo(expensePoints.last().x, chartHeight + 8.dp.toPx())
                        lineTo(expensePoints.first().x, chartHeight + 8.dp.toPx())
                        close()
                    }

                    // Draw Gradient Area under curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.35f * progress),
                                lineColor.copy(alpha = 0.05f * progress),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = chartHeight + 8.dp.toPx()
                        )
                    )

                    // Draw Main Glowing Line Path
                    drawPath(
                        path = strokePath,
                        color = lineColor,
                        style = Stroke(
                            width = 3.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw Nodes and Interactive Highlight Rings
                    expensePoints.forEachIndexed { idx, point ->
                        val isSelected = selectedIndex == idx
                        if (isSelected) {
                            // Pulsing / glowing outer halo
                            drawCircle(
                                color = lineColor.copy(alpha = 0.25f),
                                radius = 12.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = lineColor,
                                radius = 6.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = point
                            )
                        } else {
                            drawCircle(
                                color = nodeBgColor,
                                radius = 4.5.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = lineColor,
                                radius = 3.5.dp.toPx(),
                                style = Stroke(width = 2.dp.toPx()),
                                center = point
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Month Node Selectors along X-axis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                monthlyCashflows.forEachIndexed { idx, item ->
                    val isSelected = selectedIndex == idx
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) NavySkyPrimary else Color.Transparent,
                        contentColor = if (isSelected) NavyMidnight else TextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedIndex = idx
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.monthLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
