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
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.FinanceUiState
import com.example.ui.components.AccountManagementModal
import com.example.ui.components.TransactionDetailDialog
import com.example.util.CurrencyConverter
import com.example.util.Translations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    state: FinanceUiState,
    onAddTxClick: () -> Unit,
    onDeleteTx: (TransactionEntity) -> Unit,
    onSaveAccount: (AccountEntity) -> Unit = {},
    onDeleteAccount: (AccountEntity) -> Unit = {},
    onExportPdfClick: () -> Unit = {}
) {
    val lang = state.appLanguage
    val baseCurr = state.baseCurrency
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedAccountFilter by remember { mutableStateOf<String?>(null) }
    var selectedTxForDetail by remember { mutableStateOf<TransactionEntity?>(null) }

    var showAccountModal by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<AccountEntity?>(null) }

    val filterAllText = Translations.getString("filter_all", lang)
    val categories = listOf(filterAllText, "Alimentação", "Transporte", "Moradia", "Lazer", "Saúde", "Outros")

    val filteredTransactions = state.transactions.filter { tx ->
        val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true) || tx.category.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter
        val matchesCat = selectedCategoryFilter == null || selectedCategoryFilter == filterAllText || tx.category.equals(selectedCategoryFilter, ignoreCase = true)
        val matchesAcc = selectedAccountFilter == null || tx.accountName.contains(selectedAccountFilter!!, ignoreCase = true)
        matchesSearch && matchesType && matchesCat && matchesAcc
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTxClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        text = Translations.getString("new_transaction", lang),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_transaction")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 160.dp,
                start = 16.dp,
                end = 16.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translations.getString("nav_transactions", lang),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    FilledTonalButton(
                        onClick = onExportPdfClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("export_pdf_extrato_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Translations.getString("export_pdf", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            // Réplica de Bancos & Carteiras Header & Carousel
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
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
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Translations.getString("accounts_replica_title", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        TextButton(
                            onClick = {
                                accountToEdit = null
                                showAccountModal = true
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = Translations.getString("add_account_btn", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.accounts.isEmpty()) {
                            item {
                                Card(
                                    onClick = {
                                        accountToEdit = null
                                        showAccountModal = true
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                    modifier = Modifier.width(220.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = Translations.getString("new_account_title", lang),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            items(state.accounts, key = { it.id }) { acc ->
                                val isSelected = selectedAccountFilter?.equals(acc.name, ignoreCase = true) == true
                                val baseEquivalent = CurrencyConverter.convert(acc.balance, acc.currency, baseCurr)

                                Card(
                                    onClick = {
                                        selectedAccountFilter = if (isSelected) null else acc.name
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                                    modifier = Modifier.width(180.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = acc.bankName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    accountToEdit = acc
                                                    showAccountModal = true
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = acc.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = if (state.showHideBalance) "••••••" else CurrencyConverter.format(acc.balance, acc.currency),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (acc.balance >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                                            maxLines = 1
                                        )

                                        if (!acc.currency.equals(baseCurr, ignoreCase = true)) {
                                            Text(
                                                text = if (state.showHideBalance) "••••••" else "≈ ${CurrencyConverter.format(baseEquivalent, baseCurr)}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(Translations.getString("description", lang), maxLines = 1) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_search_input")
                )
            }

            // Filter Tabs by Type
            item {
                ScrollableTabRow(
                    selectedTabIndex = when(selectedTypeFilter) {
                        null -> 0
                        TransactionType.INCOME -> 1
                        TransactionType.EXPENSE -> 2
                        TransactionType.FUTURE_EXPENSE -> 3
                        TransactionType.RECEIVABLE -> 4
                        TransactionType.INVESTMENT -> 5
                    },
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        text = { Text(filterAllText, fontSize = 12.sp, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedTypeFilter == TransactionType.INCOME,
                        onClick = { selectedTypeFilter = TransactionType.INCOME },
                        text = { Text(Translations.getString("incomes", lang), fontSize = 12.sp, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedTypeFilter == TransactionType.EXPENSE,
                        onClick = { selectedTypeFilter = TransactionType.EXPENSE },
                        text = { Text(Translations.getString("expenses", lang), fontSize = 12.sp, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedTypeFilter == TransactionType.FUTURE_EXPENSE,
                        onClick = { selectedTypeFilter = TransactionType.FUTURE_EXPENSE },
                        text = { Text(Translations.getString("payables", lang), fontSize = 12.sp, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedTypeFilter == TransactionType.RECEIVABLE,
                        onClick = { selectedTypeFilter = TransactionType.RECEIVABLE },
                        text = { Text(Translations.getString("receivables", lang), fontSize = 12.sp, maxLines = 1) }
                    )
                    Tab(
                        selected = selectedTypeFilter == TransactionType.INVESTMENT,
                        onClick = { selectedTypeFilter = TransactionType.INVESTMENT },
                        text = { Text(Translations.getString("nav_investments", lang), fontSize = 12.sp, maxLines = 1) }
                    )
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = (selectedCategoryFilter == cat) || (cat == filterAllText && selectedCategoryFilter == null),
                            onClick = { selectedCategoryFilter = if (cat == filterAllText) null else cat },
                            label = { Text(Translations.translateCategory(cat, lang), fontSize = 11.sp, maxLines = 1) }
                        )
                    }
                }
            }

            // Transactions List Items or Empty State
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = Translations.getString("no_transactions", lang),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TransactionRowItem(
                                transaction = tx,
                                baseCurrency = state.baseCurrency,
                                lang = lang,
                                onClick = { selectedTxForDetail = tx }
                            )
                        }
                        IconButton(onClick = { onDeleteTx(tx) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAccountModal) {
        AccountManagementModal(
            existingAccount = accountToEdit,
            baseCurrency = baseCurr,
            appLanguage = lang,
            onDismiss = {
                showAccountModal = false
                accountToEdit = null
            },
            onSave = { acc ->
                onSaveAccount(acc)
                showAccountModal = false
                accountToEdit = null
            },
            onDelete = { acc ->
                onDeleteAccount(acc)
                showAccountModal = false
                accountToEdit = null
            }
        )
    }

    selectedTxForDetail?.let { tx ->
        TransactionDetailDialog(
            transaction = tx,
            baseCurrency = state.baseCurrency,
            lang = lang,
            onDismiss = { selectedTxForDetail = null },
            onDelete = { onDeleteTx(tx) }
        )
    }
}
