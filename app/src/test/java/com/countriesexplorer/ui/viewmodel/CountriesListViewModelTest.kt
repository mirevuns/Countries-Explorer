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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class CountriesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private suspend fun <T> CoroutineScope.withUiStateActive(
        vm: CountriesListViewModel,
        block: suspend CoroutineScope.() -> T
    ): T {
        val sub = launch { vm.uiState.collect { } }
        try {
            return block()
        } finally {
            sub.cancel()
        }
    }

    private suspend fun settle() {
        delay(3500)
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 10_000,
        intervalMs: Long = 50,
        predicate: () -> Boolean
    ) {
        var waited = 0L
        while (!predicate() && waited < timeoutMs) {
            delay(intervalMs)
            waited += intervalMs
        }
        assertTrue(predicate())
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
            coEvery { getAllCountries() } returns list
            coEvery { searchCountries(any()) } returns emptyList()
        }

    @Test
    fun `uiState emission sequence includes Loading then Success`() = runBlocking(Dispatchers.Main) {
        val c = TestFixtures.country()
        val repo = repoWithGetAll(listOf(c))
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        val emissions = mutableListOf<UiState<List<Country>>>()
        val job = launch {
            vm.uiState.collect { emissions.add(it) }
        }
        settle()
        job.cancel()
        assertTrue(emissions.any { it is UiState.Loading })
        assertTrue(emissions.last() is UiState.Success)
        assertTrue((emissions.last() as UiState.Success).data.any { it.displayName == c.displayName })
    }

    @Test
    fun `getAllCountries error then refresh yields Success - contract`() = runBlocking(Dispatchers.Main) {
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
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        withUiStateActive(vm) {
            settle()
            assertTrue(vm.uiState.value is UiState.Error)

            vm.refresh()
            settle()
            assertTrue(vm.uiState.value is UiState.Success)
            assertEquals(2, call)
        }
    }

    @Test
    fun `empty remote list yields Empty not Success with empty list`() = runBlocking(Dispatchers.Main) {
        val repo = repoWithGetAll(emptyList())
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        withUiStateActive(vm) {
            settle()
            assertTrue(vm.uiState.value is UiState.Empty)
        }
    }

    @Test
    fun `search with no matches yields Empty not Success`() = runBlocking(Dispatchers.Main) {
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries() } returns listOf(TestFixtures.country())
            coEvery { searchCountries(any()) } returns emptyList()
        }
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        withUiStateActive(vm) {
            settle()
            vm.onSearchQueryChanged("nope")
            awaitUntil { vm.uiState.value is UiState.Empty }
        }
    }

    @Test
    fun `debounce - only last search query triggers search after wait`() = runBlocking(Dispatchers.Main) {
        var searchCalls = 0
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries() } returns listOf(TestFixtures.country("Base", "BS"))
            coEvery { searchCountries(any()) } coAnswers {
                searchCalls++
                listOf(TestFixtures.country("Found", "FD"))
            }
        }
        val vm = CountriesListViewModel(repo, FakeFavoriteDao(), prefsRepo(MutableStateFlow(ListPreferences())))
        withUiStateActive(vm) {
            settle()
            vm.onSearchQueryChanged("a")
            vm.onSearchQueryChanged("ab")
            vm.onSearchQueryChanged("abc")
            awaitUntil { searchCalls == 1 }
            awaitUntil { vm.uiState.value is UiState.Success }
        }
    }

    @Test
    fun `favorites filter from combine updates without second getAllCountries`() = runBlocking(Dispatchers.Main) {
        val alpha = TestFixtures.country("Alpha", "AA")
        val beta = TestFixtures.country("Beta", "BB")
        val repo = mockk<CountriesRepository> {
            coEvery { getAllCountries() } returns listOf(alpha, beta)
            coEvery { searchCountries(any()) } returns emptyList()
        }
        val dao = FakeFavoriteDao()
        dao.insert(FavoriteEntity.fromCountry(alpha))
        val prefsFlow = MutableStateFlow(ListPreferences(showFavoritesOnly = false))
        val vm = CountriesListViewModel(repo, dao, prefsRepo(prefsFlow))
        withUiStateActive(vm) {
            settle()
            val s0 = vm.uiState.value
            assertTrue("expected Success got $s0", s0 is UiState.Success)
            assertEquals(2, (s0 as UiState.Success).data.size)

            vm.setShowFavoritesOnly(true)
            settle()
            val s1 = vm.uiState.value
            assertTrue("expected Success got $s1", s1 is UiState.Success)
            assertEquals(1, (s1 as UiState.Success).data.size)
            assertEquals("Alpha", s1.data.first().displayName)

            coVerify(exactly = 1) { repo.getAllCountries() }
        }
    }
}
