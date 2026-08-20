package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.TransactionEntity
import com.example.ui.FinanceUiState
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportHelper {

    private const val TAG = "CsvExportHelper"

    /**
     * Escapes fields according to RFC 4180 rules.
     * Quotes are escaped by doubling them (" -> ""), and the field is wrapped in quotes
     * if it contains commas, quotes, or newlines.
     */
    fun escapeCsvField(value: String?): String {
        if (value == null) return ""
        val containsSpecialChar = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        val escapedValue = value.replace("\"", "\"\"")
        return if (containsSpecialChar) "\"$escapedValue\"" else escapedValue
    }

    /**
     * Exports a list of transactions to a standard CSV file.
     */
    fun generateTransactionsCsv(
        context: Context,
        transactions: List<TransactionEntity>,
        filePrefix: String = "transactions_backup"
    ): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val csvFile = File(exportDir, "${filePrefix}_$timestamp.csv")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

            FileWriter(csvFile).use { writer ->
                // Write standard CSV Header
                writer.append("Transaction ID,Date,Time,Type,Category,Title,Amount (INR),Account,Notes,Created Timestamp\n")

                // Write rows
                transactions.forEach { tx ->
                    val txDate = Date(tx.dateMillis)
                    val dateStr = dateFormat.format(txDate)
                    val timeStr = timeFormat.format(txDate)

                    val row = listOf(
                        tx.id.toString(),
                        escapeCsvField(dateStr),
                        escapeCsvField(timeStr),
                        escapeCsvField(tx.type),
                        escapeCsvField(tx.category),
                        escapeCsvField(tx.title),
                        String.format(Locale.ENGLISH, "%.2f", tx.amount),
                        escapeCsvField(tx.accountName),
                        escapeCsvField(tx.note),
                        tx.dateMillis.toString()
                    ).joinToString(",")

                    writer.append(row).append("\n")
                }
                writer.flush()
            }
            csvFile
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to generate transactions CSV: ${e.message}", e)
            null
        }
    }

    /**
     * Exports a complete financial backup including accounts, budgets, goals, bills, and transactions.
     */
    fun generateFullBackupCsv(context: Context, uiState: FinanceUiState): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val csvFile = File(exportDir, "finance_full_backup_$timestamp.csv")

            val fullDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

            FileWriter(csvFile).use { writer ->
                // Section 1: Overview Metadata
                writer.append("# FINANCE TRACKER FULL BACKUP\n")
                writer.append("Export Date,${escapeCsvField(fullDateFormat.format(Date()))}\n")
                writer.append("Currency,INR (₹)\n")
                writer.append("Total Net Worth,${String.format(Locale.ENGLISH, "%.2f", uiState.totalNetWorth)}\n")
                writer.append("Monthly Budget Cap,${String.format(Locale.ENGLISH, "%.2f", uiState.overallMonthlyBudgetCap)}\n")
                writer.append("Current Month Spent,${String.format(Locale.ENGLISH, "%.2f", uiState.totalExpenseThisMonth)}\n")
                writer.append("Current Month Income,${String.format(Locale.ENGLISH, "%.2f", uiState.totalIncomeThisMonth)}\n\n")

                // Section 2: Accounts
                writer.append("# ACCOUNTS\n")
                writer.append("Account ID,Account Name,Account Type,Balance (INR),Last 4 Digits\n")
                uiState.accounts.forEach { acc ->
                    writer.append(
                        listOf(
                            acc.id.toString(),
                            escapeCsvField(acc.name),
                            escapeCsvField(acc.type),
                            String.format(Locale.ENGLISH, "%.2f", acc.balance),
                            escapeCsvField(acc.accountNumberLast4)
                        ).joinToString(",")
                    ).append("\n")
                }
                writer.append("\n")

                // Section 3: Budgets
                writer.append("# CATEGORY BUDGETS\n")
                writer.append("Budget ID,Category,Monthly Limit (INR),Period\n")
                uiState.budgets.forEach { b ->
                    writer.append(
                        listOf(
                            b.id.toString(),
                            escapeCsvField(b.category),
                            String.format(Locale.ENGLISH, "%.2f", b.monthlyLimit),
                            escapeCsvField(b.period)
                        ).joinToString(",")
                    ).append("\n")
                }
                writer.append("\n")

                // Section 4: Savings Goals
                writer.append("# SAVINGS GOALS\n")
                writer.append("Goal ID,Title,Target Amount (INR),Current Amount (INR),Target Date,Remaining (INR)\n")
                uiState.savingsGoals.forEach { g ->
                    val targetDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(g.targetDateMillis))
                    val remaining = maxOf(0.0, g.targetAmount - g.currentAmount)
                    writer.append(
                        listOf(
                            g.id.toString(),
                            escapeCsvField(g.title),
                            String.format(Locale.ENGLISH, "%.2f", g.targetAmount),
                            String.format(Locale.ENGLISH, "%.2f", g.currentAmount),
                            escapeCsvField(targetDateStr),
                            String.format(Locale.ENGLISH, "%.2f", remaining)
                        ).joinToString(",")
                    ).append("\n")
                }
                writer.append("\n")

                // Section 5: Recurring Bills
                writer.append("# RECURRING BILLS\n")
                writer.append("Bill ID,Title,Amount (INR),Category,Due Day of Month,Paid This Month\n")
                uiState.recurringBills.forEach { bill ->
                    writer.append(
                        listOf(
                            bill.id.toString(),
                            escapeCsvField(bill.title),
                            String.format(Locale.ENGLISH, "%.2f", bill.amount),
                            escapeCsvField(bill.category),
                            bill.dueDayOfMonth.toString(),
                            bill.isPaidThisMonth.toString()
                        ).joinToString(",")
                    ).append("\n")
                }
                writer.append("\n")

                // Section 6: Transactions
                writer.append("# TRANSACTIONS\n")
                writer.append("Transaction ID,Date,Time,Type,Category,Title,Amount (INR),Account,Notes,Created Timestamp\n")
                uiState.transactions.forEach { tx ->
                    val txDate = Date(tx.dateMillis)
                    val row = listOf(
                        tx.id.toString(),
                        escapeCsvField(dateFormat.format(txDate)),
                        escapeCsvField(timeFormat.format(txDate)),
                        escapeCsvField(tx.type),
                        escapeCsvField(tx.category),
                        escapeCsvField(tx.title),
                        String.format(Locale.ENGLISH, "%.2f", tx.amount),
                        escapeCsvField(tx.accountName),
                        escapeCsvField(tx.note),
                        tx.dateMillis.toString()
                    ).joinToString(",")
                    writer.append(row).append("\n")
                }

                writer.flush()
            }
            csvFile
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to generate full backup CSV: ${e.message}", e)
            null
        }
    }

    /**
     * Launches the system Share / Save sheet for the generated CSV file.
     */
    fun openOrShareCsv(context: Context, csvFile: File, chooserTitle: String = "Save or Share CSV Backup") {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, csvFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Financial Data Backup (${csvFile.name})")
                putExtra(Intent.EXTRA_TEXT, "Exported financial data backup from Williams Vault. Contains ${csvFile.name}.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening CSV chooser: ${e.message}", e)
            Toast.makeText(context, "Error sharing CSV: ${e.localizedMessage ?: "File provider error"}", Toast.LENGTH_SHORT).show()
        }
    }
}
