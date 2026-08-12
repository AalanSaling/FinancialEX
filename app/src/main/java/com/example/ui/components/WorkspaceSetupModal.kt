package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppLanguage
import com.example.util.PinSecurityManager
import com.example.util.PinVerifyResult
import com.example.util.Translations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSetupModal(
    currentCurrency: String,
    currentLanguage: AppLanguage,
    isBiometricEnabled: Boolean,
    userPinCode: String,
    onDismiss: () -> Unit,
    onStartFresh: (primaryCurrency: String) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onSetPin: (String) -> Unit
) {
    var selectedCurrency by remember { mutableStateOf(currentCurrency.ifBlank { "PYG" }) }
    var currencyExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var editingPin by remember { mutableStateOf(false) }
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var pinErrorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = Translations.getString("app_settings", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = Translations.getString("workspace_setup", currentLanguage),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Language Switcher
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = Translations.getString("language", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AppLanguage.entries.forEachIndexed { index, item ->
                            SegmentedButton(
                                selected = currentLanguage == item,
                                onClick = { onSelectLanguage(item) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = AppLanguage.entries.size)
                            ) {
                                Text(
                                    text = "${item.flag} ${item.code}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Section 2: Currency Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = Translations.getString("primary_currency", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )

                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = !currencyExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = when(selectedCurrency) {
                                "PYG" -> "₲ Guaraní (PYG)"
                                "USD" -> "$ Dólar (USD)"
                                "BRL" -> "R$ Real (BRL)"
                                "EUR" -> "€ Euro (EUR)"
                                else -> selectedCurrency
                            },
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("currency_select_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            listOf(
                                Triple("PYG", "₲ Guaraní (PYG)", "Paraguay"),
                                Triple("USD", "$ Dólar (USD)", "Global"),
                                Triple("BRL", "R$ Real (BRL)", "Brasil"),
                                Triple("EUR", "€ Euro (EUR)", "Europa")
                            ).forEach { (code, name, country) ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                            Text(country, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }
                                    },
                                    onClick = {
                                        selectedCurrency = code
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Section 3: Security & Custom PIN Setup
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = Translations.getString("security_pin_title", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (userPinCode.isNotBlank()) Translations.getString("pin_registered", currentLanguage) else Translations.getString("no_pin_registered", currentLanguage),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = onToggleBiometric
                        )
                    }

                    if (isBiometricEnabled) {
                        val hasPin = PinSecurityManager.hasPinSet(context)
                        if (!editingPin) {
                            OutlinedButton(
                                onClick = { editingPin = true; currentPinInput = ""; newPinInput = ""; pinErrorMsg = null },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Pin, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (!hasPin) Translations.getString("register_pin", currentLanguage) else Translations.getString("change_pin", currentLanguage),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (hasPin) {
                                    OutlinedTextField(
                                        value = currentPinInput,
                                        onValueChange = {
                                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                                currentPinInput = it
                                                pinErrorMsg = null
                                            }
                                        },
                                        label = { Text("PIN Atual (4 dígitos)", fontSize = 11.sp) },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                OutlinedTextField(
                                    value = newPinInput,
                                    onValueChange = {
                                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                            newPinInput = it
                                            pinErrorMsg = null
                                        }
                                    },
                                    label = { Text(if (hasPin) "Novo PIN (4 dígitos)" else Translations.getString("register_pin", currentLanguage), fontSize = 11.sp) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (pinErrorMsg != null) {
                                    Text(
                                        text = pinErrorMsg!!,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 10.sp
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { editingPin = false; currentPinInput = ""; newPinInput = "" },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(Translations.getString("cancel", currentLanguage), fontSize = 11.sp, maxLines = 1)
                                    }

                                    Button(
                                        onClick = {
                                            if (hasPin) {
                                                val verifyResult = PinSecurityManager.verifyPin(context, currentPinInput)
                                                if (verifyResult !is PinVerifyResult.Success) {
                                                    pinErrorMsg = "PIN atual incorreto"
                                                    return@Button
                                                }
                                            }
                                            if (newPinInput.length == 4) {
                                                PinSecurityManager.savePin(context, newPinInput)
                                                onSetPin(newPinInput)
                                                editingPin = false
                                                currentPinInput = ""
                                                newPinInput = ""
                                            } else {
                                                pinErrorMsg = "Novo PIN deve ter 4 dígitos"
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(Translations.getString("save_pin", currentLanguage), fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Section 4: Start Fresh Option (Zero Balance Clean Slate)
                Card(
                    onClick = {
                        onStartFresh(selectedCurrency)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.getString("reset_to_zero", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = Translations.getString("reset_desc", currentLanguage),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = Translations.getString("close", currentLanguage),
                    maxLines = 1
                )
            }
        }
    )
}
