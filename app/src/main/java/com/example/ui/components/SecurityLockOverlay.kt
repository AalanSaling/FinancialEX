package com.example.ui.components

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.util.AppLanguage
import com.example.util.PinSecurityManager
import com.example.util.PinVerifyResult
import com.example.util.Translations

@Composable
fun SecurityLockOverlay(
    userPinCode: String,
    appLanguage: AppLanguage = AppLanguage.PORTUGUESE,
    isBiometricEnabled: Boolean = true,
    onUnlock: () -> Unit,
    onSetPin: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val isPinAlreadySet = PinSecurityManager.hasPinSet(context)

    var enteredPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun triggerBiometric() {
        try {
            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                val biometricManager = BiometricManager.from(activity)
                val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
                when (biometricManager.canAuthenticate(authenticators)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        val executor = ContextCompat.getMainExecutor(activity)
                        val biometricPrompt = BiometricPrompt(
                            activity,
                            executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    onUnlock()
                                }

                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    super.onAuthenticationError(errorCode, errString)
                                    errorMsg = "Biometric Error: $errString"
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                    errorMsg = "Fingerprint not recognized."
                                }
                            }
                        )

                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle(Translations.getString("auth_biometric", appLanguage))
                            .setSubtitle(Translations.getString("enter_pin_sub", appLanguage))
                            .setNegativeButtonText(Translations.getString("enter_pin", appLanguage))
                            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                            .build()

                        biometricPrompt.authenticate(promptInfo)
                    }
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        errorMsg = "No biometrics registered on phone. Use PIN."
                    }
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                        errorMsg = "Device lacks biometric hardware. Use PIN."
                    }
                    else -> {
                        errorMsg = "Biometrics unavailable. Please use PIN."
                    }
                }
            } else {
                errorMsg = "Please authenticate using PIN."
            }
        } catch (e: Exception) {
            errorMsg = "Biometrics unavailable. Please use PIN."
        }
    }

    // Automatically trigger biometric prompt on lock overlay open
    LaunchedEffect(Unit) {
        if (isPinAlreadySet && isBiometricEnabled) {
            kotlinx.coroutines.delay(300)
            try {
                triggerBiometric()
            } catch (_: Exception) {
                // Silently fallback to PIN prompt without crashing
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isPinAlreadySet) Translations.getString("protected_access", appLanguage) else Translations.getString("register_pin_title", appLanguage),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (isPinAlreadySet) Translations.getString("enter_pin_sub", appLanguage) else Translations.getString("create_pin_sub", appLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isPinAlreadySet) {
                    // PIN Entry Mode
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                enteredPin = input
                                errorMsg = null
                                if (input.length == 4) {
                                    val result = PinSecurityManager.verifyPin(context, input)
                                    when (result) {
                                        is PinVerifyResult.Success -> {
                                            onUnlock()
                                        }
                                        is PinVerifyResult.IncorrectPin -> {
                                            enteredPin = ""
                                            errorMsg = if (result.lockoutSeconds > 0) {
                                                "PIN incorreto. Bloqueado por ${result.lockoutSeconds}s."
                                            } else {
                                                "${Translations.getString("incorrect_pin_msg", appLanguage)} (${result.failedAttempts}/5)"
                                            }
                                        }
                                        is PinVerifyResult.LockedOut -> {
                                            enteredPin = ""
                                            errorMsg = "Acesso temporariamente bloqueado. Aguarde ${result.remainingSeconds}s."
                                        }
                                        is PinVerifyResult.NoPinSet -> {
                                            onUnlock()
                                        }
                                    }
                                }
                            }
                        },
                        label = { Text(Translations.getString("pin_label_4digit", appLanguage)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        isError = errorMsg != null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("security_pin_input")
                    )

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Biometric Unlock Button
                    Button(
                        onClick = { triggerBiometric() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("biometric_unlock_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Translations.getString("biometric_btn", appLanguage), maxLines = 1)
                    }
                } else {
                    // PIN Creation Mode
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                enteredPin = input
                            }
                        },
                        label = { Text(Translations.getString("new_pin_label", appLanguage)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                confirmPin = input
                            }
                        },
                        label = { Text(Translations.getString("confirm_pin_label", appLanguage)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (enteredPin.length != 4) {
                                errorMsg = Translations.getString("pin_length_err", appLanguage)
                            } else if (enteredPin != confirmPin) {
                                errorMsg = Translations.getString("pin_mismatch_err", appLanguage)
                            } else {
                                PinSecurityManager.savePin(context, enteredPin)
                                onSetPin(enteredPin)
                                onUnlock()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Pin, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Translations.getString("register_pin_btn", appLanguage), maxLines = 1)
                    }
                }
            }
        }
    }
}
