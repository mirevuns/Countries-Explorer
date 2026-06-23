package com.countriesexplorer.ui.viewmodel

import com.countriesexplorer.MainDispatcherRule
import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.local.FavoriteEntity
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.preferences.ListPreferences
import com.countriesexplorer.data.preferences.ListPreferencesRepository
import com.countriesexplorer.data.repository.CountriesLoadResult
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.data.repository.FavoriteRepository
import com.countriesexplorer.data.repository.ProfileRepository
import com.countriesexplorer.ui.state.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
            coEvery { setSortByName(any()) } coAnswers {
                flow.value = flow.value.copy(sortByName = invocation.args[0] as Boolean)
            }
        }

    private fun profileRepo(): ProfileRepository = mockk(relaxed = true) {
        every { activeProfile } returns flowOf(null)
    }

    private fun favoriteRepo(codes: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())): FavoriteRepository =
        mockk {
            every { favoriteCodes } returns codes
        }

    private fun loadResult(
        list: List<Country>,
        isStale: Boolean = false,
        isOffline: Boolean = false
    ) = CountriesLoadResult(
        countries = list,
        isStale = isStale,
        lastSyncAt = null,
        isOffline = isOffline
    )

    private fun repoWithGetAll(list: List<Country>): CountriesRepository =
        mockk {
            coEvery { getAllCountries(any()) } returns loadResult(list)
            coEvery { searchCountries(any()) } returns loadResult(emptyList())
        }

    @Test
    fun `uiState emission sequence includes Loading then Success`() = runTest(mainDispatcherRule.dispatcher) {
        val c = TestFixtures.country()
        val repo = repoWithGetAll(listOf(c))
        val vm = CountriesListViewModel(repo, favoriteRepo(), prefsRepo(MutableStateFlow(ListPreferences())), profileRepo())
        val emissions = mutableListOf<UiState<List<Country>>>()
        val job = backgroundScope.launch { vm.uiState.collect { emissions.add(it) } }
        advanceUntilIdle()
        job.cancel()
        assertTrue(emissions.any { it is UiState.Loading })
        assertTrue(emissions.last() is UiState.Success)
        assertTrue((emissions.last() as UiState.Success).data.any { it.displayName == c.displayName })
    }

    @Test
    fun `getAllCountries error then refresh yields Success`() = runTest(mainDispatcherRule.dispatcher) {
        val c = TestFixtures.country()
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries(false) } throws IOException("network")
            coEvery { getAllCountries(true) } returns loadResult(listOf(c))
            coEvery { searchCountries(any()) } returns loadResult(emptyList())
        }
        val vm = CountriesListViewModel(repo, favoriteRepo(), prefsRepo(MutableStateFlow(ListPreferences())), profileRepo())
        withUiStateActive(vm) {
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Error)
            vm.refresh()
            advanceUntilIdle()
            assertTrue(vm.uiState.value is UiState.Success)
        }
    }

    @Test
    fun `search query debounced triggers searchCountries`() = runTest(mainDispatcherRule.dispatcher) {
        val c = TestFixtures.country("Searchland", "SL")
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries(any()) } returns loadResult(listOf(c))
            coEvery { searchCountries("sea") } returns loadResult(listOf(c))
        }
        val vm = CountriesListViewModel(repo, favoriteRepo(), prefsRepo(MutableStateFlow(ListPreferences())), profileRepo())
        withUiStateActive(vm) {
            advanceUntilIdle()
            vm.onSearchQueryChanged("sea")
            settleDebounce()
            assertTrue(vm.uiState.value is UiState.Success)
            coVerify { repo.searchCountries("sea") }
        }
    }

    @Test
    fun `search error shows Error state`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries(any()) } returns loadResult(listOf(TestFixtures.country()))
            coEvery { searchCountries("x") } throws IOException("search failed")
        }
        val vm = CountriesListViewModel(repo, favoriteRepo(), prefsRepo(MutableStateFlow(ListPreferences())), profileRepo())
        withUiStateActive(vm) {
            advanceUntilIdle()
            vm.onSearchQueryChanged("x")
            settleDebounce()
            assertTrue(vm.uiState.value is UiState.Error)
        }
    }

    @Test
    fun `refresh with active search increments retry without getAllCountries force`() = runTest(mainDispatcherRule.dispatcher) {
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries(any()) } returns loadResult(listOf(TestFixtures.country()))
            coEvery { searchCountries("q") } throws IOException("fail") andThen loadResult(listOf(TestFixtures.country()))
        }
        val vm = CountriesListViewModel(repo, favoriteRepo(), prefsRepo(MutableStateFlow(ListPreferences())), profileRepo())
        withUiStateActive(vm) {
            advanceUntilIdle()
            vm.onSearchQueryChanged("q")
            settleDebounce()
            vm.refresh()
            settleDebounce()
            assertTrue(vm.uiState.value is UiState.Success)
        }
    }

    @Test
    fun `sort by population orders list descending`() = runTest(mainDispatcherRule.dispatcher) {
        val low = TestFixtures.country("Low", "LO").copy(population = 100)
        val high = TestFixtures.country("High", "HI").copy(population = 9_000_000)
        val repo = repoWithGetAll(listOf(low, high))
        val prefsFlow = MutableStateFlow(ListPreferences(sortByName = false))
        val vm = CountriesListViewModel(repo, favoriteRepo(), prefsRepo(prefsFlow), profileRepo())
        withUiStateActive(vm) {
            advanceUntilIdle()
            val data = (vm.uiState.value as UiState.Success).data
            assertEquals("High", data.first().displayName)
        }
    }

    @Test
    fun `favorites filter from combine updates without second getAllCountries`() = runTest(mainDispatcherRule.dispatcher) {
        val alpha = TestFixtures.country("Alpha", "AA")
        val beta = TestFixtures.country("Beta", "BB")
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries(any()) } returns loadResult(listOf(alpha, beta))
            coEvery { searchCountries(any()) } returns loadResult(emptyList())
        }
        val favCodes = MutableStateFlow(setOf("AA"))
        val vm = CountriesListViewModel(
            repo,
            favoriteRepo(favCodes),
            prefsRepo(MutableStateFlow(ListPreferences(showFavoritesOnly = false))),
            profileRepo()
        )
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

            coVerify(exactly = 1) { repo.getAllCountries(any()) }
        }
    }
}
