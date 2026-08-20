package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.model.toCategoryItem
import com.example.ui.theme.*

// ----------------------------------------------------
// 1. Overall Monthly Budget Cap Setter Dialog
// ----------------------------------------------------
@Composable
fun SetMonthlyCapDialog(
    currentCap: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var capText by remember { mutableStateOf(if (currentCap > 0) currentCap.toInt().toString() else "35000") }
    val presetCaps = listOf(15000.0, 25000.0, 35000.0, 50000.0, 75000.0, 100000.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = LilacPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Monthly Spending Cap", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Set your single overall monthly budget limit. The dashboard automatically resets on the 1st of every month.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = capText,
                    onValueChange = { capText = it },
                    label = { Text("Monthly Cap (₹ INR)", color = TextMuted) },
                    prefix = { Text("₹ ", color = LilacPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedBorderColor = LilacPrimary,
                        unfocusedBorderColor = BorderDarkSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("monthly_cap_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Quick Presets:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetCaps.take(3).forEach { amount ->
                        AssistChip(
                            onClick = { capText = amount.toInt().toString() },
                            label = { Text("₹${(amount/1000).toInt()}k", fontSize = 11.sp, color = TextPrimary) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = SecondaryDarkContainer),
                            border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = BorderDarkSubtle)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetCaps.drop(3).forEach { amount ->
                        AssistChip(
                            onClick = { capText = amount.toInt().toString() },
                            label = { Text("₹${(amount/1000).toInt()}k", fontSize = 11.sp, color = TextPrimary) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = SecondaryDarkContainer),
                            border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = BorderDarkSubtle)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = capText.toDoubleOrNull() ?: 35000.0
                    onConfirm(value)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LilacPrimary, contentColor = LilacOnPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_monthly_cap_button")
            ) {
                Text("Save Cap", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

// ----------------------------------------------------
// 2. Two-Click Quick Log Modal (Fast 2-Click Entry)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogTwoClickModal(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onLogExpense: (amount: Double, title: String, category: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedAmount by remember { mutableStateOf(100.0) }
    var customAmountText by remember { mutableStateOf("") }
    var customTitleText by remember { mutableStateOf("") }

    val presetAmounts = listOf(50.0, 100.0, 200.0, 500.0, 1000.0, 2000.0)
    val expenseCategories = categories.filter { it.type == "EXPENSE" }.ifEmpty { FinanceCategories.defaultExpenseCategories }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        contentColor = TextPrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderDark)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("quick_log_two_click_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AmberGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("2-Click Quick Expense", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("1. Pick Amount → 2. Tap Category to log!", style = MaterialTheme.typography.bodySmall, color = LilacPrimary)
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Select Amount
            Text("Step 1: Choose Amount (₹)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetAmounts.forEach { amt ->
                    val isSelected = selectedAmount == amt && customAmountText.isEmpty()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) LilacPrimary else DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) LilacPrimary else BorderDarkSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedAmount = amt
                                customAmountText = ""
                            }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "₹${amt.toInt()}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) LilacOnPrimary else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Amount input optional
            OutlinedTextField(
                value = customAmountText,
                onValueChange = {
                    customAmountText = it
                    val parsed = it.toDoubleOrNull()
                    if (parsed != null && parsed > 0) selectedAmount = parsed
                },
                placeholder = { Text("Or enter custom ₹ amount...", color = TextMuted) },
                prefix = { Text("₹ ", color = LilacPrimary, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedBorderColor = LilacPrimary,
                    unfocusedBorderColor = BorderDarkSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Tap Category to LOG immediately
            Text("Step 2: Tap Category to Log Expense", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                items(expenseCategories) { cat ->
                    val catItem = cat.toCategoryItem()
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bouncyClickable(pressedScale = 0.92f) {
                                val finalAmount = if (customAmountText.isNotBlank()) {
                                    customAmountText.toDoubleOrNull() ?: selectedAmount
                                } else selectedAmount

                                val title = if (customTitleText.isNotBlank()) customTitleText else "${cat.name} (Quick)"
                                onLogExpense(finalAmount, title, cat.name)
                                onDismiss()
                            }
                            .testTag("quick_log_cat_${cat.name.replace(" ", "_")}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
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
                                    imageVector = catItem.icon,
                                    contentDescription = cat.name,
                                    tint = LilacPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ----------------------------------------------------
// 3. Biometric & 4-Digit PIN Lock Screen Overlay
// ----------------------------------------------------
@Composable
fun PinLockScreen(
    onUnlockSuccess: () -> Unit,
    savedPin: String,
    onBiometricClick: () -> Unit,
    isBiometricAvailable: Boolean = true
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("pin_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Lock Icon Badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SecondaryDarkContainer)
                    .border(1.dp, BorderDarkSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Lock",
                    tint = if (isError) CoralRed else LilacPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Williams Vault Locked",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isError) "Incorrect PIN. Try again." else "Enter your 4-digit PIN to access",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) CoralRed else TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 4-Dot PIN Indicator
            Row(
                modifier = Modifier
                    .shakeOnError(isError)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    val dotScale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isFilled) 1.25f else 1.0f,
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                        ),
                        label = "dot_scale_$i"
                    )

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer {
                                scaleX = dotScale
                                scaleY = dotScale
                            }
                            .clip(CircleShape)
                            .background(
                                when {
                                    isError -> CoralRed
                                    isFilled -> LilacPrimary
                                    else -> SecondaryDarkContainer
                                }
                            )
                            .border(
                                1.dp,
                                if (isFilled) LilacPrimary else BorderDark,
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Numeric Keypad (3x4)
            val keypadNumbers = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                keypadNumbers.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            when (key) {
                                "DEL" -> {
                                    IconButton(
                                        onClick = {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                                isError = false
                                            }
                                        },
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Delete",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                "BIO" -> {
                                    if (isBiometricAvailable) {
                                        IconButton(
                                            onClick = onBiometricClick,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(SecondaryDarkContainer)
                                                .border(1.dp, BorderDarkSubtle, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Fingerprint,
                                                contentDescription = "Biometric Unlock",
                                                tint = LilacPrimary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(64.dp))
                                    }
                                }
                                else -> {
                                    Surface(
                                        shape = CircleShape,
                                        color = DarkSurfaceElevated,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle),
                                        modifier = Modifier
                                            .size(64.dp)
                                            .bouncyClickable(pressedScale = 0.88f) {
                                                if (enteredPin.length < 4) {
                                                    val newPin = enteredPin + key
                                                    enteredPin = newPin
                                                    isError = false
                                                    if (newPin.length == 4) {
                                                        if (newPin == savedPin || savedPin.isEmpty()) {
                                                            onUnlockSuccess()
                                                        } else {
                                                            isError = true
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                                                color = TextPrimary
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

// ----------------------------------------------------
// 4. App Settings, Theme Palette & PIN Dialog
// ----------------------------------------------------
@Composable
fun SecuritySettingsDialog(
    isPinEnabled: Boolean,
    currentPin: String,
    isBiometricEnabled: Boolean = true,
    onToggleBiometric: (Boolean) -> Unit = {},
    isDarkMode: Boolean = true,
    onToggleDarkMode: () -> Unit = {},
    selectedThemePalette: String = "NAVY_SKY",
    onSelectThemePalette: (String) -> Unit = {},
    onResetData: () -> Unit = {},
    onDismiss: () -> Unit,
    onSetPin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onLockNow: () -> Unit
) {
    var mode by remember { mutableStateOf("OVERVIEW") } // "OVERVIEW", "SETUP", "CHANGE", "DISABLE", "RESET_CONFIRM"
    var enteredOldPin by remember { mutableStateOf("") }
    var enteredNewPin by remember { mutableStateOf("") }
    var enteredConfirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (mode) {
                        "SETUP", "CHANGE", "DISABLE" -> Icons.Filled.Lock
                        "RESET_CONFIRM" -> Icons.Outlined.RestartAlt
                        else -> Icons.Outlined.Palette
                    },
                    contentDescription = null,
                    tint = if (mode == "RESET_CONFIRM") CoralRed else LilacPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when (mode) {
                        "SETUP" -> "Set Security PIN"
                        "CHANGE" -> "Change Security PIN"
                        "DISABLE" -> "Disable PIN Lock"
                        "RESET_CONFIRM" -> "Reset All Data?"
                        else -> "Settings & Appearance"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .testTag("security_settings_dialog")
            ) {
                when (mode) {
                    "OVERVIEW" -> {
                        // --- Section 1: Color Palette Selector ---
                        Text(
                            text = "APP COLOR THEME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = LilacPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Palette Cards / Swatches Grid
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AppThemePalette.entries.forEach { palette ->
                                val isSelected = palette.id.equals(selectedThemePalette, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) SecondaryDarkContainer else DarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) palette.primaryPreview else BorderDarkSubtle
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectThemePalette(palette.id) }
                                        .testTag("palette_option_${palette.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Palette Color Swatches Preview
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(palette.primaryPreview)
                                                        .border(1.dp, BorderDarkSubtle, CircleShape)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(palette.accentPreview)
                                                        .border(1.dp, BorderDarkSubtle, CircleShape)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isDarkMode) palette.darkBgPreview else palette.lightBgPreview)
                                                        .border(1.dp, BorderDarkSubtle, CircleShape)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = palette.displayName,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = TextPrimary,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = palette.description,
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = "Selected",
                                                tint = palette.primaryPreview,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- Section 2: Dark / Light Theme Mode Toggle ---
                        Text(
                            text = "DISPLAY MODE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = LilacPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(SecondaryDarkContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                            contentDescription = null,
                                            tint = AmberGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (isDarkMode) "Dark Mode (Midnight)" else "Light Mode (Crisp)",
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (isDarkMode) "Optimized for OLED & low-light" else "High contrast clean daylight",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = { onToggleDarkMode() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = LilacPrimary,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = SecondaryDarkContainer
                                    ),
                                    modifier = Modifier.testTag("theme_toggle_switch")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- Section 3: Privacy & Security ---
                        Text(
                            text = "SECURITY & PRIVACY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = LilacPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // PIN Lock status card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (isPinEnabled) MintCyanContainer else SecondaryDarkContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isPinEnabled) Icons.Default.Lock else Icons.Outlined.Lock,
                                                contentDescription = null,
                                                tint = if (isPinEnabled) MintCyan else TextMuted,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (isPinEnabled) "PIN Protection Active" else "PIN Protection Off",
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = if (isPinEnabled) "4-digit offline PIN enabled" else "Tap to set 4-digit PIN",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (!isPinEnabled) {
                                        FilledTonalButton(
                                            onClick = { mode = "SETUP" },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Set PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (isPinEnabled) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                errorMessage = null
                                                enteredOldPin = ""
                                                enteredNewPin = ""
                                                enteredConfirmPin = ""
                                                mode = "CHANGE"
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Text("Change PIN", fontSize = 12.sp, color = TextPrimary)
                                        }
                                        TextButton(
                                            onClick = {
                                                errorMessage = null
                                                enteredOldPin = ""
                                                mode = "DISABLE"
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Text("Turn Off", fontSize = 12.sp, color = CoralRed)
                                        }
                                    }
                                }
                            }
                        }

                        if (isPinEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Biometric toggle
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(SecondaryDarkContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Fingerprint,
                                                contentDescription = null,
                                                tint = if (isBiometricEnabled) MintCyan else TextMuted,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Biometric / Face Unlock", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                            Text("Quick unlock using fingerprint", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                    Switch(
                                        checked = isBiometricEnabled,
                                        onCheckedChange = onToggleBiometric,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = PureWhite,
                                            checkedTrackColor = MintCyan,
                                            uncheckedThumbColor = TextMuted,
                                            uncheckedTrackColor = SecondaryDarkContainer
                                        ),
                                        modifier = Modifier.testTag("biometric_toggle_switch")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    onDismiss()
                                    onLockNow()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LilacPrimary,
                                    contentColor = LilacOnPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("lock_now_button")
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lock App Now", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // --- Section 4: Data Management & Fresh Start ---
                        Text(
                            text = "DATA MANAGEMENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = CoralRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { mode = "RESET_CONFIRM" },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fresh_start_reset_button")
                        ) {
                            Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp), tint = CoralRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Data & Fresh Start", fontWeight = FontWeight.Bold, color = CoralRed)
                        }
                    }

                    "RESET_CONFIRM" -> {
                        Text(
                            text = "Are you sure you want to start fresh? This will clear all transactions, accounts, budgets, and bills. Default categories will be restored.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CoralRedContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ This offline action is permanent. Make sure you have exported a CSV backup if you need to keep previous records.",
                                color = CoralRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    "SETUP" -> {
                        Text(
                            text = "Create a 4-digit numeric PIN to protect your personal finance records.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = enteredNewPin,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) enteredNewPin = it },
                            label = { Text("New 4-Digit PIN", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_pin_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = LilacPrimary,
                                unfocusedBorderColor = BorderDarkSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = enteredConfirmPin,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) enteredConfirmPin = it },
                            label = { Text("Confirm 4-Digit PIN", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_pin_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = LilacPrimary,
                                unfocusedBorderColor = BorderDarkSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMessage!!, color = CoralRed, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    "CHANGE" -> {
                        Text(
                            text = "Enter your current PIN, then choose a new 4-digit PIN.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = enteredOldPin,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) enteredOldPin = it },
                            label = { Text("Current PIN", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = LilacPrimary,
                                unfocusedBorderColor = BorderDarkSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = enteredNewPin,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) enteredNewPin = it },
                            label = { Text("New 4-Digit PIN", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = LilacPrimary,
                                unfocusedBorderColor = BorderDarkSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = enteredConfirmPin,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) enteredConfirmPin = it },
                            label = { Text("Confirm New PIN", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = LilacPrimary,
                                unfocusedBorderColor = BorderDarkSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMessage!!, color = CoralRed, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    "DISABLE" -> {
                        Text(
                            text = "Enter your current 4-digit PIN to turn off app lock protection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = enteredOldPin,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) enteredOldPin = it },
                            label = { Text("Current PIN", color = TextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = LilacPrimary,
                                unfocusedBorderColor = BorderDarkSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMessage!!, color = CoralRed, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (mode) {
                "RESET_CONFIRM" -> {
                    Button(
                        onClick = {
                            onResetData()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralRed,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier.testTag("confirm_reset_button")
                    ) {
                        Text("Confirm & Start Fresh", fontWeight = FontWeight.Bold)
                    }
                }

                "SETUP" -> {
                    Button(
                        onClick = {
                            if (enteredNewPin.length != 4) {
                                errorMessage = "PIN must be exactly 4 digits."
                            } else if (enteredNewPin != enteredConfirmPin) {
                                errorMessage = "PINs do not match."
                            } else {
                                onSetPin(enteredNewPin)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LilacPrimary,
                            contentColor = LilacOnPrimary
                        )
                    ) {
                        Text("Save & Enable PIN", fontWeight = FontWeight.Bold)
                    }
                }

                "CHANGE" -> {
                    Button(
                        onClick = {
                            if (enteredOldPin != currentPin) {
                                errorMessage = "Current PIN is incorrect."
                            } else if (enteredNewPin.length != 4) {
                                errorMessage = "New PIN must be exactly 4 digits."
                            } else if (enteredNewPin != enteredConfirmPin) {
                                errorMessage = "New PINs do not match."
                            } else {
                                onSetPin(enteredNewPin)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LilacPrimary,
                            contentColor = LilacOnPrimary
                        )
                    ) {
                        Text("Update PIN", fontWeight = FontWeight.Bold)
                    }
                }

                "DISABLE" -> {
                    Button(
                        onClick = {
                            if (enteredOldPin != currentPin) {
                                errorMessage = "Current PIN is incorrect."
                            } else {
                                onDisablePin()
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralRed,
                            contentColor = PureWhite
                        )
                    ) {
                        Text("Disable PIN", fontWeight = FontWeight.Bold)
                    }
                }

                else -> {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LilacPrimary,
                            contentColor = LilacOnPrimary
                        )
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            if (mode != "OVERVIEW") {
                TextButton(
                    onClick = {
                        mode = "OVERVIEW"
                        errorMessage = null
                    }
                ) {
                    Text("Back", color = TextSecondary)
                }
            }
        }
    )
}

// ----------------------------------------------------
// 5. Download PDF & Export Data Options Modal
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportOptionsModal(
    onDismiss: () -> Unit,
    onDownloadPdf: () -> Unit,
    onExportTransactionsCsv: () -> Unit,
    onExportFullBackupCsv: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = BorderDark)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("export_options_modal")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LilacContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = LilacPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Backup & Export Financial Data",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Export transactions to CSV or PDF for backup (₹ INR)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Option 1: Export Transactions History CSV
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClickable {
                        onExportTransactionsCsv()
                        onDismiss()
                    }
                    .testTag("export_transactions_csv_option")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MintCyanContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Transactions CSV",
                            tint = MintCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Export Transactions CSV",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MintCyanContainer,
                                contentColor = MintCyan
                            ) {
                                Text(
                                    text = "CSV",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Clean spreadsheet with date, category, type, amount, account & notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Option 2: Export Full Data Backup CSV
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClickable {
                        onExportFullBackupCsv()
                        onDismiss()
                    }
                    .testTag("export_full_backup_csv_option")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AmberGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Full Backup CSV",
                            tint = AmberGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Export Full Financial Backup",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmberGold.copy(alpha = 0.2f),
                                contentColor = AmberGold
                            ) {
                                Text(
                                    text = "FULL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Complete snapshot: Accounts, Budgets, Goals, Bills & Transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Option 3: Download Formatted PDF
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClickable {
                        onDownloadPdf()
                        onDismiss()
                    }
                    .testTag("download_pdf_option")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LilacContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Report",
                            tint = LilacPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Download PDF Statement",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LilacPrimary,
                                contentColor = LilacOnPrimary
                            ) {
                                Text(
                                    text = "PDF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Formatted statement with summary cards, budget caps & transaction table",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ----------------------------------------------------
// 6. In-App Budget Exceeded Real-Time Alert Dialog
// ----------------------------------------------------
@Composable
fun BudgetExceededDialog(
    alert: com.example.ui.BudgetAlert,
    onDismiss: () -> Unit,
    onAdjustBudget: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CoralRedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Budget Exceeded Warning",
                    tint = CoralRed,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = {
            Text(
                text = "Budget Limit Exceeded!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Spending in ${alert.category} has surpassed your set monthly limit for ${FinanceFormatters.formatCurrentMonth()}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Spending", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                FinanceFormatters.formatCurrency(alert.spent),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CoralRed
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Monthly Budget Limit", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                FinanceFormatters.formatCurrency(alert.limit),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                        HorizontalDivider(color = BorderDarkSubtle, modifier = Modifier.padding(vertical = 2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Exceeded By", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = CoralRed)
                            Text(
                                "+${FinanceFormatters.formatCurrency(alert.excess)} (${(alert.percentage * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CoralRed
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdjustBudget()
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralRed,
                    contentColor = PureWhite
                )
            ) {
                Text("Adjust Budget Limit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = TextSecondary)
            }
        }
    )
}
