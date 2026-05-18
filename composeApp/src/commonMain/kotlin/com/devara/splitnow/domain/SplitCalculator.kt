package com.devara.splitnow.domain

/**
 * Pure-Kotlin per-person split calculator.
 *
 * Invariants:
 *  - Every item with no recognized owner falls back to SHARED.
 *  - Each item's price is split exactly across its assignees (integer
 *    remainder distributed deterministically to the first assignee).
 *  - In EQUAL mode: Σ(personShare.totalCents) == itemsSubtotal + chargesTotal.
 *  - In SKIP mode:  Σ(personShare.totalCents) == itemsSubtotal (host absorbs
 *    the charges; friends only owe for their own items + shared).
 *
 * Person-name matching is case-insensitive against the `people` list, with
 * canonicalization happening upstream in SplitParser.toDomain().
 */
fun calculateShares(
    items: List<BillItem>,
    charges: List<Charge>,
    mode: SplitMode,
    people: List<String>,
): List<PersonShare> {
    if (people.isEmpty()) return emptyList()
    val canonical: Map<String, String> = people.associateBy { it.lowercase() }

    // Per-item assignee resolution. Items with unrecognized assignees become SHARED.
    fun resolved(item: BillItem): List<String> {
        if (item.isShared) return emptyList()
        val raw = item.people
        val matched = raw.mapNotNull { canonical[it.lowercase()] }
        return matched.distinct()
    }

    val personalCents = HashMap<String, Long>().apply { people.forEach { put(it, 0L) } }
    val sharedItems = mutableListOf<BillItem>()
    for (item in items) {
        val owners = resolved(item)
        if (item.isShared || owners.isEmpty()) {
            sharedItems.add(item)
            continue
        }
        val per = item.priceCents / owners.size
        val remainder = item.priceCents - per * owners.size
        owners.forEachIndexed { i, name ->
            personalCents[name] = personalCents[name]!! + per + if (i == 0) remainder else 0L
        }
    }

    // Shared items: split equally across all people, deterministic remainder.
    val sharedTotal = sharedItems.sumOf { it.priceCents }
    val sharedPer = sharedTotal / people.size
    val sharedRemainder = sharedTotal - sharedPer * people.size
    val sharedCents = HashMap<String, Long>().apply {
        people.forEachIndexed { i, name -> put(name, sharedPer + if (i == 0) sharedRemainder else 0L) }
    }

    // Charges: EQUAL splits across everyone with remainder; SKIP leaves zeros.
    val chargesTotal = charges.sumOf { it.valueCents }
    val chargesCents = HashMap<String, Long>().apply { people.forEach { put(it, 0L) } }
    when (mode) {
        SplitMode.SKIP -> { /* host absorbs */ }
        // PROPORTIONAL exists only for backward-compat; treat as EQUAL.
        SplitMode.EQUAL, SplitMode.PROPORTIONAL -> {
            val per = chargesTotal / people.size
            val rem = chargesTotal - per * people.size
            people.forEachIndexed { i, name ->
                chargesCents[name] = per + if (i == 0) rem else 0L
            }
        }
    }

    // Each person's items list = personal items they participate in + every shared item.
    return people.map { name ->
        val personItems = items.filter { item ->
            val owners = resolved(item)
            (item.isShared || owners.isEmpty()) || name in owners
        }
        val itemsC = personalCents[name]!!
        val sharedC = sharedCents[name]!!
        val chargesC = chargesCents[name]!!
        PersonShare(
            name = name,
            itemsCents = itemsC,
            sharedCents = sharedC,
            chargesCents = chargesC,
            totalCents = itemsC + sharedC + chargesC,
            items = personItems,
        )
    }
}

/** Diagnostic result for the Review screen's sanity-check pill. */
data class SplitAudit(
    val itemsSubtotalCents: Long,
    val chargesTotalCents: Long,
    val billTotalCents: Long,
    val sumOfSharesCents: Long,
    val mode: SplitMode,
    val unassignedItems: List<BillItem>,
) {
    val matchesBill: Boolean
        get() = when (mode) {
            SplitMode.SKIP -> sumOfSharesCents == itemsSubtotalCents
            else -> sumOfSharesCents == billTotalCents
        }
}

fun auditShares(
    items: List<BillItem>,
    charges: List<Charge>,
    shares: List<PersonShare>,
    mode: SplitMode,
    people: List<String>,
): SplitAudit {
    val canonical = people.associateBy { it.lowercase() }
    val unassigned = items.filter { item ->
        if (item.isShared) false
        else item.people.none { canonical.containsKey(it.lowercase()) }
    }
    val itemsSub = items.sumOf { it.priceCents }
    val chargesT = charges.sumOf { it.valueCents }
    return SplitAudit(
        itemsSubtotalCents = itemsSub,
        chargesTotalCents = chargesT,
        billTotalCents = itemsSub + chargesT,
        sumOfSharesCents = shares.sumOf { it.totalCents },
        mode = mode,
        unassignedItems = unassigned,
    )
}
