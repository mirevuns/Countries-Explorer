package com.countriesexplorer.data.repository

import com.countriesexplorer.testdoubles.FakeCountryNoteDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CountryNoteRepositoryTest {

    private val dao = FakeCountryNoteDao()
    private val repository = CountryNoteRepository(dao)

    @Test
    fun saveAndReadNote() = runBlocking {
        repository.saveNote("TL", "My travel plan")
        val note = repository.getNote("TL")
        assertEquals("My travel plan", note?.text)
    }

    @Test
    fun blankNoteDeletesExisting() = runBlocking {
        repository.saveNote("TL", "Draft")
        repository.saveNote("TL", "   ")
        assertNull(repository.getNote("TL"))
    }
}
