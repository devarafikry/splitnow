package com.devara.splitnow.domain

/**
 * SplitNow stores all monetary values as a single Long called `cents` in the
 * data layer. The actual semantics depend on the currency:
 *  - **Zero-decimal currencies** (IDR, JPY, KRW…): the Long IS the major unit.
 *    e.g. Rp 32,000 -> 32_000.
 *  - **Two-decimal currencies** (USD, EUR, SGD, MYR, GBP…): the Long is in
 *    cents/sen. e.g. $12.50 -> 1_250.
 *
 * Display + parsing always go through these helpers so the per-currency rule
 * is centralized.
 */

fun Currency.hasDecimals(): Boolean = code != "IDR" && code != "JPY" && code != "KRW"

/** Format a stored Long for display, with locale-appropriate grouping. */
fun formatMoney(cents: Long, currency: Currency, withSymbol: Boolean = false): String {
    val sign = if (cents < 0) "-" else ""
    val abs = kotlin.math.abs(cents)
    val grouped = if (!currency.hasDecimals()) {
        // IDR-style: dot-grouped thousands, no decimal part.
        groupThousands(abs.toString(), '.')
    } else {
        val whole = (abs / 100).toString()
        val frac = (abs % 100).toString().padStart(2, '0')
        "${groupThousands(whole, ',')}.$frac"
    }
    val out = "$sign$grouped"
    return if (withSymbol) "${currency.symbol}$out" else out
}

private fun groupThousands(num: String, sep: Char): String {
    if (num.length <= 3) return num
    val out = StringBuilder()
    var i = num.length
    while (i > 3) {
        out.insert(0, num.substring(i - 3, i))
        out.insert(0, sep)
        i -= 3
    }
    out.insert(0, num.substring(0, i))
    return out.toString()
}

/** Parse a user-typed money string into the per-currency Long. */
fun parseMoneyToCents(text: String, currency: Currency): Long {
    val cleaned = text.replace(",", "").replace(".", "").replace(" ", "").replace("-", "")
    if (cleaned.isEmpty()) return 0L
    val asNumber = cleaned.toLongOrNull() ?: return 0L
    // We accept the same digit sequence in both decimal and zero-decimal modes
    // — user types "32000" or "1250" without separators in their input box.
    return asNumber
}
