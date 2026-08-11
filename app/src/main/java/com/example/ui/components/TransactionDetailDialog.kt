package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.util.AppLanguage
import com.example.util.CurrencyConverter
import com.example.util.Translations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailDialog(
    transaction: TransactionEntity,
    baseCurrency: String,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateFormatted = dateFormat.format(Date(transaction.dateMillis))

    val (typeLabel, icon, iconBg, amountColor) = when (transaction.type) {
        TransactionType.INCOME -> Quadruple(
            Translations.getString("income_label", lang),
            Icons.Default.ArrowUpward,
            Color(0xFFD1FAE5),
            Color(0xFF059669)
        )
        TransactionType.EXPENSE -> Quadruple(
            Translations.getString("expense_label", lang),
            Icons.Default.ArrowDownward,
            Color(0xFFFEE2E2),
            Color(0xFFDC2626)
        )
        TransactionType.RECEIVABLE -> Quadruple(
            Translations.getString("receivable_label", lang),
            Icons.Default.CallMade,
            Color(0xFFDBEAFE),
            Color(0xFF2563EB)
        )
        TransactionType.FUTURE_EXPENSE -> Quadruple(
            Translations.getString("future_expense_label", lang),
            Icons.Default.Schedule,
            Color(0xFFFEF3C7),
            Color(0xFFD97706)
        )
        TransactionType.INVESTMENT -> Quadruple(
            Translations.getString("investment_label", lang),
            Icons.Default.ShowChart,
            Color(0xFFEDE9FE),
            Color(0xFF7C3AED)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = amountColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = typeLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = amountColor
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card showing Amount
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = iconBg.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Valor do Lançamento",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val formattedOrig = CurrencyConverter.format(transaction.amount, transaction.currency)
                        Text(
                            text = formattedOrig,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = amountColor,
                            textAlign = TextAlign.Center
                        )

                        // If original currency is different from workspace base currency
                        if (!transaction.currency.equals(baseCurrency, ignoreCase = true)) {
                            val convertedAmount = CurrencyConverter.convert(
                                transaction.amount,
                                transaction.currency,
                                baseCurrency
                            )
                            val formattedConverted = CurrencyConverter.format(convertedAmount, baseCurrency)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Equivalente em $baseCurrency: $formattedConverted",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Detail Items List
                DetailRowItem(
                    icon = Icons.Default.Category,
                    label = "Categoria",
                    value = Translations.translateCategory(transaction.category, lang)
                )

                DetailRowItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Conta / Origem",
                    value = transaction.accountName.ifBlank { "Geral" }
                )

                DetailRowItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Data do Lançamento",
                    value = dateFormatted
                )

                DetailRowItem(
                    icon = Icons.Default.Notes,
                    label = "Observações",
                    value = transaction.notes.ifBlank { "Nenhuma observação registrada." }
                )

                if (transaction.isOfflinePending) {
                    DetailRowItem(
                        icon = Icons.Default.CloudOff,
                        label = "Status de Conexão",
                        value = "Pendente de Sincronização (Modo Off-line)",
                        valueColor = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("close_tx_detail_btn")
            ) {
                Text("Fechar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (onDelete != null) {
                TextButton(
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("delete_tx_detail_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Excluir",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}

@Composable
private fun DetailRowItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = valueColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
