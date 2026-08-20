package com.example.util

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuthManager {

    private const val TAG = "BiometricAuthManager"

    enum class BiometricStatus {
        AVAILABLE,
        NOT_ENROLLED,
        NOT_SUPPORTED,
        UNKNOWN_ERROR
    }

    fun checkBiometricAvailability(context: Context): BiometricStatus {
        return try {
            val biometricManager = BiometricManager.from(context)
            val authenticators = BIOMETRIC_STRONG or BIOMETRIC_WEAK
            when (biometricManager.canAuthenticate(authenticators)) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.NOT_SUPPORTED
                else -> BiometricStatus.UNKNOWN_ERROR
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Biometric check failed safely: ${e.message}")
            BiometricStatus.UNKNOWN_ERROR
        }
    }

    fun isBiometricAvailable(context: Context): Boolean {
        return try {
            checkBiometricAvailability(context) == BiometricStatus.AVAILABLE
        } catch (e: Throwable) {
            false
        }
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock Williams Vault",
        subtitle: String = "Scan your fingerprint or face to protect your financial data",
        negativeButtonText: String = "Use PIN",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        try {
            if (activity.isFinishing || activity.isDestroyed) {
                return
            }

            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    try {
                        onSuccess()
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error in onSuccess biometric callback: ${e.message}")
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    try {
                        onError(errorCode, errString)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error in onError biometric callback: ${e.message}")
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    try {
                        onFailed()
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error in onFailed biometric callback: ${e.message}")
                    }
                }
            }

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setConfirmationRequired(false)
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to launch BiometricPrompt: ${e.message}")
            try {
                onError(-1, e.message ?: "Biometric prompt error")
            } catch (t: Throwable) {
                // Ignore safe fallback
            }
        }
    }
}

