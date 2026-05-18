package com.devara.splitnow.ui.flow

import com.devara.splitnow.domain.BillItem
import com.devara.splitnow.domain.Charge
import com.devara.splitnow.domain.Currency
import com.devara.splitnow.domain.SplitMode

/**
 * In-memory state passed across the new-split flow (scan→OCR→describe→AI→review).
 * Held as a Koin singleton; reset() called when the user starts a fresh split or
 * finishes/cancels the current one.
 */
class SplitFlowState {
    var capturedImage: ByteArray? = null
    var ocrText: String = ""
    var description: String = ""
    var restaurantName: String = ""
    var currency: Currency = Currency.IDR
    var people: MutableList<String> = mutableListOf()
    var items: MutableList<BillItem> = mutableListOf()
    var charges: MutableList<Charge> = mutableListOf()
    var splitMode: SplitMode = SplitMode.EQUAL
    var subtotalCents: Long = 0L
    var totalCents: Long = 0L
    var error: String? = null
    /** Set once the split is first written to the DB so Review/Share update the same row. */
    var persistedSplitId: Long? = null

    fun reset() {
        capturedImage = null
        ocrText = ""
        description = ""
        restaurantName = ""
        people.clear()
        items.clear()
        charges.clear()
        splitMode = SplitMode.EQUAL
        subtotalCents = 0L
        totalCents = 0L
        error = null
        persistedSplitId = null
    }

    // ── Per-person exclusion helpers ────────────────────────────────────
    // Both operate on canonical names — match case-insensitively but write
    // back the canonical (from `people`) form.

    /**
     * Opt a person out of a shared item. If the item is currently SHARED,
     * we replace the assignedTo with the CSV of remaining people. If it's
     * already a CSV (subset), we just drop the name from the list. If the
     * removal would leave zero owners, the item stays unchanged (UI guard).
     */
    fun excludeFromSharedItem(itemId: Long, personName: String) {
        val idx = items.indexOfFirst { it.id == itemId }.takeIf { it >= 0 } ?: return
        val item = items[idx]
        val canonical = people.firstOrNull { it.equals(personName, ignoreCase = true) } ?: return
        val effectiveOwners = if (item.isShared) people.toList()
            else item.people.mapNotNull { n -> people.firstOrNull { it.equals(n, ignoreCase = true) } }
        val remaining = effectiveOwners.filterNot { it.equals(canonical, ignoreCase = true) }
        if (remaining.isEmpty()) return
        items[idx] = item.copy(assignedTo = remaining.joinToString(","))
    }

    /** Re-include a person on a shared item (mirror of excludeFromSharedItem). */
    fun includeOnSharedItem(itemId: Long, personName: String) {
        val idx = items.indexOfFirst { it.id == itemId }.takeIf { it >= 0 } ?: return
        val item = items[idx]
        val canonical = people.firstOrNull { it.equals(personName, ignoreCase = true) } ?: return
        if (item.isShared) return // already includes everyone
        val current = item.people.mapNotNull { n -> people.firstOrNull { it.equals(n, ignoreCase = true) } }
            .toMutableList()
        if (current.any { it.equals(canonical, ignoreCase = true) }) return
        current.add(canonical)
        items[idx] = if (current.size == people.size) item.copy(assignedTo = BillItem.SHARED)
            else item.copy(assignedTo = current.joinToString(","))
    }

    /** Opt a person out of a specific charge — appends to that charge's excludeFromNames CSV. */
    fun excludeFromCharge(chargeId: Long, personName: String) {
        val idx = charges.indexOfFirst { it.id == chargeId }.takeIf { it >= 0 } ?: return
        val charge = charges[idx]
        val canonical = people.firstOrNull { it.equals(personName, ignoreCase = true) } ?: return
        val current = charge.excluded.toMutableList()
        if (current.any { it.equals(canonical, ignoreCase = true) }) return
        current.add(canonical)
        // Don't let everyone opt out — at least one must pay.
        if (current.size >= people.size) return
        charges[idx] = charge.copy(excludeFromNames = current.joinToString(","))
    }

    /** Re-include a person on a charge (mirror of excludeFromCharge). */
    fun includeOnCharge(chargeId: Long, personName: String) {
        val idx = charges.indexOfFirst { it.id == chargeId }.takeIf { it >= 0 } ?: return
        val charge = charges[idx]
        val canonical = people.firstOrNull { it.equals(personName, ignoreCase = true) } ?: return
        val current = charge.excluded.filterNot { it.equals(canonical, ignoreCase = true) }
        charges[idx] = charge.copy(excludeFromNames = current.joinToString(","))
    }
}
