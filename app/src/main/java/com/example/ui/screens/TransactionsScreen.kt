package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.FinanceViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    categories: List<CategoryEntity>,
    filteredTransactions: List<TransactionEntity>,
    onAddTransaction: () -> Unit,
    onManageCategories: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onDownloadPdf: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onExportFilteredCsv: (List<TransactionEntity>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedTypeFilter.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRangeFilter.collectAsState()
    val selectedAmountPreset by viewModel.selectedAmountPreset.collectAsState()
    val minAmount by viewModel.minAmountFilter.collectAsState()
    val maxAmount by viewModel.maxAmountFilter.collectAsState()
    val customStartDate by viewModel.customStartDateMillis.collectAsState()
    val customEndDate by viewModel.customEndDateMillis.collectAsState()

    var showCustomDateDialog by remember { mutableStateOf(false) }
    var showCustomAmountDialog by remember { mutableStateOf(false) }

    val filteredIncome = filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val filteredExpense = filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

    val isFilterActive = searchQuery.isNotBlank() ||
            selectedType != "ALL" ||
            selectedCategory != "ALL" ||
            selectedTimeRange != "THIS_MONTH" ||
            minAmount != null ||
            maxAmount != null ||
            selectedAmountPreset != "ALL"

    val activeCount = viewModel.getActiveFilterCount()
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    if (showCustomDateDialog) {
        CustomDateRangeDialog(
            initialStartMillis = customStartDate,
            initialEndMillis = customEndDate,
            onDismiss = { showCustomDateDialog = false },
            onApplyRange = { start, end ->
                viewModel.setCustomDateRange(start, end)
                showCustomDateDialog = false
            }
        )
    }

    if (showCustomAmountDialog) {
        CustomAmountRangeDialog(
            initialMinAmount = minAmount,
            initialMaxAmount = maxAmount,
            onDismiss = { showCustomAmountDialog = false },
            onApplyRange = { min, max ->
                viewModel.setCustomAmountBounds(min, max)
                showCustomAmountDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("transactions_screen"),
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
                    text = "Transactions",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp
                    ),
                    color = TextPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onExportCsv,
                        modifier = Modifier.testTag("export_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Export Transactions CSV",
                            tint = MintCyan
                        )
                    }
                    IconButton(
                        onClick = onDownloadPdf,
                        modifier = Modifier.testTag("download_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Download PDF Statement",
                            tint = NavySkyPrimary
                        )
                    }
                    IconButton(
                        onClick = onManageCategories,
                        modifier = Modifier.testTag("manage_categories_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Manage Categories",
                            tint = LilacPrimary
                        )
                    }
                    Button(
                        onClick = onAddTransaction,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LilacPrimary,
                            contentColor = LilacOnPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_transaction_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Bar (Keyword, Date, or Amount)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("transactions_search_input"),
                    placeholder = { Text("Search keyword, date, or amount...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (searchQuery.isNotEmpty()) LilacPrimary else TextSecondary
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.searchQuery.value = "" },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = LilacPrimary,
                        unfocusedBorderColor = BorderDarkSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Custom Date Picker Shortcut Button
                IconButton(
                    onClick = { showCustomDateDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selectedTimeRange == "CUSTOM") LilacContainer else DarkSurface)
                        .testTag("custom_date_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Filter by date",
                        tint = if (selectedTimeRange == "CUSTOM") LilacPrimary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Custom Amount Shortcut Button
                IconButton(
                    onClick = { showCustomAmountDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (minAmount != null || maxAmount != null || selectedAmountPreset != "ALL") MintCyanContainer else DarkSurface)
                        .testTag("custom_amount_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyRupee,
                        contentDescription = "Filter by amount",
                        tint = if (minAmount != null || maxAmount != null || selectedAmountPreset != "ALL") MintCyan else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Active Filter Chips Row (Quick Dismiss)
        if (isFilterActive) {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CoralRedContainer.copy(alpha = 0.6f),
                            contentColor = CoralRed,
                            modifier = Modifier
                                .bouncyClickable { viewModel.clearAllFilters() }
                                .testTag("clear_all_filters_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset ($activeCount)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    if (searchQuery.isNotBlank()) {
                        item {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.searchQuery.value = "" },
                                label = { Text("\"$searchQuery\"", fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                colors = InputChipDefaults.inputChipColors(selectedContainerColor = LilacContainer, selectedLabelColor = LilacPrimary)
                            )
                        }
                    }

                    if (selectedTimeRange != "THIS_MONTH") {
                        item {
                            val dateLabel = when (selectedTimeRange) {
                                "THIS_WEEK" -> "This Week"
                                "LAST_30_DAYS" -> "30 Days"
                                "LAST_3_MONTHS" -> "3 Months"
                                "THIS_YEAR" -> "This Year"
                                "ALL_TIME" -> "All Time"
                                "CUSTOM" -> {
                                    if (customStartDate != null && customEndDate != null && customStartDate == customEndDate) {
                                        dateFormat.format(Date(customStartDate!!))
                                    } else if (customStartDate != null && customEndDate != null) {
                                        "${dateFormat.format(Date(customStartDate!!))} - ${dateFormat.format(Date(customEndDate!!))}"
                                    } else {
                                        "Custom Date"
                                    }
                                }
                                else -> selectedTimeRange
                            }
                            InputChip(
                                selected = true,
                                onClick = {
                                    viewModel.selectedTimeRangeFilter.value = "THIS_MONTH"
                                    viewModel.customStartDateMillis.value = null
                                    viewModel.customEndDateMillis.value = null
                                },
                                label = { Text("Date: $dateLabel", fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                colors = InputChipDefaults.inputChipColors(selectedContainerColor = LilacContainer, selectedLabelColor = LilacPrimary)
                            )
                        }
                    }

                    if (minAmount != null || maxAmount != null || selectedAmountPreset != "ALL") {
                        item {
                            val amountLabel = when {
                                minAmount != null && maxAmount != null -> "₹${minAmount!!.toInt()} - ₹${maxAmount!!.toInt()}"
                                minAmount != null -> "> ₹${minAmount!!.toInt()}"
                                maxAmount != null -> "< ₹${maxAmount!!.toInt()}"
                                else -> "Amount Filter"
                            }
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setAmountPreset("ALL") },
                                label = { Text(amountLabel, fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                colors = InputChipDefaults.inputChipColors(selectedContainerColor = MintCyanContainer, selectedLabelColor = MintCyan)
                            )
                        }
                    }

                    if (selectedType != "ALL") {
                        item {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.selectedTypeFilter.value = "ALL" },
                                label = { Text(if (selectedType == "EXPENSE") "Expenses" else "Income", fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = if (selectedType == "EXPENSE") CoralRedContainer else MintCyanContainer,
                                    selectedLabelColor = if (selectedType == "EXPENSE") CoralRed else MintCyan
                                )
                            )
                        }
                    }

                    if (selectedCategory != "ALL") {
                        item {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.selectedCategoryFilter.value = "ALL" },
                                label = { Text(selectedCategory, fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                colors = InputChipDefaults.inputChipColors(selectedContainerColor = SecondaryDarkContainer, selectedLabelColor = TextPrimary)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Category Filter Pill Row (e.g. Food, Rent, Entertainment)
        item {
            CategoryFilterPillRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onSelectCategory = { viewModel.selectedCategoryFilter.value = it },
                onManageCategories = onManageCategories
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Type Filter Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedType == "ALL",
                        onClick = { viewModel.selectedTypeFilter.value = "ALL" },
                        label = { Text("All Types", fontWeight = if (selectedType == "ALL") FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LilacPrimary,
                            selectedLabelColor = LilacOnPrimary,
                            containerColor = SecondaryDarkContainer,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedType == "ALL",
                            borderColor = BorderDarkSubtle
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == "EXPENSE",
                        onClick = { viewModel.selectedTypeFilter.value = "EXPENSE" },
                        label = { Text("Expenses", fontWeight = if (selectedType == "EXPENSE") FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralRedContainer,
                            selectedLabelColor = CoralRed,
                            containerColor = SecondaryDarkContainer,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedType == "EXPENSE",
                            borderColor = BorderDarkSubtle
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == "INCOME",
                        onClick = { viewModel.selectedTypeFilter.value = "INCOME" },
                        label = { Text("Income", fontWeight = if (selectedType == "INCOME") FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintCyanContainer,
                            selectedLabelColor = MintCyan,
                            containerColor = SecondaryDarkContainer,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedType == "INCOME",
                            borderColor = BorderDarkSubtle
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Date Filter Chips Row
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val timeOptions = listOf(
                    "THIS_MONTH" to "This Month",
                    "THIS_WEEK" to "This Week",
                    "LAST_30_DAYS" to "Last 30 Days",
                    "LAST_3_MONTHS" to "Last 3 Months",
                    "THIS_YEAR" to "This Year",
                    "ALL_TIME" to "All Time"
                )
                timeOptions.forEach { (key, label) ->
                    item {
                        FilterChip(
                            selected = selectedTimeRange == key,
                            onClick = {
                                viewModel.selectedTimeRangeFilter.value = key
                                viewModel.customStartDateMillis.value = null
                                viewModel.customEndDateMillis.value = null
                            },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (selectedTimeRange == key) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LilacPrimary,
                                selectedLabelColor = LilacOnPrimary,
                                containerColor = DarkSurfaceElevated,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedTimeRange == key,
                                borderColor = BorderDarkSubtle
                            )
                        )
                    }
                }
                item {
                    FilterChip(
                        selected = selectedTimeRange == "CUSTOM",
                        onClick = { showCustomDateDialog = true },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = {
                            val labelText = if (selectedTimeRange == "CUSTOM" && customStartDate != null) {
                                if (customStartDate == customEndDate) dateFormat.format(Date(customStartDate!!))
                                else "${dateFormat.format(Date(customStartDate!!))} - ${dateFormat.format(Date(customEndDate ?: customStartDate!!))}"
                            } else {
                                "Pick Date"
                            }
                            Text(labelText, fontSize = 12.sp, fontWeight = if (selectedTimeRange == "CUSTOM") FontWeight.Bold else FontWeight.Normal)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LilacPrimary,
                            selectedLabelColor = LilacOnPrimary,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedTimeRange == "CUSTOM",
                            borderColor = BorderDarkSubtle
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Amount Filter Chips Row
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val amountPresets = listOf(
                    "ALL" to "All Amounts",
                    "UNDER_500" to "< ₹500",
                    "500_2000" to "₹500 - 2k",
                    "2000_10000" to "₹2k - 10k",
                    "OVER_10000" to "> ₹10k"
                )
                amountPresets.forEach { (preset, label) ->
                    item {
                        FilterChip(
                            selected = selectedAmountPreset == preset && minAmount == null && maxAmount == null && preset == "ALL" || (selectedAmountPreset == preset && preset != "ALL"),
                            onClick = { viewModel.setAmountPreset(preset) },
                            label = {
                                Text(
                                    label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedAmountPreset == preset) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MintCyanContainer,
                                selectedLabelColor = MintCyan,
                                containerColor = DarkSurfaceElevated,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedAmountPreset == preset,
                                borderColor = BorderDarkSubtle
                            )
                        )
                    }
                }
                item {
                    FilterChip(
                        selected = selectedAmountPreset == "CUSTOM",
                        onClick = { showCustomAmountDialog = true },
                        leadingIcon = {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = {
                            val labelText = if (selectedAmountPreset == "CUSTOM") {
                                if (minAmount != null && maxAmount != null) "₹${minAmount!!.toInt()} - ₹${maxAmount!!.toInt()}"
                                else if (minAmount != null) "> ₹${minAmount!!.toInt()}"
                                else if (maxAmount != null) "< ₹${maxAmount!!.toInt()}"
                                else "Custom ₹"
                            } else "Custom ₹"
                            Text(labelText, fontSize = 12.sp, fontWeight = if (selectedAmountPreset == "CUSTOM") FontWeight.Bold else FontWeight.Normal)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintCyanContainer,
                            selectedLabelColor = MintCyan,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedAmountPreset == "CUSTOM",
                            borderColor = BorderDarkSubtle
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Filter Summary Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredTransactions.size} transactions" + if (selectedCategory != "ALL") " in $selectedCategory" else "",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (filteredIncome > 0) {
                                Text(
                                    text = "+${FinanceFormatters.formatCurrency(filteredIncome)}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MintCyan
                                )
                            }
                            if (filteredExpense > 0) {
                                Text(
                                    text = "-${FinanceFormatters.formatCurrency(filteredExpense)}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    if (filteredTransactions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = BorderDarkSubtle, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ready to backup or open in Sheets/Excel",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MintCyanContainer,
                                contentColor = MintCyan,
                                modifier = Modifier
                                    .bouncyClickable {
                                        onExportFilteredCsv(filteredTransactions)
                                    }
                                    .testTag("export_filtered_csv_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MintCyan
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Export CSV",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            item {
                if (isFilterActive) {
                    EmptyStateView(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "No Matches Found",
                        subtitle = "No transactions found for the current search, date range, or amount filters.",
                        actionButtonText = "Reset All Filters",
                        onActionClick = { viewModel.clearAllFilters() }
                    )
                } else {
                    EmptyStateView(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "No Transactions Found",
                        subtitle = "Try adjusting your category filter or time range, or log a new transaction.",
                        actionButtonText = "Add Transaction",
                        onActionClick = onAddTransaction
                    )
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                    TransactionItemRow(
                        transaction = tx,
                        onDelete = { onDeleteTransaction(tx) }
                    )
                }
            }
        }
    }
}

