package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillEntity
import com.example.util.AppLanguage
import com.example.util.CurrencyConverter
import com.example.util.Translations
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillRemindersSection(
    bills: List<BillEntity>,
    baseCurrency: String,
    lang: AppLanguage = AppLanguage.PORTUGUESE,
    onAddBillClick: () -> Unit,
    onMarkAsPaid: (BillEntity) -> Unit,
    onDeleteBill: (BillEntity) -> Unit
) {
    val pendingBills = bills.filter { !it.isPaid }
    val paidBills = bills.filter { it.isPaid }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = Translations.getString("bills_reminders", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (lang == AppLanguage.SPANISH) "Avisos de vencimientos y cuentas por pagar"
                            else if (lang == AppLanguage.ENGLISH) "Due date notifications and payables"
                            else "Notificações de vencimento e pagamentos",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onAddBillClick,
                    modifier = Modifier.testTag("add_bill_reminder_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = Translations.getString("add_bill", lang),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (bills.isEmpty()) {
                Text(
                    text = if (lang == AppLanguage.SPANISH) "No hay cuentas programadas. Haz clic en + para crear un recordatorio."
                    else if (lang == AppLanguage.ENGLISH) "No scheduled bills. Tap + to create a reminder."
                    else "Nenhuma conta agendada. Clique no + para criar um lembrete.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pendingBills.forEach { bill ->
                        BillItemRow(
                            bill = bill,
                            baseCurrency = baseCurrency,
                            lang = lang,
                            onMarkAsPaid = { onMarkAsPaid(bill) },
                            onDelete = { onDeleteBill(bill) }
                        )
                    }

                    if (paidBills.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == AppLanguage.SPANISH) "Pagadas Recientemente" else if (lang == AppLanguage.ENGLISH) "Recently Paid" else "Quitadas Recentemente",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        paidBills.take(2).forEach { bill ->
                            BillItemRow(
                                bill = bill,
                                baseCurrency = baseCurrency,
                                lang = lang,
                                onMarkAsPaid = {},
                                onDelete = { onDeleteBill(bill) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillItemRow(
    bill: BillEntity,
    baseCurrency: String,
    lang: AppLanguage = AppLanguage.PORTUGUESE,
    onMarkAsPaid: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dueDateStr = dateFormat.format(Date(bill.dueDateMillis))

    val daysDiff = ((bill.dueDateMillis - System.currentTimeMillis()) / (1000 * 3600 * 24)).toInt()

    val (badgeBg, badgeText) = when {
        bill.isPaid -> Pair(Color(0xFF10B981), if (lang == AppLanguage.SPANISH) "PAGADO" else if (lang == AppLanguage.ENGLISH) "PAID" else "PAGO")
        daysDiff < 0 -> Pair(Color(0xFFEF4444), if (lang == AppLanguage.SPANISH) "VENCIDO" else if (lang == AppLanguage.ENGLISH) "OVERDUE" else "VENCIDO")
        daysDiff == 0 -> Pair(Color(0xFFF59E0B), if (lang == AppLanguage.SPANISH) "HOY" else if (lang == AppLanguage.ENGLISH) "TODAY" else "HOJE")
        else -> Pair(
            Color(0xFF3B82F6),
            if (lang == AppLanguage.SPANISH) "Vence en $daysDiff días"
            else if (lang == AppLanguage.ENGLISH) "Due in $daysDiff days"
            else "Vence em $daysDiff dias"
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
        ),
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
                Icon(
                    imageVector = if (bill.isPaid) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = badgeBg,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = bill.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = badgeBg.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeBg,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "• $dueDateStr (${bill.recurrence})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = CurrencyConverter.format(bill.amount, bill.currency),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!bill.isPaid) {
                    FilledTonalButton(
                        onClick = onMarkAsPaid,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("mark_paid_btn_${bill.id}")
                    ) {
                        Text(Translations.getString("mark_paid", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = Translations.getString("delete_btn", lang),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillDialog(
    lang: AppLanguage = AppLanguage.PORTUGUESE,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, category: String, dueDateMillis: Long, recurrence: String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Moradia") }
    var recurrence by remember { mutableStateOf("MENSAL") }
    var dueDateText by remember { mutableStateOf(dateFormat.format(Date(System.currentTimeMillis() + 5 * 24 * 3600 * 1000))) }

    val categories = listOf("Moradia", "Alimentação", "Transporte", "Saúde", "Lazer", "Outros")
    val recurrences = listOf("MENSAL", "SEMANAL", "ANUAL", "ÚNICO")

    var catExpanded by remember { mutableStateOf(false) }
    var recExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Translations.getString("add_bill", lang), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            if (lang == AppLanguage.SPANISH) "Nombre de la cuenta / Proveedor"
                            else if (lang == AppLanguage.ENGLISH) "Bill Title / Vendor"
                            else "Nome da Conta / Fornecedor"
                        )
                    },
                    placeholder = { Text("ex: Aluguel, Fatura Cartão") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_title_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.replace(',', '.') },
                    label = { Text(Translations.getString("amount", lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_amount_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = catExpanded,
                        onExpandedChange = { catExpanded = !catExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = Translations.translateCategory(category, lang),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Translations.getString("category", lang)) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(Translations.translateCategory(cat, lang)) },
                                    onClick = {
                                        category = cat
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = recExpanded,
                        onExpandedChange = { recExpanded = !recExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = recurrence,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (lang == AppLanguage.SPANISH) "Frecuencia" else if (lang == AppLanguage.ENGLISH) "Frequency" else "Frequência") },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = recExpanded,
                            onDismissRequest = { recExpanded = false }
                        ) {
                            recurrences.forEach { rec ->
                                DropdownMenuItem(
                                    text = { Text(rec) },
                                    onClick = {
                                        recurrence = rec
                                        recExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { dueDateText = it },
                    label = { Text(if (lang == AppLanguage.SPANISH) "Fecha de Vencimiento (dd/MM/yyyy)" else if (lang == AppLanguage.ENGLISH) "Due Date (dd/MM/yyyy)" else "Data Fixa de Vencimento (dd/MM/yyyy)") },
                    placeholder = { Text("ex: 15/08/2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val parsedDateMillis = try {
                        dateFormat.parse(dueDateText)?.time ?: (System.currentTimeMillis() + 5 * 24 * 3600 * 1000)
                    } catch (e: Exception) {
                        System.currentTimeMillis() + 5 * 24 * 3600 * 1000
                    }
                    if (title.isNotBlank() && amount > 0) {
                        onConfirm(title, amount, category, parsedDateMillis, recurrence)
                    }
                },
                modifier = Modifier.testTag("confirm_add_bill_btn")
            ) {
                Text(Translations.getString("save", lang), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Translations.getString("cancel", lang)) }
        }
    )
}
