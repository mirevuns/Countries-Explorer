package com.countriesexplorer.data.repository

import com.countriesexplorer.data.local.VisitHistoryDao
import com.countriesexplorer.data.local.VisitHistoryEntity
import com.countriesexplorer.data.model.Country
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitHistoryRepository @Inject constructor(
    private val visitHistoryDao: VisitHistoryDao
) {
    fun getRecentVisits(limit: Int = VisitHistoryDao.DEFAULT_LIMIT): Flow<List<VisitHistoryEntity>> {
        return visitHistoryDao.getRecentFlow(limit)
    }

    suspend fun recordVisit(country: Country) {
        visitHistoryDao.insert(VisitHistoryEntity.fromCountry(country))
    }

    suspend fun clearHistory() {
        visitHistoryDao.clearAll()
    }
}
