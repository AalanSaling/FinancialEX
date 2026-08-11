package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.util.AppLanguage
import com.example.util.CurrencyConverter
import com.example.util.Translations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementModal(
    existingAccount: AccountEntity? = null,
    baseCurrency: String,
    appLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit,
    onDelete: ((AccountEntity) -> Unit)? = null
) {
    var name by remember { mutableStateOf(existingAccount?.name ?: "") }
    var bankName by remember { mutableStateOf(existingAccount?.bankName ?: "Nubank") }
    var accountType by remember { mutableStateOf(existingAccount?.accountType ?: "CHECKING") }
    var balanceText by remember { mutableStateOf(existingAccount?.balance?.let { if (it == 0.0) "" else it.toLong().toString() } ?: "") }
    var selectedCurrency by remember { mutableStateOf(existingAccount?.currency ?: baseCurrency) }

    var bankExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }

    val popularBanks = listOf(
        "Nubank", "Itaú", "Bradesco", "Santander", "Inter", "Mercado Pago", 
        "ueno bank", "Banco Familiar", "GNB", "Continental", "BNF", "Sudameris",
        "XP Investimentos", "Binance", "Carteira Física / Dinheiro", "Caixinha / Fundo", "Outro Banco"
    )

    val accountTypesMap = mapOf(
        "CHECKING" to Translations.getString("type_checking", appLanguage),
        "SAVINGS" to Translations.getString("type_savings", appLanguage),
        "CASH" to Translations.getString("type_cash", appLanguage),
        "INVESTMENT" to Translations.getString("type_investment", appLanguage),
        "CREDIT_CARD" to Translations.getString("type_credit_card", appLanguage)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingAccount == null) Translations.getString("new_account_title", appLanguage) else Translations.getString("edit_account_title", appLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (existingAccount != null && onDelete != null) {
                    IconButton(onClick = { onDelete(existingAccount) }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Account Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Translations.getString("account_name_label", appLanguage), maxLines = 1) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_name_input")
                )

                // Bank Institution Dropdown
                ExposedDropdownMenuBox(
                    expanded = bankExpanded,
                    onExpandedChange = { bankExpanded = !bankExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text(Translations.getString("bank_institution_label", appLanguage), maxLines = 1) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = bankExpanded,
                        onDismissRequest = { bankExpanded = false }
                    ) {
                        popularBanks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank) },
                                onClick = {
                                    bankName = bank
                                    if (name.isBlank()) name = bank
                                    if (bank.contains("Carteira") || bank.contains("Dinheiro")) accountType = "CASH"
                                    if (bank.contains("XP") || bank.contains("Binance") || bank.contains("Caixinha")) accountType = "INVESTMENT"
                                    bankExpanded = false
                                }
                            )
                        }
                    }
                }

                // Account Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = accountTypesMap[accountType] ?: accountType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Translations.getString("account_type_label", appLanguage), maxLines = 1) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        accountTypesMap.forEach { (typeKey, typeLabel) ->
                            DropdownMenuItem(
                                text = { Text(typeLabel) },
                                onClick = {
                                    accountType = typeKey
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Current Initial Balance & Currency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it.replace(',', '.') },
                        label = { Text(Translations.getString("initial_balance_label", appLanguage), maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("account_balance_input")
                    )

                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = !currencyExpanded },
                        modifier = Modifier.width(115.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedCurrency,
                            onValueChange = {},
                            readOnly = true,
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
                                    text = { Text(curr) },
                                    onClick = {
                                        selectedCurrency = curr
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Translations.getString("auto_updated_balance", appLanguage),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toDoubleOrNull() ?: 0.0
                    val finalName = name.ifBlank { bankName }
                    if (finalName.isNotBlank()) {
                        val acc = (existingAccount ?: AccountEntity(name = "", bankName = "", accountType = "", balance = 0.0)).copy(
                            name = finalName,
                            bankName = bankName,
                            accountType = accountType,
                            balance = bal,
                            currency = selectedCurrency,
                            lastSyncedMillis = System.currentTimeMillis()
                        )
                        onSave(acc)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_account_btn")
            ) {
                Text(Translations.getString("save", appLanguage), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.getString("cancel", appLanguage))
            }
        }
    )
}
