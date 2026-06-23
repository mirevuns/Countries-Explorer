package com.countriesexplorer.data.repository

import com.countriesexplorer.data.local.JournalEntryDao
import com.countriesexplorer.data.local.JournalEntryEntity
import com.countriesexplorer.data.model.Country
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class JournalRepository @Inject constructor(
    private val journalEntryDao: JournalEntryDao,
    private val profileRepository: ProfileRepository
) {
    val entries: Flow<List<JournalEntryEntity>> = profileRepository.activeProfileId
        .flatMapLatest { profileId -> journalEntryDao.getByProfileFlow(profileId) }

    suspend fun recordVisit(country: Country) {
        val profileId = profileRepository.currentProfileId()
        journalEntryDao.insert(JournalEntryEntity.fromVisit(country, profileId))
    }

    suspend fun addManualEntry(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val profileId = profileRepository.currentProfileId()
        journalEntryDao.insert(JournalEntryEntity.manual(profileId, trimmed))
    }

    suspend fun deleteEntry(id: Long) {
        journalEntryDao.deleteByIdForProfile(id, profileRepository.currentProfileId())
    }

    suspend fun clearEntries() {
        journalEntryDao.clearForProfile(profileRepository.currentProfileId())
    }
}
