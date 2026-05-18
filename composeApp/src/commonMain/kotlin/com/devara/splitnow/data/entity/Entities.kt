package com.devara.splitnow.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.devara.splitnow.domain.BillItem
import com.devara.splitnow.domain.Charge
import com.devara.splitnow.domain.ChargeType
import com.devara.splitnow.domain.PaymentKind
import com.devara.splitnow.domain.PaymentMethod
import com.devara.splitnow.domain.Split
import com.devara.splitnow.domain.SplitMode

@Entity(tableName = "splits")
data class SplitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val restaurantName: String,
    val dateMs: Long,
    val currencyCode: String,
    val totalCents: Long,
    val splitMode: String,
    val paymentMethodId: Long?,
    val notes: String?,
) {
    fun toDomain() = Split(
        id, restaurantName, dateMs, currencyCode, totalCents,
        runCatching { SplitMode.valueOf(splitMode) }.getOrDefault(SplitMode.EQUAL),
        paymentMethodId, notes,
    )
    companion object {
        fun fromDomain(s: Split) = SplitEntity(
            s.id, s.restaurantName, s.dateMs, s.currencyCode, s.totalCents,
            s.splitMode.name, s.paymentMethodId, s.notes,
        )
    }
}

@Entity(
    tableName = "bill_items",
    foreignKeys = [ForeignKey(
        entity = SplitEntity::class,
        parentColumns = ["id"],
        childColumns = ["splitId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("splitId")],
)
data class BillItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val splitId: Long,
    val name: String,
    val priceCents: Long,
    val assignedTo: String,
) {
    fun toDomain() = BillItem(id, splitId, name, priceCents, assignedTo)
    companion object {
        fun fromDomain(b: BillItem) =
            BillItemEntity(b.id, b.splitId, b.name, b.priceCents, b.assignedTo)
    }
}

@Entity(
    tableName = "charges",
    foreignKeys = [ForeignKey(
        entity = SplitEntity::class,
        parentColumns = ["id"],
        childColumns = ["splitId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("splitId")],
)
data class ChargeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val splitId: Long,
    val label: String,
    val type: String,
    val rate: Double,
    val valueCents: Long,
    val excludeFromNames: String = "",
) {
    fun toDomain() = Charge(
        id, splitId, label,
        runCatching { ChargeType.valueOf(type) }.getOrDefault(ChargeType.FIXED),
        rate, valueCents, excludeFromNames,
    )
    companion object {
        fun fromDomain(c: Charge) =
            ChargeEntity(c.id, c.splitId, c.label, c.type.name, c.rate, c.valueCents, c.excludeFromNames)
    }
}

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val name: String,
    val account: String,
    val holder: String,
    val qrUri: String?,
    val isDefault: Boolean,
) {
    fun toDomain() = PaymentMethod(
        id,
        runCatching { PaymentKind.valueOf(kind) }.getOrDefault(PaymentKind.BANK),
        name, account, holder, qrUri, isDefault,
    )
    companion object {
        fun fromDomain(p: PaymentMethod) =
            PaymentMethodEntity(p.id, p.kind.name, p.name, p.account, p.holder, p.qrUri, p.isDefault)
    }
}
