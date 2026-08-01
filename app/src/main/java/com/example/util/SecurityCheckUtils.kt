package com.example.util

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.view.WindowManager
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class SecurityStatus(
    val isRooted: Boolean,
    val isDebuggable: Boolean,
    val isDebuggerConnected: Boolean,
    val isFridaDetected: Boolean,
    val isXposedDetected: Boolean,
    val isTraced: Boolean,
    val isSignatureValid: Boolean,
    val isSecure: Boolean
)

object SecurityCheckUtils {

    const val RELEASE_FINGERPRINT = "a21939c3c828682ec170e21d04533c448fd9056c38360def90d11e8905db7ef5"
    const val DEBUG_FINGERPRINT = "6a933c831c6de0c400352985944af9d1a40c455686e5bc2c9c9042b69f3b22f2"

    fun checkSecurityStatus(context: Context, expectedCertSha256: String? = null): SecurityStatus {
        val rooted = isDeviceRooted()
        val debuggable = isAppDebuggable(context)
        val debuggerConnected = Debug.isDebuggerConnected() || Debug.waitingForDebugger()
        val frida = isFridaRunning()
        val xposed = isXposedInstalled()
        val traced = isTracedByDebugger()
        
        val allowedCertificates = if (!expectedCertSha256.isNull_or_blank()) {
            listOfNotNull(expectedCertSha256)
        } else {
            listOf(RELEASE_FINGERPRINT, DEBUG_FINGERPRINT)
        }
        val signatureValid = verifySignatureList(context, allowedCertificates)

        return SecurityStatus(
            isRooted = rooted,
            isDebuggable = debuggable,
            isDebuggerConnected = debuggerConnected,
            isFridaDetected = frida,
            isXposedDetected = xposed,
            isTraced = traced,
            isSignatureValid = signatureValid,
            isSecure = !rooted && !debuggerConnected && !frida && !xposed && !traced && signatureValid
        )
    }

    fun isDeviceRooted(): Boolean {
        // Check Build Tags
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        // Check common SU paths
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/system/xbin/daemonsu"
        )
        for (path in paths) {
            try {
                if (File(path).exists()) return true
            } catch (_: Throwable) {
                // Ignore permission or file system exceptions
            }
        }

        // Check Magisk / mount entries
        try {
            val mountsFile = File("/proc/self/mounts")
            if (mountsFile.exists()) {
                BufferedReader(FileReader(mountsFile)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line?.contains("magisk", ignoreCase = true) == true ||
                            line?.contains("core/mirror", ignoreCase = true) == true) {
                            return true
                        }
                    }
                }
            }
        } catch (_: Throwable) {
            // Memory reading restricted or unavailable
        }

        return false
    }

    fun isFridaRunning(): Boolean {
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                BufferedReader(FileReader(mapsFile)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line?.contains("frida", ignoreCase = true) == true || 
                            line?.contains("gadget", ignoreCase = true) == true ||
                            line?.contains("lsposed", ignoreCase = true) == true ||
                            line?.contains("substrate", ignoreCase = true) == true) {
                            return true
                        }
                    }
                }
            }
        } catch (_: Throwable) {
            // Memory reading restricted or unavailable
        }
        return false
    }

    fun isXposedInstalled(): Boolean {
        try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            return true
        } catch (_: Throwable) {
            // Xposed class not loaded
        }
        return false
    }

    fun isTracedByDebugger(): Boolean {
        try {
            val statusFile = File("/proc/self/status")
            if (statusFile.exists()) {
                BufferedReader(FileReader(statusFile)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line?.startsWith("TracerPid:") == true) {
                            val pidStr = line?.substringAfter("TracerPid:")?.trim()
                            val pid = pidStr?.toIntOrNull() ?: 0
                            if (pid > 0) return true
                        }
                    }
                }
            }
        } catch (_: Throwable) {
            // Restricted environment
        }
        return false
    }

    fun verifySignatureList(context: Context, expectedSha256List: List<String>): Boolean {
        if (expectedSha256List.isEmpty()) return true
        return try {
            val pm = context.packageManager
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            val pkgInfo = pm.getPackageInfo(context.packageName, flags)
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.signatures
            } ?: return false

            val md = MessageDigest.getInstance("SHA-256")
            for (sig in signatures) {
                val digest = md.digest(sig.toByteArray())
                val hexStr = digest.joinToString("") { "%02x".format(it) }
                if (expectedSha256List.any { it.equals(hexStr, ignoreCase = true) }) {
                    return true
                }
            }
            false
        } catch (_: Throwable) {
            false
        }
    }

    fun verifySignature(context: Context, expectedCertSha256: String?): Boolean {
        if (expectedCertSha256.isNull_or_blank()) return true
        return verifySignatureList(context, listOfNotNull(expectedCertSha256))
    }

    fun isAppDebuggable(context: Context): Boolean {
        return try {
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } catch (_: Throwable) {
            false
        }
    }

    fun applyScreenProtection(activity: Activity) {
        try {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } catch (_: Throwable) {
            // Ignore if activity is destroyed or flags cannot be set
        }
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}

/**
 * KeyStore-backed AES Encrypted Storage Helper for storing sensitive strings securely.
 */
object KeyStoreStorageHelper {
    private const val KEY_ALIAS = "AppSecretKeyAlias"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, combined, 0, 12)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        val decrypted = cipher.doFinal(combined, 12, combined.size - 12)
        return String(decrypted, Charsets.UTF_8)
    }
}


