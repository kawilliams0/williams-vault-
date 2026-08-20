package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    initialType: String = "EXPENSE",
    onDismiss: () -> Unit,
    onAddNewCategory: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountId: Long,
        accountName: String,
        dateMillis: Long,
        note: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    val filteredCategories = categories.filter { it.type == selectedType }.ifEmpty {
        if (selectedType == "EXPENSE") FinanceCategories.defaultExpenseCategories else FinanceCategories.defaultIncomeCategories
    }

    var selectedCategory by remember(selectedType, filteredCategories) {
        mutableStateOf(filteredCategories.firstOrNull()?.name ?: "Food & Chai")
    }

    var selectedAccount by remember(accounts) {
        mutableStateOf(accounts.firstOrNull())
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        modifier = Modifier.testTag("add_transaction_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "New Transaction",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Segmented Type Selector: Expense vs Income
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedType == "EXPENSE",
                    onClick = { selectedType = "EXPENSE" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = SecondaryDarkContainer,
                        activeContentColor = CoralRed,
                        inactiveContainerColor = DarkBackground,
                        inactiveContentColor = TextSecondary
                    )
                ) {
                    Text("Expense", fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = selectedType == "INCOME",
                    onClick = { selectedType = "INCOME" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MintCyanContainer,
                        activeContentColor = MintCyan,
                        inactiveContainerColor = DarkBackground,
                        inactiveContentColor = TextSecondary
                    )
                ) {
                    Text("Income", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Big Amount Input (INR)
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                        amountText = input
                    }
                },
                label = { Text("Amount (₹ INR)") },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedType == "EXPENSE") CoralRed else MintCyan
                    )
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_amount_input")
            )

            // Quick Preset Amount Chips (INR context: +₹100, +₹500, +₹1,000, +₹2,000)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(100, 250, 500, 1000).forEach { preset ->
                    AssistChip(
                        onClick = {
                            val current = amountText.toDoubleOrNull() ?: 0.0
                            amountText = String.format("%.0f", current + preset)
                        },
                        label = { Text("+₹$preset", color = TextPrimary) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = SecondaryDarkContainer)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title / Payee
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text(if (selectedType == "EXPENSE") "Merchant / Description" else "Income Source") },
                placeholder = { Text(if (selectedType == "EXPENSE") "e.g. Swiggy, DMart, Petrol" else "e.g. Monthly Salary, Freelance") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection with CategorySelectorGrid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                TextButton(onClick = onAddNewCategory) {
                    Text("+ Add Custom", color = LilacPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            CategorySelectorGrid(
                categories = filteredCategories,
                selectedCategoryName = selectedCategory,
                onSelectCategory = { selectedCategory = it.name },
                onAddNewCategory = onAddNewCategory
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Account Selector
            if (accounts.isNotEmpty()) {
                Text(
                    text = "Paid with Account / UPI",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = selectedAccount?.id == acc.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAccount = acc },
                            label = { Text(acc.name, color = if (isSelected) LilacPrimary else TextPrimary) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = LilacPrimary, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Note input
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note (Optional)") },
                placeholder = { Text("Add any extra details") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoralRed
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorMessage = "Please enter a valid positive amount."
                        return@Button
                    }
                    if (titleText.isBlank()) {
                        errorMessage = "Please enter a description or merchant name."
                        return@Button
                    }
                    val acc = selectedAccount ?: accounts.firstOrNull()
                    val accId = acc?.id ?: 1L
                    val accName = acc?.name ?: "Main Account"

                    onSave(
                        titleText.trim(),
                        amt,
                        selectedType,
                        selectedCategory,
                        accId,
                        accName,
                        System.currentTimeMillis(),
                        noteText.trim()
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LilacPrimary,
                    contentColor = LilacOnPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button")
            ) {
                Text(
                    text = "Save Transaction",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, balance: Double, last4: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("SAVINGS") }
    var balanceText by remember { mutableStateOf("") }
    var last4 by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#D0BCFF") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val colorOptions = listOf("#D0BCFF", "#B4EBEB", "#E1E3AD", "#F2B8B5", "#CCC2DC", "#80D8D8")
    val typeOptions = listOf(
        "SAVINGS" to "Bank (Savings)",
        "CHECKING" to "Current A/c",
        "CASH" to "Cash in Hand",
        "CREDIT_CARD" to "Credit Card",
        "INVESTMENT" to "Mutual Funds / Stocks"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Add Account / UPI Wallet", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    placeholder = { Text("e.g. HDFC Bank, GPay, Paytm") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_name_input")
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Account Type", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    typeOptions.take(3).forEach { (key, label) ->
                        FilterChip(
                            selected = type == key,
                            onClick = { type = key },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    typeOptions.drop(3).forEach { (key, label) ->
                        FilterChip(
                            selected = type == key,
                            onClick = { type = key },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d{0,2}$"""))) balanceText = it },
                    label = { Text("Current Balance (₹ INR)") },
                    prefix = { Text("₹ ", color = LilacPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = last4,
                    onValueChange = { if (it.length <= 4) last4 = it },
                    label = { Text("Last 4 Digits (Optional)") },
                    placeholder = { Text("e.g. 5678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Accent Tone", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 0.dp,
                                    color = if (selectedColor == hex) TextPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = CoralRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter an account name."
                        return@Button
                    }
                    val bal = balanceText.toDoubleOrNull() ?: 0.0
                    onSave(name.trim(), type, bal, last4.trim(), selectedColor)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LilacPrimary,
                    contentColor = LilacOnPrimary
                ),
                modifier = Modifier.testTag("save_account_button")
            ) {
                Text("Save Account", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun AddBudgetDialog(
    categories: List<CategoryEntity>,
    initialCategory: String? = null,
    initialLimit: Double? = null,
    onDismiss: () -> Unit,
    onSave: (category: String, monthlyLimit: Double) -> Unit
) {
    val expenseCategories = categories.filter { it.type == "EXPENSE" }.ifEmpty { FinanceCategories.defaultExpenseCategories }
    var selectedCategory by remember(expenseCategories, initialCategory) {
        mutableStateOf(initialCategory ?: expenseCategories.first().name)
    }
    var limitText by remember(initialLimit) {
        mutableStateOf(initialLimit?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = if (initialLimit != null) "Update Category Budget" else "Set Category Budget",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(expenseCategories, key = { it.name }) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(cat.name, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d{0,2}$"""))) limitText = it },
                    label = { Text("Monthly Spending Limit (₹ INR)") },
                    placeholder = { Text("e.g. 5000") },
                    prefix = { Text("₹ ", color = LilacPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = CoralRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull()
                    if (limit == null || limit <= 0) {
                        errorMessage = "Please enter a valid monthly limit."
                        return@Button
                    }
                    onSave(selectedCategory, limit)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LilacPrimary,
                    contentColor = LilacOnPrimary
                ),
                modifier = Modifier.testTag("save_budget_button")
            ) {
                Text(if (initialLimit != null) "Update Limit" else "Save Budget", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, targetAmount: Double, currentAmount: Double, targetDateMillis: Long, colorHex: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var currentText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#B4EBEB") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val colorOptions = listOf("#B4EBEB", "#D0BCFF", "#E1E3AD", "#F2B8B5", "#80D8D8", "#CCC2DC")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("New Savings Goal", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. Diwali Vacation, Emergency Fund") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d{0,2}$"""))) targetText = it },
                    label = { Text("Target Goal (₹ INR)") },
                    placeholder = { Text("50000") },
                    prefix = { Text("₹ ", color = LilacPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currentText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d{0,2}$"""))) currentText = it },
                    label = { Text("Already Saved (₹ INR)") },
                    placeholder = { Text("0") },
                    prefix = { Text("₹ ", color = LilacPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Theme Color", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 0.dp,
                                    color = if (selectedColor == hex) TextPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = CoralRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a goal title."
                        return@Button
                    }
                    val target = targetText.toDoubleOrNull()
                    if (target == null || target <= 0) {
                        errorMessage = "Please enter a valid target amount."
                        return@Button
                    }
                    val current = currentText.toDoubleOrNull() ?: 0.0
                    val targetDate = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000)
                    onSave(title.trim(), target, current, targetDate, selectedColor)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LilacPrimary,
                    contentColor = LilacOnPrimary
                )
            ) {
                Text("Create Goal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun DepositGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onDeposit: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Deposit to ${goal.title}", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Current saved: ${FinanceFormatters.formatCurrency(goal.currentAmount)} / ${FinanceFormatters.formatCurrency(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d{0,2}$"""))) amountText = it },
                    label = { Text("Deposit Amount (₹ INR)") },
                    prefix = { Text("₹ ", color = LilacPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = CoralRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        errorMessage = "Please enter a valid deposit amount."
                        return@Button
                    }
                    onDeposit(amt)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LilacPrimary,
                    contentColor = LilacOnPrimary
                )
            ) {
                Text("Deposit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun AddRecurringBillDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, category: String, dueDay: Int) -> Unit
) {
    val expenseCategories = categories.filter { it.type == "EXPENSE" }.ifEmpty { FinanceCategories.defaultExpenseCategories }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueDayText by remember { mutableStateOf("1") }
    var selectedCategory by remember(expenseCategories) { mutableStateOf(expenseCategories.first().name) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Add Subscription / Bill", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill / Subscription Name") },
                    placeholder = { Text("e.g. Electricity, Jio Fiber, Netflix") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d{0,2}$"""))) amountText = it },
                    label = { Text("Monthly Amount (₹ INR)") },
                    prefix = { Text("₹ ", color = LilacPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dueDayText,
                    onValueChange = { if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 1..31)) dueDayText = it },
                    label = { Text("Due Day of Month (1 - 31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = CoralRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a bill name."
                        return@Button
                    }
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        errorMessage = "Please enter a valid amount."
                        return@Button
                    }
                    val day = dueDayText.toIntOrNull() ?: 1
                    onSave(title.trim(), amt, selectedCategory, day)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LilacPrimary,
                    contentColor = LilacOnPrimary
                )
            ) {
                Text("Save Bill", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
