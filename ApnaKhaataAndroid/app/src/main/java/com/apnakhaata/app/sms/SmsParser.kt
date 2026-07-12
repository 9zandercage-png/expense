package com.apnakhaata.app.sms

import com.apnakhaata.app.data.CategoryRule

object SmsParser {

    data class ParsedSms(
        val amount: Double,
        val type: String,        // "debit" | "credit"
        val merchant: String,
        val rawBody: String,
        val sender: String,
        val timestamp: Long
    )

    // Rs. 500 | Rs 500.00 | INR 1,234.56 | ₹500
    private val AMOUNT = Regex(
        """(?:rs\.?|inr|₹)\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // "500.00 debited" / "1,200 credited" style (amount before keyword, no Rs prefix)
    private val AMOUNT_ALT = Regex(
        """([\d,]+(?:\.\d{1,2})?)\s*(?:has been\s+|is\s+)?(?:debited|credited)""",
        RegexOption.IGNORE_CASE
    )

    private val DEBIT_WORDS = listOf(
        "debited", "debit", "paid to", "paid", "sent to", "sent",
        "withdrawn", "purchase of", "spent", "payment of"
    )
    private val CREDIT_WORDS = listOf(
        "credited", "received from", "received", "deposited", "refund"
    )

    // UPI VPA like merchant@ybl
    private val VPA = Regex("""([a-zA-Z0-9.\-_]{2,})@[a-zA-Z][a-zA-Z0-9]{1,}""")

    // "to MERCHANT NAME on/via/ref/." patterns
    private val TO_PATTERN = Regex(
        """(?:\bto|\bat|\btowards)\s+([A-Za-z][A-Za-z0-9 &.\-_']{2,40}?)(?=\s+on\b|\s+via\b|\s+ref\b|\s+upi\b|\s+avl\b|\s+bal\b|\s+in\b|\s+your\b|\.|,|;|$)""",
        RegexOption.IGNORE_CASE
    )
    private val FROM_PATTERN = Regex(
        """\bfrom\s+([A-Za-z][A-Za-z0-9 &.\-_']{2,40}?)(?=\s+on\b|\s+via\b|\s+ref\b|\s+upi\b|\s+avl\b|\s+bal\b|\s+in\b|\s+your\b|\.|,|;|$)""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Parse a bank/UPI SMS. Returns null if this does not look like a
     * money-movement message (OTP, promo, reminder, balance info etc.)
     */
    fun parse(body: String, sender: String, timestamp: Long): ParsedSms? {
        if (body.isBlank()) return null
        val lower = body.lowercase()

        // Hard exclusions — not actual transactions
        if (lower.contains("otp") || lower.contains("one time password")) return null
        if (lower.contains("will be debited") || lower.contains("will be credited")) return null
        if (lower.contains("due on") && !lower.contains("debited")) return null
        if (lower.contains("requested money") || lower.contains("has requested")) return null
        if (lower.contains("failed") || lower.contains("declined") || lower.contains("reversed")) return null

        // Type detection: first keyword position wins
        val firstDebit = DEBIT_WORDS
            .map { lower.indexOf(it) }.filter { it >= 0 }.minOrNull()
        val firstCredit = CREDIT_WORDS
            .map { lower.indexOf(it) }.filter { it >= 0 }.minOrNull()

        val type = when {
            firstDebit != null && (firstCredit == null || firstDebit < firstCredit) -> "debit"
            firstCredit != null -> "credit"
            else -> return null
        }

        val amountStr = AMOUNT.find(body)?.groupValues?.get(1)
            ?: AMOUNT_ALT.find(body)?.groupValues?.get(1)
            ?: return null
        val amount = amountStr.replace(",", "").toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null

        val merchant = extractMerchant(body, type) ?: cleanSender(sender)
        return ParsedSms(amount, type, merchant.trim(), body, sender, timestamp)
    }

    private fun extractMerchant(body: String, type: String): String? {
        // Priority 1: named party (to/from pattern) — most human-readable
        val named = if (type == "debit") {
            TO_PATTERN.find(body)?.groupValues?.get(1)
        } else {
            FROM_PATTERN.find(body)?.groupValues?.get(1)
        }
        if (!named.isNullOrBlank()) {
            val cleaned = named.trim().replace(Regex("""\s{2,}"""), " ")
            // Reject if it's just generic bank words
            val genericWords = setOf("your", "a/c", "account", "vpa", "upi", "bank")
            if (cleaned.lowercase() !in genericWords && cleaned.length >= 3) {
                return cleaned
            }
        }
        // Priority 2: VPA handle (strip @bank suffix)
        val vpa = VPA.find(body)?.groupValues?.get(1)
        if (!vpa.isNullOrBlank()) return vpa
        return null
    }

    private fun cleanSender(sender: String): String {
        // "VM-HDFCBK" -> "HDFCBK"
        return sender.substringAfterLast("-").ifBlank { sender }
    }

    /**
     * Keyword-based auto categorization (same logic as web app).
     */
    fun autoCategorize(text: String, rules: List<CategoryRule>): String {
        val lower = text.lowercase()
        for (rule in rules) {
            if (rule.keyword.isNotBlank() && lower.contains(rule.keyword.lowercase())) {
                return rule.category
            }
        }
        return "Uncategorised"
    }

    /** Stable dedup hash for an SMS. */
    fun smsHash(sender: String, body: String, timestamp: Long): Long {
        var h = 1125899906842597L
        val s = "$sender|$body|$timestamp"
        for (c in s) h = 31 * h + c.code
        return h
    }
}
