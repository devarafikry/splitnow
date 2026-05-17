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
    }
}
