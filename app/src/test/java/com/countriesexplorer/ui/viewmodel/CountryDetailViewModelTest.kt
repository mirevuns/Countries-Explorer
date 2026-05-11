package com.countriesexplorer.ui.viewmodel

import com.countriesexplorer.MainDispatcherRule
import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.ui.state.UiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CountryDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadCountry success emits Success`() = runBlocking(Dispatchers.Main) {
        val c = TestFixtures.country()
        val repo = mockk<CountriesRepository> {
            coEvery { getCountryByCode("TL") } returns c
        }
        val vm = CountryDetailViewModel(repo)
        vm.loadCountry("TL")
        delay(200)
        assertTrue(vm.uiState.value is UiState.Success)
    }

    @Test
    fun `loadCountry null emits Error not Success`() = runBlocking(Dispatchers.Main) {
        val repo = mockk<CountriesRepository> {
            coEvery { getCountryByCode("ZZ") } returns null
        }
        val vm = CountryDetailViewModel(repo)
        vm.loadCountry("ZZ")
        delay(200)
        val s = vm.uiState.value
        assertTrue(s is UiState.Error && (s as UiState.Error).message.contains("не найдена", ignoreCase = true))
    }

    @Test
    fun `loadCountry exception emits Error`() = runBlocking(Dispatchers.Main) {
        val repo = mockk<CountriesRepository> {
            coEvery { getCountryByCode(any()) } throws RuntimeException("boom")
        }
        val vm = CountryDetailViewModel(repo)
        vm.loadCountry("TL")
        delay(200)
        assertTrue(vm.uiState.value is UiState.Error)
    }
}
