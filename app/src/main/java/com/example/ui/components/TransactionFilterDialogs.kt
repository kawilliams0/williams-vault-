package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomDateRangeDialog(
    initialStartMillis: Long?,
    initialEndMillis: Long?,
    onDismiss: () -> Unit,
    onApplyRange: (startMillis: Long?, endMillis: Long?) -> Unit
) {
    val context = LocalContext.current
    var startMillis by remember { mutableStateOf(initialStartMillis ?: System.currentTimeMillis()) }
    var endMillis by remember { mutableStateOf(initialEndMillis ?: System.currentTimeMillis()) }
    var isSingleDate by remember { mutableStateOf(initialStartMillis != null && initialStartMillis == initialEndMillis) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    val showDatePicker = { isStart: Boolean ->
        val currentTarget = if (isStart) startMillis else endMillis
        val cal = Calendar.getInstance().apply { timeInMillis = currentTarget }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                if (isStart) {
                    startMillis = newCal.timeInMillis
                    if (isSingleDate || startMillis > endMillis) {
                        endMillis = startMillis
                    }
                } else {
                    endMillis = newCal.timeInMillis
                    if (endMillis < startMillis) {
                        startMillis = endMillis
                    }
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = LilacPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Filter by Date",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Choose a single day or a specific date range:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Quick presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            val today = System.currentTimeMillis()
                            startMillis = today
                            endMillis = today
                            isSingleDate = true
                        },
                        label = { Text("Today") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = SecondaryDarkContainer,
                            labelColor = TextPrimary
                        )
                    )
                    SuggestionChip(
                        onClick = {
                            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                            startMillis = cal.timeInMillis
                            endMillis = cal.timeInMillis
                            isSingleDate = true
                        },
                        label = { Text("Yesterday") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = SecondaryDarkContainer,
                            labelColor = TextPrimary
                        )
                    )
                    SuggestionChip(
                        onClick = {
                            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
                            startMillis = cal.timeInMillis
                            endMillis = System.currentTimeMillis()
                            isSingleDate = false
                        },
                        label = { Text("Last 7 Days") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = SecondaryDarkContainer,
                            labelColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode toggle: Range vs Single Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isSingleDate,
                        onClick = { isSingleDate = false },
                        label = { Text("Date Range") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LilacPrimary,
                            selectedLabelColor = LilacOnPrimary,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = isSingleDate,
                        onClick = {
                            isSingleDate = true
                            endMillis = startMillis
                        },
                        label = { Text("Specific Date") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LilacPrimary,
                            selectedLabelColor = LilacOnPrimary,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isSingleDate) {
                    // Single Date Selector
                    DateSelectorBox(
                        label = "Selected Date",
                        formattedDate = dateFormat.format(Date(startMillis)),
                        onClick = { showDatePicker(true) }
                    )
                } else {
                    // Start and End Date Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            DateSelectorBox(
                                label = "From Date",
                                formattedDate = dateFormat.format(Date(startMillis)),
                                onClick = { showDatePicker(true) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DateSelectorBox(
                                label = "To Date",
                                formattedDate = dateFormat.format(Date(endMillis)),
                                onClick = { showDatePicker(false) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isSingleDate) {
                        onApplyRange(startMillis, startMillis)
                    } else {
                        onApplyRange(startMillis, endMillis)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LilacPrimary, contentColor = LilacOnPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply Date Filter", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun DateSelectorBox(
    label: String,
    formattedDate: String,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, BorderDarkSubtle, RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = LilacPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CustomAmountRangeDialog(
    initialMinAmount: Double?,
    initialMaxAmount: Double?,
    onDismiss: () -> Unit,
    onApplyRange: (minAmount: Double?, maxAmount: Double?) -> Unit
) {
    var minText by remember { mutableStateOf(initialMinAmount?.let { String.format(Locale.ENGLISH, "%.0f", it) } ?: "") }
    var maxText by remember { mutableStateOf(initialMaxAmount?.let { String.format(Locale.ENGLISH, "%.0f", it) } ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CurrencyRupee,
                    contentDescription = null,
                    tint = MintCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Filter by Amount",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter minimum and/or maximum amount (₹):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { minText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Min Amount (₹)") },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("filter_min_amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintCyan,
                            unfocusedBorderColor = BorderDarkSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = maxText,
                        onValueChange = { maxText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Max Amount (₹)") },
                        placeholder = { Text("No limit") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("filter_max_amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintCyan,
                            unfocusedBorderColor = BorderDarkSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick shortcuts
                Text("Quick presets:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = { minText = ""; maxText = "500" },
                        label = { Text("< ₹500", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = { minText = "500"; maxText = "2000" },
                        label = { Text("₹500 - 2k", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = { minText = "2000"; maxText = "10000" },
                        label = { Text("₹2k - 10k", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = { minText = "10000"; maxText = "" },
                        label = { Text("> ₹10k", fontSize = 11.sp) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val min = minText.toDoubleOrNull()
                    val max = maxText.toDoubleOrNull()
                    onApplyRange(min, max)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MintCyan, contentColor = DarkBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply Amount Filter", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    minText = ""
                    maxText = ""
                    onApplyRange(null, null)
                }
            ) {
                Text("Reset", color = CoralRed)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
