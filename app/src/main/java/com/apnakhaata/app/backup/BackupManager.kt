package com.apnakhaata.app.backup

import com.apnakhaata.app.data.CategoryRule
import com.apnakhaata.app.data.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * JSON backup format — compatible with the Apna Khaata web app,
 * so data can move web -> android and android -> web.
 */
object BackupManager {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun export(transactions: List<TransactionEntity>, rules: List<CategoryRule>): String {
        val root = JSONObject()
        root.put("app", "Apna Khaata")
        root.put("platform", "android")
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date()))

        val txArr = JSONArray()
        for (t in transactions) {
            val o = JSONObject()
            o.put("id", t.id)
            o.put("date", dateFmt.format(Date(t.date)))
            o.put("dateMillis", t.date)
            o.put("merchant", t.merchant)
            o.put("description", t.purpose)
            o.put("amount", t.amount)
            o.put("type", t.type)
            o.put("category", t.category)
            o.put("source", t.source)
            o.put("confirmed", t.confirmed)
            txArr.put(o)
        }
        root.put("transactions", txArr)

        val ruleArr = JSONArray()
        for (r in rules) {
            val o = JSONObject()
            o.put("keyword", r.keyword)
            o.put("category", r.category)
            ruleArr.put(o)
        }
        root.put("rules", ruleArr)
        return root.toString(2)
    }

    data class ImportResult(
        val transactions: List<TransactionEntity>,
        val rules: List<CategoryRule>
    )

    /** Parses backup JSON (web or android format). Throws on invalid JSON. */
    fun parse(json: String): ImportResult {
        val root = JSONObject(json)
        val txList = mutableListOf<TransactionEntity>()
        val txArr = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until txArr.length()) {
            val o = txArr.getJSONObject(i)
            val millis: Long = when {
                o.has("dateMillis") -> o.getLong("dateMillis")
                o.has("date") -> parseDateToMillis(o.getString("date"))
                else -> System.currentTimeMillis()
            }
            txList.add(
                TransactionEntity(
                    id = 0, // fresh id on import
                    date = millis,
                    merchant = o.optString("merchant", "Unknown"),
                    purpose = o.optString("description", o.optString("purpose", "")),
                    amount = o.optDouble("amount", 0.0),
                    type = o.optString("type", "debit"),
                    category = o.optString("category", "Uncategorised"),
                    source = "import",
                    confirmed = o.optBoolean("confirmed", true),
                    rawSms = o.optString("rawSms", ""),
                    smsHash = null
                )
            )
        }

        val ruleList = mutableListOf<CategoryRule>()
        val ruleArr = root.optJSONArray("rules") ?: JSONArray()
        for (i in 0 until ruleArr.length()) {
            val o = ruleArr.getJSONObject(i)
            val kw = o.optString("keyword", "")
            val cat = o.optString("category", "")
            if (kw.isNotBlank() && cat.isNotBlank()) {
                ruleList.add(CategoryRule(keyword = kw, category = cat))
            }
        }
        return ImportResult(txList, ruleList)
    }

    private fun parseDateToMillis(s: String): Long = try {
        dateFmt.parse(s)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
