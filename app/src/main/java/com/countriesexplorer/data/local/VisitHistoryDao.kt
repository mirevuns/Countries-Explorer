package com.countriesexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitHistoryDao {

    @Query("SELECT * FROM visit_history ORDER BY visitedAt DESC LIMIT :limit")
    fun getRecentFlow(limit: Int = DEFAULT_LIMIT): Flow<List<VisitHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VisitHistoryEntity)

    @Query("DELETE FROM visit_history WHERE countryCode = :countryCode")
    suspend fun deleteByCountryCode(countryCode: String)

    @Query("DELETE FROM visit_history")
    suspend fun clearAll()

    companion object {
        const val DEFAULT_LIMIT = 50
    }
}
