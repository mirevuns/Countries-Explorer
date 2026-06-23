package com.countriesexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryNoteDao {

    @Query("SELECT * FROM country_notes WHERE profileId = :profileId AND countryCode = :countryCode LIMIT 1")
    suspend fun getByCountryCode(profileId: Long, countryCode: String): CountryNoteEntity?

    @Query("SELECT * FROM country_notes WHERE profileId = :profileId AND countryCode = :countryCode LIMIT 1")
    fun observeByCountryCode(profileId: Long, countryCode: String): Flow<CountryNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CountryNoteEntity)

    @Query("DELETE FROM country_notes WHERE profileId = :profileId AND countryCode = :countryCode")
    suspend fun deleteByCountryCode(profileId: Long, countryCode: String)

    @Query("DELETE FROM country_notes WHERE profileId = :profileId")
    suspend fun deleteForProfile(profileId: Long)
}
