package com.countriesexplorer.data.repository

import com.countriesexplorer.data.local.VisitHistoryDao
import com.countriesexplorer.data.local.VisitHistoryEntity
import com.countriesexplorer.data.model.Country
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class VisitHistoryRepository @Inject constructor(
    private val visitHistoryDao: VisitHistoryDao,
    private val profileRepository: ProfileRepository,
    private val journalRepository: JournalRepository
) {
    fun getRecentVisits(limit: Int = VisitHistoryDao.DEFAULT_LIMIT): Flow<List<VisitHistoryEntity>> {
        return profileRepository.activeProfileId.flatMapLatest { profileId ->
            visitHistoryDao.getRecentFlow(profileId, limit)
        }
    }

    suspend fun recordVisit(country: Country) {
        val profileId = profileRepository.currentProfileId()
        visitHistoryDao.insert(VisitHistoryEntity.fromCountry(country, profileId))
        journalRepository.recordVisit(country)
    }

    suspend fun clearHistory() {
        visitHistoryDao.clearForProfile(profileRepository.currentProfileId())
    }
}
