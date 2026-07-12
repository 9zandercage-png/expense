package com.apnakhaata.app.sms

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.apnakhaata.app.MainActivity
import java.text.NumberFormat
import java.util.Locale

object NotificationHelper {

    const val CHANNEL_ID = "transactions"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Transaction Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Naye transaction pakde jaane par notification"
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    fun showTransaction(context: Context, parsed: SmsParser.ParsedSms, category: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED &&
            android.os.Build.VERSION.SDK_INT >= 33
        ) return

        val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
        val amountStr = "₹" + nf.format(parsed.amount)
        val sign = if (parsed.type == "debit") "−" else "+"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_tab", "pending")
        }
        val pi = PendingIntent.getActivity(
            context, parsed.timestamp.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("$sign$amountStr — ${parsed.merchant}")
            .setContentText("Category: $category · Tap karke confirm karo (kis liye tha?)")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        NotificationManagerCompat.from(context).notify(parsed.timestamp.toInt(), notif)
    }
}
