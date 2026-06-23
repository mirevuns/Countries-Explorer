package com.countriesexplorer.data.repository

import com.countriesexplorer.data.repository.ProfileRepository.Companion.DEFAULT_PROFILE_ID
import com.countriesexplorer.testdoubles.FakeCountryNoteDao
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CountryNoteRepositoryTest {

    private val dao = FakeCountryNoteDao()
    private val profileRepository = mockk<ProfileRepository>(relaxed = true) {
        coEvery { currentProfileId() } returns DEFAULT_PROFILE_ID
        every { activeProfileId } returns flowOf(DEFAULT_PROFILE_ID)
    }
    private val repository = CountryNoteRepository(dao, profileRepository)

    @Test
    fun saveAndReadNote() = runBlocking {
        repository.saveNote("TL", "My travel plan")
        val note = repository.getNote("TL")
        assertEquals("My travel plan", note?.text)
        assertEquals(DEFAULT_PROFILE_ID, note?.profileId)
    }

    @Test
    fun blankNoteDeletesExisting() = runBlocking {
        repository.saveNote("TL", "Draft")
        repository.saveNote("TL", "   ")
        assertNull(repository.getNote("TL"))
    }
}
