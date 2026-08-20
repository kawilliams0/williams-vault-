package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.TransactionEntity
import com.example.ui.FinanceUiState
import com.example.ui.components.FinanceFormatters
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    // Palette for PDF Document (Navy Blue, Crisp White, Slate & Accent Colors)
    private val COLOR_NAVY_DARK = Color.rgb(10, 25, 47)        // #0A192F
    private val COLOR_NAVY_PRIMARY = Color.rgb(30, 58, 138)    // #1E3A8A
    private val COLOR_NAVY_ACCENT = Color.rgb(37, 99, 235)     // #2563EB
    private val COLOR_WHITE = Color.rgb(255, 255, 255)
    private val COLOR_BG_LIGHT = Color.rgb(248, 250, 252)      // #F8FAFC
    private val COLOR_ROW_ALT = Color.rgb(241, 245, 249)       // #F1F5F9
    private val COLOR_TEXT_PRIMARY = Color.rgb(15, 23, 42)     // #0F172A
    private val COLOR_TEXT_SECONDARY = Color.rgb(71, 85, 105)  // #475569
    private val COLOR_TEXT_MUTED = Color.rgb(148, 163, 184)    // #94A3B8
    private val COLOR_BORDER = Color.rgb(226, 232, 240)        // #E2E8F0
    private val COLOR_GREEN_INCOME = Color.rgb(16, 185, 129)   // #10B981
    private val COLOR_RED_EXPENSE = Color.rgb(239, 68, 68)     // #EF4444

    private const val PAGE_WIDTH = 595 // Standard A4 width at 72dpi
    private const val PAGE_HEIGHT = 842 // Standard A4 height at 72dpi

    fun generateFinancePdf(context: Context, uiState: FinanceUiState): File? {
        val document = PdfDocument()
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        val shortDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val transactions = uiState.transactions

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var pageNumber = 1

        // Page 1: Header + Financial Summary Cards + Initial Transactions
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // Fill background
        paint.color = COLOR_BG_LIGHT
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Draw Navy Blue Header Banner
        paint.color = COLOR_NAVY_DARK
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 95f, paint)

        // Header Accent line
        paint.color = COLOR_NAVY_ACCENT
        canvas.drawRect(0f, 92f, PAGE_WIDTH.toFloat(), 95f, paint)

        // App Title
        paint.color = COLOR_WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FINANCE TRACKER", 30f, 42f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawText("Comprehensive Financial & Transaction Statement (₹ INR)", 30f, 62f, paint)

        // Date generated (Right aligned)
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 9f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Generated: ${dateFormat.format(Date())}", (PAGE_WIDTH - 30).toFloat(), 42f, paint)
        canvas.drawText("Currency: Indian Rupee (₹)", (PAGE_WIDTH - 30).toFloat(), 58f, paint)
        paint.textAlign = Paint.Align.LEFT

        var currentY = 115f

        // Financial Overview KPI Cards (4 mini boxes in a 2x2 or 4x1 grid)
        val cardWidth = (PAGE_WIDTH - 60 - 24) / 3f
        val cardHeight = 55f

        // Card 1: Net Worth / Balance
        drawKpiCard(
            canvas, paint,
            x = 30f, y = currentY, width = cardWidth, height = cardHeight,
            title = "TOTAL NET BALANCE",
            value = FinanceFormatters.formatCurrency(uiState.totalNetWorth),
            valueColor = COLOR_NAVY_PRIMARY
        )

        // Card 2: Income This Month
        drawKpiCard(
            canvas, paint,
            x = 30f + cardWidth + 12f, y = currentY, width = cardWidth, height = cardHeight,
            title = "MONTHLY INCOME",
            value = "+${FinanceFormatters.formatCurrency(uiState.totalIncomeThisMonth)}",
            valueColor = COLOR_GREEN_INCOME
        )

        // Card 3: Expense This Month
        drawKpiCard(
            canvas, paint,
            x = 30f + (cardWidth + 12f) * 2, y = currentY, width = cardWidth, height = cardHeight,
            title = "MONTHLY EXPENSE",
            value = "-${FinanceFormatters.formatCurrency(uiState.totalExpenseThisMonth)}",
            valueColor = COLOR_RED_EXPENSE
        )

        currentY += cardHeight + 20f

        // Budget Cap & Overview Banner
        paint.color = COLOR_WHITE
        val bannerRect = RectF(30f, currentY, (PAGE_WIDTH - 30).toFloat(), currentY + 36f)
        canvas.drawRoundRect(bannerRect, 6f, 6f, paint)
        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(bannerRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = COLOR_TEXT_SECONDARY
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MONTHLY SPENDING CAP: ${FinanceFormatters.formatCurrency(uiState.overallMonthlyBudgetCap)}", 44f, currentY + 22f, paint)

        paint.textAlign = Paint.Align.RIGHT
        val remainingText = "Remaining Budget: ${FinanceFormatters.formatCurrency(uiState.remainingMonthlyBudget)} (${(uiState.budgetCapPercentage * 100).toInt()}% used)"
        canvas.drawText(remainingText, (PAGE_WIDTH - 44).toFloat(), currentY + 22f, paint)
        paint.textAlign = Paint.Align.LEFT

        currentY += 52f

        // Section Title: Transaction History
        paint.color = COLOR_NAVY_DARK
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TRANSACTION HISTORY (${transactions.size} Records)", 30f, currentY, paint)

        currentY += 12f

        // Table Header
        currentY = drawTableHeader(canvas, paint, currentY)

        val rowHeight = 26f
        var transactionIndex = 0

        while (transactionIndex < transactions.size) {
            // Check if we reached bottom margin of page (leave 45pt for footer)
            if (currentY + rowHeight > PAGE_HEIGHT - 45f) {
                // Draw Footer for current page
                drawFooter(canvas, paint, pageNumber)
                document.finishPage(page)

                // Start Next Page
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas

                // Background
                paint.color = COLOR_BG_LIGHT
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

                // Mini Header on subsequent pages
                paint.color = COLOR_NAVY_DARK
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 40f, paint)
                paint.color = COLOR_WHITE
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("FINANCE TRACKER — Statement Continued", 30f, 25f, paint)

                currentY = 55f
                currentY = drawTableHeader(canvas, paint, currentY)
            }

            val tx = transactions[transactionIndex]
            val isEven = transactionIndex % 2 == 0

            // Draw Row Background
            paint.color = if (isEven) COLOR_WHITE else COLOR_ROW_ALT
            canvas.drawRect(30f, currentY, (PAGE_WIDTH - 30).toFloat(), currentY + rowHeight, paint)

            // Draw Bottom Border
            paint.color = COLOR_BORDER
            paint.strokeWidth = 0.5f
            canvas.drawLine(30f, currentY + rowHeight, (PAGE_WIDTH - 30).toFloat(), currentY + rowHeight, paint)

            // 1. Date
            paint.color = COLOR_TEXT_SECONDARY
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(shortDateFormat.format(Date(tx.dateMillis)), 38f, currentY + 16f, paint)

            // 2. Title / Description
            paint.color = COLOR_TEXT_PRIMARY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val displayTitle = if (tx.title.length > 22) tx.title.take(20) + "…" else tx.title
            canvas.drawText(displayTitle, 115f, currentY + 16f, paint)

            // 3. Category
            paint.color = COLOR_TEXT_SECONDARY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val displayCat = if (tx.category.length > 18) tx.category.take(16) + "…" else tx.category
            canvas.drawText(displayCat, 250f, currentY + 16f, paint)

            // 4. Account / Mode
            paint.color = COLOR_TEXT_MUTED
            val displayAccount = if (tx.accountName.isNotBlank()) tx.accountName else "General"
            canvas.drawText(displayAccount, 370f, currentY + 16f, paint)

            // 5. Amount (₹)
            val isExpense = tx.type == "EXPENSE"
            paint.color = if (isExpense) COLOR_RED_EXPENSE else COLOR_GREEN_INCOME
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.RIGHT
            val prefix = if (isExpense) "- " else "+ "
            val formattedAmount = prefix + FinanceFormatters.formatCurrency(tx.amount)
            canvas.drawText(formattedAmount, (PAGE_WIDTH - 38).toFloat(), currentY + 16f, paint)
            paint.textAlign = Paint.Align.LEFT

            currentY += rowHeight
            transactionIndex++
        }

        // Draw Footer on the final page
        drawFooter(canvas, paint, pageNumber)
        document.finishPage(page)

        // Save PDF to cache directory
        return try {
            val fileName = "Finance_Statement_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    private fun drawTableHeader(canvas: Canvas, paint: Paint, startY: Float): Float {
        val headerHeight = 22f
        paint.color = COLOR_NAVY_PRIMARY
        val rect = RectF(30f, startY, (PAGE_WIDTH - 30).toFloat(), startY + headerHeight)
        canvas.drawRoundRect(rect, 4f, 4f, paint)

        paint.color = COLOR_WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        canvas.drawText("DATE", 38f, startY + 14f, paint)
        canvas.drawText("DESCRIPTION", 115f, startY + 14f, paint)
        canvas.drawText("CATEGORY", 250f, startY + 14f, paint)
        canvas.drawText("ACCOUNT", 370f, startY + 14f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT (₹)", (PAGE_WIDTH - 38).toFloat(), startY + 14f, paint)
        paint.textAlign = Paint.Align.LEFT

        return startY + headerHeight + 2f
    }

    private fun drawKpiCard(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String,
        valueColor: Int
    ) {
        // Card Background
        paint.color = COLOR_WHITE
        val cardRect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(cardRect, 6f, 6f, paint)

        // Border
        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(cardRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Title
        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, x + 10f, y + 18f, paint)

        // Value
        paint.color = valueColor
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 10f, y + 38f, paint)
    }

    private fun drawFooter(canvas: Canvas, paint: Paint, pageNumber: Int) {
        val footerY = PAGE_HEIGHT - 25f
        paint.color = COLOR_BORDER
        paint.strokeWidth = 0.5f
        canvas.drawLine(30f, footerY - 10f, (PAGE_WIDTH - 30).toFloat(), footerY - 10f, paint)

        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Williams Vault • 100% Offline & Private • Generated Report", 30f, footerY, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page $pageNumber", (PAGE_WIDTH - 30).toFloat(), footerY, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    fun openOrSharePdf(context: Context, pdfFile: File) {
        try {
            val authority = try {
                "${context.packageName}.fileprovider"
            } catch (e: Exception) {
                "com.example.fileprovider"
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                authority,
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Williams Vault Statement (${pdfFile.name})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Download / Share PDF Statement")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing PDF: ${e.localizedMessage ?: "File provider error"}", Toast.LENGTH_SHORT).show()
        }
    }
}
