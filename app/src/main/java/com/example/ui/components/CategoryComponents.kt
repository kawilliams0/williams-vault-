package com.example.ui.components

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun CategorySelectorGrid(
    categories: List<CategoryEntity>,
    selectedCategoryName: String,
    onSelectCategory: (CategoryEntity) -> Unit,
    onAddNewCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it.name + it.type }) { category ->
            val isSelected = selectedCategoryName.equals(category.name, ignoreCase = true)
            val icon = CategoryIconHelper.getIcon(category.iconKey)
            val itemColor = try {
                Color(android.graphics.Color.parseColor(category.colorHex))
            } catch (e: Exception) {
                LilacPrimary
            }

            Surface(
                onClick = { onSelectCategory(category) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) LilacOnPrimary else DarkSurfaceElevated,
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, LilacPrimary) else androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle),
                modifier = Modifier
                    .height(60.dp)
                    .testTag("category_select_${category.name}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) LilacPrimary.copy(alpha = 0.2f) else itemColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = category.name,
                            tint = if (isSelected) LilacPrimary else itemColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isSelected) LilacPrimary else TextPrimary
                    )
                }
            }
        }

        // Quick "+ New Category" card
        item {
            Surface(
                onClick = onAddNewCategory,
                shape = RoundedCornerShape(14.dp),
                color = DarkSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier
                    .height(60.dp)
                    .testTag("add_custom_category_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecondaryDarkContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Category",
                            tint = LilacPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Custom",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = LilacPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryFilterPillRow(
    categories: List<CategoryEntity>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onManageCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All Categories" chip
        item {
            FilterChip(
                selected = selectedCategory == "ALL",
                onClick = { onSelectCategory("ALL") },
                label = { Text("All Categories", fontWeight = if (selectedCategory == "ALL") FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LilacPrimary,
                    selectedLabelColor = LilacOnPrimary,
                    containerColor = DarkSurfaceElevated,
                    labelColor = TextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategory == "ALL",
                    borderColor = BorderDarkSubtle
                )
            )
        }

        items(categories, key = { it.id }) { cat ->
            val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
            val icon = CategoryIconHelper.getIcon(cat.iconKey)
            val itemColor = try {
                Color(android.graphics.Color.parseColor(cat.colorHex))
            } catch (e: Exception) {
                LilacPrimary
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(cat.name) },
                label = { Text(cat.name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) LilacOnPrimary else itemColor,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LilacPrimary,
                    selectedLabelColor = LilacOnPrimary,
                    containerColor = DarkSurfaceElevated,
                    labelColor = TextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = BorderDarkSubtle
                )
            )
        }

        // Manage Categories Button
        item {
            AssistChip(
                onClick = onManageCategories,
                label = { Text("Manage", color = LilacPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Tune, contentDescription = "Manage Categories", tint = LilacPrimary, modifier = Modifier.size(16.dp))
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = SecondaryDarkContainer
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = BorderDarkSubtle
                ),
                modifier = Modifier.testTag("manage_categories_chip")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementSheet(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onAddNewCategory: () -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTypeTab by remember { mutableStateOf("EXPENSE") }

    val filteredList = categories.filter { it.type == selectedTypeTab }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        modifier = Modifier.testTag("category_management_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage Categories",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Button(
                    onClick = onAddNewCategory,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LilacPrimary,
                        contentColor = LilacOnPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switcher
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedTypeTab == "EXPENSE",
                    onClick = { selectedTypeTab = "EXPENSE" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = SecondaryDarkContainer,
                        activeContentColor = CoralRed,
                        inactiveContainerColor = DarkBackground,
                        inactiveContentColor = TextSecondary
                    )
                ) {
                    Text("Expense Categories", fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = selectedTypeTab == "INCOME",
                    onClick = { selectedTypeTab = "INCOME" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MintCyanContainer,
                        activeContentColor = MintCyan,
                        inactiveContainerColor = DarkBackground,
                        inactiveContentColor = TextSecondary
                    )
                ) {
                    Text("Income Categories", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredList.forEach { category ->
                    val icon = CategoryIconHelper.getIcon(category.iconKey)
                    val color = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (e: Exception) {
                        LilacPrimary
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDarkSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(color.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = category.name,
                                        tint = color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (category.isDefault) "Default System Category" else "Custom Category",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (!category.isDefault) {
                                IconButton(onClick = { onDeleteCategory(category) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete Category",
                                        tint = CoralRed
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

@Composable
fun CreateCategoryDialog(
    initialType: String = "EXPENSE",
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, iconKey: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedIconKey by remember { mutableStateOf("restaurant") }
    var selectedColorHex by remember { mutableStateOf("#D0BCFF") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Create Category", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Type Selector
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
                        Text("Expense", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                        Text("Income", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g., Coffee, Pet Care, Rent") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_category_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Choose Icon", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                // Icon Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(CategoryIconHelper.availableIcons) { (key, info) ->
                        val isSelected = selectedIconKey == key
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) LilacPrimary else DarkSurfaceElevated)
                                .clickable { selectedIconKey = key }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) LilacPrimary else BorderDarkSubtle,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = info.second,
                                contentDescription = info.first,
                                tint = if (isSelected) LilacOnPrimary else TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Choose Color Accent", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                // Color Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIconHelper.availableColors.take(6).forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColorHex = hex }
                                .border(
                                    width = if (selectedColorHex == hex) 3.dp else 0.dp,
                                    color = if (selectedColorHex == hex) TextPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIconHelper.availableColors.drop(6).take(6).forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColorHex = hex }
                                .border(
                                    width = if (selectedColorHex == hex) 3.dp else 0.dp,
                                    color = if (selectedColorHex == hex) TextPrimary else Color.Transparent,
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
                        errorMessage = "Please enter a category name."
                        return@Button
                    }
                    onSave(name.trim(), selectedType, selectedIconKey, selectedColorHex)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LilacPrimary,
                    contentColor = LilacOnPrimary
                ),
                modifier = Modifier.testTag("save_custom_category_button")
            ) {
                Text("Create Category", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
