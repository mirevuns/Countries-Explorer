package com.countriesexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY name ASC")
    fun getAllFavoritesFlow(): Flow<List<FavoriteEntity>>

    @Query("SELECT code FROM favorites ORDER BY name ASC")
    fun getAllFavoriteCodes(): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)
    
    @Query("DELETE FROM favorites WHERE code = :code")
    suspend fun deleteByCode(code: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE code = :code)")
    suspend fun isFavorite(code: String): Boolean

    @Transaction
    suspend fun toggleFavorite(entity: FavoriteEntity) {
        if (isFavorite(entity.code)) {
            deleteByCode(entity.code)
        } else {
            insert(entity)
        }
    }
}
