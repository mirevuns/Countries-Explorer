package com.countriesexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites WHERE profileId = :profileId ORDER BY name ASC")
    fun getAllFavoritesFlow(profileId: Long): Flow<List<FavoriteEntity>>

    @Query("SELECT code FROM favorites WHERE profileId = :profileId ORDER BY name ASC")
    fun getAllFavoriteCodes(profileId: Long): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE profileId = :profileId AND code = :code")
    suspend fun deleteByCode(profileId: Long, code: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE profileId = :profileId AND code = :code)")
    suspend fun isFavorite(profileId: Long, code: String): Boolean

    @Query("DELETE FROM favorites WHERE profileId = :profileId")
    suspend fun deleteForProfile(profileId: Long)

    @Transaction
    suspend fun toggleFavorite(entity: FavoriteEntity) {
        if (isFavorite(entity.profileId, entity.code)) {
            deleteByCode(entity.profileId, entity.code)
        } else {
            insert(entity)
        }
    }
}
