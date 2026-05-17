package com.devara.splitnow.data

import com.devara.splitnow.data.entity.BillItemEntity
import com.devara.splitnow.data.entity.ChargeEntity
import com.devara.splitnow.data.entity.PaymentMethodEntity
import com.devara.splitnow.data.entity.SplitEntity
import com.devara.splitnow.domain.BillItem
import com.devara.splitnow.domain.Charge
import com.devara.splitnow.domain.PaymentMethod
import com.devara.splitnow.domain.Split
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SplitRepository(private val db: SplitNowDatabase) {

    fun observeSplits(): Flow<List<Split>> =
        db.splitDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun loadSplitDetail(id: Long): Triple<Split, List<BillItem>, List<Charge>>? {
        val split = db.splitDao().get(id)?.toDomain() ?: return null
        val items = db.billItemDao().forSplit(id).map { it.toDomain() }
        val charges = db.chargeDao().forSplit(id).map { it.toDomain() }
        return Triple(split, items, charges)
    }

    suspend fun saveSplit(split: Split, items: List<BillItem>, charges: List<Charge>): Long {
        val id = db.splitDao().upsert(SplitEntity.fromDomain(split))
        // Replace items + charges atomically.
        db.billItemDao().clearForSplit(id)
        db.chargeDao().clearForSplit(id)
        db.billItemDao().upsertAll(items.map { BillItemEntity.fromDomain(it.copy(splitId = id)) })
        db.chargeDao().upsertAll(charges.map { ChargeEntity.fromDomain(it.copy(splitId = id)) })
        return id
    }

    suspend fun countSplitsSince(ms: Long): Int = db.splitDao().countSince(ms)
    suspend fun sumSplitsSince(ms: Long): Long = db.splitDao().sumSince(ms)

    fun observePaymentMethods(): Flow<List<PaymentMethod>> =
        db.paymentMethodDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun getDefaultPaymentMethod(): PaymentMethod? =
        db.paymentMethodDao().getDefault()?.toDomain()

    suspend fun savePaymentMethod(method: PaymentMethod): Long {
        if (method.isDefault) db.paymentMethodDao().clearDefault()
        return db.paymentMethodDao().upsert(PaymentMethodEntity.fromDomain(method))
    }

    suspend fun deletePaymentMethod(method: PaymentMethod) {
        db.paymentMethodDao().delete(PaymentMethodEntity.fromDomain(method))
    }

    suspend fun paymentMethodCount(): Int = db.paymentMethodDao().count()
}
