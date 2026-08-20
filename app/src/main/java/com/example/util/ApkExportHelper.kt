package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

object ApkExportHelper {

    private const val TAG = "ApkExportHelper"
    const val APK_FILE_NAME = "WilliamsVault.apk"

    data class ApkInfo(
        val fileName: String,
        val sizeFormatted: String,
        val packageName: String,
        val isAvailable: Boolean
    )

    /**
     * Retrieves APK metadata such as installed file size and package name.
     */
    fun getApkInfo(context: Context): ApkInfo {
        return try {
            val sourceApkPath = context.applicationInfo.sourceDir
            val sourceFile = File(sourceApkPath)
            val sizeMb = if (sourceFile.exists()) sourceFile.length().toDouble() / (1024 * 1024) else 0.0
            ApkInfo(
                fileName = APK_FILE_NAME,
                sizeFormatted = String.format(Locale.ENGLISH, "%.1f MB", sizeMb),
                packageName = context.packageName,
                isAvailable = sourceFile.exists() && sourceFile.canRead()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting APK info: ${e.message}", e)
            ApkInfo(
                fileName = APK_FILE_NAME,
                sizeFormatted = "Ready",
                packageName = context.packageName,
                isAvailable = true
            )
        }
    }

    /**
     * Copies the installed application base APK to the app's export cache directory.
     */
    fun extractApkFile(context: Context): File? {
        return try {
            val sourceApkPath = context.applicationInfo.sourceDir
            val sourceFile = File(sourceApkPath)
            if (!sourceFile.exists() || !sourceFile.canRead()) {
                Log.e(TAG, "Source APK not found or not readable: $sourceApkPath")
                return null
            }

            val exportDir = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(exportDir, APK_FILE_NAME)

            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            destinationFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy APK: ${e.message}", e)
            null
        }
    }

    /**
     * Triggers the system Save / Share intent allowing the user to download, save, or transfer the APK.
     */
    fun downloadOrShareApk(
        context: Context,
        chooserTitle: String = "Download / Share Williams Vault APK"
    ): Boolean {
        return try {
            val apkFile = extractApkFile(context)
            if (apkFile == null || !apkFile.exists()) {
                Toast.makeText(context, "Unable to package APK for download", Toast.LENGTH_SHORT).show()
                return false
            }

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Williams Vault Android Application (${apkFile.name})")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Download and install Williams Vault offline finance management app (APK package)."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Toast.makeText(context, "Williams Vault APK package ready to download!", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening APK chooser: ${e.message}", e)
            Toast.makeText(context, "Error exporting APK: ${e.localizedMessage ?: "File provider error"}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
