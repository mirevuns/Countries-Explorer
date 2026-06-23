package com.countriesexplorer.ui.viewmodel

import com.countriesexplorer.MainDispatcherRule
import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.local.FavoriteEntity
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.preferences.ListPreferences
import com.countriesexplorer.data.preferences.ListPreferencesRepository
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.testdoubles.FakeFavoriteDao
import com.countriesexplorer.ui.state.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
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

    private fun TestScope.settleDebounce() {
        advanceTimeBy(600)
        advanceUntilIdle()
    }

    private fun prefsRepo(flow: MutableStateFlow<ListPreferences>): ListPreferencesRepository =
        mockk(relaxed = true) {
            every { listPreferences } returns flow
            coEvery { setShowFavoritesOnly(any()) } coAnswers {
                flow.value = flow.value.copy(showFavoritesOnly = invocation.args[0] as Boolean)
            }
            coEvery { setSortByName(any()) } coAnswers {
                flow.value = flow.value.copy(sortByName = invocation.args[0] as Boolean)
            }
        }

    private fun repoWithGetAll(list: List<Country>): CountriesRepository =
        mockk {
            coEvery { getAllCountries(forceRefresh = any()) } returns list
            coEvery { searchCountries(any()) } returns emptyList()
        }

    @Test
    fun `uiState emission sequence includes Loading then Success`() = runTest(mainDispatcherRule.dispatcher) {
        val c = TestFixtures.country()
        val repo = repoWithGetAll(listOf(c))
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        val emissions = mutableListOf<UiState<List<Country>>>()
        withUiStateActive(vm) {
            val job = backgroundScope.launch { vm.uiState.collect { emissions.add(it) } }
            advanceUntilIdle()
            job.cancel()
            assertTrue(emissions.any { it is UiState.Loading })
            assertTrue(vm.uiState.value is UiState.Success)
            assertTrue((vm.uiState.value as UiState.Success).data.any { it.displayName == c.displayName })
        }
    }

    @Test
    fun `getAllCountries error then refresh yields Success`() = runTest(mainDispatcherRule.dispatcher) {
        val c = TestFixtures.country()
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries(forceRefresh = false) } throws IOException("network")
            coEvery { getAllCountries(forceRefresh = true) } returns listOf(c)
            coEvery { searchCountries(any()) } returns emptyList()
        }
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        withUiStateActive(vm) {
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Error)

            vm.refresh()
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Success)
            coVerify { repo.getAllCountries(forceRefresh = true) }
        }
    }

    @Test
    fun `refresh with network error yields Error`() = runTest(mainDispatcherRule.dispatcher) {
        val c = TestFixtures.country()
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries(forceRefresh = false) } returns listOf(c)
            coEvery { getAllCountries(forceRefresh = true) } throws IOException("network")
            coEvery { searchCountries(any()) } returns emptyList()
        }
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        withUiStateActive(vm) {
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Success)

            vm.refresh()
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Error)
        }
    }

    @Test
    fun `search error then refresh retries search without clearing query`() = runTest(mainDispatcherRule.dispatcher) {
        val c = TestFixtures.country("Found", "FD")
        var searchCalls = 0
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries(forceRefresh = false) } returns listOf(TestFixtures.country())
            coEvery { searchCountries(any()) } coAnswers {
                searchCalls++
                if (searchCalls == 1) throw IOException("search failed")
                listOf(c)
            }
        }
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        withUiStateActive(vm) {
            advanceUntilIdle()
            vm.onSearchQueryChanged("found")
            settleDebounce()
            assertTrue(vm.uiState.value is UiState.Error)
            assertEquals("found", vm.searchQuery.value)

            vm.refresh()
            settleDebounce()
            assertTrue(vm.uiState.value is UiState.Success)
            assertEquals(2, searchCalls)
            assertEquals("found", vm.searchQuery.value)
        }
    }

    @Test
    fun `empty remote list yields Empty not Success with empty list`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = repoWithGetAll(emptyList())
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        withUiStateActive(vm) {
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Empty)
        }
    }

    @Test
    fun `search with no matches yields Empty not Success`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries(forceRefresh = false) } returns listOf(TestFixtures.country())
            coEvery { searchCountries(any()) } returns emptyList()
        }
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        withUiStateActive(vm) {
            advanceUntilIdle()
            vm.onSearchQueryChanged("nope")
            settleDebounce()
            assertTrue(vm.uiState.value is UiState.Empty)
        }
    }

    @Test
    fun `debounce - only last search query triggers search after wait`() = runTest(mainDispatcherRule.dispatcher) {
        var searchCalls = 0
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries(forceRefresh = false) } returns listOf(TestFixtures.country("Base", "BS"))
            coEvery { searchCountries(any()) } coAnswers {
                searchCalls++
                listOf(TestFixtures.country("Found", "FD"))
            }
        }
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        withUiStateActive(vm) {
            advanceUntilIdle()
            vm.onSearchQueryChanged("a")
            vm.onSearchQueryChanged("ab")
            vm.onSearchQueryChanged("abc")
            settleDebounce()
            assertEquals(1, searchCalls)
            assertTrue(vm.uiState.value is UiState.Success)
        }
    }

    @Test
    fun `favorites filter from combine updates without second getAllCountries`() = runTest(mainDispatcherRule.dispatcher) {
        val alpha = TestFixtures.country("Alpha", "AA")
        val beta = TestFixtures.country("Beta", "BB")
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries(forceRefresh = false) } returns listOf(alpha, beta)
            coEvery { searchCountries(any()) } returns emptyList()
        }
        val dao = FakeFavoriteDao()
        dao.insert(FavoriteEntity.fromCountry(alpha))
        val prefsFlow = MutableStateFlow(ListPreferences(showFavoritesOnly = false))
        val vm = CountriesListViewModel(repo, dao, prefsRepo(prefsFlow))
        withUiStateActive(vm) {
            advanceUntilIdle()
            val s0 = vm.uiState.value
            assertTrue("expected Success got $s0", s0 is UiState.Success)
            assertEquals(2, (s0 as UiState.Success).data.size)

            vm.setShowFavoritesOnly(true)
            advanceUntilIdle()
            val s1 = vm.uiState.value
            assertTrue("expected Success got $s1", s1 is UiState.Success)
            assertEquals(1, (s1 as UiState.Success).data.size)
            assertEquals("Alpha", s1.data.first().displayName)

            coVerify(exactly = 1) { repo.getAllCountries(forceRefresh = false) }
        }
    }
}
