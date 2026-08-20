package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.ui.components.FinanceFormatters
import kotlin.math.abs

object BudgetNotificationHelper {

    private const val TAG = "BudgetNotificationHelper"
    private const val CHANNEL_ID = "budget_alerts_channel"
    private const val CHANNEL_NAME = "Budget Spending Alerts"
    private const val CHANNEL_DESC = "Notifications when your spending exceeds or nears a category budget limit"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                    description = CHANNEL_DESC
                    enableVibration(true)
                    setShowBadge(true)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.createNotificationChannel(channel)
            } catch (e: Throwable) {
                Log.e(TAG, "Could not create notification channel safely: ${e.message}")
            }
        }
    }

    /**
     * Sends an Android System Notification informing the user that a category budget limit has been exceeded.
     */
    fun sendBudgetExceededNotification(
        context: Context,
        category: String,
        spentAmount: Double,
        limitAmount: Double
    ) {
        try {
            createNotificationChannel(context)

            val excessAmount = (spentAmount - limitAmount).coerceAtLeast(0.0)
            val percentage = if (limitAmount > 0) ((spentAmount / limitAmount) * 100).toInt() else 100

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val requestCode = abs(category.hashCode())
            val pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val title = "⚠️ Budget Limit Exceeded: $category"
            val message = "You've spent ${FinanceFormatters.formatCurrency(spentAmount)} of your ${FinanceFormatters.formatCurrency(limitAmount)} monthly limit ($percentage%). Over by ${FinanceFormatters.formatCurrency(excessAmount)}."

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_ALARM)

            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = 1000 + (abs(category.hashCode()) % 1000)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to send budget exceeded notification: ${e.message}")
        }
    }

    /**
     * Sends an alert when approaching budget (e.g. 90% or 100%).
     */
    fun sendBudgetWarningNotification(
        context: Context,
        category: String,
        spentAmount: Double,
        limitAmount: Double
    ) {
        try {
            createNotificationChannel(context)

            val remaining = (limitAmount - spentAmount).coerceAtLeast(0.0)
            val percentage = if (limitAmount > 0) ((spentAmount / limitAmount) * 100).toInt() else 0

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val requestCode = abs(category.hashCode()) + 1
            val pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val title = "⚡ Budget Warning: $category ($percentage%)"
            val message = "You have ${FinanceFormatters.formatCurrency(remaining)} left of your ${FinanceFormatters.formatCurrency(limitAmount)} budget for $category."

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = 2000 + (abs(category.hashCode()) % 1000)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to send budget warning notification: ${e.message}")
        }
    }
}

