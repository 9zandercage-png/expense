package com.apnakhaata.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.apnakhaata.app.data.AppDatabase
import com.apnakhaata.app.data.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        // Multipart SMS: parts share the same originating address — join bodies
        val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }
        val sender = messages.first().displayOriginatingAddress ?: "Unknown"
        val timestamp = messages.first().timestampMillis

        val parsed = SmsParser.parse(fullBody, sender, timestamp) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)
                val rules = db.ruleDao().getAllOnce()
                val category = SmsParser.autoCategorize(
                    parsed.merchant + " " + parsed.rawBody, rules
                )
                val entity = TransactionEntity(
                    date = parsed.timestamp,
                    merchant = parsed.merchant,
                    purpose = "",
                    amount = parsed.amount,
                    type = parsed.type,
                    category = category,
                    source = "sms-auto",
                    confirmed = false,
                    rawSms = parsed.rawBody,
                    smsHash = SmsParser.smsHash(parsed.sender, parsed.rawBody, parsed.timestamp)
                )
                val id = db.transactionDao().insert(entity)
                if (id != -1L) {
                    // Inserted (not a duplicate) — notify user to confirm
                    NotificationHelper.showTransaction(context, parsed, category)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
