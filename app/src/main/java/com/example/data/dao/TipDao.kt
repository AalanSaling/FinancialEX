package com.example.data.dao

import androidx.room.*
import com.example.data.model.FinancialTipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TipDao {
    @Query("SELECT * FROM tips ORDER BY id ASC")
    fun getAllTips(): Flow<List<FinancialTipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: FinancialTipEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tips: List<FinancialTipEntity>)

    @Update
    suspend fun updateTip(tip: FinancialTipEntity)
}
