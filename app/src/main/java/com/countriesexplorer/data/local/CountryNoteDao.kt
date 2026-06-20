package com.countriesexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryNoteDao {

    @Query("SELECT * FROM country_notes WHERE countryCode = :countryCode LIMIT 1")
    suspend fun getByCountryCode(countryCode: String): CountryNoteEntity?

    @Query("SELECT * FROM country_notes WHERE countryCode = :countryCode LIMIT 1")
    fun observeByCountryCode(countryCode: String): Flow<CountryNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CountryNoteEntity)

    @Query("DELETE FROM country_notes WHERE countryCode = :countryCode")
    suspend fun deleteByCountryCode(countryCode: String)
}
