package com.countriesexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {

    @Query("SELECT * FROM journal_entries WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun getByProfileFlow(profileId: Long): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id AND profileId = :profileId")
    suspend fun deleteByIdForProfile(id: Long, profileId: Long)

    @Query("DELETE FROM journal_entries WHERE profileId = :profileId")
    suspend fun clearForProfile(profileId: Long)
}
