package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import com.example.util.AppLanguage
import com.example.util.CurrencyConverter
import com.example.util.Translations
import kotlin.math.pow

enum class RateType {
    ANNUAL, MONTHLY
}

enum class PeriodUnit {
    MONTHS, YEARS
}

data class CalculationHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val initial: Double,
    val monthly: Double,
    val rate: Double,
    val period: Int,
    val periodUnit: PeriodUnit,
    val totalInvested: Double,
    val totalInterest: Double,
    val finalBalance: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompoundInterestScreen(
    baseCurrency: String,
    appLanguage: AppLanguage = AppLanguage.PORTUGUESE
) {
    // Starting with clean 0 / empty defaults on first launch
    var initialAmountText by remember { mutableStateOf("0") }
    var monthlyDepositText by remember { mutableStateOf("0") }
    var rateText by remember { mutableStateOf("0") }
    var periodText by remember { mutableStateOf("0") }

    var rateType by remember { mutableStateOf(RateType.ANNUAL) }
    var periodUnit by remember { mutableStateOf(PeriodUnit.MONTHS) }

    val calculationHistory = remember { mutableStateListOf<CalculationHistoryItem>() }

    // Parse numeric inputs safely
    val P = initialAmountText.toDoubleOrNull() ?: 0.0
    val PMT = monthlyDepositText.toDoubleOrNull() ?: 0.0
    val rawRate = rateText.toDoubleOrNull() ?: 0.0
    val rawPeriod = periodText.toIntOrNull() ?: 0

    val totalMonths = if (periodUnit == PeriodUnit.YEARS) rawPeriod * 12 else rawPeriod

    // Calculate monthly interest rate (r) using standard exact compounding formula
    val monthlyRate = if (rateType == RateType.ANNUAL) {
        if (rawRate > 0) (1.0 + rawRate / 100.0).pow(1.0 / 12.0) - 1.0 else 0.0
    } else {
        rawRate / 100.0
    }

    // Daily rate for estimated daily earnings
    val dailyRate = if (monthlyRate > 0) (1.0 + monthlyRate).pow(1.0 / 30.0) - 1.0 else 0.0

    // Future Value calculations
    val fvInitial = P * (1.0 + monthlyRate).pow(totalMonths.toDouble())
    val fvMonthly = if (monthlyRate > 0) {
        PMT * (((1.0 + monthlyRate).pow(totalMonths.toDouble()) - 1.0) / monthlyRate)
    } else {
        PMT * totalMonths
    }

    val totalBalance = if (totalMonths > 0) fvInitial + fvMonthly else P
    val totalInvested = P + (PMT * totalMonths)
    val totalInterest = (totalBalance - totalInvested).coerceAtLeast(0.0)

    // Projected earnings breakdown per period
    val averageDailyInterest = if (totalMonths > 0) totalInterest / (totalMonths * 30.0) else P * dailyRate
    val averageMonthlyInterest = if (totalMonths > 0) totalInterest / totalMonths.toDouble() else P * monthlyRate
    val averageAnnualInterest = if (totalMonths > 0) totalInterest / (totalMonths / 12.0).coerceAtLeast(1.0) else totalInterest

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = Translations.getString("interest_simulator", appLanguage),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Translations.getString("calc_sim_history", appLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        // Inputs Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = Translations.getString("interest_simulator", appLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                    }

                    // Initial Amount
                    OutlinedTextField(
                        value = initialAmountText,
                        onValueChange = { initialAmountText = it.replace(',', '.') },
                        label = { Text("${Translations.getString("initial_deposit", appLanguage)} ($baseCurrency)", maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("initial_amount_input")
                    )

                    // Monthly Deposit
                    OutlinedTextField(
                        value = monthlyDepositText,
                        onValueChange = { monthlyDepositText = it.replace(',', '.') },
                        label = { Text("${Translations.getString("monthly_deposit", appLanguage)} ($baseCurrency)", maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("monthly_deposit_input")
                    )

                    // Interest Rate & Rate Unit Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = rateText,
                            onValueChange = { rateText = it.replace(',', '.') },
                            label = { Text(Translations.getString("annual_rate", appLanguage), maxLines = 1) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("interest_rate_input")
                        )

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.width(140.dp)) {
                            SegmentedButton(
                                selected = rateType == RateType.ANNUAL,
                                onClick = { rateType = RateType.ANNUAL },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("a.a.", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            SegmentedButton(
                                selected = rateType == RateType.MONTHLY,
                                onClick = { rateType = RateType.MONTHLY },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("a.m.", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }

                    // Period Length & Unit Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = periodText,
                            onValueChange = { periodText = it.filter { c -> c.isDigit() } },
                            label = { Text(Translations.getString("term_label", appLanguage), maxLines = 1) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("period_input")
                        )

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.width(140.dp)) {
                            SegmentedButton(
                                selected = periodUnit == PeriodUnit.MONTHS,
                                onClick = { periodUnit = PeriodUnit.MONTHS },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text(Translations.getString("months_unit", appLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            SegmentedButton(
                                selected = periodUnit == PeriodUnit.YEARS,
                                onClick = { periodUnit = PeriodUnit.YEARS },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text(Translations.getString("years_unit", appLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }

                    // Save Calculation to History Button
                    Button(
                        onClick = {
                            if (totalBalance > 0) {
                                calculationHistory.add(
                                    0,
                                    CalculationHistoryItem(
                                        initial = P,
                                        monthly = PMT,
                                        rate = rawRate,
                                        period = rawPeriod,
                                        periodUnit = periodUnit,
                                        totalInvested = totalInvested,
                                        totalInterest = totalInterest,
                                        finalBalance = totalBalance
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_calc_btn")
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Translations.getString("save_calc_history", appLanguage), fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        // Summary Hero Card
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
                        text = Translations.getString("final_amount", appLanguage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = CurrencyConverter.format(totalBalance, baseCurrency),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = Translations.getString("total_invested", appLanguage),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                            Text(
                                text = CurrencyConverter.format(totalInvested, baseCurrency),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Translations.getString("total_interest", appLanguage),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                            Text(
                                text = CurrencyConverter.format(totalInterest, baseCurrency),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Saved History Section (Like a smartphone calculator memory history)
        if (calculationHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Translations.getString("calc_history_title", appLanguage)} (${calculationHistory.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1
                    )
                    TextButton(onClick = { calculationHistory.clear() }) {
                        Text(if (appLanguage == AppLanguage.SPANISH) "Limpiar" else if (appLanguage == AppLanguage.ENGLISH) "Clear" else "Limpar", fontSize = 12.sp)
                    }
                }
            }

            items(calculationHistory) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${Translations.getString("aporte_val_label", appLanguage)}: ${CurrencyConverter.format(item.initial, baseCurrency)} + ${CurrencyConverter.format(item.monthly, baseCurrency)}/${if (appLanguage == AppLanguage.ENGLISH) "mo" else "mês"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "${item.rate}% a.a. • ${item.period} ${if (item.periodUnit == PeriodUnit.YEARS) Translations.getString("years_unit", appLanguage).lowercase() else Translations.getString("months_unit", appLanguage).lowercase()}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${Translations.getString("final_amount", appLanguage)}: ${CurrencyConverter.format(item.finalBalance, baseCurrency)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${Translations.getString("total_interest", appLanguage)}: +${CurrencyConverter.format(item.totalInterest, baseCurrency)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }
            }
        }
    }
}
