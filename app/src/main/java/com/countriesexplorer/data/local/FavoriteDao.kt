package com.countriesexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    
    @Query("SELECT code FROM favorites ORDER BY name ASC")
    fun getAllFavoriteCodes(): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)
    
    @Query("DELETE FROM favorites WHERE code = :code")
    suspend fun deleteByCode(code: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE code = :code)")
    suspend fun isFavorite(code: String): Boolean
}
