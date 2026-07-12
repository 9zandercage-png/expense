package com.apnakhaata.app.sms

import android.content.Context
import android.provider.Telephony
import com.apnakhaata.app.data.AppDatabase
import com.apnakhaata.app.data.TransactionEntity

/**
 * Scans the existing SMS inbox and imports every message that parses
 * as a transaction. Duplicates are skipped via the unique smsHash index.
 */
object InboxScanner {

    suspend fun scan(context: Context): Int {
        val db = AppDatabase.get(context)
        val dao = db.transactionDao()
        val rules = db.ruleDao().getAllOnce()
        var inserted = 0

        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            ),
            null, null,
            Telephony.Sms.DATE + " DESC"
        ) ?: return 0

        // Collect first, then insert (avoid holding cursor during DB ops)
        val candidates = mutableListOf<TransactionEntity>()
        cursor.use { c ->
            val idxAddr = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val idxBody = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val idxDate = c.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (c.moveToNext()) {
                val sender = c.getString(idxAddr) ?: continue
                val body = c.getString(idxBody) ?: continue
                val date = c.getLong(idxDate)

                val parsed = SmsParser.parse(body, sender, date) ?: continue
                val category = SmsParser.autoCategorize(parsed.merchant + " " + body, rules)

                candidates.add(
                    TransactionEntity(
                        date = parsed.timestamp,
                        merchant = parsed.merchant,
                        purpose = "",
                        amount = parsed.amount,
                        type = parsed.type,
                        category = category,
                        source = "inbox-scan",
                        confirmed = false,
                        rawSms = body,
                        smsHash = SmsParser.smsHash(sender, body, date)
                    )
                )
            }
        }

        for (entity in candidates) {
            val id = dao.insert(entity)
            if (id != -1L) inserted++
        }
        return inserted
    }
}
