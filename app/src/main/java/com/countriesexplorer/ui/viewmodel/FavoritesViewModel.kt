package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: CountriesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<Country>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Country>>> = _uiState.asStateFlow()

    fun loadFavorites(favoriteCodes: Set<String>) {
        if (favoriteCodes.isEmpty()) {
            _uiState.value = UiState.Empty
            return
        }
        
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val countries = favoriteCodes.mapNotNull { code ->
                    repository.getCountryByCode(code)
                }
                _uiState.value = if (countries.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(countries)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}
