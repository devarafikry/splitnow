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

    fun toDomain(parsed: ParsedSplit, currency: Currency): Triple<List<BillItem>, List<Charge>, List<String>> {
        val people = parsed.people.distinct().filter { it.isNotBlank() }
        val items = parsed.items.map { p ->
            BillItem(
                name = p.name.trim(),
                priceCents = priceToCents(p.price, currency),
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
                valueCents = priceToCents(c.value, currency),
            )
        }
        return Triple(items, charges, people)
    }

    private fun priceToCents(value: String, currency: Currency): Long {
        val noDecimals = currency.code == "IDR" || currency.code == "JPY"
        val stripped = value.replace(",", "").replace(".", "").replace(" ", "")
        val n = stripped.toLongOrNull() ?: 0L
        return if (noDecimals) n * 100 else n
    }

    private fun stripCodeFences(raw: String): String {
        var s = raw.trim()
        if (s.startsWith("```")) {
            s = s.removePrefix("```json").removePrefix("```").trim()
            if (s.endsWith("```")) s = s.removeSuffix("```").trim()
        }
        return s
    }

    private fun buildPrompt(ocr: String, description: String, currency: Currency): String {
        return """
You are a receipt-parsing assistant for SplitNow, a split-bill app.

Given OCR text from a restaurant receipt and a free-text description of who ordered what, produce a strict JSON object that matches this schema:

{
  "restaurantName": "string",
  "people": ["string", ...],
  "items": [
    { "name": "string", "price": "string (numbers only, ${if (currency.code == "IDR" || currency.code == "JPY") "no decimals" else "two decimals like 12.50 stored as cents 1250"})", "assignedTo": "Name1,Name2 OR SHARED" }
  ],
  "charges": [
    { "label": "Tax / Service / Discount / etc", "type": "PERCENT or FIXED", "rate": 10.0, "value": "string" }
  ],
  "currencyCode": "${currency.code}"
}

Hard rules:
- Return JSON ONLY, no prose, no markdown fences.
- Every item.assignedTo MUST be either SHARED or a comma-separated list of names that appear in people.
- If the description doesn't mention a person, fall back to SHARED.
- Include tax/service/discount in charges, NOT in items.
- For discounts, charges.value is negative.
- For PERCENT charges, rate is the percentage (e.g. 10.0). For FIXED, rate may be 0.
- Use the same currency as the receipt (or default to ${currency.code}).
- All prices stored as integer major units (e.g. Rp 32.000 -> "32000").

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
