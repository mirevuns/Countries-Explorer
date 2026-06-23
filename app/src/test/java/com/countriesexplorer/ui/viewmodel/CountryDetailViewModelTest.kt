package com.countriesexplorer.ui.viewmodel

import com.countriesexplorer.MainDispatcherRule
import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.repository.CollectionRepository
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.data.repository.CountryLoadResult
import com.countriesexplorer.data.repository.CountryNoteRepository
import com.countriesexplorer.data.repository.CountryNotFoundException
import com.countriesexplorer.ui.state.UiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class CountryDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun noteRepo(): CountryNoteRepository = mockk(relaxed = true) {
        coEvery { getNote(any()) } returns null
        every { observeNote(any()) } returns flowOf(null)
    }

    private fun collectionRepo(): CollectionRepository = mockk(relaxed = true) {
        every { collections } returns flowOf(emptyList())
    }

    @Test
    fun `loadCountry success emits Success`() = runTest(mainDispatcherRule.dispatcher) {
        val c = TestFixtures.country()
        val repo = mockk<CountriesRepository> {
            coEvery { getCountryByCode("TL") } returns CountryLoadResult(c, isStale = false, isOffline = false)
        }
        val vm = CountryDetailViewModel(repo, noteRepo(), collectionRepo())
        vm.loadCountry("TL")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is UiState.Success)
    }

    @Test
    fun `loadCountry not found emits non-retryable Error`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = mockk<CountriesRepository> {
            coEvery { getCountryByCode("ZZ") } throws CountryNotFoundException("ZZ")
        }
        val vm = CountryDetailViewModel(repo, noteRepo(), collectionRepo())
        vm.loadCountry("ZZ")
        advanceUntilIdle()
        val s = vm.uiState.value as UiState.Error
        assertTrue(s.message.contains("не найдена", ignoreCase = true))
        assertFalse(s.retryable)
    }

    @Test
    fun `loadCountry network error emits retryable Error`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = mockk<CountriesRepository> {
            coEvery { getCountryByCode(any()) } throws IOException("network")
        }
        val vm = CountryDetailViewModel(repo, noteRepo(), collectionRepo())
        vm.loadCountry("TL")
        advanceUntilIdle()
        val s = vm.uiState.value as UiState.Error
        assertTrue(s.retryable)
    }
}
