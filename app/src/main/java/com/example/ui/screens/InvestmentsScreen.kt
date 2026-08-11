package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.InvestmentEntity
import com.example.data.model.InvestmentType
import com.example.ui.FinanceUiState
import com.example.util.AppLanguage
import com.example.util.CurrencyConverter
import com.example.util.InvestmentHistoryHelper
import com.example.util.InvestmentMovementLog
import com.example.util.Translations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(
    state: FinanceUiState,
    onAddOrUpdateInvestment: (
        id: Long,
        title: String,
        type: InvestmentType,
        institution: String,
        amountInvested: Double,
        currentValue: Double,
        yieldRate: Double,
        currency: String,
        notes: String,
        addToStatement: Boolean,
        additionalAporteAmount: Double,
        originAccountName: String,
        isHistorical: Boolean,
        existingLogsJson: String,
        depositDateMillis: Long
    ) -> Unit,
    onWithdrawInvestment: (
        investment: InvestmentEntity,
        amountToWithdraw: Double,
        destinationAccount: String,
        notes: String,
        dateMillis: Long
    ) -> Unit,
    onAddMovementLog: (
        investment: InvestmentEntity,
        type: String,
        amount: Double,
        accountName: String,
        notes: String,
        dateMillis: Long
    ) -> Unit,
    onDeleteMovementLog: (
        investment: InvestmentEntity,
        log: InvestmentMovementLog
    ) -> Unit,
    onDeleteInvestment: (InvestmentEntity) -> Unit
) {
    val lang = state.appLanguage
    val baseCurrency = state.baseCurrency
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Portfólio, 1 = Simulador de Juros

    var selectedTypeFilter by remember { mutableStateOf<InvestmentType?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingInvestment by remember { mutableStateOf<InvestmentEntity?>(null) }

    var selectedInvestmentForAction by remember { mutableStateOf<InvestmentEntity?>(null) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    val filteredInvestments = remember(state.investments, selectedTypeFilter) {
        if (selectedTypeFilter == null) state.investments
        else state.investments.filter { it.type == selectedTypeFilter }
    }

    val totalInvestedInBase = state.investments.sumOf {
        CurrencyConverter.convert(it.amountInvested, it.currency, baseCurrency)
    }
    val totalCurrentValueInBase = state.investments.sumOf {
        CurrencyConverter.convert(it.currentValue, it.currency, baseCurrency)
    }
    val totalProfitInBase = totalCurrentValueInBase - totalInvestedInBase

    Scaffold(
        floatingActionButton = {
            if (selectedSubTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingInvestment = null
                        showAddDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(Translations.getString("new_investment", lang), fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_investment_fab")
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Translations.getString("investments", lang),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = Translations.getString("investments_subtitle", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Top Tab Navigation
            PrimaryTabRow(
                selectedTabIndex = selectedSubTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text(Translations.getString("tab_portfolio", lang), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text(Translations.getString("tab_simulator", lang), fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedSubTab == 1) {
                InvestmentSimulatorView(lang = lang, baseCurrency = baseCurrency)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 160.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Summary Portfolio Hero Card
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = Translations.getString("total_portfolio_val", lang),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = CurrencyConverter.format(totalCurrentValueInBase, baseCurrency),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = Translations.getString("invested_capital", lang),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = CurrencyConverter.format(totalInvestedInBase, baseCurrency),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            maxLines = 1
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = Translations.getString("profit_yield", lang),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${if (totalProfitInBase >= 0) "+" else ""}${CurrencyConverter.format(totalProfitInBase, baseCurrency)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (totalProfitInBase >= 0) Color(0xFF059669) else Color(0xFFEF4444),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Asset Type Filter Chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedTypeFilter == null,
                                    onClick = { selectedTypeFilter = null },
                                    label = { Text("${Translations.getString("filter_all", lang)} (${state.investments.size})", fontSize = 11.sp) }
                                )
                            }
                            items(InvestmentType.entries) { type ->
                                val count = state.investments.count { it.type == type }
                                FilterChip(
                                    selected = selectedTypeFilter == type,
                                    onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type },
                                    label = { Text("${type.getLocalizedName(lang)} ($count)", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // List of Investments
                    if (filteredInvestments.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = Translations.getString("no_investments_title", lang),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = Translations.getString("no_investments_sub", lang),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredInvestments) { investment ->
                            InvestmentCardItem(
                                investment = investment,
                                lang = lang,
                                onEdit = {
                                    editingInvestment = investment
                                    showAddDialog = true
                                },
                                onWithdraw = {
                                    selectedInvestmentForAction = investment
                                    showWithdrawDialog = true
                                },
                                onShowHistory = {
                                    selectedInvestmentForAction = investment
                                    showHistoryDialog = true
                                },
                                onDelete = { onDeleteInvestment(investment) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddOrEditInvestmentDialog(
            investment = editingInvestment,
            accounts = state.accounts,
            lang = lang,
            onDismiss = {
                showAddDialog = false
                editingInvestment = null
            },
            onConfirm = { id, title, type, institution, amount, currentVal, yield, currency, notes, addToStmt, additionalAporte, originAcc, isHist, logsJson, depositDateMillis ->
                onAddOrUpdateInvestment(id, title, type, institution, amount, currentVal, yield, currency, notes, addToStmt, additionalAporte, originAcc, isHist, logsJson, depositDateMillis)
                showAddDialog = false
                editingInvestment = null
            }
        )
    }

    if (showWithdrawDialog && selectedInvestmentForAction != null) {
        WithdrawInvestmentDialog(
            investment = selectedInvestmentForAction!!,
            accounts = state.accounts,
            lang = lang,
            onDismiss = {
                showWithdrawDialog = false
                selectedInvestmentForAction = null
            },
            onConfirm = { amount, destAcc, notes, dateMillis ->
                onWithdrawInvestment(selectedInvestmentForAction!!, amount, destAcc, notes, dateMillis)
                showWithdrawDialog = false
                selectedInvestmentForAction = null
            }
        )
    }

    if (showHistoryDialog && selectedInvestmentForAction != null) {
        InvestmentHistoryDialog(
            investment = selectedInvestmentForAction!!,
            accounts = state.accounts,
            lang = lang,
            onDismiss = {
                showHistoryDialog = false
                selectedInvestmentForAction = null
            },
            onAddMovement = { type, amount, accName, notes, dateMillis ->
                onAddMovementLog(selectedInvestmentForAction!!, type, amount, accName, notes, dateMillis)
            },
            onDeleteMovement = { log ->
                onDeleteMovementLog(selectedInvestmentForAction!!, log)
            }
        )
    }
}

@Composable
private fun InvestmentCardItem(
    investment: InvestmentEntity,
    lang: AppLanguage = AppLanguage.PORTUGUESE,
    onEdit: () -> Unit,
    onWithdraw: () -> Unit,
    onShowHistory: () -> Unit,
    onDelete: () -> Unit
) {
    val profit = investment.currentValue - investment.amountInvested
    val profitPercent = if (investment.amountInvested > 0) (profit / investment.amountInvested) * 100 else 0.0

    val typeColor = when (investment.type) {
        InvestmentType.MUTUAL_FUND -> Color(0xFF3B82F6)
        InvestmentType.CDA -> Color(0xFF10B981)
        InvestmentType.STOCKS -> Color(0xFF8B5CF6)
        InvestmentType.BONDS -> Color(0xFFF59E0B)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(typeColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = investment.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (investment.isHistorical) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Histórico",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${investment.institution} • ${investment.type.getLocalizedName(lang)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Translations.getString("current_val", lang),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = CurrencyConverter.format(investment.currentValue, investment.currency),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Translations.getString("estimated_profit", lang),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = "${if (profit >= 0) "+" else ""}${CurrencyConverter.format(profit, investment.currency)} (${String.format("%.1f", profitPercent)}%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (profit >= 0) Color(0xFF059669) else Color(0xFFEF4444),
                        maxLines = 1
                    )
                }
            }

            if (investment.yieldRate > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${Translations.getString("contracted_rate", lang)} ${investment.yieldRate}% a.a.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Withdraw, History & Additional Deposit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onWithdraw,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallMade,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Translations.getString("resgate_investment", lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onShowHistory,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Translations.getString("movement_history", lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WithdrawInvestmentDialog(
    investment: InvestmentEntity,
    accounts: List<AccountEntity>,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, destinationAccount: String, notes: String, dateMillis: Long) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var amountText by remember { mutableStateOf("") }
    var withdrawalDateText by remember { mutableStateOf(dateFormat.format(Date())) }
    var isHistoricalResgate by remember { mutableStateOf(false) }
    var destinationAccount by remember { mutableStateOf(accounts.firstOrNull()?.name ?: "Carteira / Dinheiro") }
    var destExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Translations.getString("investment_withdrawal_title", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = investment.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Saldo acumulado atual: ${CurrencyConverter.format(investment.currentValue, investment.currency)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.replace(',', '.') },
                    label = { Text("Valor a Resgatar / Retirar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = withdrawalDateText,
                    onValueChange = { withdrawalDateText = it },
                    label = { Text(Translations.getString("withdrawal_date", lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Option: Historical Redemption Switch/Checkbox
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Resgate Passado / Histórico",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Não creditar valor no saldo do banco atual",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isHistoricalResgate,
                            onCheckedChange = { isChecked ->
                                isHistoricalResgate = isChecked
                                if (isChecked) {
                                    destinationAccount = "Resgate Passado (Histórico)"
                                } else {
                                    destinationAccount = accounts.firstOrNull()?.name ?: "Carteira / Dinheiro"
                                }
                            }
                        )
                    }
                }

                // Destination Account Dropdown (if not historical)
                if (!isHistoricalResgate) {
                    ExposedDropdownMenuBox(
                        expanded = destExpanded,
                        onExpandedChange = { destExpanded = !destExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = destinationAccount,
                            onValueChange = { destinationAccount = it },
                            readOnly = true,
                            label = { Text(Translations.getString("destination_account", lang)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = destExpanded,
                            onDismissRequest = { destExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sem Conta / Resgate Passado (Histórico)") },
                                onClick = {
                                    isHistoricalResgate = true
                                    destinationAccount = "Resgate Passado (Histórico)"
                                    destExpanded = false
                                }
                            )
                            HorizontalDivider()
                            if (accounts.isNotEmpty()) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text("${acc.name} (${acc.bankName})") },
                                        onClick = {
                                            destinationAccount = acc.name
                                            destExpanded = false
                                        }
                                    )
                                }
                            } else {
                                listOf("ueno bank", "Nubank", "Itaú", "Carteira / Dinheiro").forEach { defaultAcc ->
                                    DropdownMenuItem(
                                        text = { Text(defaultAcc) },
                                        onClick = {
                                            destinationAccount = defaultAcc
                                            destExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ℹ️ Este resgate atualizará os saldos do investimento sem alterar o saldo do seu banco atual.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observação / Motivo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        val finalDest = if (isHistoricalResgate) "Resgate Passado (Histórico)" else destinationAccount
                        val dateMillis = try {
                            dateFormat.parse(withdrawalDateText)?.time ?: System.currentTimeMillis()
                        } catch (_: Exception) {
                            System.currentTimeMillis()
                        }
                        onConfirm(amount, finalDest, notes, dateMillis)
                    }
                }
            ) {
                Text(Translations.getString("resgate_investment", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.getString("cancel", lang))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvestmentHistoryDialog(
    investment: InvestmentEntity,
    accounts: List<AccountEntity>,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onAddMovement: (type: String, amount: Double, accountName: String, notes: String, dateMillis: Long) -> Unit,
    onDeleteMovement: (log: InvestmentMovementLog) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showAddMovement by remember { mutableStateOf(false) }
    var moveType by remember { mutableStateOf("APORTE") }
    var moveAmountText by remember { mutableStateOf("") }
    var moveDateText by remember { mutableStateOf(dateFormat.format(Date())) }
    var isHistoricalMovement by remember { mutableStateOf(false) }
    var moveAccountName by remember { mutableStateOf(accounts.firstOrNull()?.name ?: "Carteira / Dinheiro") }
    var moveAccountExpanded by remember { mutableStateOf(false) }
    var moveNotes by remember { mutableStateOf("") }

    val logs = remember(investment.movementHistoryJson) {
        InvestmentHistoryHelper.parseLogs(investment.movementHistoryJson).sortedByDescending { it.dateMillis }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = Translations.getString("movement_history", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = investment.title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total de movimentações: ${logs.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedButton(
                        onClick = { showAddMovement = !showAddMovement },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(Translations.getString("add_movement", lang), fontSize = 11.sp)
                    }
                }

                if (showAddMovement) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Nova Movimentação no Histórico", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = moveType == "APORTE",
                                    onClick = { moveType = "APORTE" },
                                    label = { Text("Aporte", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = moveType == "RESGATE",
                                    onClick = { moveType = "RESGATE" },
                                    label = { Text("Resgate", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = moveType == "RENDIMENTO",
                                    onClick = { moveType = "RENDIMENTO" },
                                    label = { Text("Rendimento", fontSize = 10.sp) }
                                )
                            }

                            OutlinedTextField(
                                value = moveAmountText,
                                onValueChange = { moveAmountText = it.replace(',', '.') },
                                label = { Text("Valor") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = moveDateText,
                                onValueChange = { moveDateText = it },
                                label = { Text(Translations.getString("movement_date", lang)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Historical switch for movement
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Movimentação Passada (Sem afetar conta)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Switch(
                                    checked = isHistoricalMovement,
                                    onCheckedChange = { isChecked ->
                                        isHistoricalMovement = isChecked
                                        moveAccountName = if (isChecked) "Histórico / Sem Conta" else (accounts.firstOrNull()?.name ?: "Carteira / Dinheiro")
                                    }
                                )
                            }

                            if (!isHistoricalMovement) {
                                ExposedDropdownMenuBox(
                                    expanded = moveAccountExpanded,
                                    onExpandedChange = { moveAccountExpanded = !moveAccountExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = moveAccountName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Conta de Origem / Destino", maxLines = 1) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = moveAccountExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = moveAccountExpanded,
                                        onDismissRequest = { moveAccountExpanded = false }
                                    ) {
                                        if (accounts.isNotEmpty()) {
                                            accounts.forEach { acc ->
                                                DropdownMenuItem(
                                                    text = { Text("${acc.name} (${acc.bankName})", maxLines = 1) },
                                                    onClick = {
                                                        moveAccountName = acc.name
                                                        moveAccountExpanded = false
                                                    }
                                                )
                                            }
                                        } else {
                                            listOf("ueno bank", "Nubank", "Itaú", "Carteira / Dinheiro").forEach { defaultAcc ->
                                                DropdownMenuItem(
                                                    text = { Text(defaultAcc, maxLines = 1) },
                                                    onClick = {
                                                        moveAccountName = defaultAcc
                                                        moveAccountExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = moveNotes,
                                onValueChange = { moveNotes = it },
                                label = { Text("Observação / Detalhes") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    val amount = moveAmountText.toDoubleOrNull() ?: 0.0
                                    if (amount > 0) {
                                        val targetAcc = if (isHistoricalMovement) "Histórico / Sem Conta" else moveAccountName
                                        val moveDateMillis = try {
                                            dateFormat.parse(moveDateText)?.time ?: System.currentTimeMillis()
                                        } catch (_: Exception) {
                                            System.currentTimeMillis()
                                        }
                                        onAddMovement(moveType, amount, targetAcc, moveNotes, moveDateMillis)
                                        moveAmountText = ""
                                        moveNotes = ""
                                        showAddMovement = false
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Adicionar", fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (logs.isEmpty()) {
                    Text(
                        text = "Nenhuma movimentação registrada.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        logs.forEach { log ->
                            val badgeColor = when (log.type) {
                                "APORTE" -> Color(0xFF10B981)
                                "RESGATE" -> Color(0xFFEF4444)
                                else -> Color(0xFF8B5CF6)
                            }
                            val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(log.dateMillis))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                color = badgeColor.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = log.type,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = badgeColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = dateStr,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = CurrencyConverter.format(log.amount, investment.currency),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        if (log.notes.isNotBlank() || log.accountName.isNotBlank()) {
                                            Text(
                                                text = "${if (log.accountName.isNotBlank()) log.accountName else ""} ${if (log.notes.isNotBlank()) "• ${log.notes}" else ""}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onDeleteMovement(log) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remover",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(Translations.getString("close", lang))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOrEditInvestmentDialog(
    investment: InvestmentEntity?,
    accounts: List<AccountEntity> = emptyList(),
    lang: AppLanguage = AppLanguage.PORTUGUESE,
    onDismiss: () -> Unit,
    onConfirm: (
        id: Long,
        title: String,
        type: InvestmentType,
        institution: String,
        amountInvested: Double,
        currentValue: Double,
        yieldRate: Double,
        currency: String,
        notes: String,
        addToStatement: Boolean,
        additionalAporteAmount: Double,
        originAccountName: String,
        isHistorical: Boolean,
        existingLogsJson: String,
        depositDateMillis: Long
    ) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var title by remember { mutableStateOf(investment?.title ?: "") }
    var selectedType by remember { mutableStateOf(investment?.type ?: InvestmentType.MUTUAL_FUND) }
    var institution by remember { mutableStateOf(investment?.institution ?: "") }

    val initialOrigin = accounts.firstOrNull()?.name ?: "Carteira / Dinheiro"
    var originAccountName by remember { mutableStateOf(initialOrigin) }
    var originExpanded by remember { mutableStateOf(false) }

    val baseInvested = investment?.amountInvested ?: 0.0
    val baseCurrent = investment?.currentValue ?: 0.0

    var isHistorical by remember { mutableStateOf(investment?.isHistorical ?: false) }
    var additionalAporteText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf(investment?.amountInvested?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
    var currentValueText by remember { mutableStateOf(investment?.currentValue?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
    var yieldRateText by remember { mutableStateOf(investment?.yieldRate?.toString() ?: "6.5") }
    var selectedCurrency by remember { mutableStateOf(investment?.currency ?: "PYG") }
    
    var depositDateText by remember {
        mutableStateOf(dateFormat.format(Date(investment?.firstDepositDateMillis ?: System.currentTimeMillis())))
    }
    
    var notes by remember { mutableStateOf(investment?.notes ?: "") }
    var addToStatement by remember { mutableStateOf(true) }

    var typeExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }

    fun autoCalculateCurrentValue() {
        if (investment == null) {
            val amount = amountText.toDoubleOrNull() ?: 0.0
            val rate = yieldRateText.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                try {
                    val date = dateFormat.parse(depositDateText)
                    if (date != null) {
                        val daysDiff = ((System.currentTimeMillis() - date.time) / (1000.0 * 60 * 60 * 24)).coerceAtLeast(0.0)
                        if (daysDiff > 0 && rate > 0) {
                            val calculated = amount * (1.0 + (rate / 100.0)).pow(daysDiff / 365.25)
                            currentValueText = String.format(Locale.US, "%.2f", calculated)
                        } else {
                            currentValueText = String.format(Locale.US, "%.2f", amount)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun handleAdditionalAporteChange(newValue: String) {
        val clean = newValue.replace(',', '.')
        additionalAporteText = clean
        val aporteVal = clean.toDoubleOrNull() ?: 0.0
        val newTotalInvested = baseInvested + aporteVal
        val newTotalCurrent = baseCurrent + aporteVal
        amountText = if (newTotalInvested > 0) String.format(Locale.US, "%.2f", newTotalInvested) else ""
        currentValueText = if (newTotalCurrent > 0) String.format(Locale.US, "%.2f", newTotalCurrent) else ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (investment == null) Translations.getString("new_investment", lang) else Translations.getString("edit_add_aporte_title", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Historical Investment Switch
                if (investment == null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Translations.getString("is_historical_investment", lang),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = Translations.getString("historical_investment_subtitle", lang),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isHistorical,
                                onCheckedChange = { isHistorical = it }
                            )
                        }
                    }
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(Translations.getString("title", lang), maxLines = 1) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Asset Type Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedType.getLocalizedName(lang),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Translations.getString("asset_type", lang), maxLines = 1) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        InvestmentType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.getLocalizedName(lang), maxLines = 1) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Institution / Platform
                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text(Translations.getString("institution_label", lang), maxLines = 1) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Deposit Date
                OutlinedTextField(
                    value = depositDateText,
                    onValueChange = {
                        depositDateText = it
                        autoCalculateCurrentValue()
                    },
                    label = { Text(Translations.getString("first_deposit_date", lang), maxLines = 1) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Origin Bank / Wallet Selector Dropdown (if not historical)
                if (!isHistorical) {
                    ExposedDropdownMenuBox(
                        expanded = originExpanded,
                        onExpandedChange = { originExpanded = !originExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = originAccountName,
                            onValueChange = { originAccountName = it },
                            readOnly = true,
                            label = { Text(Translations.getString("investment_origin_account", lang), maxLines = 1) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = originExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = originExpanded,
                            onDismissRequest = { originExpanded = false }
                        ) {
                            if (accounts.isNotEmpty()) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text("${acc.name} (${acc.bankName})", maxLines = 1) },
                                        onClick = {
                                            originAccountName = acc.name
                                            selectedCurrency = acc.currency
                                            originExpanded = false
                                        }
                                    )
                                }
                            } else {
                                listOf("ueno bank", "Nubank", "Itaú", "Carteira / Dinheiro").forEach { defaultAcc ->
                                    DropdownMenuItem(
                                        text = { Text(defaultAcc, maxLines = 1) },
                                        onClick = {
                                            originAccountName = defaultAcc
                                            originExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Amounts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.replace(',', '.')
                            autoCalculateCurrentValue()
                        },
                        label = { Text(Translations.getString("invested_amount", lang), maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = !currencyExpanded },
                        modifier = Modifier.width(100.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedCurrency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Translations.getString("currency", lang), maxLines = 1) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            listOf("PYG", "USD", "BRL").forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text(curr, maxLines = 1) },
                                    onClick = {
                                        selectedCurrency = curr
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentValueText,
                        onValueChange = { currentValueText = it.replace(',', '.') },
                        label = { Text(Translations.getString("current_bank_val", lang), maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = yieldRateText,
                        onValueChange = {
                            yieldRateText = it.replace(',', '.')
                            autoCalculateCurrentValue()
                        },
                        label = { Text(Translations.getString("annual_yield_rate", lang), maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val currentVal = currentValueText.toDoubleOrNull() ?: amount
                    val yield = yieldRateText.toDoubleOrNull() ?: 0.0
                    val cleanInst = institution.ifBlank { "Geral" }
                    val additionalAporteVal = additionalAporteText.toDoubleOrNull() ?: 0.0

                    if (title.isNotBlank() && amount > 0) {
                        val depositDateMillis = try {
                            dateFormat.parse(depositDateText)?.time ?: System.currentTimeMillis()
                        } catch (_: Exception) {
                            System.currentTimeMillis()
                        }
                        onConfirm(
                            investment?.id ?: 0L,
                            title,
                            selectedType,
                            cleanInst,
                            amount,
                            currentVal,
                            yield,
                            selectedCurrency,
                            notes,
                            addToStatement,
                            additionalAporteVal,
                            originAccountName,
                            isHistorical,
                            investment?.movementHistoryJson ?: "",
                            depositDateMillis
                        )
                    }
                }
            ) {
                Text(Translations.getString("save", lang), maxLines = 1)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.getString("cancel", lang), maxLines = 1)
            }
        }
    )
}

@Composable
private fun InvestmentSimulatorView(lang: AppLanguage, baseCurrency: String) {
    var initialAmountText by remember { mutableStateOf("10000000") }
    var monthlyDepositText by remember { mutableStateOf("500000") }
    var annualRateText by remember { mutableStateOf("8.5") }
    var yearsText by remember { mutableStateOf("5") }

    val initialAmount = initialAmountText.toDoubleOrNull() ?: 0.0
    val monthlyDeposit = monthlyDepositText.toDoubleOrNull() ?: 0.0
    val annualRate = annualRateText.toDoubleOrNull() ?: 0.0
    val years = yearsText.toIntOrNull() ?: 0

    val months = years * 12
    val monthlyRate = if (annualRate > 0) (annualRate / 100.0) / 12.0 else 0.0

    var futureValue = initialAmount
    var totalInvested = initialAmount

    if (months > 0) {
        for (m in 1..months) {
            futureValue = (futureValue + monthlyDeposit) * (1.0 + monthlyRate)
            totalInvested += monthlyDeposit
        }
    }
    val totalInterest = futureValue - totalInvested

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
                .padding(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = Translations.getString("simulator_title", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = initialAmountText,
                    onValueChange = { initialAmountText = it.replace(',', '.') },
                    label = { Text(Translations.getString("initial_deposit", lang), maxLines = 1) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = monthlyDepositText,
                    onValueChange = { monthlyDepositText = it.replace(',', '.') },
                    label = { Text(Translations.getString("monthly_deposit", lang), maxLines = 1) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = annualRateText,
                    onValueChange = { annualRateText = it.replace(',', '.') },
                    label = { Text(Translations.getString("annual_rate_percent", lang), maxLines = 1) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = yearsText,
                    onValueChange = { yearsText = it },
                    label = { Text(Translations.getString("horizon_years", lang), maxLines = 1) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(Translations.getString("future_wealth", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(CurrencyConverter.format(futureValue, baseCurrency), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(Translations.getString("total_invested_calc", lang), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyConverter.format(totalInvested, baseCurrency), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(Translations.getString("earned_interest", lang), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+${CurrencyConverter.format(totalInterest, baseCurrency)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                }
            }
        }
    }
}
