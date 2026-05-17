package com.devara.splitnow.domain

import kotlinx.serialization.Serializable

/** Currency codes ISO-4217. Default IDR — Indonesian Rupiah (no decimals in UI). */
@Serializable
data class Currency(val code: String, val symbol: String, val locale: String) {
    companion object {
        val IDR = Currency("IDR", "Rp", "id-ID")
        val USD = Currency("USD", "$", "en-US")
        val EUR = Currency("EUR", "€", "en-EU")
        val SGD = Currency("SGD", "S$", "en-SG")
        val MYR = Currency("MYR", "RM", "ms-MY")
        val JPY = Currency("JPY", "¥", "ja-JP")
        val GBP = Currency("GBP", "£", "en-GB")
        val all = listOf(IDR, USD, EUR, SGD, MYR, JPY, GBP)
        fun byCode(code: String): Currency = all.firstOrNull { it.code == code } ?: IDR
    }
}

enum class ChargeType { PERCENT, FIXED }

/**
 * How tax/service/charges are split across the diners.
 *  - EQUAL: charges divided equally across all people.
 *  - SKIP: charges aren't added to anyone's share — the host absorbs them.
 *
 * PROPORTIONAL is kept in the enum for backward compatibility with existing
 * Room rows; the picker no longer offers it and Review coerces stored
 * PROPORTIONAL values to EQUAL on read.
 */
enum class SplitMode { PROPORTIONAL, EQUAL, SKIP }

enum class PaymentKind { BANK, EWALLET, QRIS }

/**
 * In-progress / completed split. Persisted in Room.
 * Monetary fields stored as integer cents to avoid floating-point drift.
 */
@Serializable
data class Split(
    val id: Long = 0L,
    val restaurantName: String,
    val dateMs: Long,
    val currencyCode: String,
    val totalCents: Long,
    val splitMode: SplitMode = SplitMode.EQUAL,
    val paymentMethodId: Long? = null,
    val notes: String? = null,
)

@Serializable
data class BillItem(
    val id: Long = 0L,
    val splitId: Long = 0L,
    val name: String,
    val priceCents: Long,
    /** comma-separated person names, or "SHARED" for items split among all. */
    val assignedTo: String,
) {
    val isShared: Boolean get() = assignedTo == SHARED
    val people: List<String> get() =
        if (isShared) emptyList() else assignedTo.split(",").mapNotNull { it.trim().takeIf(String::isNotBlank) }

    companion object { const val SHARED = "SHARED" }
}

@Serializable
data class Charge(
    val id: Long = 0L,
    val splitId: Long = 0L,
    val label: String,
    val type: ChargeType,
    /** For PERCENT this is e.g. 10.0 (=10%); for FIXED this is unused (use valueCents). */
    val rate: Double = 0.0,
    /** For FIXED this is the absolute amount in cents; for PERCENT this is the computed value. */
    val valueCents: Long = 0L,
    /** Discounts have negative valueCents. */
)

@Serializable
data class PaymentMethod(
    val id: Long = 0L,
    val kind: PaymentKind,
    val name: String,
    val account: String,
    val holder: String,
    val qrUri: String? = null,
    val isDefault: Boolean = false,
)

/**
 * Computed per-person breakdown — not stored, derived from items+charges.
 */
data class PersonShare(
    val name: String,
    val itemsCents: Long,
    val sharedCents: Long,
    val chargesCents: Long,
    val totalCents: Long,
    val items: List<BillItem>,
)
