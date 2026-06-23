package com.countriesexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryCacheDao {

    @Query("SELECT * FROM cached_countries")
    suspend fun getAll(): List<CachedCountryEntity>

    @Query("SELECT * FROM cached_countries WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): CachedCountryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CachedCountryEntity>)

    @Query("DELETE FROM cached_countries")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM cached_countries")
    suspend fun count(): Int
}
