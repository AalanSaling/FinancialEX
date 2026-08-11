package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionType
import com.example.ui.FinanceUiState
import com.example.ui.components.CategoryPieChart
import com.example.ui.components.CashFlowBarChart
import com.example.ui.components.PieChartSlice
import com.example.ui.components.TransactionDetailDialog
import com.example.ui.components.categoryColors
import com.example.util.CurrencyConverter
import com.example.util.Translations
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    state: FinanceUiState,
    onExportPdfClick: () -> Unit,
    onAddTxClick: () -> Unit,
    onToggleHideBalance: () -> Unit,
    onCurrencyChange: (String) -> Unit,
    onWorkspaceSetupClick: () -> Unit = {},
    onSelectWorkspace: (String) -> Unit = {}
) {
    val lang = state.appLanguage
    val baseCurr = state.baseCurrency
    val activeWs = state.activeWorkspace
    val availableWs = state.availableWorkspaces
    val transactions = state.transactions

    var selectedTxForDetail by remember { mutableStateOf<com.example.data.model.TransactionEntity?>(null) }

    // Calculations converted to selected Base Currency
    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }
        .sumOf { CurrencyConverter.convert(it.amount, it.currency, baseCurr) }
    val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }
        .sumOf { CurrencyConverter.convert(it.amount, it.currency, baseCurr) }
    val totalReceivables = transactions.filter { it.type == TransactionType.RECEIVABLE }
        .sumOf { CurrencyConverter.convert(it.amount, it.currency, baseCurr) }
    val totalFutureExpenses = transactions.filter { it.type == TransactionType.FUTURE_EXPENSE }
        .sumOf { CurrencyConverter.convert(it.amount, it.currency, baseCurr) }

    // Liquid Bank Accounts Balance (from Replica de Bancos)
    val totalBankBalance = if (state.accounts.isNotEmpty()) {
        state.accounts.sumOf { CurrencyConverter.convert(it.balance, it.currency, baseCurr) }
    } else {
        totalIncome - totalExpenses
    }

    // Investments Principal & Interest Earned
    val totalInvestedPrincipal = state.investments
        .sumOf { CurrencyConverter.convert(it.amountInvested, it.currency, baseCurr) }
    val totalInvestedCurrentValue = state.investments
        .sumOf { CurrencyConverter.convert(it.currentValue, it.currency, baseCurr) }
    val totalInterestEarned = (totalInvestedCurrentValue - totalInvestedPrincipal).coerceAtLeast(0.0)

    // Total Net Worth (Bank Liquid Balance + Total Investment Value)
    val totalBalance = totalBankBalance + totalInvestedCurrentValue

    // Pie chart slice calculation
    val expenseByCategory = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { entry ->
            entry.value.sumOf { CurrencyConverter.convert(it.amount, it.currency, baseCurr) }
        }

    val pieSlices = expenseByCategory.entries.mapIndexed { index, entry ->
        PieChartSlice(
            label = entry.key,
            value = entry.value,
            color = categoryColors[index % categoryColors.size]
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header, Workspace Selector & Settings Gear Icon
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Translations.getString("nav_dashboard", lang),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${Translations.getString("workspace", lang)}: $activeWs",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Gear icon (Open Settings & Language modal) & Currency chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onWorkspaceSetupClick,
                            modifier = Modifier.testTag("workspace_setup_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = Translations.getString("app_settings", lang),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        var currMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            AssistChip(
                                onClick = { currMenuExpanded = true },
                                label = { Text(baseCurr, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1) },
                                leadingIcon = {
                                    Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.testTag("base_currency_chip")
                            )
                            DropdownMenu(
                                expanded = currMenuExpanded,
                                onDismissRequest = { currMenuExpanded = false }
                            ) {
                                listOf("PYG", "USD", "BRL", "EUR").forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr) },
                                        onClick = {
                                            onCurrencyChange(curr)
                                            currMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Workspace Selector Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(availableWs) { wsName ->
                        FilterChip(
                            selected = wsName == activeWs,
                            onClick = { onSelectWorkspace(wsName) },
                            label = { Text(wsName, fontSize = 12.sp, fontWeight = if (wsName == activeWs) FontWeight.Bold else FontWeight.Normal, maxLines = 1) },
                            leadingIcon = if (wsName == activeWs) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // Main Balance Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = Translations.getString("total_balance", lang),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onToggleHideBalance,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.showHideBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Ocultar Saldo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (state.isOfflineMode) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = Translations.getString("offline_mode", lang),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (state.showHideBalance) "••••••••" else CurrencyConverter.format(totalBalance, baseCurr),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Color-Coded Stat Pills
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            StatItem(
                                title = Translations.getString("bank_balance", lang),
                                value = if (state.showHideBalance) "••••" else CurrencyConverter.format(totalBankBalance, baseCurr),
                                color = MaterialTheme.colorScheme.primary,
                                icon = Icons.Default.AccountBalance,
                                modifier = Modifier.width(115.dp)
                            )
                        }
                        item {
                            StatItem(
                                title = Translations.getString("invested", lang),
                                value = if (state.showHideBalance) "••••" else CurrencyConverter.format(totalInvestedPrincipal, baseCurr),
                                color = Color(0xFF7C3AED),
                                icon = Icons.Default.TrendingUp,
                                modifier = Modifier.width(115.dp)
                            )
                        }
                        item {
                            StatItem(
                                title = Translations.getString("earned_interest", lang),
                                value = if (state.showHideBalance) "••••" else "+${CurrencyConverter.format(totalInterestEarned, baseCurr)}",
                                color = Color(0xFF059669),
                                icon = Icons.Default.MonetizationOn,
                                modifier = Modifier.width(115.dp)
                            )
                        }
                        item {
                            StatItem(
                                title = Translations.getString("incomes", lang),
                                value = if (state.showHideBalance) "••••" else CurrencyConverter.format(totalIncome, baseCurr),
                                color = Color(0xFF10B981),
                                icon = Icons.Default.ArrowUpward,
                                modifier = Modifier.width(115.dp)
                            )
                        }
                        item {
                            StatItem(
                                title = Translations.getString("expenses", lang),
                                value = if (state.showHideBalance) "••••" else CurrencyConverter.format(totalExpenses, baseCurr),
                                color = Color(0xFFEF4444),
                                icon = Icons.Default.ArrowDownward,
                                modifier = Modifier.width(115.dp)
                            )
                        }
                        item {
                            StatItem(
                                title = Translations.getString("receivables", lang),
                                value = if (state.showHideBalance) "••••" else CurrencyConverter.format(totalReceivables, baseCurr),
                                color = Color(0xFF3B82F6),
                                icon = Icons.Default.CallMade,
                                modifier = Modifier.width(115.dp)
                            )
                        }
                        item {
                            StatItem(
                                title = Translations.getString("payables", lang),
                                value = if (state.showHideBalance) "••••" else CurrencyConverter.format(totalFutureExpenses, baseCurr),
                                color = Color(0xFFF59E0B),
                                icon = Icons.Default.Schedule,
                                modifier = Modifier.width(115.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddTxClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_transaction_quick_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Translations.getString("new_transaction", lang),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onExportPdfClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_pdf_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Translations.getString("export_pdf", lang),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Bank Accounts / Carteiras Replica Summary Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translations.getString("accounts_wallets", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = CurrencyConverter.format(totalBankBalance, baseCurr),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.accounts.isEmpty()) {
                        Text(
                            text = Translations.getString("accounts_replica_subtitle", lang),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(state.accounts) { acc ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 2.dp,
                                    modifier = Modifier.width(150.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = acc.bankName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Text(
                                            text = acc.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = if (state.showHideBalance) "••••••••" else CurrencyConverter.format(acc.balance, acc.currency),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (acc.balance >= 0) MaterialTheme.colorScheme.onSurface else Color(0xFFEF4444),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upcoming Bill Reminders Widget
        item {
            val pendingBills = state.bills.filter { !it.isPaid }
            if (pendingBills.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${Translations.getString("bills_reminders", lang)} (${pendingBills.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        pendingBills.take(2).forEach { bill ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${bill.title} (${bill.category})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = CurrencyConverter.format(bill.amount, bill.currency),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Pie Chart (Expense by Category)
        item {
            CategoryPieChart(
                slices = pieSlices,
                totalValue = totalExpenses,
                currency = baseCurr
            )
        }

        // Interactive Cash Flow Bar Chart
        item {
            CashFlowBarChart(
                income = totalIncome,
                expense = totalExpenses,
                receivable = totalReceivables,
                futureExpense = totalFutureExpenses,
                invested = totalInvestedCurrentValue,
                currency = baseCurr
            )
        }

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Translations.getString("recent_activity", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Recent Transactions List (Take 5)
        if (transactions.isEmpty()) {
            item {
                Text(
                    text = Translations.getString("no_transactions", lang),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(transactions.take(5)) { tx ->
                TransactionRowItem(
                    transaction = tx,
                    baseCurrency = baseCurr,
                    lang = lang,
                    onClick = { selectedTxForDetail = tx }
                )
            }
        }

        // Financial Tips Section (Rotating Daily with Author Credits & Disclaimer)
        item {
            val dailyTip = remember(lang) { Translations.getDailyFinancialTip(lang) }
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = Translations.getString("tips_title", lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = dailyTip.author,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = dailyTip.text,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = Translations.getString("tips_disclaimer", lang),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    selectedTxForDetail?.let { tx ->
        TransactionDetailDialog(
            transaction = tx,
            baseCurrency = baseCurr,
            lang = lang,
            onDismiss = { selectedTxForDetail = null }
        )
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.Start, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TransactionRowItem(
    transaction: com.example.data.model.TransactionEntity,
    baseCurrency: String,
    lang: com.example.util.AppLanguage,
    onClick: (() -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    val dateStr = dateFormat.format(Date(transaction.dateMillis))

    val (icon, iconBg, amountColor) = when(transaction.type) {
        TransactionType.INCOME -> Triple(Icons.Default.ArrowUpward, Color(0xFFD1FAE5), Color(0xFF059669))
        TransactionType.EXPENSE -> Triple(Icons.Default.ArrowDownward, Color(0xFFFEE2E2), Color(0xFFDC2626))
        TransactionType.RECEIVABLE -> Triple(Icons.Default.CallMade, Color(0xFFDBEAFE), Color(0xFF2563EB))
        TransactionType.FUTURE_EXPENSE -> Triple(Icons.Default.Schedule, Color(0xFFFEF3C7), Color(0xFFD97706))
        TransactionType.INVESTMENT -> Triple(Icons.Default.ShowChart, Color(0xFFEDE9FE), Color(0xFF7C3AED))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { onClick?.invoke() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = amountColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = transaction.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = Translations.translateCategory(transaction.category, lang),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = "•", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = dateStr,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(horizontalAlignment = Alignment.End) {
                val prefix = when(transaction.type) {
                    TransactionType.INCOME, TransactionType.RECEIVABLE -> "+"
                    TransactionType.INVESTMENT -> "📈 "
                    else -> "-"
                }
                Text(
                    text = "$prefix ${CurrencyConverter.format(transaction.amount, transaction.currency)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = amountColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.accountName,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
