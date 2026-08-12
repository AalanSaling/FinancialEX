package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.data.model.*
import com.example.util.AppLanguage
import com.example.util.CurrencyConverter
import com.example.util.NotificationHelper
import com.example.util.PdfReportExporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.example.util.InvestmentHistoryHelper
import com.example.util.InvestmentMovementLog
import com.example.util.PinSecurityManager

data class FinanceUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val tips: List<FinancialTipEntity> = emptyList(),
    val bills: List<BillEntity> = emptyList(),
    val investments: List<InvestmentEntity> = emptyList(),
    val baseCurrency: String = "PYG",
    val activeWorkspace: String = "Pessoal",
    val availableWorkspaces: List<String> = listOf("Pessoal", "Empresa / Negócios", "Viagens & Lazer", "Projetos"),
    val isAppLocked: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val userPinCode: String = "",
    val isOfflineMode: Boolean = false,
    val isSyncingBank: Boolean = false,
    val showHideBalance: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.PORTUGUESE,
    val lastRatesUpdateMillis: Long = System.currentTimeMillis(),
    val messageToast: String? = null
)

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(
        db.transactionDao(),
        db.accountDao(),
        db.goalDao(),
        db.tipDao(),
        db.billDao(),
        db.investmentDao()
    )

    private val securityPrefs = application.getSharedPreferences("app_security_prefs", android.content.Context.MODE_PRIVATE)

    private val _baseCurrency = MutableStateFlow(
        securityPrefs.getString("base_currency", "PYG") ?: "PYG"
    )
    private val _activeWorkspace = MutableStateFlow(
        securityPrefs.getString("active_workspace", "Pessoal") ?: "Pessoal"
    )
    private val _customWorkspaces = MutableStateFlow(
        loadCustomWorkspaces(securityPrefs)
    )
    private val _userPinCode = MutableStateFlow(if (PinSecurityManager.hasPinSet(application)) "SET" else "")
    private val _isBiometricEnabled = MutableStateFlow(securityPrefs.getBoolean("is_biometric_enabled", false))
    private val _isAppLocked = MutableStateFlow(
        PinSecurityManager.hasPinSet(application) || _isBiometricEnabled.value
    )
    private val _isOfflineMode = MutableStateFlow(securityPrefs.getBoolean("is_offline_mode", false))
    private val _isSyncingBank = MutableStateFlow(false)
    private val _showHideBalance = MutableStateFlow(securityPrefs.getBoolean("show_hide_balance", false))
    private val _appLanguage = MutableStateFlow(
        getInitialLanguage(securityPrefs)
    )
    private val _lastRatesUpdateMillis = MutableStateFlow(System.currentTimeMillis())
    private val _messageToast = MutableStateFlow<String?>(null)

    private data class SettingsFlags(
        val isAppLocked: Boolean,
        val isBiometricEnabled: Boolean,
        val userPinCode: String,
        val isOfflineMode: Boolean,
        val isSyncingBank: Boolean,
        val showHideBalance: Boolean,
        val appLanguage: AppLanguage,
        val lastRatesUpdateMillis: Long
    )

    private val settingsFlagsFlow: Flow<SettingsFlags> = combine(
        combine(_isAppLocked, _isBiometricEnabled, _userPinCode, _isOfflineMode) { locked, bio, pin, offline -> Quadruple(locked, bio, pin, offline) },
        combine(_isSyncingBank, _showHideBalance, _appLanguage, _lastRatesUpdateMillis) { syncing, hide, lang, ratesTime -> Quadruple(syncing, hide, lang, ratesTime) }
    ) { (locked, bio, pin, offline), (syncing, hide, lang, ratesTime) ->
        SettingsFlags(locked, bio, pin, offline, syncing, hide, lang, ratesTime)
    }

    val uiState: StateFlow<FinanceUiState> = combine(
        combine(repository.allTransactions, repository.allAccounts, repository.allGoals, repository.allTips, repository.allInvestments) { txs, accounts, goals, tips, investments ->
            Quadruple(txs, accounts, Pair(goals, tips), investments)
        },
        combine(repository.allBills, _baseCurrency, _activeWorkspace, _customWorkspaces) { bills, curr, ws, wsList ->
            Triple(bills, curr, Pair(ws, wsList))
        },
        settingsFlagsFlow
    ) { dbData, workspaceData, flags ->
        val currentWs = workspaceData.third.first
        
        // Filter workspace data precisely
        val filteredTxs = dbData.first.filter { it.workspaceName == currentWs || (currentWs == "Pessoal" && it.workspaceName.isBlank()) }
        val filteredAccounts = dbData.second.filter { it.workspaceName == currentWs || (currentWs == "Pessoal" && it.workspaceName.isBlank()) }
        val filteredGoals = dbData.third.first.filter { it.workspaceName == currentWs || (currentWs == "Pessoal" && it.workspaceName.isBlank()) }
        val filteredBills = workspaceData.first.filter { it.workspaceName == currentWs || (currentWs == "Pessoal" && it.workspaceName.isBlank()) }
        val filteredInvestments = dbData.fourth.filter { it.workspaceName == currentWs || (currentWs == "Pessoal" && it.workspaceName.isBlank()) }

        FinanceUiState(
            transactions = filteredTxs,
            accounts = filteredAccounts,
            goals = filteredGoals,
            tips = dbData.third.second,
            bills = filteredBills,
            investments = filteredInvestments,
            baseCurrency = workspaceData.second,
            activeWorkspace = currentWs,
            availableWorkspaces = workspaceData.third.second,
            isAppLocked = flags.isAppLocked,
            isBiometricEnabled = flags.isBiometricEnabled,
            userPinCode = flags.userPinCode,
            isOfflineMode = flags.isOfflineMode,
            isSyncingBank = flags.isSyncingBank,
            showHideBalance = flags.showHideBalance,
            appLanguage = flags.appLanguage,
            lastRatesUpdateMillis = flags.lastRatesUpdateMillis,
            messageToast = _messageToast.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanceUiState()
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            CurrencyConverter.fetchLiveExchangeRates()
        }
    }

    fun setActiveWorkspace(workspaceName: String) {
        securityPrefs.edit().putString("active_workspace", workspaceName).apply()
        _activeWorkspace.value = workspaceName
        _messageToast.value = "Espaço alterado para '$workspaceName'"
    }

    fun createNewWorkspace(newWorkspaceName: String) {
        val trimmed = newWorkspaceName.trim()
        if (trimmed.isNotBlank() && !_customWorkspaces.value.contains(trimmed)) {
            val newList = _customWorkspaces.value + trimmed
            saveCustomWorkspaces(securityPrefs, newList)
            _customWorkspaces.value = newList
            setActiveWorkspace(trimmed)
            _messageToast.value = "Novo espaço '$trimmed' criado com sucesso!"
        }
    }

    fun setBaseCurrency(currency: String) {
        securityPrefs.edit().putString("base_currency", currency).apply()
        _baseCurrency.value = currency
    }

    fun toggleShowHideBalance() {
        val newValue = !_showHideBalance.value
        securityPrefs.edit().putBoolean("show_hide_balance", newValue).apply()
        _showHideBalance.value = newValue
    }

    fun toggleOfflineMode() {
        val newValue = !_isOfflineMode.value
        securityPrefs.edit().putBoolean("is_offline_mode", newValue).apply()
        _isOfflineMode.value = newValue
        val status = if (newValue) "Modo Offline Ativado (Viagem/Sem Sinal)" else "Modo Online Sincronizado com Nuvem"
        _messageToast.value = status
    }

    fun setAppLanguage(lang: AppLanguage) {
        securityPrefs.edit().putString("app_language_code", lang.code).apply()
        _appLanguage.value = lang
        _messageToast.value = when(lang) {
            AppLanguage.PORTUGUESE -> "Idioma alterado para Português 🇧🇷"
            AppLanguage.SPANISH -> "Idioma cambiado a Español 🇵🇾"
            AppLanguage.ENGLISH -> "Language changed to English 🇺🇸"
        }
    }

    fun startFreshZeroBalance(primaryCurrency: String) {
        viewModelScope.launch {
            setBaseCurrency(primaryCurrency)
            repository.wipeAllData()
            _messageToast.value = "Espaço configurado do zero com moeda principal $primaryCurrency!"
        }
    }

    fun setUserPinCode(pin: String) {
        PinSecurityManager.savePin(getApplication(), pin)
        _userPinCode.value = if (PinSecurityManager.hasPinSet(getApplication())) "SET" else ""
        _messageToast.value = "PIN de segurança cadastrado com sucesso!"
    }

    fun toggleBiometricEnabled(enabled: Boolean) {
        securityPrefs.edit().putBoolean("is_biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
        if (enabled) {
            _isAppLocked.value = true
        }
        _messageToast.value = if (enabled) "Proteção de Acesso Ativada" else "Proteção de Acesso Desativada"
    }

    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun lockApp() {
        _isAppLocked.value = true
    }

    fun clearToast() {
        _messageToast.value = null
    }

    // Account Management & Replica Sync
    fun addAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.addAccount(account)
            _messageToast.value = "Conta/Banco '${account.name}' cadastrada com sucesso!"
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
            _messageToast.value = "Conta/Banco '${account.name}' atualizada com sucesso!"
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            _messageToast.value = "Conta '${account.name}' removida."
        }
    }

    private suspend fun adjustAccountBalanceForTransaction(
        accountName: String,
        amount: Double,
        currency: String,
        type: TransactionType,
        isReversal: Boolean = false
    ) {
        val currentAccounts = repository.allAccounts.first()
        var targetAccount = currentAccounts.find { 
            it.name.equals(accountName, ignoreCase = true) || it.bankName.equals(accountName, ignoreCase = true) 
        }

        if (targetAccount == null) {
            val cleanName = accountName.ifBlank { "Carteira / Dinheiro" }
            val newAcc = AccountEntity(
                name = cleanName,
                bankName = cleanName,
                accountType = "CHECKING",
                balance = 0.0,
                currency = currency
            )
            repository.addAccount(newAcc)
            targetAccount = newAcc
        }

        val convertedAmount = CurrencyConverter.convert(amount, currency, targetAccount.currency)
        var delta = when (type) {
            TransactionType.INCOME, TransactionType.RECEIVABLE -> convertedAmount
            TransactionType.EXPENSE, TransactionType.FUTURE_EXPENSE, TransactionType.INVESTMENT -> -convertedAmount
        }

        if (isReversal) {
            delta = -delta
        }

        val updatedBalance = targetAccount.balance + delta
        repository.updateAccount(targetAccount.copy(balance = updatedBalance, lastSyncedMillis = System.currentTimeMillis()))
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        accountName: String = "Carteira / Geral",
        currency: String = "PYG",
        notes: String = "",
        dateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val isOffline = _isOfflineMode.value
            val tx = TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                accountName = accountName,
                currency = currency,
                notes = notes,
                dateMillis = dateMillis,
                isOfflinePending = isOffline
            )
            repository.addTransaction(tx)

            // Auto sync account balance
            adjustAccountBalanceForTransaction(
                accountName = accountName,
                amount = amount,
                currency = currency,
                type = type,
                isReversal = false
            )

            // Update associated goal or category budget current amount
            val goalsList = repository.allGoals.first()
            val categoryGoal = goalsList.find { it.category.equals(category, ignoreCase = true) }
            if (categoryGoal != null && type == TransactionType.EXPENSE) {
                val updatedAmount = categoryGoal.currentAmount + CurrencyConverter.convert(amount, currency, "BRL")
                repository.updateGoal(categoryGoal.copy(currentAmount = updatedAmount))

                // Check for anti-impulsive spending budget warning notification
                if (categoryGoal.monthlyLimit > 0 && updatedAmount > categoryGoal.monthlyLimit * 0.85) {
                    val pct = ((updatedAmount / categoryGoal.monthlyLimit) * 100).toInt()
                    NotificationHelper.showSmartNotification(
                        getApplication(),
                        "⚠️ Alerta Anti-Gasto Impulsivo!",
                        "Você atingiu $pct% do seu limite mensal na categoria '$category'. Dica: Respire fundo e aguarde 24h antes da próxima compra!"
                    )
                }
            }

            val modeText = if (isOffline) " (Salvo em Modo Offline)" else ""
            _messageToast.value = "Transação '$title' adicionada com sucesso!$modeText"
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)

            // Revert balance adjustment on account
            adjustAccountBalanceForTransaction(
                accountName = transaction.accountName,
                amount = transaction.amount,
                currency = transaction.currency,
                type = transaction.type,
                isReversal = true
            )

            _messageToast.value = "Transação removida."
        }
    }

    fun addGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.addGoal(goal)
            _messageToast.value = "Meta/Orçamento '${goal.title}' salvo com sucesso!"
        }
    }

    fun addOrUpdateGoal(title: String, category: String, targetAmount: Double, isCategoryBudget: Boolean) {
        viewModelScope.launch {
            val goal = GoalEntity(
                title = title,
                category = category,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                monthlyLimit = if (isCategoryBudget) targetAmount else 0.0,
                isCategoryBudget = isCategoryBudget
            )
            repository.addGoal(goal)
            _messageToast.value = "Meta/Orçamento '$title' salvo com sucesso!"
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(goal)
            _messageToast.value = "Meta/Teto '${goal.title}' atualizado com sucesso!"
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)

            // Revert all transactions in extratos associated with this goal
            val allTxs = repository.allTransactions.first()
            val relatedTxs = allTxs.filter { tx ->
                tx.notes.contains("[GoalID:${goal.id}]") ||
                tx.notes.contains("GoalID:${goal.id}") ||
                (goal.title.isNotBlank() && (
                    tx.title.contains(goal.title, ignoreCase = true) ||
                    tx.notes.contains(goal.title, ignoreCase = true)
                )) ||
                (tx.title.contains("Pagamento Meta:", ignoreCase = true) && goal.title.isNotBlank() && tx.title.contains(goal.title, ignoreCase = true))
            }

            relatedTxs.forEach { tx ->
                adjustAccountBalanceForTransaction(
                    accountName = tx.accountName,
                    amount = tx.amount,
                    currency = tx.currency,
                    type = tx.type,
                    isReversal = true
                )
                repository.deleteTransaction(tx)
            }

            _messageToast.value = "Meta '${goal.title}' e extratos associados removidos com sucesso."
        }
    }

    fun deleteGoalPaymentLog(
        goal: GoalEntity,
        logToDelete: com.example.data.model.GoalPaymentLog
    ) {
        viewModelScope.launch {
            val logs = com.example.data.model.GoalPaymentHistoryHelper.parseLogs(goal.paymentHistoryJson).toMutableList()
            logs.removeAll { it.id == logToDelete.id }

            val newAmount = (goal.currentAmount - logToDelete.amount).coerceAtLeast(0.0)
            val newPaidInstallments = if (goal.isInstallmentMode || goal.installmentValue > 0) {
                (goal.paidInstallments - 1).coerceAtLeast(0)
            } else goal.paidInstallments

            val updatedGoal = goal.copy(
                currentAmount = newAmount,
                paidInstallments = newPaidInstallments,
                paymentHistoryJson = com.example.data.model.GoalPaymentHistoryHelper.serializeLogs(logs)
            )
            repository.updateGoal(updatedGoal)

            // Find matching transaction in extratos and revert
            val allTxs = repository.allTransactions.first()
            val matchingTx = allTxs.find { tx ->
                (
                    tx.notes.contains("[GoalID:${goal.id}]") ||
                    tx.notes.contains("GoalID:${goal.id}") ||
                    (goal.title.isNotBlank() && (tx.title.contains(goal.title, ignoreCase = true) || tx.notes.contains(goal.title, ignoreCase = true)))
                ) && Math.abs(tx.amount - logToDelete.amount) < 0.01
            }

            if (matchingTx != null) {
                adjustAccountBalanceForTransaction(
                    accountName = matchingTx.accountName,
                    amount = matchingTx.amount,
                    currency = matchingTx.currency,
                    type = matchingTx.type,
                    isReversal = true
                )
                repository.deleteTransaction(matchingTx)
            }

            _messageToast.value = "Pagamento da meta removido e extrato estornado."
        }
    }

    fun addBill(
        title: String,
        amount: Double,
        category: String,
        dueDateMillis: Long,
        recurrence: String = "MENSAL",
        currency: String = "BRL"
    ) {
        viewModelScope.launch {
            val bill = BillEntity(
                title = title,
                amount = amount,
                category = category,
                dueDateMillis = dueDateMillis,
                recurrence = recurrence,
                isPaid = false,
                currency = currency
            )
            repository.addBill(bill)
            _messageToast.value = "Lembrete de conta '$title' agendado!"

            // Trigger simulated push notification
            NotificationHelper.showSmartNotification(
                getApplication(),
                "⏰ Lembrete de Conta Configurado",
                "Conta '$title' no valor de $currency $amount cadastrada para vencimento em breve."
            )
        }
    }

    fun markBillAsPaid(bill: BillEntity) {
        viewModelScope.launch {
            repository.markBillAsPaid(bill)
            _messageToast.value = "Conta '${bill.title}' marcada como PAGA! Lançamento registrado nas despesas."
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.deleteBill(bill)
            _messageToast.value = "Lembrete de conta removido."
        }
    }

    fun refreshExchangeRates() {
        viewModelScope.launch {
            val success = CurrencyConverter.fetchLiveExchangeRates()
            _lastRatesUpdateMillis.value = System.currentTimeMillis()
            _messageToast.value = if (success) "Cotações de moedas atualizadas em tempo real!" else "Cotações atualizadas localmente."
        }
    }

    fun addOrUpdateInvestment(
        id: Long = 0,
        title: String,
        type: InvestmentType,
        institution: String,
        amountInvested: Double,
        currentValue: Double,
        yieldRate: Double,
        currency: String,
        notes: String,
        addToStatement: Boolean = true,
        additionalAporteAmount: Double = 0.0,
        originAccountName: String = "Carteira / Dinheiro",
        isHistorical: Boolean = false,
        existingLogsJson: String = "",
        depositDateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val logs = InvestmentHistoryHelper.parseLogs(existingLogsJson).toMutableList()
            if (id == 0L) {
                logs.add(
                    InvestmentMovementLog(
                        type = "APORTE",
                        amount = amountInvested,
                        accountName = if (isHistorical) "Aporte Histórico" else originAccountName,
                        dateMillis = depositDateMillis,
                        notes = if (isHistorical) "Aporte cadastrado do passado" else "Aporte inicial"
                    )
                )
            } else if (additionalAporteAmount > 0) {
                logs.add(
                    InvestmentMovementLog(
                        type = "APORTE",
                        amount = additionalAporteAmount,
                        accountName = originAccountName,
                        dateMillis = depositDateMillis,
                        notes = "Novo Aporte"
                    )
                )
            }

            val updatedLogsJson = InvestmentHistoryHelper.serializeLogs(logs)

            val investment = InvestmentEntity(
                id = id,
                title = title,
                type = type,
                institution = institution,
                amountInvested = amountInvested,
                currentValue = currentValue,
                yieldRate = yieldRate,
                currency = currency,
                firstDepositDateMillis = depositDateMillis,
                notes = notes,
                isHistorical = isHistorical,
                movementHistoryJson = updatedLogsJson,
                workspaceName = _activeWorkspace.value,
                updatedAtMillis = System.currentTimeMillis()
            )
            repository.addInvestment(investment)
            _messageToast.value = "Investimento '$title' salvo com sucesso!"

            val txAmount = if (id == 0L) amountInvested else additionalAporteAmount
            if (txAmount > 0 && !isHistorical) {
                val targetAcc = originAccountName.ifBlank { institution }
                val tx = TransactionEntity(
                    title = if (id == 0L) "Aporte Investimento: $title" else "Novo Aporte: $title",
                    amount = txAmount,
                    type = TransactionType.INVESTMENT,
                    category = "Investimentos",
                    dateMillis = depositDateMillis,
                    accountName = targetAcc,
                    currency = currency,
                    notes = "Aporte de investimento em ${type.displayName} ($institution)"
                )
                repository.addTransaction(tx)

                adjustAccountBalanceForTransaction(
                    accountName = targetAcc,
                    amount = txAmount,
                    currency = currency,
                    type = TransactionType.INVESTMENT,
                    isReversal = false
                )
            }
        }
    }

    fun withdrawInvestment(
        investment: InvestmentEntity,
        amountToWithdraw: Double,
        destinationAccount: String,
        notes: String = "",
        dateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            if (amountToWithdraw <= 0) return@launch

            val isHistorical = destinationAccount.contains("Histórico", ignoreCase = true) ||
                    destinationAccount.contains("Passado", ignoreCase = true) ||
                    destinationAccount.contains("Sem Conta", ignoreCase = true)

            val logs = InvestmentHistoryHelper.parseLogs(investment.movementHistoryJson).toMutableList()
            logs.add(
                InvestmentMovementLog(
                    type = "RESGATE",
                    amount = amountToWithdraw,
                    accountName = if (isHistorical) "Resgate Passado" else destinationAccount,
                    dateMillis = dateMillis,
                    notes = notes.ifBlank { "Resgate de investimento" }
                )
            )

            val newCurrentValue = (investment.currentValue - amountToWithdraw).coerceAtLeast(0.0)
            val newAmountInvested = (investment.amountInvested - amountToWithdraw).coerceAtLeast(0.0)

            val updatedInvestment = investment.copy(
                currentValue = newCurrentValue,
                amountInvested = newAmountInvested,
                movementHistoryJson = InvestmentHistoryHelper.serializeLogs(logs),
                updatedAtMillis = System.currentTimeMillis()
            )

            repository.addInvestment(updatedInvestment)

            if (!isHistorical) {
                val targetAcc = destinationAccount.ifBlank { "Carteira / Dinheiro" }
                val tx = TransactionEntity(
                    title = "Resgate Investimento: ${investment.title}",
                    amount = amountToWithdraw,
                    type = TransactionType.INCOME,
                    category = "Investimentos",
                    dateMillis = dateMillis,
                    accountName = targetAcc,
                    currency = investment.currency,
                    notes = "Resgate creditado na conta $targetAcc. $notes"
                )
                repository.addTransaction(tx)

                adjustAccountBalanceForTransaction(
                    accountName = targetAcc,
                    amount = amountToWithdraw,
                    currency = investment.currency,
                    type = TransactionType.INCOME,
                    isReversal = false
                )

                _messageToast.value = "Resgate de ${CurrencyConverter.format(amountToWithdraw, investment.currency)} creditado em '$targetAcc'!"
            } else {
                _messageToast.value = "Resgate passado de ${CurrencyConverter.format(amountToWithdraw, investment.currency)} registrado sem alterar o saldo das contas!"
            }
        }
    }

    fun addInvestmentMovementLog(
        investment: InvestmentEntity,
        type: String,
        amount: Double,
        accountName: String,
        notes: String,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val logs = InvestmentHistoryHelper.parseLogs(investment.movementHistoryJson).toMutableList()
            logs.add(
                InvestmentMovementLog(
                    type = type,
                    amount = amount,
                    accountName = accountName,
                    dateMillis = dateMillis,
                    notes = notes
                )
            )

            var newInvested = investment.amountInvested
            var newCurrent = investment.currentValue

            val isHist = accountName.isBlank() ||
                    accountName.contains("Histórico", ignoreCase = true) ||
                    accountName.contains("Passado", ignoreCase = true) ||
                    accountName.contains("Sem Conta", ignoreCase = true)

            when (type) {
                "APORTE" -> {
                    newInvested += amount
                    newCurrent += amount
                    if (!isHist) {
                        val tx = TransactionEntity(
                            title = "Aporte Investimento: ${investment.title}",
                            amount = amount,
                            type = TransactionType.INVESTMENT,
                            category = "Investimentos",
                            dateMillis = dateMillis,
                            accountName = accountName,
                            currency = investment.currency,
                            notes = notes
                        )
                        repository.addTransaction(tx)
                        adjustAccountBalanceForTransaction(accountName, amount, investment.currency, TransactionType.INVESTMENT, false)
                    }
                }
                "RESGATE" -> {
                    newCurrent = (newCurrent - amount).coerceAtLeast(0.0)
                    newInvested = (newInvested - amount).coerceAtLeast(0.0)
                    if (!isHist) {
                        val tx = TransactionEntity(
                            title = "Resgate Investimento: ${investment.title}",
                            amount = amount,
                            type = TransactionType.INCOME,
                            category = "Investimentos",
                            dateMillis = dateMillis,
                            accountName = accountName,
                            currency = investment.currency,
                            notes = notes
                        )
                        repository.addTransaction(tx)
                        adjustAccountBalanceForTransaction(accountName, amount, investment.currency, TransactionType.INCOME, false)
                    }
                }
                "RENDIMENTO" -> {
                    newCurrent += amount
                }
            }

            val updated = investment.copy(
                amountInvested = newInvested,
                currentValue = newCurrent,
                movementHistoryJson = InvestmentHistoryHelper.serializeLogs(logs),
                updatedAtMillis = System.currentTimeMillis()
            )
            repository.addInvestment(updated)
            _messageToast.value = "Movimentação adicionada ao histórico do investimento."
        }
    }

    fun deleteInvestmentMovementLog(
        investment: InvestmentEntity,
        logToDelete: InvestmentMovementLog
    ) {
        viewModelScope.launch {
            val logs = InvestmentHistoryHelper.parseLogs(investment.movementHistoryJson).toMutableList()
            logs.removeAll { it.id == logToDelete.id }

            var newInvested = investment.amountInvested
            var newCurrent = investment.currentValue

            when (logToDelete.type) {
                "APORTE" -> {
                    newInvested = (newInvested - logToDelete.amount).coerceAtLeast(0.0)
                    newCurrent = (newCurrent - logToDelete.amount).coerceAtLeast(0.0)
                }
                "RESGATE" -> {
                    newCurrent += logToDelete.amount
                    newInvested += logToDelete.amount
                }
                "RENDIMENTO" -> {
                    newCurrent = (newCurrent - logToDelete.amount).coerceAtLeast(0.0)
                }
            }

            val updated = investment.copy(
                amountInvested = newInvested,
                currentValue = newCurrent,
                movementHistoryJson = InvestmentHistoryHelper.serializeLogs(logs),
                updatedAtMillis = System.currentTimeMillis()
            )
            repository.addInvestment(updated)

            val isHist = logToDelete.accountName.contains("Histórico", ignoreCase = true) ||
                    logToDelete.accountName.contains("Passado", ignoreCase = true) ||
                    logToDelete.accountName.contains("Sem Conta", ignoreCase = true)

            if (!isHist) {
                val allTxs = repository.allTransactions.first()
                val matchingTx = allTxs.find { tx ->
                    (tx.title.contains(investment.title, ignoreCase = true) || tx.notes.contains(investment.title, ignoreCase = true)) &&
                    Math.abs(tx.amount - logToDelete.amount) < 0.01 &&
                    tx.accountName.equals(logToDelete.accountName, ignoreCase = true)
                }

                if (matchingTx != null) {
                    adjustAccountBalanceForTransaction(
                        accountName = matchingTx.accountName,
                        amount = matchingTx.amount,
                        currency = matchingTx.currency,
                        type = matchingTx.type,
                        isReversal = true
                    )
                    repository.deleteTransaction(matchingTx)
                }
            }

            _messageToast.value = "Movimentação removida do histórico e extrato estornado."
        }
    }

    private fun getInitialLanguage(prefs: android.content.SharedPreferences): AppLanguage {
        val saved = prefs.getString("app_language_code", null)
        if (!saved.isNullOrEmpty()) {
            return AppLanguage.fromCode(saved)
        }
        val systemLang = java.util.Locale.getDefault().language.lowercase()
        return when {
            systemLang.startsWith("es") -> AppLanguage.SPANISH
            systemLang.startsWith("en") -> AppLanguage.ENGLISH
            else -> AppLanguage.PORTUGUESE
        }
    }

    fun deleteInvestment(investment: InvestmentEntity) {
        viewModelScope.launch {
            repository.deleteInvestment(investment)

            // Revert all transactions linked to this investment title/institution
            val allTxs = repository.allTransactions.first()
            val relatedTxs = allTxs.filter { tx ->
                tx.title.contains(investment.title, ignoreCase = true) ||
                tx.notes.contains(investment.title, ignoreCase = true) ||
                (tx.type == TransactionType.INVESTMENT && tx.accountName.equals(investment.institution, ignoreCase = true))
            }

            relatedTxs.forEach { tx ->
                adjustAccountBalanceForTransaction(
                    accountName = tx.accountName,
                    amount = tx.amount,
                    currency = tx.currency,
                    type = tx.type,
                    isReversal = true
                )
                repository.deleteTransaction(tx)
            }

            _messageToast.value = "Investimento removido e saldos das contas estornados com sucesso."
        }
    }

    /**
     * Smart Budget Assistant: Analyzes user income (monthly, weekly, daily) and expense patterns,
     * allocating realistic spending ceilings based on real income limits.
     */
    fun applySmartBudgetSuggestions(incomeAmount: Double = 0.0, incomePeriod: String = "MENSAL") {
        viewModelScope.launch {
            val txs = repository.allTransactions.first()
            val expenses = txs.filter { it.type == TransactionType.EXPENSE }
            val currentBaseCurr = _baseCurrency.value

            // Convert income to monthly equivalent
            val monthlyIncome = when (incomePeriod) {
                "DIARIO" -> incomeAmount * 22.0
                "SEMANAL" -> incomeAmount * 4.33
                else -> incomeAmount
            }

            val categoryTotals = if (monthlyIncome > 0) {
                // Proportionally distribute realistic caps based on user's actual income
                mapOf(
                    "Alimentação" to monthlyIncome * 0.25,
                    "Moradia" to monthlyIncome * 0.30,
                    "Transporte" to monthlyIncome * 0.12,
                    "Lazer" to monthlyIncome * 0.10,
                    "Saúde" to monthlyIncome * 0.08,
                    "Educação" to monthlyIncome * 0.05,
                    "Investimentos" to monthlyIncome * 0.10
                )
            } else if (expenses.isNotEmpty()) {
                expenses.groupBy { it.category }
                    .mapValues { entry ->
                        entry.value.sumOf { CurrencyConverter.convert(it.amount, it.currency, currentBaseCurr) }
                    }
            } else {
                when (currentBaseCurr) {
                    "PYG" -> mapOf(
                        "Alimentação" to 800000.0,
                        "Transporte" to 300000.0,
                        "Moradia" to 1200000.0,
                        "Lazer" to 250000.0,
                        "Saúde" to 200000.0
                    )
                    "USD" -> mapOf(
                        "Alimentação" to 200.0,
                        "Transporte" to 80.0,
                        "Moradia" to 500.0,
                        "Lazer" to 100.0,
                        "Saúde" to 80.0
                    )
                    "BRL" -> mapOf(
                        "Alimentação" to 600.0,
                        "Transporte" to 250.0,
                        "Moradia" to 900.0,
                        "Lazer" to 200.0,
                        "Saúde" to 150.0
                    )
                    else -> mapOf(
                        "Alimentação" to 500.0,
                        "Transporte" to 200.0,
                        "Moradia" to 800.0,
                        "Lazer" to 150.0,
                        "Saúde" to 150.0
                    )
                }
            }

            categoryTotals.forEach { (category, targetCap) ->
                val suggestedBudget = when (currentBaseCurr) {
                    "PYG" -> Math.ceil(targetCap / 10000.0) * 10000.0
                    else -> Math.ceil(targetCap / 10.0) * 10.0
                }

                val existingGoal = repository.allGoals.first()
                    .find { it.category.equals(category, ignoreCase = true) && it.isCategoryBudget }

                if (existingGoal != null) {
                    repository.updateGoal(
                        existingGoal.copy(
                            targetAmount = suggestedBudget,
                            monthlyLimit = suggestedBudget
                        )
                    )
                } else {
                    repository.addGoal(
                        GoalEntity(
                            title = "Teto Otimizado: $category",
                            category = category,
                            targetAmount = suggestedBudget,
                            currentAmount = if (expenses.isNotEmpty()) {
                                expenses.filter { it.category.equals(category, ignoreCase = true) }
                                    .sumOf { CurrencyConverter.convert(it.amount, it.currency, currentBaseCurr) }
                            } else 0.0,
                            monthlyLimit = suggestedBudget,
                            isCategoryBudget = true
                        )
                    )
                }
            }

            _messageToast.value = "Tetos otimizados com base no seu ganho em $currentBaseCurr!"

            NotificationHelper.showSmartNotification(
                getApplication(),
                "🤖 Assistente de Orçamento Personalizado",
                "Seus tetos de gastos foram calculados sob medida para a sua renda em $currentBaseCurr."
            )
        }
    }

    fun applyCustomBudgetProposals(proposals: List<com.example.data.model.BudgetItemProposal>) {
        viewModelScope.launch {
            val currentBaseCurr = _baseCurrency.value
            val txs = repository.allTransactions.first()
            val expenses = txs.filter { it.type == TransactionType.EXPENSE }

            proposals.forEach { proposal ->
                val existingGoal = repository.allGoals.first()
                    .find { it.title.equals(proposal.title, ignoreCase = true) || (it.category.equals(proposal.category, ignoreCase = true) && it.isCategoryBudget) }

                val currentSpent = expenses.filter { it.category.equals(proposal.category, ignoreCase = true) }
                    .sumOf { CurrencyConverter.convert(it.amount, it.currency, currentBaseCurr) }

                if (existingGoal != null) {
                    repository.updateGoal(
                        existingGoal.copy(
                            title = proposal.title,
                            category = proposal.category,
                            targetAmount = proposal.amount,
                            monthlyLimit = if (proposal.isCategoryBudget) proposal.amount else 0.0,
                            isCategoryBudget = proposal.isCategoryBudget
                        )
                    )
                } else {
                    repository.addGoal(
                        GoalEntity(
                            title = proposal.title,
                            category = proposal.category,
                            targetAmount = proposal.amount,
                            currentAmount = currentSpent,
                            monthlyLimit = if (proposal.isCategoryBudget) proposal.amount else 0.0,
                            isCategoryBudget = proposal.isCategoryBudget
                        )
                    )
                }
            }

            _messageToast.value = "Tetos e botões personalizados configurados com sucesso!"
            NotificationHelper.showSmartNotification(
                getApplication(),
                "🤖 Orçamento IA Configurado",
                "${proposals.size} tetos de gastos e contas foram configurados no seu orçamento."
            )
        }
    }

    fun exportMonthlyReportPdf() {
        val state = uiState.value
        val totalBrlBalance = state.accounts.sumOf { CurrencyConverter.convert(it.balance, it.currency, "BRL") }
        val file = PdfReportExporter.generateAndSharePdf(
            getApplication(),
            state.transactions,
            state.goals,
            totalBrlBalance,
            state.baseCurrency
        )
        if (file != null) {
            _messageToast.value = "Relatório PDF gerado e pronto para exportar!"
        } else {
            _messageToast.value = "Erro ao criar relatório PDF."
        }
    }

    private fun loadCustomWorkspaces(prefs: android.content.SharedPreferences): List<String> {
        val savedStr = prefs.getString("custom_workspaces", null)
        if (!savedStr.isNullOrBlank()) {
            val list = savedStr.split("|||").map { it.trim() }.filter { it.isNotBlank() }
            if (list.isNotEmpty()) return list
        }
        return listOf("Pessoal", "Empresa / Negócios", "Viagens & Lazer", "Projetos")
    }

    private fun saveCustomWorkspaces(prefs: android.content.SharedPreferences, list: List<String>) {
        prefs.edit().putString("custom_workspaces", list.joinToString("|||")).apply()
    }
}
