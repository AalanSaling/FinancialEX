package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.GoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportExporter {

    fun generateAndSharePdf(
        context: Context,
        transactions: List<TransactionEntity>,
        goals: List<GoalEntity>,
        totalBalanceBrl: Double,
        baseCurrency: String = "BRL"
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()
        val boldPaint = Paint()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
        val currentDateStr = dateFormat.format(Date())
        val currentMonthStr = monthFormat.format(Date()).replaceFirstChar { it.uppercase() }

        // Header Background
        paint.color = Color.parseColor("#0F172A") // Navy header
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        // Header Text
        titlePaint.color = Color.WHITE
        titlePaint.textSize = 24f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RELATÓRIO FINANCEIRO MENSAL", 30f, 45f, titlePaint)

        titlePaint.textSize = 12f
        titlePaint.typeface = Typeface.DEFAULT
        canvas.drawText("Gerado em: $currentDateStr | Período: $currentMonthStr", 30f, 75f, titlePaint)

        // Summary Card Section
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(30f, 110f, 565f, 200f, 12f, 12f, paint)

        boldPaint.color = Color.parseColor("#1E293B")
        boldPaint.textSize = 13f
        boldPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RESUMO CONSOLIDADO ($baseCurrency)", 45f, 130f, boldPaint)

        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }
            .sumOf { CurrencyConverter.convert(it.amount, it.currency, baseCurrency) }
        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }
            .sumOf { CurrencyConverter.convert(it.amount, it.currency, baseCurrency) }
        val totalFuture = transactions.filter { it.type == TransactionType.FUTURE_EXPENSE }
            .sumOf { CurrencyConverter.convert(it.amount, it.currency, baseCurrency) }
        val totalReceivables = transactions.filter { it.type == TransactionType.RECEIVABLE }
            .sumOf { CurrencyConverter.convert(it.amount, it.currency, baseCurrency) }

        paint.textSize = 11f
        paint.color = Color.parseColor("#059669") // Green
        canvas.drawText("Entradas: ${CurrencyConverter.format(totalIncome, baseCurrency)}", 45f, 155f, paint)

        paint.color = Color.parseColor("#DC2626") // Red
        canvas.drawText("Saídas: ${CurrencyConverter.format(totalExpenses, baseCurrency)}", 200f, 155f, paint)

        paint.color = Color.parseColor("#2563EB") // Blue
        canvas.drawText("A Receber: ${CurrencyConverter.format(totalReceivables, baseCurrency)}", 350f, 155f, paint)

        paint.color = Color.parseColor("#D97706") // Amber
        canvas.drawText("Futuros Gastos: ${CurrencyConverter.format(totalFuture, baseCurrency)}", 45f, 180f, paint)

        paint.color = Color.parseColor("#0F172A")
        boldPaint.textSize = 11f
        canvas.drawText("Saldo Atual: ${CurrencyConverter.format(totalBalanceBrl, baseCurrency)}", 350f, 180f, boldPaint)

        // VISUAL CHARTS SECTION
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(30f, 210f, 565f, 310f, 12f, 12f, paint)

        boldPaint.color = Color.parseColor("#0F172A")
        boldPaint.textSize = 11f
        canvas.drawText("GRÁFICOS E COMPARAÇÕES VISUAIS", 45f, 228f, boldPaint)

        // Draw Income vs Expense Proportion Bar Chart
        val maxVal = maxOf(totalIncome, totalExpenses, 1.0)
        val incomeBarWidth = ((totalIncome / maxVal) * 220f).toFloat().coerceIn(10f, 220f)
        val expenseBarWidth = ((totalExpenses / maxVal) * 220f).toFloat().coerceIn(10f, 220f)

        // Income Bar
        paint.color = Color.parseColor("#10B981") // Green
        canvas.drawRoundRect(45f, 240f, 45f + incomeBarWidth, 255f, 4f, 4f, paint)
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 9f
        canvas.drawText("Entradas", 50f + incomeBarWidth, 252f, paint)

        // Expense Bar
        paint.color = Color.parseColor("#EF4444") // Red
        canvas.drawRoundRect(45f, 262f, 45f + expenseBarWidth, 277f, 4f, 4f, paint)
        canvas.drawText("Saídas", 50f + expenseBarWidth, 274f, paint)

        // Category Breakdown Mini Chart (Right Side)
        val expCategoryTotals = transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { CurrencyConverter.convert(it.amount, it.currency, baseCurrency) } }
            .toList()
            .sortedByDescending { it.second }
            .take(3)

        var catY = 238f
        boldPaint.textSize = 9f
        boldPaint.color = Color.parseColor("#475569")
        canvas.drawText("Principais Gastos por Categoria:", 340f, catY, boldPaint)

        val colors = listOf("#8B5CF6", "#F59E0B", "#06B6D4")
        expCategoryTotals.forEachIndexed { idx, (cat, amount) ->
            catY += 18f
            val pct = if (totalExpenses > 0) (amount / totalExpenses) else 0.0
            val barW = (pct * 120f).toFloat().coerceIn(5f, 120f)

            paint.color = Color.parseColor(colors.getOrElse(idx) { "#64748B" })
            canvas.drawRoundRect(340f, catY - 10f, 340f + barW, catY, 3f, 3f, paint)

            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 8f
            val label = if (cat.length > 12) cat.take(10) + ".." else cat
            canvas.drawText("$label (${(pct * 100).toInt()}%)", 345f + barW, catY - 2f, paint)
        }

        // Category Breakdown Section Header
        canvas.drawText("HISTÓRICO DE TRANSAÇÕES RECENTES", 30f, 335f, boldPaint)

        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(30f, 345f, 565f, 345f, paint)

        // Table Headers
        boldPaint.textSize = 10f
        boldPaint.color = Color.parseColor("#64748B")
        canvas.drawText("DATA", 35f, 360f, boldPaint)
        canvas.drawText("DESCRIÇÃO", 110f, 360f, boldPaint)
        canvas.drawText("CATEGORIA", 280f, 360f, boldPaint)
        canvas.drawText("TIPO", 400f, 360f, boldPaint)
        canvas.drawText("VALOR", 480f, 360f, boldPaint)

        var yPos = 380f
        val itemDateFormat = SimpleDateFormat("dd/MM", Locale("pt", "BR"))

        paint.textSize = 9.5f
        paint.color = Color.parseColor("#334155")

        transactions.take(15).forEach { tx ->
            val dateStr = itemDateFormat.format(Date(tx.dateMillis))
            val titleCut = if (tx.title.length > 25) tx.title.take(22) + "..." else tx.title
            val catCut = if (tx.category.length > 15) tx.category.take(13) + "..." else tx.category
            val typeStr = when(tx.type) {
                TransactionType.INCOME -> "Entrada"
                TransactionType.EXPENSE -> "Saída"
                TransactionType.FUTURE_EXPENSE -> "A Pagar"
                TransactionType.RECEIVABLE -> "A Receber"
                TransactionType.INVESTMENT -> "Investimento"
            }

            val amountStr = CurrencyConverter.format(tx.amount, tx.currency)

            canvas.drawText(dateStr, 35f, yPos, paint)
            canvas.drawText(titleCut, 110f, yPos, paint)
            canvas.drawText(catCut, 280f, yPos, paint)
            canvas.drawText(typeStr, 400f, yPos, paint)

            when(tx.type) {
                TransactionType.INCOME, TransactionType.RECEIVABLE -> paint.color = Color.parseColor("#059669")
                else -> paint.color = Color.parseColor("#DC2626")
            }
            canvas.drawText(amountStr, 480f, yPos, paint)
            paint.color = Color.parseColor("#334155")

            yPos += 20f
            if (yPos > 720f) return@forEach
        }

        // Footer Goal Summary
        if (goals.isNotEmpty()) {
            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRoundRect(30f, 740f, 565f, 810f, 8f, 8f, paint)

            boldPaint.color = Color.parseColor("#0F172A")
            boldPaint.textSize = 11f
            canvas.drawText("STATUS DE METAS & ORÇAMENTOS", 40f, 760f, boldPaint)

            paint.color = Color.parseColor("#475569")
            paint.textSize = 9f
            var goalX = 40f
            goals.take(3).forEach { g ->
                val pct = if (g.targetAmount > 0) ((g.currentAmount / g.targetAmount) * 100).toInt() else 0
                canvas.drawText("${g.title}: $pct% (${CurrencyConverter.format(g.currentAmount, "BRL")})", goalX, 785f, paint)
                goalX += 170f
            }
        }

        document.finishPage(page)

        // Save PDF to cache dir
        val pdfFile = File(context.cacheDir, "relatorio_financeiro_${System.currentTimeMillis()}.pdf")
        return try {
            val outputStream = FileOutputStream(pdfFile)
            document.writeTo(outputStream)
            outputStream.close()
            document.close()

            // Share / Open PDF Intent
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, pdfFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Relatório Financeiro Mensal")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Exportar Relatório PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }
}
