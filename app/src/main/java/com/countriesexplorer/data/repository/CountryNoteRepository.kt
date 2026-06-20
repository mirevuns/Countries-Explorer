package com.countriesexplorer.data.repository

import com.countriesexplorer.data.local.CountryNoteDao
import com.countriesexplorer.data.local.CountryNoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryNoteRepository @Inject constructor(
    private val countryNoteDao: CountryNoteDao
) {
    fun observeNote(countryCode: String): Flow<CountryNoteEntity?> {
        return countryNoteDao.observeByCountryCode(countryCode)
    }

    suspend fun getNote(countryCode: String): CountryNoteEntity? {
        return countryNoteDao.getByCountryCode(countryCode)
    }

    suspend fun saveNote(countryCode: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            countryNoteDao.deleteByCountryCode(countryCode)
            return
        }
        countryNoteDao.upsert(
            CountryNoteEntity(
                countryCode = countryCode,
                text = trimmed,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNote(countryCode: String) {
        countryNoteDao.deleteByCountryCode(countryCode)
    }
}
