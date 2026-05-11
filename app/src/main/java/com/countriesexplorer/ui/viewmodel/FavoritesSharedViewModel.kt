package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.local.FavoriteDao
import com.countriesexplorer.data.local.FavoriteEntity
import com.countriesexplorer.data.model.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesSharedViewModel @Inject constructor(
    private val favoriteDao: FavoriteDao
) : ViewModel() {

    val favorites: StateFlow<Set<String>> = favoriteDao.getAllFavoriteCodes()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val favoriteEntries: StateFlow<List<FavoriteEntity>> = favoriteDao.getAllFavoritesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleFavorite(code: String, country: Country? = null) {
        viewModelScope.launch {
            val isFav = favoriteDao.isFavorite(code)
            if (isFav) {
                favoriteDao.deleteByCode(code)
            } else if (country != null) {
                favoriteDao.insert(FavoriteEntity.fromCountry(country))
            }
        }
    }
}
