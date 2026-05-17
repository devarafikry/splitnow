package com.devara.splitnow.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.devara.splitnow.data.entity.BillItemEntity
import com.devara.splitnow.data.entity.ChargeEntity
import com.devara.splitnow.data.entity.PaymentMethodEntity
import com.devara.splitnow.data.entity.SplitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(split: SplitEntity): Long

    @Update
    suspend fun update(split: SplitEntity)

    @Delete
    suspend fun delete(split: SplitEntity)

    @Query("SELECT * FROM splits ORDER BY dateMs DESC")
    fun observeAll(): Flow<List<SplitEntity>>

    @Query("SELECT * FROM splits WHERE id = :id")
    suspend fun get(id: Long): SplitEntity?

    @Query("SELECT COUNT(*) FROM splits WHERE dateMs >= :sinceMs")
    suspend fun countSince(sinceMs: Long): Int

    @Query("SELECT COALESCE(SUM(totalCents),0) FROM splits WHERE dateMs >= :sinceMs")
    suspend fun sumSince(sinceMs: Long): Long
}

@Dao
interface BillItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: BillItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<BillItemEntity>)

    @Delete
    suspend fun delete(item: BillItemEntity)

    @Query("DELETE FROM bill_items WHERE splitId = :splitId")
    suspend fun clearForSplit(splitId: Long)

    @Query("SELECT * FROM bill_items WHERE splitId = :splitId")
    suspend fun forSplit(splitId: Long): List<BillItemEntity>
}

@Dao
interface ChargeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(charge: ChargeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(charges: List<ChargeEntity>)

    @Delete
    suspend fun delete(charge: ChargeEntity)

    @Query("DELETE FROM charges WHERE splitId = :splitId")
    suspend fun clearForSplit(splitId: Long)

    @Query("SELECT * FROM charges WHERE splitId = :splitId")
    suspend fun forSplit(splitId: Long): List<ChargeEntity>
}

@Dao
interface PaymentMethodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(method: PaymentMethodEntity): Long

    @Update
    suspend fun update(method: PaymentMethodEntity)

    @Delete
    suspend fun delete(method: PaymentMethodEntity)

    @Query("SELECT * FROM payment_methods ORDER BY isDefault DESC, id ASC")
    fun observeAll(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): PaymentMethodEntity?

    @Query("UPDATE payment_methods SET isDefault = 0")
    suspend fun clearDefault()

    @Query("SELECT COUNT(*) FROM payment_methods")
    suspend fun count(): Int
}
