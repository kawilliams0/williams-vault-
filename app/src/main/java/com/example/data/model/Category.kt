package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class AccountType {
    CHECKING,
    SAVINGS,
    CASH,
    CREDIT_CARD,
    INVESTMENT
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "EXPENSE" or "INCOME"
    val iconKey: String = "category",
    val colorHex: String = "#D0BCFF",
    val isDefault: Boolean = false
)

data class CategoryItem(
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val icon: ImageVector,
    val color: Color,
    val iconKey: String = "category",
    val colorHex: String = "#D0BCFF",
    val isDefault: Boolean = false
)

object CategoryIconHelper {
    val availableIcons = listOf(
        "local_cafe" to ("Food & Chai" to Icons.Default.LocalCafe),
        "restaurant" to ("Dining & Food" to Icons.Default.Restaurant),
        "directions_car" to ("Transport & Fuel" to Icons.Default.DirectionsCar),
        "bolt" to ("Bills & Recharge" to Icons.Default.Bolt),
        "shopping_bag" to ("Shopping" to Icons.Default.ShoppingBag),
        "shopping_cart" to ("Groceries & Kirana" to Icons.Default.ShoppingCart),
        "home" to ("Rent & Housing" to Icons.Default.Home),
        "movie" to ("Entertainment & OTT" to Icons.Default.Movie),
        "medical_services" to ("Health & Medicine" to Icons.Default.MedicalServices),
        "school" to ("Education & Courses" to Icons.Default.School),
        "flight" to ("Travel & Commute" to Icons.Default.Flight),
        "fitness_center" to ("Fitness & Gym" to Icons.Default.FitnessCenter),
        "spa" to ("Personal Care" to Icons.Default.Spa),
        "pets" to ("Pets" to Icons.Default.Pets),
        "account_balance_wallet" to ("Salary & Earnings" to Icons.Default.AccountBalanceWallet),
        "laptop_mac" to ("Freelance & Consulting" to Icons.Default.LaptopMac),
        "trending_up" to ("Investments & SIP" to Icons.Default.TrendingUp),
        "storefront" to ("Business Income" to Icons.Default.Storefront),
        "card_giftcard" to ("Gifts & Cashback" to Icons.Default.CardGiftcard),
        "apartment" to ("Rental Income" to Icons.Default.Apartment),
        "monetization_on" to ("Other Income" to Icons.Default.MonetizationOn),
        "category" to ("General" to Icons.Default.Category)
    )

    val availableColors = listOf(
        "#D0BCFF", // Lilac
        "#B4EBEB", // Mint Cyan
        "#F2B8B5", // Coral Red
        "#E1E3AD", // Olive Gold
        "#CCC2DC", // Lavender
        "#80D8D8", // Aqua
        "#F97316", // Orange
        "#10B981", // Emerald
        "#3B82F6", // Blue
        "#EC4899", // Pink
        "#8B5CF6", // Purple
        "#06B6D4"  // Cyan
    )

    fun getIcon(iconKey: String): ImageVector {
        return availableIcons.find { it.first == iconKey }?.second?.second ?: Icons.Default.Category
    }
}

object FinanceCategories {
    val defaultExpenseCategories = listOf(
        CategoryEntity(id = 1, name = "Food & Chai", type = "EXPENSE", iconKey = "local_cafe", colorHex = "#F97316", isDefault = true),
        CategoryEntity(id = 2, name = "Groceries & Kirana", type = "EXPENSE", iconKey = "shopping_cart", colorHex = "#10B981", isDefault = true),
        CategoryEntity(id = 3, name = "Transport & Fuel", type = "EXPENSE", iconKey = "directions_car", colorHex = "#3B82F6", isDefault = true),
        CategoryEntity(id = 4, name = "Bills & Recharge", type = "EXPENSE", iconKey = "bolt", colorHex = "#F59E0B", isDefault = true),
        CategoryEntity(id = 5, name = "Shopping", type = "EXPENSE", iconKey = "shopping_bag", colorHex = "#EC4899", isDefault = true),
        CategoryEntity(id = 6, name = "Rent & Housing", type = "EXPENSE", iconKey = "home", colorHex = "#6366F1", isDefault = true),
        CategoryEntity(id = 7, name = "Entertainment & Movies", type = "EXPENSE", iconKey = "movie", colorHex = "#8B5CF6", isDefault = true),
        CategoryEntity(id = 8, name = "Health & Medicine", type = "EXPENSE", iconKey = "medical_services", colorHex = "#EF4444", isDefault = true),
        CategoryEntity(id = 9, name = "Travel & Trips", type = "EXPENSE", iconKey = "flight", colorHex = "#14B8A6", isDefault = true),
        CategoryEntity(id = 10, name = "Other Expense", type = "EXPENSE", iconKey = "category", colorHex = "#64748B", isDefault = true)
    )

    val defaultIncomeCategories = listOf(
        CategoryEntity(id = 11, name = "Salary", type = "INCOME", iconKey = "account_balance_wallet", colorHex = "#10B981", isDefault = true),
        CategoryEntity(id = 12, name = "Freelance / Projects", type = "INCOME", iconKey = "laptop_mac", colorHex = "#059669", isDefault = true),
        CategoryEntity(id = 13, name = "Investments & SIP", type = "INCOME", iconKey = "trending_up", colorHex = "#0284C7", isDefault = true),
        CategoryEntity(id = 14, name = "Business & Sales", type = "INCOME", iconKey = "storefront", colorHex = "#7C3AED", isDefault = true),
        CategoryEntity(id = 15, name = "Cashback & Gifts", type = "INCOME", iconKey = "card_giftcard", colorHex = "#F59E0B", isDefault = true),
        CategoryEntity(id = 16, name = "Other Income", type = "INCOME", iconKey = "monetization_on", colorHex = "#64748B", isDefault = true)
    )

    val expenseCategories = defaultExpenseCategories.map { it.toCategoryItem() }
    val incomeCategories = defaultIncomeCategories.map { it.toCategoryItem() }

    fun getCategory(name: String, customCategories: List<CategoryEntity> = emptyList()): CategoryItem {
        val trimmed = name.trim()
        val foundCustom = customCategories.find { it.name.equals(trimmed, ignoreCase = true) }
        if (foundCustom != null) return foundCustom.toCategoryItem()

        val foundDefault = (defaultExpenseCategories + defaultIncomeCategories).find { it.name.equals(trimmed, ignoreCase = true) }
        if (foundDefault != null) return foundDefault.toCategoryItem()

        // Smart keyword-based matching for intuitive icons and colors
        val lower = trimmed.lowercase()
        return when {
            // Food & Drinks
            lower.contains("chai") || lower.contains("tea") || lower.contains("coffee") || lower.contains("cafe") ->
                CategoryItem(name = trimmed.ifBlank { "Food & Chai" }, type = TransactionType.EXPENSE, icon = Icons.Default.LocalCafe, color = Color(0xFFF97316), iconKey = "local_cafe", colorHex = "#F97316")
            
            lower.contains("food") || lower.contains("dinner") || lower.contains("lunch") || lower.contains("breakfast") ||
            lower.contains("restaurant") || lower.contains("swiggy") || lower.contains("zomato") || lower.contains("snack") ||
            lower.contains("pizza") || lower.contains("burger") || lower.contains("dining") || lower.contains("eat") ->
                CategoryItem(name = trimmed.ifBlank { "Dining & Food" }, type = TransactionType.EXPENSE, icon = Icons.Default.Restaurant, color = Color(0xFFEA580C), iconKey = "restaurant", colorHex = "#EA580C")

            // Groceries & Daily Needs
            lower.contains("grocer") || lower.contains("kirana") || lower.contains("market") || lower.contains("vegetable") ||
            lower.contains("milk") || lower.contains("blinkit") || lower.contains("zepto") || lower.contains("instamart") ||
            lower.contains("fruits") || lower.contains("ration") || lower.contains("supermarket") ->
                CategoryItem(name = trimmed.ifBlank { "Groceries" }, type = TransactionType.EXPENSE, icon = Icons.Default.ShoppingCart, color = Color(0xFF10B981), iconKey = "shopping_cart", colorHex = "#10B981")

            // Transport & Fuel
            lower.contains("fuel") || lower.contains("petrol") || lower.contains("diesel") || lower.contains("transport") ||
            lower.contains("cab") || lower.contains("uber") || lower.contains("ola") || lower.contains("auto") ||
            lower.contains("car") || lower.contains("parking") || lower.contains("toll") || lower.contains("metro") ||
            lower.contains("bus") || lower.contains("bike") || lower.contains("rapido") ->
                CategoryItem(name = trimmed.ifBlank { "Transport & Fuel" }, type = TransactionType.EXPENSE, icon = Icons.Default.DirectionsCar, color = Color(0xFF3B82F6), iconKey = "directions_car", colorHex = "#3B82F6")

            // Utilities & Bills
            lower.contains("bill") || lower.contains("recharge") || lower.contains("electricity") || lower.contains("wifi") ||
            lower.contains("broadband") || lower.contains("power") || lower.contains("water") || lower.contains("utility") ||
            lower.contains("gas") || lower.contains("mobile") || lower.contains("dth") || lower.contains("cylinder") ->
                CategoryItem(name = trimmed.ifBlank { "Bills & Recharge" }, type = TransactionType.EXPENSE, icon = Icons.Default.Bolt, color = Color(0xFFF59E0B), iconKey = "bolt", colorHex = "#F59E0B")

            // Shopping & Retail
            lower.contains("shop") || lower.contains("cloth") || lower.contains("amazon") || lower.contains("flipkart") ||
            lower.contains("myntra") || lower.contains("mall") || lower.contains("shoes") || lower.contains("fashion") ||
            lower.contains("dress") || lower.contains("electronics") || lower.contains("gadget") || lower.contains("purchase") ->
                CategoryItem(name = trimmed.ifBlank { "Shopping" }, type = TransactionType.EXPENSE, icon = Icons.Default.ShoppingBag, color = Color(0xFFEC4899), iconKey = "shopping_bag", colorHex = "#EC4899")

            // Housing & Rent
            lower.contains("rent") || lower.contains("house") || lower.contains("home") || lower.contains("flat") ||
            lower.contains("apartment") || lower.contains("maintenance") || lower.contains("society") || lower.contains("pg") ||
            lower.contains("stay") || lower.contains("landlord") ->
                CategoryItem(name = trimmed.ifBlank { "Rent & Housing" }, type = TransactionType.EXPENSE, icon = Icons.Default.Home, color = Color(0xFF6366F1), iconKey = "home", colorHex = "#6366F1")

            // Entertainment & Streaming
            lower.contains("movie") || lower.contains("entertainment") || lower.contains("ott") || lower.contains("netflix") ||
            lower.contains("prime") || lower.contains("hotstar") || lower.contains("cinema") || lower.contains("game") ||
            lower.contains("gaming") || lower.contains("spotify") || lower.contains("music") || lower.contains("youtube") ->
                CategoryItem(name = trimmed.ifBlank { "Entertainment" }, type = TransactionType.EXPENSE, icon = Icons.Default.Movie, color = Color(0xFF8B5CF6), iconKey = "movie", colorHex = "#8B5CF6")

            // Health & Medical
            lower.contains("health") || lower.contains("med") || lower.contains("doctor") || lower.contains("hospital") ||
            lower.contains("pharma") || lower.contains("clinic") || lower.contains("test") || lower.contains("dental") ||
            lower.contains("tablet") || lower.contains("apollo") || lower.contains("1mg") ->
                CategoryItem(name = trimmed.ifBlank { "Health & Medicine" }, type = TransactionType.EXPENSE, icon = Icons.Default.MedicalServices, color = Color(0xFFEF4444), iconKey = "medical_services", colorHex = "#EF4444")

            // Travel & Vacation
            lower.contains("travel") || lower.contains("flight") || lower.contains("trip") || lower.contains("hotel") ||
            lower.contains("train") || lower.contains("irctc") || lower.contains("vacation") || lower.contains("tour") ||
            lower.contains("air") || lower.contains("holiday") ->
                CategoryItem(name = trimmed.ifBlank { "Travel & Trips" }, type = TransactionType.EXPENSE, icon = Icons.Default.Flight, color = Color(0xFF14B8A6), iconKey = "flight", colorHex = "#14B8A6")

            // Fitness & Gym
            lower.contains("gym") || lower.contains("fit") || lower.contains("workout") || lower.contains("yoga") ||
            lower.contains("sport") || lower.contains("trainer") ->
                CategoryItem(name = trimmed.ifBlank { "Fitness & Gym" }, type = TransactionType.EXPENSE, icon = Icons.Default.FitnessCenter, color = Color(0xFF06B6D4), iconKey = "fitness_center", colorHex = "#06B6D4")

            // Education & Learning
            lower.contains("edu") || lower.contains("school") || lower.contains("college") || lower.contains("course") ||
            lower.contains("tuition") || lower.contains("book") || lower.contains("fee") || lower.contains("class") ||
            lower.contains("study") ->
                CategoryItem(name = trimmed.ifBlank { "Education" }, type = TransactionType.EXPENSE, icon = Icons.Default.School, color = Color(0xFF2563EB), iconKey = "school", colorHex = "#2563EB")

            // Pets
            lower.contains("pet") || lower.contains("dog") || lower.contains("cat") || lower.contains("vet") ->
                CategoryItem(name = trimmed.ifBlank { "Pets" }, type = TransactionType.EXPENSE, icon = Icons.Default.Pets, color = Color(0xFF84CC16), iconKey = "pets", colorHex = "#84CC16")

            // Income: Salary & Earnings
            lower.contains("salary") || lower.contains("wage") || lower.contains("paycheck") || lower.contains("stipend") ->
                CategoryItem(name = trimmed.ifBlank { "Salary" }, type = TransactionType.INCOME, icon = Icons.Default.AccountBalanceWallet, color = Color(0xFF10B981), iconKey = "account_balance_wallet", colorHex = "#10B981")

            // Income: Freelance & Consulting
            lower.contains("freelance") || lower.contains("project") || lower.contains("consult") || lower.contains("client") ||
            lower.contains("upwork") || lower.contains("gig") || lower.contains("fiverr") ->
                CategoryItem(name = trimmed.ifBlank { "Freelance" }, type = TransactionType.INCOME, icon = Icons.Default.LaptopMac, color = Color(0xFF059669), iconKey = "laptop_mac", colorHex = "#059669")

            // Income: Investments & SIP
            lower.contains("invest") || lower.contains("sip") || lower.contains("stock") || lower.contains("mutual") ||
            lower.contains("dividend") || lower.contains("crypto") || lower.contains("groww") || lower.contains("zerodha") ||
            lower.contains("trading") || lower.contains("share") ->
                CategoryItem(name = trimmed.ifBlank { "Investments & SIP" }, type = TransactionType.INCOME, icon = Icons.Default.TrendingUp, color = Color(0xFF0284C7), iconKey = "trending_up", colorHex = "#0284C7")

            // Income: Business & Sales
            lower.contains("business") || lower.contains("sale") || lower.contains("store") || lower.contains("shop") ||
            lower.contains("revenue") || lower.contains("profit") ->
                CategoryItem(name = trimmed.ifBlank { "Business Income" }, type = TransactionType.INCOME, icon = Icons.Default.Storefront, color = Color(0xFF7C3AED), iconKey = "storefront", colorHex = "#7C3AED")

            // Income: Cashback & Gifts
            lower.contains("gift") || lower.contains("cashback") || lower.contains("reward") || lower.contains("bonus") ||
            lower.contains("coupon") || lower.contains("prize") ->
                CategoryItem(name = trimmed.ifBlank { "Cashback & Gifts" }, type = TransactionType.INCOME, icon = Icons.Default.CardGiftcard, color = Color(0xFFF59E0B), iconKey = "card_giftcard", colorHex = "#F59E0B")

            // Fallback general category
            else -> CategoryItem(
                name = trimmed.ifBlank { "General" },
                type = TransactionType.EXPENSE,
                icon = Icons.Default.Category,
                color = Color(0xFF64748B),
                iconKey = "category",
                colorHex = "#64748B"
            )
        }
    }
}

fun CategoryEntity.toCategoryItem(): CategoryItem {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFFD0BCFF)
    }
    val txType = if (type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
    val icon = CategoryIconHelper.getIcon(iconKey)
    return CategoryItem(
        id = id,
        name = name,
        type = txType,
        icon = icon,
        color = color,
        iconKey = iconKey,
        colorHex = colorHex,
        isDefault = isDefault
    )
}
