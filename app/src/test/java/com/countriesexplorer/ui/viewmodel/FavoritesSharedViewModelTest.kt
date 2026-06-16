package com.countriesexplorer.ui.viewmodel

import app.cash.turbine.test
import com.countriesexplorer.MainDispatcherRule
import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.local.FavoriteEntity
import com.countriesexplorer.testdoubles.FakeFavoriteDao
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FavoritesSharedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `favoriteEntries emission sequence empty then one`() = runTest(mainDispatcherRule.dispatcher) {
        val dao = FakeFavoriteDao()
        val vm = FavoritesSharedViewModel(dao)
        vm.favoriteEntries.test {
            assertEquals(0, awaitItem().size)
            dao.insert(FavoriteEntity.fromCountry(TestFixtures.country("One", "O1")))
            advanceUntilIdle()
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite with null country removes`() = runTest(mainDispatcherRule.dispatcher) {
        val dao = FakeFavoriteDao()
        val vm = FavoritesSharedViewModel(dao)
        backgroundScope.launch { vm.favorites.collect { } }
        dao.insert(FavoriteEntity.fromCountry(TestFixtures.country("X", "XX")))
        advanceUntilIdle()
        assertTrue(vm.favorites.value.contains("XX"))
        vm.toggleFavorite("XX", null)
        advanceUntilIdle()
        assertTrue(vm.favorites.value.isEmpty())
    }
}
