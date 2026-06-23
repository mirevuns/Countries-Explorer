package com.countriesexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collections WHERE profileId = :profileId ORDER BY name ASC")
    fun getCollectionsFlow(profileId: Long): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    suspend fun getCollectionById(id: Long): CollectionEntity?

    @Query("SELECT * FROM collection_countries WHERE collectionId = :collectionId ORDER BY countryName ASC")
    fun getCountriesFlow(collectionId: Long): Flow<List<CollectionCountryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(entity: CollectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountry(entity: CollectionCountryEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollection(id: Long)

    @Query("DELETE FROM collection_countries WHERE collectionId = :collectionId AND countryCode = :countryCode")
    suspend fun removeCountry(collectionId: Long, countryCode: String)

    @Transaction
    suspend fun deleteCollectionWithCountries(id: Long) {
        deleteCountriesInCollection(id)
        deleteCollection(id)
    }

    @Query("DELETE FROM collection_countries WHERE collectionId = :collectionId")
    suspend fun deleteCountriesInCollection(collectionId: Long)

    @Query("DELETE FROM collection_countries WHERE collectionId IN (SELECT id FROM collections WHERE profileId = :profileId)")
    suspend fun deleteCountriesForProfile(profileId: Long)

    @Query("DELETE FROM collections WHERE profileId = :profileId")
    suspend fun deleteCollectionsForProfile(profileId: Long)
}
