package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinSecurityManager {
    private const val PREFS_NAME = "app_security_prefs"
    private const val KEY_PIN_SALT = "pin_salt"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_LEGACY_PIN = "user_pin_code"
    private const val KEY_FAILED_ATTEMPTS = "pin_failed_attempts"
    private const val KEY_LOCKOUT_UNTIL = "pin_lockout_until"

    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val SALT_SIZE = 16

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun hasPinSet(context: Context): Boolean {
        val prefs = getPrefs(context)
        val hash = prefs.getString(KEY_PIN_HASH, "") ?: ""
        val legacy = prefs.getString(KEY_LEGACY_PIN, "") ?: ""
        return hash.isNotBlank() || legacy.isNotBlank()
    }

    fun savePin(context: Context, pin: String) {
        val prefs = getPrefs(context)
        val salt = generateSalt()
        val hash = hashPin(pin, salt)

        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP)

        prefs.edit()
            .putString(KEY_PIN_SALT, saltBase64)
            .putString(KEY_PIN_HASH, hashBase64)
            .remove(KEY_LEGACY_PIN)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
    }

    fun verifyPin(context: Context, enteredPin: String): PinVerifyResult {
        val prefs = getPrefs(context)
        val currentTime = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)

        if (currentTime < lockoutUntil) {
            val remainingSec = ((lockoutUntil - currentTime) / 1000).coerceAtLeast(1)
            return PinVerifyResult.LockedOut(remainingSec)
        }

        var saltBase64 = prefs.getString(KEY_PIN_SALT, "") ?: ""
        var hashBase64 = prefs.getString(KEY_PIN_HASH, "") ?: ""
        val legacyPin = prefs.getString(KEY_LEGACY_PIN, "") ?: ""

        // Migrate legacy plain-text PIN if present
        if (hashBase64.isBlank() && legacyPin.isNotBlank()) {
            savePin(context, legacyPin)
            saltBase64 = prefs.getString(KEY_PIN_SALT, "") ?: ""
            hashBase64 = prefs.getString(KEY_PIN_HASH, "") ?: ""
        }

        if (saltBase64.isBlank() || hashBase64.isBlank()) {
            return PinVerifyResult.NoPinSet
        }

        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val expectedHash = Base64.decode(hashBase64, Base64.NO_WRAP)
        val computedHash = hashPin(enteredPin, salt)

        val isCorrect = MessageDigest.isEqual(expectedHash, computedHash)

        if (isCorrect) {
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .apply()
            return PinVerifyResult.Success
        } else {
            val failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val nextLockoutMs = when {
                failedAttempts >= 20 -> 30 * 60 * 1000L // 30 minutes lockout
                failedAttempts >= 15 -> 30 * 60 * 1000L // 30 minutes
                failedAttempts >= 10 -> 5 * 60 * 1000L  // 5 minutes
                failedAttempts >= 5 -> 30 * 1000L       // 30 seconds
                else -> 0L
            }

            val newLockoutUntil = if (nextLockoutMs > 0) currentTime + nextLockoutMs else 0L

            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, failedAttempts)
                .putLong(KEY_LOCKOUT_UNTIL, newLockoutUntil)
                .apply()

            val remainingSec = if (nextLockoutMs > 0) nextLockoutMs / 1000 else 0
            return PinVerifyResult.IncorrectPin(failedAttempts, remainingSec)
        }
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_SIZE)
        random.nextBytes(salt)
        return salt
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return skf.generateSecret(spec).encoded
    }
}

sealed class PinVerifyResult {
    object Success : PinVerifyResult()
    object NoPinSet : PinVerifyResult()
    data class IncorrectPin(val failedAttempts: Int, val lockoutSeconds: Long) : PinVerifyResult()
    data class LockedOut(val remainingSeconds: Long) : PinVerifyResult()
}
