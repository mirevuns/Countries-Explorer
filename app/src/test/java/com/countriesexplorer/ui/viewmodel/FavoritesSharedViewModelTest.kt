package com.countriesexplorer.ui.viewmodel

import com.countriesexplorer.MainDispatcherRule
import com.countriesexplorer.data.repository.FavoriteRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
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

    private fun favoriteRepository(
        codes: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    ): FavoriteRepository = mockk {
        every { favoriteCodes } returns codes
        every { favorites } returns MutableStateFlow(emptyList())
        coEvery { toggleFavorite(any()) } coAnswers {
            val country = firstArg<com.countriesexplorer.data.model.Country>()
            codes.value = codes.value + country.countryCode
        }
        coEvery { removeFavorite(any()) } coAnswers {
            val code = firstArg<String>()
            codes.value = codes.value - code
        }
    }

    @Test
    fun `favorites flow emits codes after insert`() = runTest(mainDispatcherRule.dispatcher) {
        val codes = MutableStateFlow(setOf("O1"))
        val vm = FavoritesSharedViewModel(favoriteRepository(codes))
        backgroundScope.launch { vm.favorites.collect { } }
        advanceUntilIdle()
        assertTrue(vm.favorites.value.contains("O1"))
    }

    @Test
    fun `toggleFavorite with null country removes`() = runTest(mainDispatcherRule.dispatcher) {
        val codes = MutableStateFlow(setOf("XX"))
        val repo = favoriteRepository(codes)
        val vm = FavoritesSharedViewModel(repo)
        backgroundScope.launch { vm.favorites.collect { } }
        advanceUntilIdle()
        assertEquals(setOf("XX"), vm.favorites.value)
        vm.toggleFavorite("XX", null)
        advanceUntilIdle()
        assertTrue(vm.favorites.value.isEmpty())
    }
}
