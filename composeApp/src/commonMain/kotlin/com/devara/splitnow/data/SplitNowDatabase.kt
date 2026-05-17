package com.devara.splitnow.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.devara.splitnow.data.dao.BillItemDao
import com.devara.splitnow.data.dao.ChargeDao
import com.devara.splitnow.data.dao.PaymentMethodDao
import com.devara.splitnow.data.dao.SplitDao
import com.devara.splitnow.data.entity.BillItemEntity
import com.devara.splitnow.data.entity.ChargeEntity
import com.devara.splitnow.data.entity.PaymentMethodEntity
import com.devara.splitnow.data.entity.SplitEntity

@Database(
    entities = [
        SplitEntity::class,
        BillItemEntity::class,
        ChargeEntity::class,
        PaymentMethodEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(SplitNowDbConstructor::class)
abstract class SplitNowDatabase : RoomDatabase() {
    abstract fun splitDao(): SplitDao
    abstract fun billItemDao(): BillItemDao
    abstract fun chargeDao(): ChargeDao
    abstract fun paymentMethodDao(): PaymentMethodDao
}

// expect/actual is required for Room KMP. KSP generates the actual impl
// at build time on each platform — no hand-written actual file needed.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object SplitNowDbConstructor : RoomDatabaseConstructor<SplitNowDatabase> {
    override fun initialize(): SplitNowDatabase
}
