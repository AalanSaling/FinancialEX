package com.example.util

import com.example.data.model.TransactionType

enum class AppLanguage(val code: String, val label: String, val flag: String) {
    PORTUGUESE("PT", "Português", "🇧🇷"),
    SPANISH("ES", "Español", "🇵🇾"),
    ENGLISH("EN", "English", "🇺🇸");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.find { it.code.equals(code, ignoreCase = true) } ?: PORTUGUESE
    }
}

object Translations {

    fun getString(key: String, lang: AppLanguage): String {
        val strings = mapOf(
            // Navigation
            "nav_dashboard" to mapOf(AppLanguage.PORTUGUESE to "Painel", AppLanguage.SPANISH to "Tablero", AppLanguage.ENGLISH to "Dashboard"),
            "nav_transactions" to mapOf(AppLanguage.PORTUGUESE to "Extrato", AppLanguage.SPANISH to "Extracto", AppLanguage.ENGLISH to "Statement"),
            "nav_investments" to mapOf(AppLanguage.PORTUGUESE to "Investimentos", AppLanguage.SPANISH to "Inversiones", AppLanguage.ENGLISH to "Investments"),
            "nav_goals" to mapOf(AppLanguage.PORTUGUESE to "Metas", AppLanguage.SPANISH to "Metas", AppLanguage.ENGLISH to "Goals"),
            "nav_interest" to mapOf(AppLanguage.PORTUGUESE to "Juros", AppLanguage.SPANISH to "Interés", AppLanguage.ENGLISH to "Interest"),
            "nav_assets" to mapOf(AppLanguage.PORTUGUESE to "Moedas", AppLanguage.SPANISH to "Monedas", AppLanguage.ENGLISH to "Currencies"),

            // Dashboard Headers & Cards
            "total_balance" to mapOf(AppLanguage.PORTUGUESE to "Saldo Total Líquido", AppLanguage.SPANISH to "Saldo Total Neto", AppLanguage.ENGLISH to "Total Net Balance"),
            "incomes" to mapOf(AppLanguage.PORTUGUESE to "Entradas", AppLanguage.SPANISH to "Ingresos", AppLanguage.ENGLISH to "Incomes"),
            "expenses" to mapOf(AppLanguage.PORTUGUESE to "Saídas", AppLanguage.SPANISH to "Egresos", AppLanguage.ENGLISH to "Expenses"),
            "receivables" to mapOf(AppLanguage.PORTUGUESE to "A Receber", AppLanguage.SPANISH to "Por Cobrar", AppLanguage.ENGLISH to "Receivables"),
            "payables" to mapOf(AppLanguage.PORTUGUESE to "A Pagar", AppLanguage.SPANISH to "Por Pagar", AppLanguage.ENGLISH to "Payables"),
            "invested" to mapOf(AppLanguage.PORTUGUESE to "Investido", AppLanguage.SPANISH to "Invertido", AppLanguage.ENGLISH to "Invested"),
            "new_transaction" to mapOf(AppLanguage.PORTUGUESE to "Nova Transação", AppLanguage.SPANISH to "Nueva Transacción", AppLanguage.ENGLISH to "New Transaction"),
            "export_pdf" to mapOf(AppLanguage.PORTUGUESE to "Exportar PDF", AppLanguage.SPANISH to "Exportar PDF", AppLanguage.ENGLISH to "Export PDF"),
            "workspace" to mapOf(AppLanguage.PORTUGUESE to "Espaço", AppLanguage.SPANISH to "Espacio", AppLanguage.ENGLISH to "Workspace"),
            "reset_workspace" to mapOf(AppLanguage.PORTUGUESE to "Zerar / Reiniciar", AppLanguage.SPANISH to "Reiniciar a Cero", AppLanguage.ENGLISH to "Reset to Zero"),
            "recent_activity" to mapOf(AppLanguage.PORTUGUESE to "Atividade Recente", AppLanguage.SPANISH to "Actividad Reciente", AppLanguage.ENGLISH to "Recent Activity"),
            "no_transactions" to mapOf(AppLanguage.PORTUGUESE to "Nenhuma transação registrada.", AppLanguage.SPANISH to "No hay transacciones registradas.", AppLanguage.ENGLISH to "No transactions recorded."),
            "show_balance" to mapOf(AppLanguage.PORTUGUESE to "Exibir Saldo", AppLanguage.SPANISH to "Mostrar Saldo", AppLanguage.ENGLISH to "Show Balance"),
            "hide_balance" to mapOf(AppLanguage.PORTUGUESE to "Ocultar Saldo", AppLanguage.SPANISH to "Ocultar Saldo", AppLanguage.ENGLISH to "Hide Balance"),
            "financial_summary" to mapOf(AppLanguage.PORTUGUESE to "Resumo Financeiro", AppLanguage.SPANISH to "Resumen Financiero", AppLanguage.ENGLISH to "Financial Summary"),
            "bank_balance" to mapOf(AppLanguage.PORTUGUESE to "Saldo em Bancos", AppLanguage.SPANISH to "Saldo en Bancos", AppLanguage.ENGLISH to "Bank Balance"),
            "earned_interest" to mapOf(AppLanguage.PORTUGUESE to "Juros Ganhos", AppLanguage.SPANISH to "Intereses Ganados", AppLanguage.ENGLISH to "Earned Interest"),
            "investment_origin_account" to mapOf(AppLanguage.PORTUGUESE to "Banco / Conta de Origem (Débito)", AppLanguage.SPANISH to "Banco / Cuenta de Origen (Débito)", AppLanguage.ENGLISH to "Source Bank / Account (Debit)"),
            "destination_account" to mapOf(AppLanguage.PORTUGUESE to "Banco / Conta de Destino (Crédito)", AppLanguage.SPANISH to "Banco / Cuenta de Destino (Crédito)", AppLanguage.ENGLISH to "Destination Bank / Account (Credit)"),
            "resgate_investment" to mapOf(AppLanguage.PORTUGUESE to "Resgatar", AppLanguage.SPANISH to "Retirar", AppLanguage.ENGLISH to "Withdraw"),
            "investment_withdrawal_title" to mapOf(AppLanguage.PORTUGUESE to "Resgate / Retiro de Investimento", AppLanguage.SPANISH to "Retiro de Inversión", AppLanguage.ENGLISH to "Investment Redemption"),
            "is_historical_investment" to mapOf(AppLanguage.PORTUGUESE to "Investimento Passado / Já Aportado", AppLanguage.SPANISH to "Inversión Pasada / Ya Depositada", AppLanguage.ENGLISH to "Historical / Past Investment"),
            "historical_investment_subtitle" to mapOf(
                AppLanguage.PORTUGUESE to "Não debitar do saldo atual do banco (ideal para cadastrar aportes do passado).",
                AppLanguage.SPANISH to "No debitar del saldo actual del banco (ideal para aportes pasados).",
                AppLanguage.ENGLISH to "Do not debit from current bank balance (for past investments)."
            ),
            "movement_history" to mapOf(AppLanguage.PORTUGUESE to "Extrato / Histórico", AppLanguage.SPANISH to "Historial de Movimientos", AppLanguage.ENGLISH to "Movement History"),
            "add_movement" to mapOf(AppLanguage.PORTUGUESE to "+ Nova Movimentação", AppLanguage.SPANISH to "+ Nuevo Movimiento", AppLanguage.ENGLISH to "+ New Movement"),
            "quick_actions" to mapOf(AppLanguage.PORTUGUESE to "Ações Rápidas", AppLanguage.SPANISH to "Acciones Rápidas", AppLanguage.ENGLISH to "Quick Actions"),
            "accounts_wallets" to mapOf(AppLanguage.PORTUGUESE to "Contas e Carteiras", AppLanguage.SPANISH to "Cuentas y Billeteras", AppLanguage.ENGLISH to "Accounts & Wallets"),
            "accounts_replica_title" to mapOf(AppLanguage.PORTUGUESE to "Réplica de Bancos & Carteiras", AppLanguage.SPANISH to "Réplica de Bancos y Billeteras", AppLanguage.ENGLISH to "Bank & Wallet Replica"),
            "accounts_replica_subtitle" to mapOf(
                AppLanguage.PORTUGUESE to "Bancos e saldos atualizados automaticamente ao gastar ou receber.",
                AppLanguage.SPANISH to "Bancos y saldos actualizados automáticamente al realizar movimientos.",
                AppLanguage.ENGLISH to "Banks and balances automatically updated when money moves."
            ),
            "add_account_btn" to mapOf(AppLanguage.PORTUGUESE to "+ Novo Banco / Conta", AppLanguage.SPANISH to "+ Nuevo Banco / Cuenta", AppLanguage.ENGLISH to "+ New Bank / Account"),
            "new_account_title" to mapOf(AppLanguage.PORTUGUESE to "Cadastrar Banco ou Carteira", AppLanguage.SPANISH to "Registrar Banco o Billetera", AppLanguage.ENGLISH to "Register Bank or Wallet"),
            "edit_account_title" to mapOf(AppLanguage.PORTUGUESE to "Editar Banco ou Carteira", AppLanguage.SPANISH to "Editar Banco o Billetera", AppLanguage.ENGLISH to "Edit Bank or Wallet"),
            "account_name_label" to mapOf(AppLanguage.PORTUGUESE to "Nome da Conta (ex: Nubank, Itaú, Carteira)", AppLanguage.SPANISH to "Nombre de la Cuenta (ej: Itaú, Ueno, Billetera)", AppLanguage.ENGLISH to "Account Name (e.g. Chase, Cash Wallet)"),
            "bank_institution_label" to mapOf(AppLanguage.PORTUGUESE to "Instituição / Banco", AppLanguage.SPANISH to "Institución / Banco", AppLanguage.ENGLISH to "Institution / Bank"),
            "account_type_label" to mapOf(AppLanguage.PORTUGUESE to "Tipo de Conta", AppLanguage.SPANISH to "Tipo de Cuenta", AppLanguage.ENGLISH to "Account Type"),
            "type_checking" to mapOf(AppLanguage.PORTUGUESE to "Conta Corrente", AppLanguage.SPANISH to "Cuenta Corriente", AppLanguage.ENGLISH to "Checking Account"),
            "type_savings" to mapOf(AppLanguage.PORTUGUESE to "Poupança / Caixinha", AppLanguage.SPANISH to "Ahorro / Caja", AppLanguage.ENGLISH to "Savings Account"),
            "type_cash" to mapOf(AppLanguage.PORTUGUESE to "Dinheiro Físico / Carteira", AppLanguage.SPANISH to "Efectivo / Billetera Física", AppLanguage.ENGLISH to "Physical Cash / Wallet"),
            "type_investment" to mapOf(AppLanguage.PORTUGUESE to "Corretora / Fundo Liquidez", AppLanguage.SPANISH to "Casa de Bolsa / Fondo Liquidez", AppLanguage.ENGLISH to "Brokerage / Liquidity Fund"),
            "type_credit_card" to mapOf(AppLanguage.PORTUGUESE to "Cartão de Crédito", AppLanguage.SPANISH to "Tarjeta de Crédito", AppLanguage.ENGLISH to "Credit Card"),
            "initial_balance_label" to mapOf(AppLanguage.PORTUGUESE to "Saldo Atual", AppLanguage.SPANISH to "Saldo Actual", AppLanguage.ENGLISH to "Current Balance"),
            "delete_account_confirm" to mapOf(AppLanguage.PORTUGUESE to "Tem certeza que deseja remover esta conta?", AppLanguage.SPANISH to "¿Está seguro de eliminar esta cuenta?", AppLanguage.ENGLISH to "Are you sure you want to delete this account?"),
            "select_account_origin" to mapOf(AppLanguage.PORTUGUESE to "Selecione o Banco / Origem", AppLanguage.SPANISH to "Seleccione el Banco / Origen", AppLanguage.ENGLISH to "Select Bank / Origin Account"),
            "auto_updated_balance" to mapOf(AppLanguage.PORTUGUESE to "Saldo sincronizado automaticamente com movimentações!", AppLanguage.SPANISH to "¡Saldo sincronizado automáticamente con movimientos!", AppLanguage.ENGLISH to "Balance synced automatically with transactions!"),

            // Workspaces
            "ws_personal" to mapOf(AppLanguage.PORTUGUESE to "Pessoal", AppLanguage.SPANISH to "Personal", AppLanguage.ENGLISH to "Personal"),
            "ws_business" to mapOf(AppLanguage.PORTUGUESE to "Empresa / Negócios", AppLanguage.SPANISH to "Empresa / Negocios", AppLanguage.ENGLISH to "Business / Work"),
            "ws_travel" to mapOf(AppLanguage.PORTUGUESE to "Viagens & Lazer", AppLanguage.SPANISH to "Viajes y Ocio", AppLanguage.ENGLISH to "Travel & Leisure"),
            "ws_projects" to mapOf(AppLanguage.PORTUGUESE to "Projetos", AppLanguage.SPANISH to "Proyectos", AppLanguage.ENGLISH to "Projects"),

            // Transaction Types & Labels
            "income_label" to mapOf(AppLanguage.PORTUGUESE to "Entrada", AppLanguage.SPANISH to "Ingreso", AppLanguage.ENGLISH to "Income"),
            "expense_label" to mapOf(AppLanguage.PORTUGUESE to "Saída", AppLanguage.SPANISH to "Egreso", AppLanguage.ENGLISH to "Expense"),
            "future_expense_label" to mapOf(AppLanguage.PORTUGUESE to "A Pagar", AppLanguage.SPANISH to "Por Pagar", AppLanguage.ENGLISH to "Payable"),
            "receivable_label" to mapOf(AppLanguage.PORTUGUESE to "A Receber", AppLanguage.SPANISH to "Por Cobrar", AppLanguage.ENGLISH to "Receivable"),
            "investment_label" to mapOf(AppLanguage.PORTUGUESE to "Investimento", AppLanguage.SPANISH to "Inversión", AppLanguage.ENGLISH to "Investment"),
            "filter_all" to mapOf(AppLanguage.PORTUGUESE to "Todos", AppLanguage.SPANISH to "Todos", AppLanguage.ENGLISH to "All"),

            // Categories
            "cat_food" to mapOf(AppLanguage.PORTUGUESE to "Alimentação", AppLanguage.SPANISH to "Alimentación", AppLanguage.ENGLISH to "Food & Dining"),
            "cat_transport" to mapOf(AppLanguage.PORTUGUESE to "Transporte", AppLanguage.SPANISH to "Transporte", AppLanguage.ENGLISH to "Transport"),
            "cat_housing" to mapOf(AppLanguage.PORTUGUESE to "Moradia", AppLanguage.SPANISH to "Vivienda", AppLanguage.ENGLISH to "Housing"),
            "cat_leisure" to mapOf(AppLanguage.PORTUGUESE to "Lazer", AppLanguage.SPANISH to "Ocio", AppLanguage.ENGLISH to "Leisure"),
            "cat_investments" to mapOf(AppLanguage.PORTUGUESE to "Investimentos", AppLanguage.SPANISH to "Inversiones", AppLanguage.ENGLISH to "Investments"),
            "cat_health" to mapOf(AppLanguage.PORTUGUESE to "Saúde", AppLanguage.SPANISH to "Salud", AppLanguage.ENGLISH to "Health"),
            "cat_education" to mapOf(AppLanguage.PORTUGUESE to "Educação", AppLanguage.SPANISH to "Educación", AppLanguage.ENGLISH to "Education"),
            "cat_other" to mapOf(AppLanguage.PORTUGUESE to "Outros", AppLanguage.SPANISH to "Otros", AppLanguage.ENGLISH to "Others"),

            // Transaction Dialog
            "add_tx_title" to mapOf(AppLanguage.PORTUGUESE to "Nova Transação", AppLanguage.SPANISH to "Nueva Transacción", AppLanguage.ENGLISH to "New Transaction"),
            "description" to mapOf(AppLanguage.PORTUGUESE to "Descrição", AppLanguage.SPANISH to "Descripción", AppLanguage.ENGLISH to "Description"),
            "amount" to mapOf(AppLanguage.PORTUGUESE to "Valor", AppLanguage.SPANISH to "Monto", AppLanguage.ENGLISH to "Amount"),
            "currency" to mapOf(AppLanguage.PORTUGUESE to "Moeda", AppLanguage.SPANISH to "Moneda", AppLanguage.ENGLISH to "Currency"),
            "account_origin" to mapOf(AppLanguage.PORTUGUESE to "Conta / Origem", AppLanguage.SPANISH to "Cuenta / Origen", AppLanguage.ENGLISH to "Account / Origin"),
            "category" to mapOf(AppLanguage.PORTUGUESE to "Categoria", AppLanguage.SPANISH to "Categoría", AppLanguage.ENGLISH to "Category"),
            "notes" to mapOf(AppLanguage.PORTUGUESE to "Observações (Opcional)", AppLanguage.SPANISH to "Notas (Opcional)", AppLanguage.ENGLISH to "Notes (Optional)"),
            "save" to mapOf(AppLanguage.PORTUGUESE to "Salvar", AppLanguage.SPANISH to "Guardar", AppLanguage.ENGLISH to "Save"),
            "save_tx" to mapOf(AppLanguage.PORTUGUESE to "Salvar Transação", AppLanguage.SPANISH to "Guardar Transacción", AppLanguage.ENGLISH to "Save Transaction"),
            "cancel" to mapOf(AppLanguage.PORTUGUESE to "Cancelar", AppLanguage.SPANISH to "Cancelar", AppLanguage.ENGLISH to "Cancel"),

            // Currencies & Exchange Screen
            "currencies_title" to mapOf(AppLanguage.PORTUGUESE to "Moedas & Câmbio Internacional", AppLanguage.SPANISH to "Monedas y Cambio Internacional", AppLanguage.ENGLISH to "Live Currencies & FX"),
            "converter_title" to mapOf(AppLanguage.PORTUGUESE to "Conversor de Câmbio em Tempo Real", AppLanguage.SPANISH to "Conversor de Moneda en Tiempo Real", AppLanguage.ENGLISH to "Real-Time Currency Converter"),
            "from_currency" to mapOf(AppLanguage.PORTUGUESE to "Moeda de Origem", AppLanguage.SPANISH to "Moneda de Origen", AppLanguage.ENGLISH to "Source Currency"),
            "to_currency" to mapOf(AppLanguage.PORTUGUESE to "Moeda de Destino", AppLanguage.SPANISH to "Moneda de Destino", AppLanguage.ENGLISH to "Target Currency"),
            "result" to mapOf(AppLanguage.PORTUGUESE to "Resultado", AppLanguage.SPANISH to "Resultado", AppLanguage.ENGLISH to "Result"),
            "live" to mapOf(AppLanguage.PORTUGUESE to "Ao vivo", AppLanguage.SPANISH to "En vivo", AppLanguage.ENGLISH to "Live"),
            "rates_today" to mapOf(AppLanguage.PORTUGUESE to "Cotações do Dia em Relação ao Guaraní (PYG)", AppLanguage.SPANISH to "Cotizaciones del Día en Relación al Guaraní (PYG)", AppLanguage.ENGLISH to "Daily FX Rates Relative to Guaraní (PYG)"),

            // Compound Interest Screen
            "interest_simulator" to mapOf(AppLanguage.PORTUGUESE to "Simulador de Juros Compostos", AppLanguage.SPANISH to "Simulador de Interés Compuesto", AppLanguage.ENGLISH to "Compound Interest Calculator"),
            "initial_deposit" to mapOf(AppLanguage.PORTUGUESE to "Aporte Inicial", AppLanguage.SPANISH to "Inversión Inicial", AppLanguage.ENGLISH to "Initial Deposit"),
            "monthly_deposit" to mapOf(AppLanguage.PORTUGUESE to "Aporte Mensal", AppLanguage.SPANISH to "Aporte Mensual", AppLanguage.ENGLISH to "Monthly Contribution"),
            "annual_rate" to mapOf(AppLanguage.PORTUGUESE to "Taxa de Juros Anual (%)", AppLanguage.SPANISH to "Tasa de Interés Anual (%)", AppLanguage.ENGLISH to "Annual Interest Rate (%)"),
            "years_period" to mapOf(AppLanguage.PORTUGUESE to "Período (Anos)", AppLanguage.SPANISH to "Período (Años)", AppLanguage.ENGLISH to "Period (Years)"),
            "calculate" to mapOf(AppLanguage.PORTUGUESE to "Calcular", AppLanguage.SPANISH to "Calcular", AppLanguage.ENGLISH to "Calculate"),
            "total_invested" to mapOf(AppLanguage.PORTUGUESE to "Total Investido", AppLanguage.SPANISH to "Total Invertido", AppLanguage.ENGLISH to "Total Invested"),
            "total_interest" to mapOf(AppLanguage.PORTUGUESE to "Total em Juros", AppLanguage.SPANISH to "Total de Intereses", AppLanguage.ENGLISH to "Total Interest Earned"),
            "final_amount" to mapOf(AppLanguage.PORTUGUESE to "Montante Final", AppLanguage.SPANISH to "Monto Final", AppLanguage.ENGLISH to "Final Amount"),

            // Settings & Workspace Modal
            "app_settings" to mapOf(AppLanguage.PORTUGUESE to "Configurações do App", AppLanguage.SPANISH to "Configuración del App", AppLanguage.ENGLISH to "App Settings"),
            "workspace_setup" to mapOf(AppLanguage.PORTUGUESE to "Configuração do Espaço", AppLanguage.SPANISH to "Configuración del Espacio", AppLanguage.ENGLISH to "Workspace Settings"),
            "primary_currency" to mapOf(AppLanguage.PORTUGUESE to "Moeda Principal do Espaço", AppLanguage.SPANISH to "Moneda Principal del Espacio", AppLanguage.ENGLISH to "Main Workspace Currency"),
            "reset_to_zero" to mapOf(AppLanguage.PORTUGUESE to "Zerar Espaço (Saldo 0)", AppLanguage.SPANISH to "Reiniciar a Cero (Saldo 0)", AppLanguage.ENGLISH to "Reset Workspace (Zero Balance)"),
            "reset_desc" to mapOf(AppLanguage.PORTUGUESE to "Limpa todas as contas e transações para iniciar do zero", AppLanguage.SPANISH to "Limpia todas las cuentas y transacciones para empezar de cero", AppLanguage.ENGLISH to "Clears all accounts and transactions to start clean"),
            "close" to mapOf(AppLanguage.PORTUGUESE to "Fechar", AppLanguage.SPANISH to "Cerrar", AppLanguage.ENGLISH to "Close"),
            "language" to mapOf(AppLanguage.PORTUGUESE to "Idioma", AppLanguage.SPANISH to "Idioma", AppLanguage.ENGLISH to "Language"),
            "security_pin_title" to mapOf(AppLanguage.PORTUGUESE to "Proteção de Acesso (Biometria/PIN)", AppLanguage.SPANISH to "Protección de Acceso (Biometría/PIN)", AppLanguage.ENGLISH to "Access Protection (Biometrics/PIN)"),
            "pin_registered" to mapOf(AppLanguage.PORTUGUESE to "PIN Personalizado Cadastrado", AppLanguage.SPANISH to "PIN Personalizado Registrado", AppLanguage.ENGLISH to "Custom PIN Registered"),
            "no_pin_registered" to mapOf(AppLanguage.PORTUGUESE to "Nenhum PIN Cadastrado", AppLanguage.SPANISH to "Sin PIN Registrado", AppLanguage.ENGLISH to "No PIN Registered"),
            "register_pin" to mapOf(AppLanguage.PORTUGUESE to "Cadastrar PIN de 4 dígitos", AppLanguage.SPANISH to "Registrar PIN de 4 dígitos", AppLanguage.ENGLISH to "Register 4-digit PIN"),
            "change_pin" to mapOf(AppLanguage.PORTUGUESE to "Alterar PIN de Segurança", AppLanguage.SPANISH to "Cambiar PIN de Seguridad", AppLanguage.ENGLISH to "Change Security PIN"),
            "save_pin" to mapOf(AppLanguage.PORTUGUESE to "Salvar PIN", AppLanguage.SPANISH to "Guardar PIN", AppLanguage.ENGLISH to "Save PIN"),

            // Goals & Bills
            "goals_title" to mapOf(AppLanguage.PORTUGUESE to "Metas & Compras Parceladas", AppLanguage.SPANISH to "Metas y Compras a Cuotas", AppLanguage.ENGLISH to "Goals & Installment Purchases"),
            "goals_subtitle" to mapOf(
                AppLanguage.PORTUGUESE to "Acompanhe suas metas de economia, compras parceladas e prazos de pagamento.",
                AppLanguage.SPANISH to "Sigue tus metas de ahorro, compras a cuotas y plazos de pago.",
                AppLanguage.ENGLISH to "Track savings goals, installment purchases, and payment due dates."
            ),
            "add_goal" to mapOf(AppLanguage.PORTUGUESE to "Nova Meta / Compra", AppLanguage.SPANISH to "Nueva Meta / Compra", AppLanguage.ENGLISH to "New Goal / Purchase"),
            "new_goal_title" to mapOf(AppLanguage.PORTUGUESE to "Cadastrar Meta ou Compra", AppLanguage.SPANISH to "Registrar Meta o Compra", AppLanguage.ENGLISH to "Register Goal or Purchase"),
            "edit_goal_title" to mapOf(AppLanguage.PORTUGUESE to "Editar Meta ou Compra", AppLanguage.SPANISH to "Editar Meta o Compra", AppLanguage.ENGLISH to "Edit Goal or Purchase"),
            "bills_reminders" to mapOf(AppLanguage.PORTUGUESE to "Contas & Lembretes", AppLanguage.SPANISH to "Cuentas y Recordatorios", AppLanguage.ENGLISH to "Bills & Reminders"),
            "add_bill" to mapOf(AppLanguage.PORTUGUESE to "Novo Lembrete", AppLanguage.SPANISH to "Nuevo Recordatorio", AppLanguage.ENGLISH to "New Reminder"),
            "mark_paid" to mapOf(AppLanguage.PORTUGUESE to "Marcar Paga", AppLanguage.SPANISH to "Marcar Pagada", AppLanguage.ENGLISH to "Mark Paid"),
            "paid" to mapOf(AppLanguage.PORTUGUESE to "Paga", AppLanguage.SPANISH to "Pagada", AppLanguage.ENGLISH to "Paid"),
            "pending" to mapOf(AppLanguage.PORTUGUESE to "Pendente", AppLanguage.SPANISH to "Pendiente", AppLanguage.ENGLISH to "Pending"),
            "target_amount" to mapOf(AppLanguage.PORTUGUESE to "Valor Total da Meta", AppLanguage.SPANISH to "Monto Total de la Meta", AppLanguage.ENGLISH to "Total Goal Amount"),
            "category_budget_limit" to mapOf(AppLanguage.PORTUGUESE to "Teto por Categoria", AppLanguage.SPANISH to "Límite por Categoría", AppLanguage.ENGLISH to "Category Budget Limit"),
            "savings_goal" to mapOf(AppLanguage.PORTUGUESE to "Meta À Vista / Economia", AppLanguage.SPANISH to "Meta Al Contado / Ahorro", AppLanguage.ENGLISH to "Lump Sum / Savings Goal"),
            "goals_empty_msg" to mapOf(
                AppLanguage.PORTUGUESE to "Nenhuma meta ou compra parcelada cadastrada.",
                AppLanguage.SPANISH to "No hay metas o compras a cuotas registradas.",
                AppLanguage.ENGLISH to "No goals or installment purchases registered."
            ),
            "goal_title_field" to mapOf(
                AppLanguage.PORTUGUESE to "Nome da Meta / Compra",
                AppLanguage.SPANISH to "Nombre de la Meta / Compra",
                AppLanguage.ENGLISH to "Goal / Purchase Name"
            ),
            "payment_mode_label" to mapOf(
                AppLanguage.PORTUGUESE to "Estrutura do Pagamento",
                AppLanguage.SPANISH to "Estructura del Pago",
                AppLanguage.ENGLISH to "Payment Structure"
            ),
            "mode_lump_sum" to mapOf(
                AppLanguage.PORTUGUESE to "À Vista",
                AppLanguage.SPANISH to "Al Contado",
                AppLanguage.ENGLISH to "Lump Sum"
            ),
            "mode_installments" to mapOf(
                AppLanguage.PORTUGUESE to "Parcelado",
                AppLanguage.SPANISH to "A Cuotas",
                AppLanguage.ENGLISH to "Installments"
            ),
            "monthly_installment_val" to mapOf(
                AppLanguage.PORTUGUESE to "Valor de Cada Parcela",
                AppLanguage.SPANISH to "Monto de Cada Cuota",
                AppLanguage.ENGLISH to "Amount Per Installment"
            ),
            "total_installments_qty" to mapOf(
                AppLanguage.PORTUGUESE to "Número Total de Parcelas",
                AppLanguage.SPANISH to "Número Total de Cuotas",
                AppLanguage.ENGLISH to "Total Number of Installments"
            ),
            "due_day_of_month" to mapOf(
                AppLanguage.PORTUGUESE to "Dia de Vencimento no Mês",
                AppLanguage.SPANISH to "Día de Vencimiento en el Mes",
                AppLanguage.ENGLISH to "Due Day of Month"
            ),
            "calculated_total" to mapOf(
                AppLanguage.PORTUGUESE to "Valor Total Calculado",
                AppLanguage.SPANISH to "Monto Total Calculado",
                AppLanguage.ENGLISH to "Calculated Total Amount"
            ),
            "installments_count_paid" to mapOf(
                AppLanguage.PORTUGUESE to "parcelas pagas",
                AppLanguage.SPANISH to "cuotas pagadas",
                AppLanguage.ENGLISH to "installments paid"
            ),
            "due_day_prefix" to mapOf(
                AppLanguage.PORTUGUESE to "Vencimento: dia",
                AppLanguage.SPANISH to "Vencimiento: día",
                AppLanguage.ENGLISH to "Due on day"
            ),

            // Goal Actions & Payment History
            "add_deposit" to mapOf(AppLanguage.PORTUGUESE to "+ Registrar Pagamento", AppLanguage.SPANISH to "+ Registrar Pago", AppLanguage.ENGLISH to "+ Register Payment"),
            "pay_single_installment" to mapOf(AppLanguage.PORTUGUESE to "+ Pagar Parcela", AppLanguage.SPANISH to "+ Pagar Cuota", AppLanguage.ENGLISH to "+ Pay Installment"),
            "payment_history_btn" to mapOf(AppLanguage.PORTUGUESE to "Histórico", AppLanguage.SPANISH to "Historial", AppLanguage.ENGLISH to "History"),
            "payment_history_title" to mapOf(AppLanguage.PORTUGUESE to "Histórico de Pagamentos", AppLanguage.SPANISH to "Historial de Pagos", AppLanguage.ENGLISH to "Payment History"),
            "mark_completed" to mapOf(AppLanguage.PORTUGUESE to "Concluir", AppLanguage.SPANISH to "Completar", AppLanguage.ENGLISH to "Complete"),
            "completed" to mapOf(AppLanguage.PORTUGUESE to "Concluída! 🎉", AppLanguage.SPANISH to "¡Completada! 🎉", AppLanguage.ENGLISH to "Completed! 🎉"),
            "add_deposit_title" to mapOf(AppLanguage.PORTUGUESE to "Registrar Pagamento / Aporte", AppLanguage.SPANISH to "Registrar Pago / Aporte", AppLanguage.ENGLISH to "Register Payment / Deposit"),
            "deposit_amount" to mapOf(AppLanguage.PORTUGUESE to "Valor do Pagamento", AppLanguage.SPANISH to "Monto del Pago", AppLanguage.ENGLISH to "Payment Amount"),
            "payment_date_label" to mapOf(AppLanguage.PORTUGUESE to "Data do Pagamento (dd/mm/aaaa)", AppLanguage.SPANISH to "Fecha del Pago (dd/mm/aaaa)", AppLanguage.ENGLISH to "Payment Date (dd/mm/yyyy)"),
            "payment_note_label" to mapOf(AppLanguage.PORTUGUESE to "Observação / Nota (Opcional)", AppLanguage.SPANISH to "Observación / Nota (Opcional)", AppLanguage.ENGLISH to "Note / Description (Optional)"),
            "installment_number_label" to mapOf(AppLanguage.PORTUGUESE to "Número da Parcela", AppLanguage.SPANISH to "Número de Cuota", AppLanguage.ENGLISH to "Installment Number"),
            "remaining_installments_txt" to mapOf(AppLanguage.PORTUGUESE to "Parcelas Restantes", AppLanguage.SPANISH to "Cuotas Restantes", AppLanguage.ENGLISH to "Remaining Installments"),
            "remaining_balance_txt" to mapOf(AppLanguage.PORTUGUESE to "Saldo Restante", AppLanguage.SPANISH to "Saldo Restante", AppLanguage.ENGLISH to "Remaining Balance"),
            "no_payments_msg" to mapOf(AppLanguage.PORTUGUESE to "Nenhum pagamento registrado ainda.", AppLanguage.SPANISH to "No hay pagos registrados aún.", AppLanguage.ENGLISH to "No payments recorded yet."),
            "target_due_date_label" to mapOf(AppLanguage.PORTUGUESE to "Data Límite / Vencimento (dd/mm/aaaa)", AppLanguage.SPANISH to "Fecha Límite / Vencimiento (dd/mm/aaaa)", AppLanguage.ENGLISH to "Target Date / Due Date (dd/mm/yyyy)"),
            "congratulations_title" to mapOf(AppLanguage.PORTUGUESE to "🎉 Parabéns pela Conquista! 🎉", AppLanguage.SPANISH to "¡Felicidades por la Conquista! 🎉", AppLanguage.ENGLISH to "🎉 Congratulations on Your Goal! 🎉"),
            "congratulations_msg" to mapOf(
                AppLanguage.PORTUGUESE to "Você concluiu com sucesso todos os pagamentos da meta! Excelente disciplina financeira!",
                AppLanguage.SPANISH to "¡Completaste con éxito todos los pagos de tu meta! ¡Excelente disciplina financiera!",
                AppLanguage.ENGLISH to "You successfully completed all payments for this goal! Outstanding financial discipline!"
            ),
            "awesome_btn" to mapOf(AppLanguage.PORTUGUESE to "Incrível!", AppLanguage.SPANISH to "¡Increíble!", AppLanguage.ENGLISH to "Awesome!"),
            "save_goal" to mapOf(AppLanguage.PORTUGUESE to "Salvar Meta / Compra", AppLanguage.SPANISH to "Guardar Meta / Compra", AppLanguage.ENGLISH to "Save Goal / Purchase"),

            // Investments Screen
            "investments" to mapOf(AppLanguage.PORTUGUESE to "Investimentos", AppLanguage.SPANISH to "Inversiones", AppLanguage.ENGLISH to "Investments"),
            "investments_subtitle" to mapOf(AppLanguage.PORTUGUESE to "Portfólio de ativos, depósitos e rendimentos", AppLanguage.SPANISH to "Portafolio de activos, depósitos y rendimientos", AppLanguage.ENGLISH to "Portfolio assets, deposits and yields"),
            "tab_portfolio" to mapOf(AppLanguage.PORTUGUESE to "Portfólio", AppLanguage.SPANISH to "Portafolio", AppLanguage.ENGLISH to "Portfolio"),
            "tab_simulator" to mapOf(AppLanguage.PORTUGUESE to "Simulador", AppLanguage.SPANISH to "Simulador", AppLanguage.ENGLISH to "Simulator"),
            "title" to mapOf(AppLanguage.PORTUGUESE to "Nome do Investimento", AppLanguage.SPANISH to "Nombre de la Inversión", AppLanguage.ENGLISH to "Investment Name"),
            "institution_label" to mapOf(AppLanguage.PORTUGUESE to "Instituição / Corretora / Banco", AppLanguage.SPANISH to "Institución / Casa de Bolsa", AppLanguage.ENGLISH to "Institution / Brokerage"),
            "invested_amount" to mapOf(AppLanguage.PORTUGUESE to "Valor Investido Total", AppLanguage.SPANISH to "Monto Invertido Total", AppLanguage.ENGLISH to "Total Invested Amount"),
            "withdrawal_date" to mapOf(AppLanguage.PORTUGUESE to "Data do Resgate (dd/mm/aaaa)", AppLanguage.SPANISH to "Fecha del Retiro (dd/mm/aaaa)", AppLanguage.ENGLISH to "Withdrawal Date (dd/mm/yyyy)"),
            "movement_date" to mapOf(AppLanguage.PORTUGUESE to "Data da Movimentação (dd/mm/aaaa)", AppLanguage.SPANISH to "Fecha del Movimiento (dd/mm/aaaa)", AppLanguage.ENGLISH to "Movement Date (dd/mm/yyyy)"),
            "account_origin_label" to mapOf(AppLanguage.PORTUGUESE to "Conta de Origem / Pagamento", AppLanguage.SPANISH to "Cuenta de Origen / Pago", AppLanguage.ENGLISH to "Source Account / Payment"),
            "delete_btn" to mapOf(AppLanguage.PORTUGUESE to "Excluir", AppLanguage.SPANISH to "Eliminar", AppLanguage.ENGLISH to "Delete"),
            "edit_btn" to mapOf(AppLanguage.PORTUGUESE to "Editar", AppLanguage.SPANISH to "Editar", AppLanguage.ENGLISH to "Edit"),
            "annual_rate_percent" to mapOf(AppLanguage.PORTUGUESE to "Taxa Anual (% a.a.)", AppLanguage.SPANISH to "Tasa Anual (% a.a.)", AppLanguage.ENGLISH to "Annual Rate (% p.a.)"),
            "horizon_years" to mapOf(AppLanguage.PORTUGUESE to "Horizonte (Anos)", AppLanguage.SPANISH to "Horizonte (Años)", AppLanguage.ENGLISH to "Horizon (Years)"),
            "future_wealth" to mapOf(AppLanguage.PORTUGUESE to "Patrimônio Futuro Estimado:", AppLanguage.SPANISH to "Patrimonio Futuro Estimado:", AppLanguage.ENGLISH to "Estimated Future Wealth:"),
            "total_invested_calc" to mapOf(AppLanguage.PORTUGUESE to "Total de Capital Aportado:", AppLanguage.SPANISH to "Total de Capital Aportado:", AppLanguage.ENGLISH to "Total Invested Capital:"),
            "earned_interest" to mapOf(AppLanguage.PORTUGUESE to "Total em Juros Compostos:", AppLanguage.SPANISH to "Total en Intereses Compuestos:", AppLanguage.ENGLISH to "Total Compound Interest:"),
            "new_investment" to mapOf(AppLanguage.PORTUGUESE to "Novo Investimento", AppLanguage.SPANISH to "Nueva Inversión", AppLanguage.ENGLISH to "New Investment"),
            "portfolio_title" to mapOf(AppLanguage.PORTUGUESE to "Ativos & Portfólio", AppLanguage.SPANISH to "Activos y Portafolio", AppLanguage.ENGLISH to "Assets & Portfolio"),
            "simulator_title" to mapOf(AppLanguage.PORTUGUESE to "Simulador de Juros", AppLanguage.SPANISH to "Simulador de Interés", AppLanguage.ENGLISH to "Interest Simulator"),
            "investments_portfolio_title" to mapOf(AppLanguage.PORTUGUESE to "Portfólio de Investimentos", AppLanguage.SPANISH to "Portafolio de Inversiones", AppLanguage.ENGLISH to "Investment Portfolio"),
            "investments_portfolio_sub" to mapOf(AppLanguage.PORTUGUESE to "Fondos Mutuos, CDA, Acciones e Bonos", AppLanguage.SPANISH to "Fondos Mutuos, CDA, Acciones y Bonos", AppLanguage.ENGLISH to "Mutual Funds, CDAs, Stocks and Bonds"),
            "total_portfolio_val" to mapOf(AppLanguage.PORTUGUESE to "Valor Total do Portfólio", AppLanguage.SPANISH to "Valor Total del Portafolio", AppLanguage.ENGLISH to "Total Portfolio Value"),
            "invested_capital" to mapOf(AppLanguage.PORTUGUESE to "Capital Investido", AppLanguage.SPANISH to "Capital Invertido", AppLanguage.ENGLISH to "Invested Capital"),
            "profit_yield" to mapOf(AppLanguage.PORTUGUESE to "Lucro / Rendimento", AppLanguage.SPANISH to "Ganancia / Rendimiento", AppLanguage.ENGLISH to "Profit / Yield"),
            "no_investments_title" to mapOf(AppLanguage.PORTUGUESE to "Nenhum investimento registrado ainda.", AppLanguage.SPANISH to "No hay inversiones registradas aún.", AppLanguage.ENGLISH to "No investments registered yet."),
            "no_investments_sub" to mapOf(
                AppLanguage.PORTUGUESE to "Cadastre seus Fondos Mutuos, CDA, Acciones ou Bonos para acompanhar rendimentos.",
                AppLanguage.SPANISH to "Registra tus Fondos Mutuos, CDA, Acciones o Bonos para seguir tus rendimientos.",
                AppLanguage.ENGLISH to "Register your Mutual Funds, CDAs, Stocks or Bonds to track returns."
            ),
            "current_val" to mapOf(AppLanguage.PORTUGUESE to "Valor Atual", AppLanguage.SPANISH to "Valor Actual", AppLanguage.ENGLISH to "Current Value"),
            "estimated_profit" to mapOf(AppLanguage.PORTUGUESE to "Rendimento Estimado", AppLanguage.SPANISH to "Rendimiento Estimado", AppLanguage.ENGLISH to "Estimated Yield"),
            "contracted_rate" to mapOf(AppLanguage.PORTUGUESE to "Taxa Contratada:", AppLanguage.SPANISH to "Tasa Contratada:", AppLanguage.ENGLISH to "Contracted Rate:"),
            "add_aporte_btn" to mapOf(AppLanguage.PORTUGUESE to "+ Novo Aporte / Editar", AppLanguage.SPANISH to "+ Nuevo Aporte / Editar", AppLanguage.ENGLISH to "+ New Deposit / Edit"),
            "edit_add_aporte_title" to mapOf(AppLanguage.PORTUGUESE to "Editar / Adicionar Aporte", AppLanguage.SPANISH to "Editar / Añadir Aporte", AppLanguage.ENGLISH to "Edit / Add Deposit"),
            "add_new_aporte_header" to mapOf(AppLanguage.PORTUGUESE to "Adicionar Novo Aporte (+)", AppLanguage.SPANISH to "Añadir Nuevo Aporte (+)", AppLanguage.ENGLISH to "Add New Deposit (+)"),
            "add_aporte_desc" to mapOf(
                AppLanguage.PORTUGUESE to "Informe o valor do novo depósito. Ele será somado ao valor investido e ao saldo atual.",
                AppLanguage.SPANISH to "Ingresa el monto del nuevo depósito. Se sumará al valor invertido y al saldo actual.",
                AppLanguage.ENGLISH to "Enter the amount of the new deposit. It will be added to invested capital and current balance."
            ),
            "aporte_val_label" to mapOf(AppLanguage.PORTUGUESE to "Valor do Aporte", AppLanguage.SPANISH to "Monto del Aporte", AppLanguage.ENGLISH to "Deposit Amount"),
            "investment_name" to mapOf(AppLanguage.PORTUGUESE to "Nome do Investimento", AppLanguage.SPANISH to "Nombre de la Inversión", AppLanguage.ENGLISH to "Investment Name"),
            "asset_type" to mapOf(AppLanguage.PORTUGUESE to "Tipo de Ativo", AppLanguage.SPANISH to "Tipo de Activo", AppLanguage.ENGLISH to "Asset Type"),
            "institution_broker" to mapOf(AppLanguage.PORTUGUESE to "Instituição / Corretora (Ex: Ueno, Itaú, Cadiem)", AppLanguage.SPANISH to "Institución / Casa de Bolsa (Ej: Ueno, Itaú, Cadiem)", AppLanguage.ENGLISH to "Institution / Brokerage (e.g. Ueno, Itaú, Cadiem)"),
            "first_deposit_date" to mapOf(AppLanguage.PORTUGUESE to "Data do 1º Depósito (dd/mm/aaaa)", AppLanguage.SPANISH to "Fecha del 1.er Depósito (dd/mm/aaaa)", AppLanguage.ENGLISH to "1st Deposit Date (dd/mm/yyyy)"),
            "total_invested_val" to mapOf(AppLanguage.PORTUGUESE to "Valor Investido Total", AppLanguage.SPANISH to "Monto Invertido Total", AppLanguage.ENGLISH to "Total Invested Amount"),
            "current_bank_val" to mapOf(AppLanguage.PORTUGUESE to "Valor Atual (Banco)", AppLanguage.SPANISH to "Valor Actual (Banco)", AppLanguage.ENGLISH to "Current Value (Bank)"),
            "annual_yield_rate" to mapOf(AppLanguage.PORTUGUESE to "Taxa Anual (%)", AppLanguage.SPANISH to "Tasa Anual (%)", AppLanguage.ENGLISH to "Annual Rate (%)"),
            "calc_sim_history" to mapOf(
                AppLanguage.PORTUGUESE to "Calcule e guarde histórico de simulações de juros",
                AppLanguage.SPANISH to "Calcula y guarda un historial de simulaciones de interés",
                AppLanguage.ENGLISH to "Calculate and keep history of interest simulations"
            ),
            "term_label" to mapOf(AppLanguage.PORTUGUESE to "Prazo", AppLanguage.SPANISH to "Plazo", AppLanguage.ENGLISH to "Period"),
            "months_unit" to mapOf(AppLanguage.PORTUGUESE to "Meses", AppLanguage.SPANISH to "Meses", AppLanguage.ENGLISH to "Months"),
            "years_unit" to mapOf(AppLanguage.PORTUGUESE to "Anos", AppLanguage.SPANISH to "Años", AppLanguage.ENGLISH to "Years"),
            "save_calc_history" to mapOf(AppLanguage.PORTUGUESE to "Salvar Cálculo no Histórico", AppLanguage.SPANISH to "Guardar Cálculo en Historial", AppLanguage.ENGLISH to "Save Calculation to History"),
            "calc_history_title" to mapOf(AppLanguage.PORTUGUESE to "Histórico de Cálculos", AppLanguage.SPANISH to "Historial de Cálculos", AppLanguage.ENGLISH to "Calculation History"),

            "tips_title" to mapOf(AppLanguage.PORTUGUESE to "Dica Financeira do Dia", AppLanguage.SPANISH to "Consejo Financiero del Día", AppLanguage.ENGLISH to "Daily Financial Tip"),
            "tips_disclaimer" to mapOf(
                AppLanguage.PORTUGUESE to "⚠️ Aviso Educacional: Estas são dicas educativas para auxílio e crescimento financeiro na realidade do Paraguai. Não constituem recomendação direta de investimento.",
                AppLanguage.SPANISH to "⚠️ Aviso Educativo: Estos son consejos educativos para apoyar tu crecimiento financiero en Paraguay. No constituyen recomendación directa de inversión.",
                AppLanguage.ENGLISH to "⚠️ Educational Disclaimer: These are educational tips for financial growth in Paraguay. They do not constitute direct investment advice."
            ),

            // PIN Lock Overlay
            "protected_access" to mapOf(AppLanguage.PORTUGUESE to "Acesso Protegido", AppLanguage.SPANISH to "Acceso Protegido", AppLanguage.ENGLISH to "Protected Access"),
            "register_pin_title" to mapOf(AppLanguage.PORTUGUESE to "Cadastre seu PIN de Segurança", AppLanguage.SPANISH to "Registre su PIN de Seguridad", AppLanguage.ENGLISH to "Set Up Security PIN"),
            "enter_pin_sub" to mapOf(AppLanguage.PORTUGUESE to "Digite seu PIN de 4 dígitos ou use a biometria", AppLanguage.SPANISH to "Ingrese su PIN de 4 dígitos o use la biometría", AppLanguage.ENGLISH to "Enter 4-digit PIN or use biometrics"),
            "create_pin_sub" to mapOf(AppLanguage.PORTUGUESE to "Crie um PIN numérico exclusivo de 4 dígitos", AppLanguage.SPANISH to "Cree un PIN numérico exclusivo de 4 dígitos", AppLanguage.ENGLISH to "Create a unique 4-digit numeric PIN"),
            "biometric_btn" to mapOf(AppLanguage.PORTUGUESE to "Autenticar com Biometria", AppLanguage.SPANISH to "Autenticar con Biometría", AppLanguage.ENGLISH to "Authenticate with Biometrics"),
            "register_pin_btn" to mapOf(AppLanguage.PORTUGUESE to "Cadastrar PIN e Acessar App", AppLanguage.SPANISH to "Registrar PIN y Acceder", AppLanguage.ENGLISH to "Set PIN & Access App"),
            "auth_biometric" to mapOf(AppLanguage.PORTUGUESE to "Autenticação Biométrica", AppLanguage.SPANISH to "Autenticación Biométrica", AppLanguage.ENGLISH to "Biometric Authentication"),
            "enter_pin" to mapOf(AppLanguage.PORTUGUESE to "Usar PIN de Segurança", AppLanguage.SPANISH to "Usar PIN de Seguridad", AppLanguage.ENGLISH to "Use Security PIN"),
            "pin_label_4digit" to mapOf(AppLanguage.PORTUGUESE to "PIN (4 dígitos)", AppLanguage.SPANISH to "PIN (4 dígitos)", AppLanguage.ENGLISH to "PIN (4 digits)"),
            "new_pin_label" to mapOf(AppLanguage.PORTUGUESE to "Novo PIN (4 dígitos)", AppLanguage.SPANISH to "Nuevo PIN (4 dígitos)", AppLanguage.ENGLISH to "New PIN (4 digits)"),
            "confirm_pin_label" to mapOf(AppLanguage.PORTUGUESE to "Confirmar PIN (4 dígitos)", AppLanguage.SPANISH to "Confirmar PIN (4 dígitos)", AppLanguage.ENGLISH to "Confirm PIN (4 digits)"),
            "incorrect_pin_msg" to mapOf(AppLanguage.PORTUGUESE to "PIN incorreto.", AppLanguage.SPANISH to "PIN incorrecto.", AppLanguage.ENGLISH to "Incorrect PIN."),
            "pin_length_err" to mapOf(AppLanguage.PORTUGUESE to "O PIN deve ter exatamente 4 dígitos.", AppLanguage.SPANISH to "El PIN debe tener exactamente 4 dígitos.", AppLanguage.ENGLISH to "PIN must be exactly 4 digits."),
            "pin_mismatch_err" to mapOf(AppLanguage.PORTUGUESE to "Os PINs não coincidem.", AppLanguage.SPANISH to "Los PINs no coinciden.", AppLanguage.ENGLISH to "PINs do not match."),
            "nav_rates" to mapOf(AppLanguage.PORTUGUESE to "Moedas", AppLanguage.SPANISH to "Monedas", AppLanguage.ENGLISH to "Currencies"),
            "offline_mode" to mapOf(AppLanguage.PORTUGUESE to "Modo Offline Ativo", AppLanguage.SPANISH to "Modo Offline Activo", AppLanguage.ENGLISH to "Offline Mode Active")
        )

        return strings[key]?.get(lang) ?: strings[key]?.get(AppLanguage.PORTUGUESE) ?: key
    }

    fun translateCategory(cat: String, lang: AppLanguage): String {
        return when (cat) {
            "Alimentação" -> getString("cat_food", lang)
            "Transporte" -> getString("cat_transport", lang)
            "Moradia" -> getString("cat_housing", lang)
            "Lazer" -> getString("cat_leisure", lang)
            "Investimentos" -> getString("cat_investments", lang)
            "Saúde" -> getString("cat_health", lang)
            "Educação" -> getString("cat_education", lang)
            "Outros" -> getString("cat_other", lang)
            else -> cat
        }
    }

    fun getDailyFinancialTip(lang: AppLanguage): DailyTip {
        val tipsList = listOf(
            DailyTip(
                author = "Benjamin Graham (O Investidor Inteligente)",
                textMap = mapOf(
                    AppLanguage.PORTUGUESE to "• \"Investir não é vencer os outros em seu próprio jogo, mas sim controlar a si mesmo.\" Monte uma reserva bimoeda em Guaraníes e Dólares no Paraguai para estabilidade.",
                    AppLanguage.SPANISH to "• \"Invertir no es vencer a los demás, sino controlarse a uno mismo.\" Mantén una reserva bimoneda en Guaraníes y Dólares para estabilidad.",
                    AppLanguage.ENGLISH to "• \"Investing isn't about beating others, it's about controlling yourself.\" Keep reserves in Guaraní and USD in Paraguay for stability."
                )
            ),
            DailyTip(
                author = "Warren Buffett (CEO Berkshire Hathaway)",
                textMap = mapOf(
                    AppLanguage.PORTUGUESE to "• \"Não economize o que sobra depois de gastar, mas gaste o que sobra depois de economizar.\" Guarde uma porcentagem assim que receber na sua conta.",
                    AppLanguage.SPANISH to "• \"No ahorres lo que te queda después de gastar, gasta lo que te queda después de ahorrar.\" Separa tu ahorro apenas cobres tu salario.",
                    AppLanguage.ENGLISH to "• \"Do not save what is left after spending, but spend what is left after saving.\" Automatically set aside savings as soon as you are paid."
                )
            ),
            DailyTip(
                author = "Robert Kiyosaki (Pai Rico, Pai Pobre)",
                textMap = mapOf(
                    AppLanguage.PORTUGUESE to "• \"A diferença entre os ricos e a classe média é como eles usam seu tempo e dinheiro.\" Adquira ativos produtivos (Fondos Mutuos, CDA, Bonos) em vez de passivos.",
                    AppLanguage.SPANISH to "• \"La diferencia entre los ricos y la clase media es cómo usan su dinero.\" Adquiere activos productivos en lugar de pasivos.",
                    AppLanguage.ENGLISH to "• \"The difference between rich and middle class is how they spend money.\" Acquire income-producing assets rather than liabilities."
                )
            ),
            DailyTip(
                author = "Gustavo Cerbasi (Educador Financeiro)",
                textMap = mapOf(
                    AppLanguage.PORTUGUESE to "• \"Gastar menos do que ganha e investir a diferença com disciplina é o único segredo.\" Acompanhe seu extrato diariamente para cortar pequenos vazamentos.",
                    AppLanguage.SPANISH to "• \"Gastar menos de lo que ganas e invertir la diferencia con disciplina es el secreto.\" Revisa tu extracto diario para eliminar fugas de dinero.",
                    AppLanguage.ENGLISH to "• \"Spending less than you earn and investing the difference with discipline is key.\" Monitor your daily statement to eliminate money leaks."
                )
            ),
            DailyTip(
                author = "BCP (Finanças Pessoais Paraguai)",
                textMap = mapOf(
                    AppLanguage.PORTUGUESE to "• \"Mantenha sua reserva em instrumentos de liquidez diária e diversifique entre PYG para custos locais e USD para proteção contra inflação global.\"",
                    AppLanguage.SPANISH to "• \"Mantén tu fondo de emergencia en liquidez diaria y diversifica entre PYG para tus gastos diarios y USD como protección patrimonial.\"",
                    AppLanguage.ENGLISH to "• \"Keep emergency funds in liquid accounts and diversify between PYG for local expenses and USD for global inflation hedge.\""
                )
            ),
            DailyTip(
                author = "Morgan Housel (A Psicologia Financeira)",
                textMap = mapOf(
                    AppLanguage.PORTUGUESE to "• \"A riqueza é o que você não vê: são os carros não comprados e luxos evitados.\" O segredo é a consistência a longo prazo.",
                    AppLanguage.SPANISH to "• \"La riqueza es lo que no ves: lujos no comprados y gastos evitado.\" La clave es la consistencia a largo plazo.",
                    AppLanguage.ENGLISH to "• \"Wealth is what you don't see: the unpurchased luxuries.\" True wealth comes from long-term compounding consistency."
                )
            ),
            DailyTip(
                author = "Paulo Vieira (Fator de Enriquecimento)",
                textMap = mapOf(
                    AppLanguage.PORTUGUESE to "• \"Pague-se primeiro. Antes de quitar faturas e compras do mês, separe ao menos 10% do seu ganho para seu futuro financeiro.\"",
                    AppLanguage.SPANISH to "• \"Págate a ti mismo primero. Antes de pagar cuentas del mes, separa al menos el 10% para tu futuro financiero.\"",
                    AppLanguage.ENGLISH to "• \"Pay yourself first. Before paying monthly bills, set aside at least 10% of your earnings for your financial future.\""
                )
            ),
            DailyTip(
                author = "John Bogle (Fundador da Vanguard)",
                textMap = mapOf(
                    AppLanguage.PORTUGUESE to "• \"Não procure a agulha no palheiro. Compre o palheiro inteiro.\" Faça aportes constantes mensalmente sem tentar prever o mercado.",
                    AppLanguage.SPANISH to "• \"No busques la aguja en el pajar. Compra el pajar entero.\" Haz aportes constantes todos los meses sin especular.",
                    AppLanguage.ENGLISH to "• \"Don't look for the needle in the haystack. Buy the whole haystack.\" Make steady monthly contributions consistently."
                )
            )
        )

        val dayEpoch = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt()
        val index = kotlin.math.abs(dayEpoch) % tipsList.size
        val selected = tipsList[index]

        return DailyTip(
            author = selected.author,
            text = selected.textMap[lang] ?: selected.textMap[AppLanguage.PORTUGUESE] ?: ""
        )
    }
}

data class DailyTip(
    val author: String,
    val textMap: Map<AppLanguage, String> = emptyMap(),
    val text: String = ""
)

