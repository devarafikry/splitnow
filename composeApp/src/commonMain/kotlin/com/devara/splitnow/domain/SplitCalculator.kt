package com.devara.splitnow.domain

/**
 * Pure-Kotlin calculator: given items + charges + split mode, return the
 * per-person breakdown. All math in long-cents.
 *
 * Algorithm:
 * 1. Each person's personal subtotal = sum of items where they appear in assignedTo
 *    (split equally if multiple names listed).
 * 2. Shared subtotal = sum of items where assignedTo = SHARED, distributed equally
 *    across all known people.
 * 3. Tax/charges:
 *    - PROPORTIONAL: each person pays charges proportional to their share of subtotal.
 *    - EQUAL: charges split equally across all people.
 *    - SKIP: charges ignored in per-person allocation (still in receipt total).
 * 4. Rounding: distribute integer cents so sum = total. Remainder goes to first person.
 */
fun calculateShares(
    items: List<BillItem>,
    charges: List<Charge>,
    mode: SplitMode,
    people: List<String>,
): List<PersonShare> {
    if (people.isEmpty()) return emptyList()

    val personalItems = people.associateWith { name -> items.filter { !it.isShared && name in it.people } }
    val sharedItems = items.filter { it.isShared }

    val personalCents = people.associateWith { name ->
        personalItems[name]!!.sumOf { item ->
            val splitAmong = item.people.size.coerceAtLeast(1)
            item.priceCents / splitAmong
        }
    }
    // Distribute shared items equally across all people.
    val sharedTotal = sharedItems.sumOf { it.priceCents }
    val sharedPer = if (people.isNotEmpty()) sharedTotal / people.size else 0L
    val sharedRemainder = sharedTotal - sharedPer * people.size

    val subtotalCents = people.sumOf { personalCents[it]!! } + sharedTotal
    val chargesTotal = charges.sumOf { it.valueCents }

    val personChargesCents: Map<String, Long> = when (mode) {
        SplitMode.PROPORTIONAL -> {
            if (subtotalCents == 0L) people.associateWith { 0L }
            else {
                val personSubtotal = people.associateWith { (personalCents[it]!! + sharedPer) }
                people.associateWith { name ->
                    val ratio = personSubtotal[name]!!.toDouble() / subtotalCents.toDouble()
                    (chargesTotal * ratio).toLong()
                }
            }
        }
        SplitMode.EQUAL -> {
            val per = if (people.isNotEmpty()) chargesTotal / people.size else 0L
            people.associateWith { per }
        }
        SplitMode.SKIP -> people.associateWith { 0L }
    }

    return people.mapIndexed { index, name ->
        val sharedAdjust = sharedPer + (if (index == 0) sharedRemainder else 0L)
        val personItems = personalItems[name]!! + sharedItems
        val itemsCents = personalCents[name]!!
        val total = itemsCents + sharedAdjust + personChargesCents[name]!!
        PersonShare(
            name = name,
            itemsCents = itemsCents,
            sharedCents = sharedAdjust,
            chargesCents = personChargesCents[name]!!,
            totalCents = total,
            items = personItems,
        )
    }
}
