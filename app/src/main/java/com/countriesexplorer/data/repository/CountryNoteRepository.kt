package com.countriesexplorer.data.repository

import com.countriesexplorer.data.local.CountryNoteDao
import com.countriesexplorer.data.local.CountryNoteEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CountryNoteRepository @Inject constructor(
    private val countryNoteDao: CountryNoteDao,
    private val profileRepository: ProfileRepository
) {
    fun observeNote(countryCode: String): Flow<CountryNoteEntity?> {
        return profileRepository.activeProfileId.flatMapLatest { profileId ->
            countryNoteDao.observeByCountryCode(profileId, countryCode)
        }
    }

    suspend fun getNote(countryCode: String): CountryNoteEntity? {
        return countryNoteDao.getByCountryCode(profileRepository.currentProfileId(), countryCode)
    }

    suspend fun saveNote(countryCode: String, text: String) {
        val trimmed = text.trim()
        val profileId = profileRepository.currentProfileId()
        if (trimmed.isEmpty()) {
            countryNoteDao.deleteByCountryCode(profileId, countryCode)
            return
        }
        countryNoteDao.upsert(
            CountryNoteEntity(
                profileId = profileId,
                countryCode = countryCode,
                text = trimmed,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNote(countryCode: String) {
        countryNoteDao.deleteByCountryCode(profileRepository.currentProfileId(), countryCode)
    }
}
