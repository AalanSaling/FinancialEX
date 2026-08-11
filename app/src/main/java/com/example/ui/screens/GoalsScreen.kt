package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import com.example.data.model.BudgetItemProposal
import com.example.data.model.GoalEntity
import com.example.data.model.GoalPaymentHistoryHelper
import com.example.data.model.GoalPaymentLog
import com.example.ui.FinanceUiState
import com.example.util.AppLanguage
import com.example.util.CurrencyConverter
import com.example.util.Translations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.data.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    state: FinanceUiState,
    onAddGoal: (GoalEntity) -> Unit = {},
    onUpdateGoal: (GoalEntity) -> Unit = {},
    onDeleteGoal: (GoalEntity) -> Unit = {},
    onDeleteGoalPaymentLog: (GoalEntity, GoalPaymentLog) -> Unit = { _, _ -> },
    onApplyCustomBudgetProposals: (List<BudgetItemProposal>) -> Unit = {},
    onAddTransaction: (title: String, amount: Double, type: TransactionType, category: String, accountName: String, currency: String, notes: String, dateMillis: Long) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    val lang = state.appLanguage
    var showAddDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<GoalEntity?>(null) }
    var goalToDeposit by remember { mutableStateOf<GoalEntity?>(null) }
    var goalForHistory by remember { mutableStateOf<GoalEntity?>(null) }
    var celebrationGoalTitle by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        text = Translations.getString("add_goal", lang),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_goal")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Translations.getString("goals_title", lang),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = Translations.getString("goals_subtitle", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 160.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${Translations.getString("nav_goals", lang)} (${state.goals.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (state.goals.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Translations.getString("goals_empty_msg", lang),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(state.goals) { goal ->
                        GoalCardItem(
                            goal = goal,
                            baseCurrency = state.baseCurrency,
                            lang = lang,
                            onEdit = { goalToEdit = goal },
                            onDelete = { onDeleteGoal(goal) },
                            onAddDeposit = { goalToDeposit = goal },
                            onViewHistory = { goalForHistory = goal }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddOrEditGoalModal(
            existingGoal = null,
            baseCurrency = state.baseCurrency,
            lang = lang,
            onDismiss = { showAddDialog = false },
            onSave = { newGoal ->
                onAddGoal(newGoal)
                showAddDialog = false
            }
        )
    }

    goalToEdit?.let { goal ->
        AddOrEditGoalModal(
            existingGoal = goal,
            baseCurrency = state.baseCurrency,
            lang = lang,
            onDismiss = { goalToEdit = null },
            onSave = { updatedGoal ->
                onUpdateGoal(updatedGoal)
                goalToEdit = null
            }
        )
    }

    goalToDeposit?.let { goal ->
        RegisterPaymentDialog(
            goal = goal,
            accounts = state.accounts,
            baseCurrency = state.baseCurrency,
            lang = lang,
            onDismiss = { goalToDeposit = null },
            onConfirm = { amount, dateMillis, installmentNum, note, accountName ->
                val logs = GoalPaymentHistoryHelper.parseLogs(goal.paymentHistoryJson).toMutableList()
                val newLog = GoalPaymentLog(
                    amount = amount,
                    paymentDateMillis = dateMillis,
                    installmentNumber = if (goal.isInstallmentMode || goal.installmentValue > 0) installmentNum else 0,
                    note = note
                )
                logs.add(newLog)

                val newAmount = goal.currentAmount + amount
                val newPaidInstallments = if (goal.isInstallmentMode || goal.installmentValue > 0) {
                    (goal.paidInstallments + 1).coerceAtMost(if (goal.totalInstallments > 0) goal.totalInstallments else Int.MAX_VALUE)
                } else goal.paidInstallments

                val isNowCompleted = (goal.totalInstallments > 0 && newPaidInstallments >= goal.totalInstallments) ||
                        (goal.targetAmount > 0 && newAmount >= goal.targetAmount)

                val updatedGoal = goal.copy(
                    currentAmount = newAmount,
                    paidInstallments = newPaidInstallments,
                    paymentHistoryJson = GoalPaymentHistoryHelper.serializeLogs(logs)
                )
                onUpdateGoal(updatedGoal)

                val isHist = accountName.contains("Histórico", ignoreCase = true) ||
                        accountName.contains("Passado", ignoreCase = true) ||
                        accountName.contains("Sem Conta", ignoreCase = true)

                if (!isHist) {
                    // Auto register transaction & deduct from account balance
                    onAddTransaction(
                        "Pagamento Meta: ${goal.title}",
                        amount,
                        TransactionType.EXPENSE,
                        goal.category.ifBlank { "Metas & Compras" },
                        accountName.ifBlank { state.accounts.firstOrNull()?.name ?: "Carteira / Dinheiro" },
                        state.baseCurrency,
                        "[GoalID:${goal.id}] " + note.ifBlank { "Pagamento registrado para meta ${goal.title}" },
                        dateMillis
                    )
                }

                goalToDeposit = null

                if (isNowCompleted) {
                    celebrationGoalTitle = goal.title
                }
            }
        )
    }

    goalForHistory?.let { goal ->
        // Keep synced with latest state
        val latestGoal = state.goals.find { it.id == goal.id } ?: goal
        GoalPaymentHistoryDialog(
            goal = latestGoal,
            baseCurrency = state.baseCurrency,
            lang = lang,
            onDismiss = { goalForHistory = null },
            onDeleteLog = { logToDelete ->
                onDeleteGoalPaymentLog(latestGoal, logToDelete)
            },
            onAddPaymentClick = {
                goalForHistory = null
                goalToDeposit = latestGoal
            }
        )
    }

    celebrationGoalTitle?.let { title ->
        GoalCelebrationDialog(
            goalTitle = title,
            lang = lang,
            onDismiss = { celebrationGoalTitle = null }
        )
    }
}

@Composable
private fun GoalCelebrationDialog(
    goalTitle: String,
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Translations.getString("congratulations_title", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF047857)
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFD1FAE5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(38.dp)
                    )
                }
                Text(
                    text = goalTitle,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = Translations.getString("congratulations_msg", lang),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(Translations.getString("awesome_btn", lang), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun GoalCardItem(
    goal: GoalEntity,
    baseCurrency: String,
    lang: AppLanguage = AppLanguage.PORTUGUESE,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onAddDeposit: () -> Unit = {},
    onViewHistory: () -> Unit = {}
) {
    val isInstallment = goal.isInstallmentMode || goal.installmentValue > 0
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val percentage = (progress * 100).toInt()
    val isCompleted = (goal.targetAmount > 0 && goal.currentAmount >= goal.targetAmount) ||
            (goal.totalInstallments > 0 && goal.paidInstallments >= goal.totalInstallments)

    val progressColor = when {
        isCompleted -> Color(0xFF10B981)
        goal.isCategoryBudget -> Color(0xFF3B82F6)
        else -> MaterialTheme.colorScheme.primary
    }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "goal_progress")

    val categoryIcon = when {
        goal.category.contains("Veículo") || goal.category.contains("Transporte") -> Icons.Default.DirectionsCar
        goal.category.contains("Moradia") || goal.category.contains("Aluguel") || goal.category.contains("Terreno") -> Icons.Default.Home
        goal.category.contains("Eletrodomésticos") || goal.category.contains("Móveis") -> Icons.Default.Tv
        goal.category.contains("Viagens") || goal.category.contains("Lazer") -> Icons.Default.Flight
        goal.category.contains("Alimentação") -> Icons.Default.Restaurant
        else -> Icons.Default.Flag
    }

    val historyLogs = remember(goal.paymentHistoryJson) {
        GoalPaymentHistoryHelper.parseLogs(goal.paymentHistoryJson)
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
                            .size(42.dp)
                            .background(
                                if (isCompleted) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else categoryIcon,
                            contentDescription = null,
                            tint = if (isCompleted) Color(0xFF059669) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = goal.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isCompleted) {
                                Surface(
                                    color = Color(0xFFD1FAE5),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = Translations.getString("completed", lang),
                                        color = Color(0xFF047857),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        val translatedCat = Translations.translateCategory(goal.category, lang)
                        val modeLabel = if (isInstallment) {
                            "${Translations.getString("mode_installments", lang)} (${goal.totalInstallments}x ${CurrencyConverter.format(goal.installmentValue, baseCurrency)})"
                        } else {
                            "${Translations.getString("mode_lump_sum", lang)}"
                        }
                        Text(
                            text = "$translatedCat • $modeLabel",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = Translations.getString("edit_btn", lang),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = Translations.getString("delete_btn", lang),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                drawStopIndicator = {}
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = CurrencyConverter.format(goal.currentAmount, baseCurrency),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (isInstallment && goal.totalInstallments > 0) {
                        Text(
                            text = "${goal.paidInstallments}/${goal.totalInstallments} ${Translations.getString("installments_count_paid", lang)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (historyLogs.isNotEmpty()) {
                        Text(
                            text = "${historyLogs.size} pagamentos registrados",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${CurrencyConverter.format(goal.targetAmount, baseCurrency)} ($percentage%)",
                        fontSize = 13.sp,
                        color = progressColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (isInstallment && goal.dueDayOfMonth > 0) {
                        Text(
                            text = "${Translations.getString("due_day_prefix", lang)} ${goal.dueDayOfMonth}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (!isInstallment && goal.deadlineMillis > 0) {
                        val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(goal.deadlineMillis))
                        Text(
                            text = "Prazo: $dateFmt",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddDeposit,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(
                        imageVector = if (isInstallment) Icons.Default.Payment else Icons.Default.AddCard,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isInstallment) Translations.getString("pay_single_installment", lang) else Translations.getString("add_deposit", lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onViewHistory,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${Translations.getString("payment_history_btn", lang)} (${historyLogs.size})",
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOrEditGoalModal(
    existingGoal: GoalEntity?,
    baseCurrency: String,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (GoalEntity) -> Unit
) {
    var title by remember { mutableStateOf(existingGoal?.title ?: "") }
    var category by remember { mutableStateOf(existingGoal?.category ?: "Eletrodomésticos") }
    var isInstallmentMode by remember { mutableStateOf(existingGoal?.isInstallmentMode ?: (existingGoal?.installmentValue ?: 0.0 > 0.0)) }

    var installmentValText by remember { mutableStateOf(if (existingGoal?.installmentValue ?: 0.0 > 0) existingGoal?.installmentValue?.toLong()?.toString() ?: "" else "") }
    var totalInstallmentsText by remember { mutableStateOf(if (existingGoal?.totalInstallments ?: 0 > 0) existingGoal?.totalInstallments?.toString() ?: "" else "") }
    var dueDayText by remember { mutableStateOf(if (existingGoal?.dueDayOfMonth ?: 0 > 0) existingGoal?.dueDayOfMonth?.toString() ?: "" else "") }

    var targetAmountText by remember { mutableStateOf(if (existingGoal?.targetAmount ?: 0.0 > 0) existingGoal?.targetAmount?.toLong()?.toString() ?: "" else "") }
    var currentAmountText by remember { mutableStateOf(if (existingGoal?.currentAmount ?: 0.0 > 0) existingGoal?.currentAmount?.toLong()?.toString() ?: "" else "") }

    val initialDateStr = if (existingGoal != null && existingGoal.deadlineMillis > 0) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(existingGoal.deadlineMillis))
    } else {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() + 30L * 24 * 3600 * 1000))
    }
    var targetDateText by remember { mutableStateOf(initialDateStr) }

    var categoryExpanded by remember { mutableStateOf(false) }
    val categories = listOf(
        "Alimentação",
        "Transporte",
        "Moradia",
        "Veículo",
        "Terreno / Imóveis",
        "Eletrodomésticos",
        "Viagens & Lazer",
        "Reserva",
        "Outros"
    )

    val installmentVal = installmentValText.toDoubleOrNull() ?: 0.0
    val totalInstallments = totalInstallmentsText.toIntOrNull() ?: 0
    val calculatedTotalFromInstallments = installmentVal * totalInstallments

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingGoal == null) Translations.getString("new_goal_title", lang) else Translations.getString("edit_goal_title", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(Translations.getString("goal_title_field", lang), maxLines = 1) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = Translations.translateCategory(category, lang),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Translations.getString("category", lang), maxLines = 1) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(Translations.translateCategory(cat, lang), maxLines = 1) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Payment Mode Selector (À Vista vs Parcelado)
                Text(
                    text = Translations.getString("payment_mode_label", lang),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isInstallmentMode,
                        onClick = { isInstallmentMode = false },
                        label = { Text(Translations.getString("mode_lump_sum", lang), fontSize = 11.sp, maxLines = 1) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isInstallmentMode,
                        onClick = { isInstallmentMode = true },
                        label = { Text(Translations.getString("mode_installments", lang), fontSize = 11.sp, maxLines = 1) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isInstallmentMode) {
                    OutlinedTextField(
                        value = installmentValText,
                        onValueChange = { installmentValText = it.replace(',', '.') },
                        label = { Text(Translations.getString("monthly_installment_val", lang) + " ($baseCurrency)", maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = totalInstallmentsText,
                            onValueChange = { totalInstallmentsText = it.filter { c -> c.isDigit() } },
                            label = { Text(Translations.getString("total_installments_qty", lang), maxLines = 1) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )

                        OutlinedTextField(
                            value = dueDayText,
                            onValueChange = { dueDayText = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text(Translations.getString("due_day_of_month", lang), maxLines = 1) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (calculatedTotalFromInstallments > 0) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${Translations.getString("calculated_total", lang)}: ${CurrencyConverter.format(calculatedTotalFromInstallments, baseCurrency)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = targetAmountText,
                        onValueChange = { targetAmountText = it.replace(',', '.') },
                        label = { Text(Translations.getString("target_amount", lang) + " ($baseCurrency)", maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetDateText,
                        onValueChange = { targetDateText = it },
                        label = { Text(Translations.getString("target_due_date_label", lang), maxLines = 1) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentAmountText,
                        onValueChange = { currentAmountText = it.replace(',', '.') },
                        label = { Text("Valor Já Guardado / Pago ($baseCurrency)", maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTargetAmount = if (isInstallmentMode) calculatedTotalFromInstallments else (targetAmountText.toDoubleOrNull() ?: 0.0)
                    val finalCurrentAmount = if (isInstallmentMode) {
                        val paidQty = existingGoal?.paidInstallments ?: 0
                        paidQty * installmentVal
                    } else {
                        currentAmountText.toDoubleOrNull() ?: 0.0
                    }

                    val deadlineMillis = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(targetDateText)?.time
                            ?: (System.currentTimeMillis() + 30L * 24 * 3600 * 1000)
                    } catch (e: Exception) {
                        System.currentTimeMillis() + 30L * 24 * 3600 * 1000
                    }

                    if (title.isNotBlank() && finalTargetAmount > 0) {
                        val newGoal = (existingGoal ?: GoalEntity(title = "", category = "", targetAmount = 0.0)).copy(
                            title = title,
                            category = category,
                            targetAmount = finalTargetAmount,
                            currentAmount = finalCurrentAmount,
                            installmentValue = if (isInstallmentMode) installmentVal else 0.0,
                            totalInstallments = if (isInstallmentMode) totalInstallments else 0,
                            dueDayOfMonth = dueDayText.toIntOrNull() ?: 0,
                            isInstallmentMode = isInstallmentMode,
                            deadlineMillis = deadlineMillis
                        )
                        onSave(newGoal)
                    }
                }
            ) {
                Text(Translations.getString("save_goal", lang), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Translations.getString("cancel", lang)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterPaymentDialog(
    goal: GoalEntity,
    accounts: List<AccountEntity>,
    baseCurrency: String,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, dateMillis: Long, installmentNum: Int, note: String, accountName: String) -> Unit
) {
    val isInstallment = goal.isInstallmentMode || goal.installmentValue > 0
    val defaultAmount = if (isInstallment && goal.installmentValue > 0) goal.installmentValue else (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)

    var amountText by remember { mutableStateOf(defaultAmount.toLong().toString()) }
    var dateText by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var installmentNumText by remember { mutableStateOf((goal.paidInstallments + 1).toString()) }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()?.name ?: "Carteira / Dinheiro") }
    var accountExpanded by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Translations.getString("add_deposit_title", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(goal.title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pago: ${CurrencyConverter.format(goal.currentAmount, baseCurrency)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: ${CurrencyConverter.format(goal.targetAmount, baseCurrency)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (isInstallment) {
                    OutlinedTextField(
                        value = installmentNumText,
                        onValueChange = { installmentNumText = it.filter { c -> c.isDigit() } },
                        label = { Text(Translations.getString("installment_number_label", lang), maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.replace(',', '.') },
                    label = { Text(Translations.getString("deposit_amount", lang) + " ($baseCurrency)", maxLines = 1) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text(Translations.getString("payment_date_label", lang), maxLines = 1) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Account / Origin Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedAccount,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Translations.getString("account_origin_label", lang), maxLines = 1) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccount = acc.name
                                    accountExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Histórico / Sem Conta (Passado)") },
                            onClick = {
                                selectedAccount = "Histórico / Sem Conta"
                                accountExpanded = false
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(Translations.getString("payment_note_label", lang), maxLines = 1) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val instNum = installmentNumText.toIntOrNull() ?: (goal.paidInstallments + 1)
                    val dateMillis = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateText)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

                    if (amt > 0) {
                        onConfirm(amt, dateMillis, instNum, noteText, selectedAccount)
                    }
                }
            ) {
                Text(Translations.getString("save", lang), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Translations.getString("cancel", lang)) }
        }
    )
}

@Composable
private fun GoalPaymentHistoryDialog(
    goal: GoalEntity,
    baseCurrency: String,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onDeleteLog: (GoalPaymentLog) -> Unit,
    onAddPaymentClick: () -> Unit
) {
    val logs = remember(goal.paymentHistoryJson) {
        GoalPaymentHistoryHelper.parseLogs(goal.paymentHistoryJson)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = Translations.getString("payment_history_title", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = goal.title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pago: ${CurrencyConverter.format(goal.currentAmount, baseCurrency)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (goal.totalInstallments > 0) {
                                Text(
                                    text = "Cuotas: ${goal.paidInstallments}/${goal.totalInstallments}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Meta: ${CurrencyConverter.format(goal.targetAmount, baseCurrency)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
                            Text(
                                text = "${Translations.getString("remaining_balance_txt", lang)}: ${CurrencyConverter.format(remaining, baseCurrency)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Translations.getString("no_payments_msg", lang),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        logs.forEach { log ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
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
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (log.installmentNumber > 0) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "Parcela ${log.installmentNumber}",
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = CurrencyConverter.format(log.amount, baseCurrency),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(log.paymentDateMillis))
                                        Text(
                                            text = "Data: $dateFmt ${if (log.note.isNotBlank()) "• ${log.note}" else ""}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteLog(log) },
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
            Button(onClick = onAddPaymentClick) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(Translations.getString("add_deposit", lang), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Translations.getString("close", lang)) }
        }
    )
}
