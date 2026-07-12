package com.apnakhaata.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apnakhaata.app.backup.BackupManager
import com.apnakhaata.app.data.AppDatabase
import com.apnakhaata.app.data.CategoryRule
import com.apnakhaata.app.data.TransactionEntity
import com.apnakhaata.app.sms.InboxScanner
import com.apnakhaata.app.sms.SmsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)

    val transactions = db.transactionDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pending = db.transactionDao().observePending()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val rules = db.ruleDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun confirmTransaction(id: Long, purpose: String, category: String) {
        viewModelScope.launch { db.transactionDao().confirm(id, purpose, category) }
    }

    fun updateCategory(id: Long, category: String) {
        viewModelScope.launch { db.transactionDao().updateCategory(id, category) }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { db.transactionDao().delete(id) }
    }

    fun addManual(
        merchant: String, purpose: String, amount: Double,
        type: String, category: String, dateMillis: Long
    ) {
        viewModelScope.launch {
            db.transactionDao().insert(
                TransactionEntity(
                    date = dateMillis,
                    merchant = merchant,
                    purpose = purpose,
                    amount = amount,
                    type = type,
                    category = category,
                    source = "manual",
                    confirmed = true,
                    smsHash = null
                )
            )
        }
    }

    fun addRule(keyword: String, category: String) {
        viewModelScope.launch {
            db.ruleDao().insert(CategoryRule(keyword = keyword.trim(), category = category.trim()))
        }
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch { db.ruleDao().delete(id) }
    }

    fun autoCategorize(text: String): String =
        SmsParser.autoCategorize(text, rules.value)

    fun scanInbox(onDone: (Int) -> Unit) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                InboxScanner.scan(getApplication())
            }
            onDone(count)
        }
    }

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        BackupManager.export(
            db.transactionDao().getAllOnce(),
            db.ruleDao().getAllOnce()
        )
    }

    /** Merge-import: skips exact duplicates (date+amount+merchant+type). Returns inserted count. */
    suspend fun importJson(json: String): Int = withContext(Dispatchers.IO) {
        val result = BackupManager.parse(json)
        var inserted = 0
        for (t in result.transactions) {
            val dup = db.transactionDao().countDuplicate(t.date, t.amount, t.merchant, t.type)
            if (dup == 0) {
                db.transactionDao().insert(t)
                inserted++
            }
        }
        val existingKeywords = db.ruleDao().getAllOnce().map { it.keyword.lowercase() }.toSet()
        for (r in result.rules) {
            if (r.keyword.lowercase() !in existingKeywords) {
                db.ruleDao().insert(r)
            }
        }
        inserted
    }
}
