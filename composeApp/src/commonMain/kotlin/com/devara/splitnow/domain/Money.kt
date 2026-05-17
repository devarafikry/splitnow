package com.devara.splitnow.domain

/**
 * Format a cents value with the currency's local grouping.
 * IDR / JPY: no decimals. Others: 2 decimals.
 */
fun formatMoney(cents: Long, currency: Currency, withSymbol: Boolean = false): String {
    val noDecimals = currency.code == "IDR" || currency.code == "JPY"
    val grouped = if (noDecimals) {
        groupThousands(cents.toString(), '.')
    } else {
        val abs = kotlin.math.abs(cents)
        val whole = (abs / 100).toString()
        val frac = (abs % 100).toString().padStart(2, '0')
        val sign = if (cents < 0) "-" else ""
        "$sign${groupThousands(whole, ',')}.$frac"
    }
    return if (withSymbol) "${currency.symbol}$grouped" else grouped
}

private fun groupThousands(num: String, sep: Char): String {
    val negative = num.startsWith("-")
    val core = if (negative) num.drop(1) else num
    if (core.length <= 3) return if (negative) "-$core" else core
    val out = StringBuilder()
    var i = core.length
    while (i > 3) {
        out.insert(0, core.substring(i - 3, i))
        out.insert(0, sep)
        i -= 3
    }
    out.insert(0, core.substring(0, i))
    return if (negative) "-$out" else out.toString()
}

/** Parse a user-typed money string into cents. Accepts "32.000", "32,000", "1,234.56". */
fun parseMoneyToCents(text: String, currency: Currency): Long {
    val noDecimals = currency.code == "IDR" || currency.code == "JPY"
    val cleaned = text.replace(",", "").replace(".", "").replace(" ", "")
    if (cleaned.isEmpty()) return 0L
    val asNumber = cleaned.toLongOrNull() ?: return 0L
    return if (noDecimals) asNumber * 100 else asNumber
}
