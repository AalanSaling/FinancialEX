package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionType
import com.example.util.AppLanguage
import com.example.util.CurrencyConverter
import com.example.util.Translations

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    isOfflineMode: Boolean,
    accounts: List<AccountEntity> = emptyList(),
    appLanguage: AppLanguage = AppLanguage.PORTUGUESE,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        accountName: String,
        currency: String,
        notes: String,
        dateMillis: Long
    ) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf("Alimentação") }
    
    val initialAccount = accounts.firstOrNull()?.name ?: "Carteira / Dinheiro"
    var accountName by remember { mutableStateOf(initialAccount) }
    var selectedCurrency by remember { mutableStateOf("PYG") }
    var notes by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(dateFormat.format(Date())) }

    var accountExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }

    val rawCategories = listOf("Alimentação", "Transporte", "Moradia", "Lazer", "Investimentos", "Saúde", "Educação", "Outros")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = Translations.getString("add_tx_title", appLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isOfflineMode) {
                    Text(
                        text = "⚡ ${Translations.getString("offline_mode", appLanguage)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
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
                // Type Selector (Scrollable Filter Chips Row for narrow screens)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TransactionType.entries.forEach { type ->
                        val label = when(type) {
                            TransactionType.INCOME -> Translations.getString("income_label", appLanguage)
                            TransactionType.EXPENSE -> Translations.getString("expense_label", appLanguage)
                            TransactionType.FUTURE_EXPENSE -> Translations.getString("future_expense_label", appLanguage)
                            TransactionType.RECEIVABLE -> Translations.getString("receivable_label", appLanguage)
                            TransactionType.INVESTMENT -> Translations.getString("investment_label", appLanguage)
                        }
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                        )
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(Translations.getString("description", appLanguage), maxLines = 1) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_title_input")
                )

                // Amount & Currency Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.replace(',', '.') },
                        label = { Text(Translations.getString("amount", appLanguage), maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_amount_input")
                    )

                    // Currency Dropdown Box
                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = !currencyExpanded },
                        modifier = Modifier.width(115.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedCurrency,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = { Text(Translations.getString("currency", appLanguage), maxLines = 1) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            CurrencyConverter.supportedCurrencies.forEach { curr ->
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

                // Account Dropdown Selector
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text(Translations.getString("account_origin", appLanguage), maxLines = 1) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        if (accounts.isNotEmpty()) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${acc.name} (${acc.bankName})", fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            Text(
                                                CurrencyConverter.format(acc.balance, acc.currency),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        accountName = acc.name
                                        selectedCurrency = acc.currency
                                        accountExpanded = false
                                    }
                                )
                            }
                        } else {
                            listOf("Carteira / Dinheiro", "Nubank", "Itaú", "ueno bank", "Banco Familiar", "Mercado Pago", "XP Investimentos").forEach { defaultAcc ->
                                DropdownMenuItem(
                                    text = { Text(defaultAcc) },
                                    onClick = {
                                        accountName = defaultAcc
                                        accountExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Category Dropdown Box
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = Translations.translateCategory(selectedCategory, appLanguage),
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text(Translations.getString("category", appLanguage), maxLines = 1) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        rawCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(Translations.translateCategory(cat, appLanguage), maxLines = 1) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date Field
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Data (dd/MM/yyyy)", maxLines = 1) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(Translations.getString("notes", appLanguage), maxLines = 1) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val cleanAccount = accountName.ifBlank { "Carteira / Geral" }
                    val parsedDateMillis = try {
                        dateFormat.parse(dateText)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                    if (title.isNotBlank() && amount > 0) {
                        onConfirm(title, amount, selectedType, selectedCategory, cleanAccount, selectedCurrency, notes, parsedDateMillis)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("confirm_add_transaction_btn")
            ) {
                Text(Translations.getString("save_tx", appLanguage), maxLines = 1)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.getString("cancel", appLanguage), maxLines = 1)
            }
        }
    )
}
