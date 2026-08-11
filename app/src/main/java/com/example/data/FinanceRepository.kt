package com.example.data

import com.example.data.dao.*
import com.example.data.model.*
import com.example.util.CurrencyConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Random

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val goalDao: GoalDao,
    private val tipDao: TipDao,
    private val billDao: BillDao,
    private val investmentDao: InvestmentDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()
    val allTips: Flow<List<FinancialTipEntity>> = tipDao.getAllTips()
    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()
    val allInvestments: Flow<List<InvestmentEntity>> = investmentDao.getAllInvestments()

    suspend fun addInvestment(investment: InvestmentEntity): Long {
        return investmentDao.insertInvestment(investment)
    }

    suspend fun updateInvestment(investment: InvestmentEntity) {
        investmentDao.updateInvestment(investment)
    }

    suspend fun deleteInvestment(investment: InvestmentEntity) {
        investmentDao.deleteInvestment(investment)
    }

    suspend fun addTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun addBill(bill: BillEntity) {
        billDao.insertBill(bill)
    }

    suspend fun updateBill(bill: BillEntity) {
        billDao.updateBill(bill)
    }

    suspend fun deleteBill(bill: BillEntity) {
        billDao.deleteBill(bill)
    }

    suspend fun markBillAsPaid(bill: BillEntity) {
        billDao.updateBill(bill.copy(isPaid = true))
        // Auto register as an Expense transaction
        transactionDao.insertTransaction(
            TransactionEntity(
                title = "Pagamento: ${bill.title}",
                amount = bill.amount,
                type = TransactionType.EXPENSE,
                category = bill.category,
                dateMillis = System.currentTimeMillis(),
                accountName = "Conta Corrente",
                currency = bill.currency,
                notes = "Lembrete de Conta quitado"
            )
        )
    }

    suspend fun addGoal(goal: GoalEntity) {
        goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
    }

    suspend fun addAccount(account: AccountEntity) {
        accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: AccountEntity) {
        accountDao.deleteAccount(account)
    }

    /**
     * Open Finance / Bank API Connection
     * Connects/links the bank account into the database with real status tracking, without injecting random fake transactions.
     */
    suspend fun syncBankApi(bankName: String): Int {
        val now = System.currentTimeMillis()
        val currentAccounts = accountDao.getAllAccounts().first()
        val targetAccount = currentAccounts.find { it.bankName.equals(bankName, ignoreCase = true) || it.name.equals(bankName, ignoreCase = true) }

        if (targetAccount == null) {
            // Register new connected bank account with 0.0 initial balance
            val newAccount = AccountEntity(
                name = bankName,
                bankName = bankName,
                accountType = "CHECKING",
                balance = 0.0,
                currency = "PYG",
                lastSyncedMillis = now
            )
            accountDao.insertAll(listOf(newAccount))
        } else {
            // Update last synced time on existing account
            accountDao.updateAccount(
                targetAccount.copy(
                    lastSyncedMillis = now
                )
            )
        }

        return 0
    }

    /**
     * Seeds rich financial tips across spending reduction, income generation, and card management.
     */
    suspend fun seedEducationalTips() {
        val existingTips = tipDao.getAllTips().first()
        if (existingTips.isNotEmpty()) return

        val tips = listOf(
            FinancialTipEntity(
                title = "Regra das 24 Horas Anti-Impulso",
                content = "Antes de efetuar compras não essenciais acima de 150.000 ₲ (ou $ 25), espere 24 a 48 horas. Em 80% dos casos a urgência de consumo desaparece e você economiza.",
                category = "Consumo",
                potentialSavingsMonthly = 250000.0,
                tipType = "WARNING"
            ),
            FinancialTipEntity(
                title = "Auditoria de Assinaturas & Serviços Recorrentes",
                content = "Revise mensalmente serviços de internet, planos de celular e streamings. Cancele serviços não utilizados e negocie pacotes para reduzir saídas mensais.",
                category = "Gastos Coerentes",
                potentialSavingsMonthly = 180000.0,
                tipType = "SAVING"
            ),
            FinancialTipEntity(
                title = "Aumento de Renda com Freelance Internacional",
                content = "Monetize habilidades em tradução (Espanhol/Guaraní/Português/Inglês), suporte e tecnologia em plataformas de trabalho remoto recebendo em USD ou PYG.",
                category = "Mais Entradas",
                potentialSavingsMonthly = 1500000.0,
                tipType = "INVESTMENT"
            ),
            FinancialTipEntity(
                title = "Gestão Consciente de Cartões & Faturas",
                content = "Nunca pague apenas o saldo mínimo da fatura. Os juros do parcelado do cartão consomem seu orçamento. Programe lembretes 3 dias antes do vencimento.",
                category = "Faturas & Cartões",
                potentialSavingsMonthly = 350000.0,
                tipType = "WARNING"
            ),
            FinancialTipEntity(
                title = "Regra Orçamentária 50 / 30 / 20",
                content = "Destine 50% da renda para custos fixos (moradia, alimentação, luz ANDE, água), 30% para desejos pessoais e 20% para acumulação de reserva e investimentos.",
                category = "Planejamento",
                potentialSavingsMonthly = 500000.0,
                tipType = "SAVING"
            ),
            FinancialTipEntity(
                title = "Reserva de Emergência Bimoeda (PYG & USD)",
                content = "Mantenha de 3 a 6 meses de despesas salvas. Guarde 70% em Guaraní para uso diário e 30% em Dólar (USD) para proteção patrimonial de longo prazo.",
                category = "Patrimônio",
                potentialSavingsMonthly = 800000.0,
                tipType = "INVESTMENT"
            )
        )
        tipDao.insertAll(tips)
    }

    /**
     * Wipes all user database records so the user starts with a completely clean zero balance workspace.
     */
    suspend fun wipeAllData() {
        transactionDao.deleteAllTransactions()
        accountDao.deleteAllAccounts()
        goalDao.deleteAllGoals()
        billDao.deleteAllBills()
    }

    /**
     * Seeds initial default data for Paraguay context (Guaraní PYG & USD) when user requests demo data.
     */
    suspend fun seedParaguayData() {
        wipeAllData()
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 3600 * 1000

        val pyAccounts = listOf(
            AccountEntity(name = "Banco Familiar S.A.E.C.A. (PYG)", bankName = "Banco Familiar", accountType = "CHECKING", balance = 12000000.0, currency = "PYG"),
            AccountEntity(name = "Banco Itaú Paraguay (USD)", bankName = "Itaú PY", accountType = "INVESTMENT", balance = 2500.0, currency = "USD"),
            AccountEntity(name = "ueno bank / Billetera Zimple", bankName = "ueno bank", accountType = "SAVINGS", balance = 2800000.0, currency = "PYG")
        )
        accountDao.insertAll(pyAccounts)

        val pyTx = listOf(
            TransactionEntity(title = "Salario Mensual", amount = 8500000.0, type = TransactionType.INCOME, category = "Investimentos", dateMillis = now - 2 * dayMillis, accountName = "Banco Familiar S.A.E.C.A. (PYG)", currency = "PYG"),
            TransactionEntity(title = "Supermercado Stock / S360", amount = 750000.0, type = TransactionType.EXPENSE, category = "Alimentação", dateMillis = now - 1 * dayMillis, accountName = "Banco Familiar S.A.E.C.A. (PYG)", currency = "PYG"),
            TransactionEntity(title = "Servicios Básicos (ANDE / ESSAP)", amount = 420000.0, type = TransactionType.EXPENSE, category = "Moradia", dateMillis = now - 3 * dayMillis, accountName = "Banco Familiar S.A.E.C.A. (PYG)", currency = "PYG")
        )
        transactionDao.insertAll(pyTx)

        val pyBills = listOf(
            BillEntity(title = "ANDE - Luz Electrica", amount = 350000.0, category = "Moradia", dueDateMillis = now + 4 * dayMillis, recurrence = "MENSAL", isPaid = false, currency = "PYG"),
            BillEntity(title = "Internet Tigo / Personal Fibra", amount = 180000.0, category = "Moradia", dueDateMillis = now + 6 * dayMillis, recurrence = "MENSAL", isPaid = false, currency = "PYG")
        )
        pyBills.forEach { billDao.insertBill(it) }
    }

    /**
     * Seeds initial default data if database is empty.
     * Default for new installs is CLEAN ZERO BALANCE + Educational Tips.
     */
    suspend fun seedInitialDataIfEmpty() {
        seedEducationalTips()
        // Keeps user accounts/transactions completely clean (zeradas) on first launch
    }
}
