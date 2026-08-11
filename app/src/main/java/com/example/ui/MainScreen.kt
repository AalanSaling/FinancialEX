package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.SecurityLockOverlay
import com.example.ui.components.WorkspaceSetupModal
import com.example.ui.screens.*
import com.example.util.Translations

sealed class Screen(val route: String, val titleKey: String, val activeIcon: androidx.compose.ui.graphics.vector.ImageVector, val inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "nav_dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Transactions : Screen("transactions", "nav_transactions", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
    object Investments : Screen("investments", "nav_investments", Icons.Filled.ShowChart, Icons.Outlined.ShowChart)
    object Goals : Screen("goals", "nav_goals", Icons.Filled.Flag, Icons.Outlined.Flag)
    object Assets : Screen("assets", "nav_assets", Icons.Filled.CurrencyExchange, Icons.Outlined.CurrencyExchange)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showWorkspaceSetupModal by remember { mutableStateOf(false) }

    val screens = listOf(
        Screen.Dashboard,
        Screen.Transactions,
        Screen.Investments,
        Screen.Goals,
        Screen.Assets
    )

    // Request Android 13+ Notification Permission on app launch
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ -> }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Show toast notifications when message is produced
    LaunchedEffect(state.messageToast) {
        state.messageToast?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (state.isAppLocked) {
        SecurityLockOverlay(
            userPinCode = state.userPinCode,
            appLanguage = state.appLanguage,
            isBiometricEnabled = state.isBiometricEnabled,
            onUnlock = { viewModel.unlockApp() },
            onSetPin = { viewModel.setUserPinCode(it) }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    tonalElevation = 8.dp
                ) {
                    screens.forEach { screen ->
                        val isSelected = selectedScreen.route == screen.route
                        val label = Translations.getString(screen.titleKey, state.appLanguage)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.activeIcon else screen.inactiveIcon,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedScreen) {
                    Screen.Dashboard -> DashboardScreen(
                        state = state,
                        onExportPdfClick = { viewModel.exportMonthlyReportPdf() },
                        onAddTxClick = { showAddTxDialog = true },
                        onToggleHideBalance = { viewModel.toggleShowHideBalance() },
                        onCurrencyChange = { viewModel.setBaseCurrency(it) },
                        onWorkspaceSetupClick = { showWorkspaceSetupModal = true },
                        onSelectWorkspace = { viewModel.setActiveWorkspace(it) }
                    )

                    Screen.Transactions -> TransactionsScreen(
                        state = state,
                        onAddTxClick = { showAddTxDialog = true },
                        onDeleteTx = { viewModel.deleteTransaction(it) },
                        onSaveAccount = { acc ->
                            if (acc.id == 0L) viewModel.addAccount(acc) else viewModel.updateAccount(acc)
                        },
                        onDeleteAccount = { viewModel.deleteAccount(it) },
                        onExportPdfClick = { viewModel.exportMonthlyReportPdf() }
                    )

                    Screen.Investments -> InvestmentsScreen(
                        state = state,
                        onAddOrUpdateInvestment = { id, title, type, inst, amount, valCurr, yield, curr, notes, stmt, additionalAporte, originAcc, isHist, logsJson, depositDateMillis ->
                            viewModel.addOrUpdateInvestment(id, title, type, inst, amount, valCurr, yield, curr, notes, stmt, additionalAporte, originAcc, isHist, logsJson, depositDateMillis)
                        },
                        onWithdrawInvestment = { inv, amount, destAcc, notes, dateMillis ->
                            viewModel.withdrawInvestment(inv, amount, destAcc, notes, dateMillis)
                        },
                        onAddMovementLog = { inv, type, amount, accName, notes, dateMillis ->
                            viewModel.addInvestmentMovementLog(inv, type, amount, accName, notes, dateMillis)
                        },
                        onDeleteMovementLog = { inv, log ->
                            viewModel.deleteInvestmentMovementLog(inv, log)
                        },
                        onDeleteInvestment = { viewModel.deleteInvestment(it) }
                    )

                    Screen.Goals -> GoalsScreen(
                        state = state,
                        onAddGoal = { goal ->
                            viewModel.addGoal(goal)
                        },
                        onUpdateGoal = { viewModel.updateGoal(it) },
                        onDeleteGoal = { viewModel.deleteGoal(it) },
                        onDeleteGoalPaymentLog = { goal, log ->
                            viewModel.deleteGoalPaymentLog(goal, log)
                        },
                        onAddTransaction = { title, amount, type, category, accountName, currency, notes, dateMillis ->
                            viewModel.addTransaction(title, amount, type, category, accountName, currency, notes, dateMillis)
                        }
                    )

                    Screen.Assets -> AssetsScreen(
                        state = state,
                        onRefreshRates = { viewModel.refreshExchangeRates() }
                    )
                }
            }
        }
    }

    if (showAddTxDialog) {
        AddTransactionDialog(
            isOfflineMode = state.isOfflineMode,
            accounts = state.accounts,
            appLanguage = state.appLanguage,
            onDismiss = { showAddTxDialog = false },
            onConfirm = { title, amount, type, category, accountName, currency, notes, dateMillis ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    accountName = accountName,
                    currency = currency,
                    notes = notes,
                    dateMillis = dateMillis
                )
            }
        )
    }

    if (showWorkspaceSetupModal) {
        WorkspaceSetupModal(
            currentCurrency = state.baseCurrency,
            currentLanguage = state.appLanguage,
            isBiometricEnabled = state.isBiometricEnabled,
            userPinCode = state.userPinCode,
            onDismiss = { showWorkspaceSetupModal = false },
            onStartFresh = { currency ->
                viewModel.startFreshZeroBalance(currency)
            },
            onSelectLanguage = { viewModel.setAppLanguage(it) },
            onToggleBiometric = { viewModel.toggleBiometricEnabled(it) },
            onSetPin = { viewModel.setUserPinCode(it) }
        )
    }
}
