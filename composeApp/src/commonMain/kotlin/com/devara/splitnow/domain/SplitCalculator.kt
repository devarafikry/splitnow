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
 *  - Per-charge `excludeFromNames` removes specific people from that charge's
 *    distribution — the remaining people split the same amount across fewer
 *    heads, so the bill total still ties out.
 *  - A "shared" item whose `assignedTo` lists names rather than "SHARED" is
 *    treated as multi-assignee — same math as a shared item but restricted
 *    to that subset. This is how the Review screen lets a person opt out
 *    of a shared item.
 */
fun calculateShares(
    items: List<BillItem>,
    charges: List<Charge>,
    mode: SplitMode,
    people: List<String>,
): List<PersonShare> {
    if (people.isEmpty()) return emptyList()
    val canonical: Map<String, String> = people.associateBy { it.lowercase() }

    fun resolve(names: List<String>): List<String> =
        names.mapNotNull { canonical[it.lowercase()] }.distinct()

    val personalCents = HashMap<String, Long>().apply { people.forEach { put(it, 0L) } }
    val sharedCents = HashMap<String, Long>().apply { people.forEach { put(it, 0L) } }

    for (item in items) {
        val resolvedOwners = if (item.isShared) people else resolve(item.people)
        // Items with no recognized owners fall back to everyone-shares.
        val effectiveOwners = if (resolvedOwners.isEmpty()) people else resolvedOwners
        val per = item.priceCents / effectiveOwners.size
        val remainder = item.priceCents - per * effectiveOwners.size
        // Bucket: a real personal item (assignedTo lists known names) credits
        // the personal bucket; SHARED-flagged + unresolved-owner items credit
        // the shared bucket. The distinction surfaces in the UI breakdown.
        val targetBucket = if (item.isShared || resolve(item.people).isEmpty()) sharedCents else personalCents
        effectiveOwners.forEachIndexed { i, name ->
            targetBucket[name] = targetBucket[name]!! + per + if (i == 0) remainder else 0L
        }
    }

    // Charges — per-charge distribution, honoring exclusions.
    val chargesCents = HashMap<String, Long>().apply { people.forEach { put(it, 0L) } }
    if (mode != SplitMode.SKIP) {
        for (charge in charges) {
            val excluded = resolve(charge.excluded).toSet()
            val payers = people.filterNot { it in excluded }
            if (payers.isEmpty()) continue
            val per = charge.valueCents / payers.size
            val rem = charge.valueCents - per * payers.size
            payers.forEachIndexed { i, name ->
                chargesCents[name] = chargesCents[name]!! + per + if (i == 0) rem else 0L
            }
        }
    }

    return people.map { name ->
        val itemsForPerson = items.filter { item ->
            val owners = if (item.isShared) people else resolve(item.people)
            val effective = if (owners.isEmpty()) people else owners
            name in effective
        }
        val pCents = personalCents[name]!!
        val sCents = sharedCents[name]!!
        val cCents = chargesCents[name]!!
        PersonShare(
            name = name,
            itemsCents = pCents,
            sharedCents = sCents,
            chargesCents = cCents,
            totalCents = pCents + sCents + cCents,
            items = itemsForPerson,
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
