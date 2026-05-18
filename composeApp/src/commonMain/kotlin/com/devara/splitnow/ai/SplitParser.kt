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
        val people = parsed.people.distinct().filter { it.isNotBlank() }.map { it.trim() }
        // Lower-case lookup keyed → canonical name from people list. Used to
        // normalize the AI's assignedTo string so case/whitespace variants
        // ("budi", "BUDI ") still resolve to the same person.
        val canonical: Map<String, String> = people.associateBy { it.lowercase() }
        val items = parsed.items.map { p ->
            val raw = p.assignedTo.trim()
            val assignedTo = when {
                raw.isEmpty() -> BillItem.SHARED
                raw.equals("SHARED", ignoreCase = true) -> BillItem.SHARED
                else -> {
                    val matched = raw.split(",")
                        .mapNotNull { token -> canonical[token.trim().lowercase()] }
                    if (matched.isEmpty()) BillItem.SHARED else matched.distinct().joinToString(",")
                }
            }
            BillItem(
                name = p.name.trim(),
                priceCents = priceToCents(p.price, resolved),
                assignedTo = assignedTo,
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

Receipt-structure rules (IMPORTANT — modifiers vs. standalone items):
- Receipts often list customizations/modifiers on a separate line BELOW the main item. These are NOT standalone items — they belong to the item above and their price is part of that item.
- Hints that a line is a modifier (not its own item):
  * Lacks the quantity prefix the main items have ("1 T", "1x", "1 ×", "1 個", etc).
  * Smaller indentation / appears nested under another line.
  * Generic add-on words in any language: ショット (shot), シロップ (syrup), カスタム (custom), 追加 (add), サイズ (size), アップ (up), オプション (option), トッピング (topping), 大盛り (large), Extra, Add-on, Side, Topping, Upgrade, +cheese, +syrup, dengan tambahan, ekstra, porsi tambah.
  * Much smaller price than typical main items in the same receipt.
  * Immediately follows a main item line — modifier lines are typically adjacent to their parent.
- When you detect a modifier, MERGE its price into the parent item's `price`. Do NOT emit a separate item for it.
  Example receipt:
    1 T コーヒー フラペチーノ        440
    ショット                       50
  → items: [{ "name": "コーヒー フラペチーノ", "price": "490", "assignedTo": "<whoever ordered the coffee>" }]
- A modifier with no price (e.g. "シロップ変更" marked カスタム / 0) just gets ignored — no item, no price change.
- When you can't decide between modifier and standalone item, prefer standalone — the user can fix it manually in Review.

Language handling (IMPORTANT — be flexible):
- The OCR text and the user description may be in DIFFERENT languages. Understand both.
- Item names ("name") MUST be reproduced in the language they appear on the receipt — do NOT translate them. e.g. if the receipt says "ナシゴレン", item.name is "ナシゴレン", not "fried rice".
- People names ("people" + every name token in "assignedTo") MUST be reproduced as the user typed them in the description, in whatever script/case they used. e.g. if the user wrote "アレックス got the curry", people = ["アレックス"].
- Match items to people based on the SEMANTIC intent of the description, regardless of source language. The user might write English while the receipt is Japanese, or vice versa — work it out.
- charges.label may use a generic English label (Tax, Service, Discount) OR the receipt's term — your choice based on what reads cleaner.

Hard rules:
- Return JSON ONLY, no prose, no markdown fences.
- DETECT the currency from the receipt (currency symbol, country/language, items). Report it in currencyCode. If genuinely unclear, fall back to ${fallback.code}.
- Price encoding rule (CRITICAL):
  * Zero-decimal currencies (IDR, JPY, KRW): price is the major unit as integer. Rp 32.000 -> "32000". ¥1,500 -> "1500".
  * Two-decimal currencies (USD, EUR, etc): price is in cents. ${'$'}12.50 -> "1250". €4.00 -> "400".
- Every item.assignedTo MUST be either SHARED or a comma-separated list of names that EXACTLY match a name in `people` (same script, same case, same spelling).
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
