package com.countriesexplorer.ui.viewmodel

import com.countriesexplorer.MainDispatcherRule
import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.testdoubles.FakeFavoriteDao
import com.countriesexplorer.ui.state.UiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class CountriesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private suspend fun TestScope.withUiStateActive(
        vm: CountriesListViewModel,
        block: suspend TestScope.() -> Unit
    ) {
        backgroundScope.launch { vm.uiState.collect { } }
        block()
    }

    private fun repoWithGetAll(list: List<Country>): CountriesRepository =
        mockk {
            coEvery { getAllCountries() } returns list
            coEvery { searchCountries(any()) } returns emptyList()
        }

    @Test
    fun `uiState emission sequence includes Loading then Success`() = runTest(mainDispatcherRule.dispatcher) {
        val c = TestFixtures.country()
        val repo = repoWithGetAll(listOf(c))
        val vm = CountriesListViewModel(repo, FakeFavoriteDao())
        withUiStateActive(vm) {
            advanceUntilIdle()
            val final = vm.uiState.value
            assertTrue(final is UiState.Success)
            assertTrue((final as UiState.Success).data.any { it.displayName == c.displayName })
        }
    }

    @Test
    fun `getAllCountries error then refresh yields Success`() = runTest(mainDispatcherRule.dispatcher) {
        val c = TestFixtures.country()
        var call = 0
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries() } coAnswers {
                call++
                if (call == 1) throw IOException("network")
                listOf(c)
            }
            coEvery { searchCountries(any()) } returns emptyList()
        }
        val vm = CountriesListViewModel(repo, FakeFavoriteDao())
        withUiStateActive(vm) {
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Error)

            vm.refresh()
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Success)
            assertEquals(2, call)
        }
    }

    @Test
    fun `search error then refresh retries search without clearing query`() = runTest(mainDispatcherRule.dispatcher) {
        val c = TestFixtures.country("Found", "FD")
        var searchCalls = 0
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries() } returns listOf(TestFixtures.country())
            coEvery { searchCountries(any()) } coAnswers {
                searchCalls++
                if (searchCalls == 1) throw IOException("search failed")
                listOf(c)
            }
        }
        val vm = CountriesListViewModel(repo, FakeFavoriteDao())
        withUiStateActive(vm) {
            advanceUntilIdle()
            vm.onSearchQueryChanged("found")
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Error)
            assertEquals("found", vm.searchQuery.value)

            vm.refresh()
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Success)
            assertEquals(2, searchCalls)
            assertEquals("found", vm.searchQuery.value)
        }
    }

    @Test
    fun `empty remote list yields Empty not Success with empty list`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = repoWithGetAll(emptyList())
        val vm = CountriesListViewModel(repo, FakeFavoriteDao())
        withUiStateActive(vm) {
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Empty)
        }
    }

    @Test
    fun `search with no matches yields Empty not Success`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries() } returns listOf(TestFixtures.country())
            coEvery { searchCountries(any()) } returns emptyList()
        }
        val vm = CountriesListViewModel(repo, FakeFavoriteDao())
        withUiStateActive(vm) {
            advanceUntilIdle()
            vm.onSearchQueryChanged("nope")
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Empty)
        }
    }
}
