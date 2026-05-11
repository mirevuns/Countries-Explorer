package com.countriesexplorer.ui.viewmodel

import app.cash.turbine.test
import com.countriesexplorer.MainDispatcherRule
import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.local.FavoriteEntity
import com.countriesexplorer.testdoubles.FakeFavoriteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FavoritesSharedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `favoriteEntries emission sequence empty then one`() = runBlocking(Dispatchers.Main) {
        val dao = FakeFavoriteDao()
        val vm = FavoritesSharedViewModel(dao)
        vm.favoriteEntries.test {
            assertEquals(0, awaitItem().size)
            dao.insert(FavoriteEntity.fromCountry(TestFixtures.country("One", "O1")))
            delay(200)
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite with null country removes`() = runBlocking(Dispatchers.Main) {
        val dao = FakeFavoriteDao()
        val vm = FavoritesSharedViewModel(dao)
        val sub = launch { vm.favorites.collect { } }
        try {
            dao.insert(FavoriteEntity.fromCountry(TestFixtures.country("X", "XX")))
            var waited = 0L
            while (!vm.favorites.value.contains("XX") && waited < 10_000L) {
                delay(50)
                waited += 50
            }
            assertTrue(vm.favorites.value.contains("XX"))
            vm.toggleFavorite("XX", null)
            waited = 0L
            while (vm.favorites.value.isNotEmpty() && waited < 10_000L) {
                delay(50)
                waited += 50
            }
            assertTrue(vm.favorites.value.isEmpty())
        } finally {
            sub.cancel()
        }
    }
}
