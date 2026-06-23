package com.countriesexplorer.data.repository

import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.repository.ProfileRepository.Companion.DEFAULT_PROFILE_ID
import com.countriesexplorer.testdoubles.FakeJournalEntryDao
import com.countriesexplorer.testdoubles.FakeVisitHistoryDao
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class VisitHistoryRepositoryTest {

    private val dao = FakeVisitHistoryDao()
    private val journalDao = FakeJournalEntryDao()
    private val profileRepository = mockk<ProfileRepository>(relaxed = true) {
        coEvery { currentProfileId() } returns DEFAULT_PROFILE_ID
        every { activeProfileId } returns flowOf(DEFAULT_PROFILE_ID)
    }
    private val journalRepository = JournalRepository(journalDao, profileRepository)
    private val repository = VisitHistoryRepository(dao, profileRepository, journalRepository)

    @Test
    fun recordVisitAddsEntry() = runBlocking {
        repository.recordVisit(TestFixtures.country())
        assertEquals(1, repository.getRecentVisits().first().size)
    }

    @Test
    fun recordVisitAddsJournalEntry() = runBlocking {
        repository.recordVisit(TestFixtures.country())
        val journal = JournalRepository(journalDao, profileRepository)
        assertEquals(1, journal.entries.first().size)
    }
}
