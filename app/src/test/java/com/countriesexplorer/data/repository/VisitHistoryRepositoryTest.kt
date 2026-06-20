package com.countriesexplorer.data.repository

import com.countriesexplorer.TestFixtures
import com.countriesexplorer.testdoubles.FakeVisitHistoryDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class VisitHistoryRepositoryTest {

    private val dao = FakeVisitHistoryDao()
    private val repository = VisitHistoryRepository(dao)

    @Test
    fun recordVisitAddsEntry() = runBlocking {
        repository.recordVisit(TestFixtures.country())
        assertEquals(1, repository.getRecentVisits().first().size)
    }
}
