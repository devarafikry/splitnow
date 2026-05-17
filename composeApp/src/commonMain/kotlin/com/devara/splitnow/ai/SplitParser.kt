package com.devara.splitnow.ai

import com.devara.splitnow.domain.BillItem
import com.devara.splitnow.domain.Charge
import com.devara.splitnow.domain.ChargeType
import com.devara.splitnow.domain.Currency
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ParsedSplit(
    val restaurantName: String = "",
    val people: List<String> = emptyList(),
    val items: List<ParsedItem> = emptyList(),
    val charges: List<ParsedCharge> = emptyList(),
    val currencyCode: String = "IDR",
)

@Serializable
data class ParsedItem(
    val name: String,
    /** Major units, e.g. "32000" for Rp 32.000 (IDR has no decimal places in receipts). */
    val price: String,
    /** Comma-separated person names that ordered this, OR "SHARED" if everyone shares it. */
    val assignedTo: String,
)

@Serializable
data class ParsedCharge(
    val label: String,
    val type: String,     // "PERCENT" or "FIXED"
    val rate: Double = 0.0,
    val value: String = "0",
)

/**
 * Wraps Gemini with a JSON-only prompt that turns (OCR text + free-text
 * description) into a structured ParsedSplit. The model is instructed to
 * use only people named in the description, attribute every line item to
 * someone or SHARED, and copy tax/service/discount from OCR.
 */
class SplitParser(private val client: GeminiClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun parse(ocrText: String, description: String, currency: Currency): ParsedSplit {
        val prompt = buildPrompt(ocrText, description, currency)
        val raw = client.generate(prompt).trim()
        val cleaned = stripCodeFences(raw)
        return runCatching { json.decodeFromString(ParsedSplit.serializer(), cleaned) }
            .getOrElse { error("AI returned malformed output. Try again or edit manually.\n\n---\n$cleaned") }
    }

    /** Returns the resolved currency (from AI), domain items, charges, and people. */
    fun toDomain(parsed: ParsedSplit, fallback: Currency): Quadruple {
        val resolved = Currency.byCode(parsed.currencyCode.ifBlank { fallback.code })
        val people = parsed.people.distinct().filter { it.isNotBlank() }
        val items = parsed.items.map { p ->
            BillItem(
                name = p.name.trim(),
                priceCents = priceToCents(p.price, resolved),
                assignedTo = p.assignedTo.trim().ifEmpty { BillItem.SHARED },
            )
        }
        val charges = parsed.charges.map { c ->
            val type = runCatching { ChargeType.valueOf(c.type.uppercase()) }
                .getOrDefault(ChargeType.FIXED)
            Charge(
                label = c.label.ifBlank { "Charge" },
                type = type,
                rate = c.rate,
                valueCents = priceToCents(c.value, resolved),
            )
        }
        return Quadruple(resolved, items, charges, people)
    }

    data class Quadruple(
        val currency: Currency,
        val items: List<BillItem>,
        val charges: List<Charge>,
        val people: List<String>,
    )

    /** AI returns prices as plain digits ("32000" for Rp 32,000 / "1250" for $12.50). */
    private fun priceToCents(value: String, currency: Currency): Long {
        val cleaned = value.replace(",", "").replace(".", "").replace(" ", "").replace("-", "")
        val negative = value.trim().startsWith("-")
        val n = cleaned.toLongOrNull() ?: 0L
        return if (negative) -n else n
    }

    private fun stripCodeFences(raw: String): String {
        var s = raw.trim()
        if (s.startsWith("```")) {
            s = s.removePrefix("```json").removePrefix("```").trim()
            if (s.endsWith("```")) s = s.removeSuffix("```").trim()
        }
        return s
    }

    private fun buildPrompt(ocr: String, description: String, fallback: Currency): String {
        return """
You are a receipt-parsing assistant for SplitNow, a split-bill app.

Given OCR text from a restaurant receipt and a free-text description of who ordered what, produce a strict JSON object matching this schema:

{
  "restaurantName": "string",
  "people": ["string", ...],
  "items": [
    { "name": "string", "price": "string (integer minor units)", "assignedTo": "Name1,Name2 OR SHARED" }
  ],
  "charges": [
    { "label": "Tax / Service / Discount / etc", "type": "PERCENT or FIXED", "rate": 10.0, "value": "string (integer minor units)" }
  ],
  "currencyCode": "ISO 4217 — IDR, JPY, KRW (zero-decimal) or USD, EUR, GBP, SGD, MYR (two-decimal)"
}

Hard rules:
- Return JSON ONLY, no prose, no markdown fences.
- DETECT the currency from the receipt (currency symbol, country/language, items). Report it in currencyCode. If genuinely unclear, fall back to ${fallback.code}.
- Price encoding rule (CRITICAL):
  * Zero-decimal currencies (IDR, JPY, KRW): price is the major unit as integer. Rp 32.000 -> "32000". ¥1,500 -> "1500".
  * Two-decimal currencies (USD, EUR, etc): price is in cents. $12.50 -> "1250". €4.00 -> "400".
- Every item.assignedTo MUST be either SHARED or a comma-separated list of names that appear in `people`.
- If the description doesn't mention who got an item, fall back to SHARED.
- Tax/service/discount go in `charges`, NOT in `items`.
- For discounts, charges.value is negative.
- For PERCENT charges, rate is the percentage (e.g. 10.0). For FIXED, rate may be 0.
- People names must come from the user description, not invented.

OCR text:
---
$ocr
---

User description:
---
$description
---

Now produce the JSON.
        """.trimIndent()
    }
}
