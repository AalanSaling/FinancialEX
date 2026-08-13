package com.example.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object DatabasePassphraseManager {
    private const val PREFS_NAME = "app_security_prefs"
    private const val KEY_ENCRYPTED_PASSPHRASE = "encrypted_db_passphrase"
    private const val KEY_PASSPHRASE_IV = "db_passphrase_iv"
    private const val KEYSTORE_ALIAS = "financas_db_passphrase_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun getOrGeneratePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedPassphraseBase64 = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null)
        val ivBase64 = prefs.getString(KEY_PASSPHRASE_IV, null)

        if (!encryptedPassphraseBase64.isNullOrBlank() && !ivBase64.isNullOrBlank()) {
            try {
                val secretKey = getOrCreateSecretKey()
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
                val gcmSpec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

                val encryptedBytes = Base64.decode(encryptedPassphraseBase64, Base64.NO_WRAP)
                return cipher.doFinal(encryptedBytes)
            } catch (e: Exception) {
                android.util.Log.e("DatabasePassphrase", "CRITICAL: KeyStore decryption failed for database passphrase! KeyStore key lost/invalid.", e)
                try {
                    context.deleteDatabase("financas_db")
                    context.deleteDatabase("financas_db_legacy_unencrypted")
                    prefs.edit().remove(KEY_ENCRYPTED_PASSPHRASE).remove(KEY_PASSPHRASE_IV).apply()
                } catch (cleanupEx: Exception) {
                    android.util.Log.e("DatabasePassphrase", "Failed to cleanup corrupted database files after KeyStore error", cleanupEx)
                }
                throw IllegalStateException("CRITICAL_KEYSTORE_DECRYPTION_FAILED: O banco de dados cifrado anterior não pôde ser aberto porque as chaves do Android KeyStore foram perdidas ou invalidadas. O banco inacessível foi removido com segurança para prevenir corrupção e perda de dados.", e)
            }
        }

        // Generate new 32-byte passphrase
        val rawPassphrase = ByteArray(32)
        SecureRandom().nextBytes(rawPassphrase)

        try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val encryptedBytes = cipher.doFinal(rawPassphrase)
            val iv = cipher.iv

            prefs.edit()
                .putString(KEY_ENCRYPTED_PASSPHRASE, Base64.encodeToString(encryptedBytes, Base64.NO_WRAP))
                .putString(KEY_PASSPHRASE_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("DatabasePassphrase", "Error encrypting passphrase with KeyStore", e)
            throw IllegalStateException("CRITICAL_KEYSTORE_ENCRYPTION_FAILED: Unable to encrypt new database passphrase with KeyStore.", e)
        }

        return rawPassphrase
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val entry = keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
