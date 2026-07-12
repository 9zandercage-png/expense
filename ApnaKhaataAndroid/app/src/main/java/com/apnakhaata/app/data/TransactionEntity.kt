package com.apnakhaata.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["smsHash"], unique = true)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,                 // epoch millis
    val merchant: String,
    val purpose: String = "",       // "yeh payment kis liye thi"
    val amount: Double,
    val type: String,               // "debit" | "credit"
    val category: String,
    val source: String,             // "sms-auto" | "inbox-scan" | "manual" | "import"
    val confirmed: Boolean,
    val rawSms: String = "",
    val smsHash: Long? = null       // dedup key; null for manual/import
)
