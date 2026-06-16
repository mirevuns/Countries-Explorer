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
class CountriesListViewModel @Inject constructor(
    private val repository: CountriesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<List<Country>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Country>>> = _uiState.asStateFlow()

    init {
        loadAllCountries()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val countries = repository.searchCountries(query.trim())
                _uiState.value = if (countries.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(countries)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message?.takeIf { it.isNotBlank() }
                        ?: "Ошибка поиска. Проверьте подключение к интернету."
                )
            }
        }
    }

    fun refresh() {
        if (_searchQuery.value.isBlank()) {
            loadAllCountries()
        } else {
            onSearchQueryChanged(_searchQuery.value)
        }
    }

    private fun loadAllCountries() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val countries = repository.getAllCountries()
                _uiState.value = if (countries.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(countries)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message?.takeIf { it.isNotBlank() }
                        ?: "Неизвестная ошибка"
                )
            }
        }
    }
}
