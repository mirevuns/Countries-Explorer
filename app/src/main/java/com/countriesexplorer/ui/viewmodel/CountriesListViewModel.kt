package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountriesListViewModel @Inject constructor(
    private val repository: CountriesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<Country>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Country>>> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private var searchJob: Job? = null
    
    init {
        loadCountries()
    }
    
    fun loadCountries() {
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
                val errorMessage = e.message ?: ""
                if (errorMessage.contains("400") || errorMessage.contains("Bad Request")) {
                    _uiState.value = UiState.Empty
                } else {
                    _uiState.value = UiState.Error(errorMessage)
                }
            }
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = null
        
        if (query.isBlank()) {
            _uiState.value = UiState.Loading
            loadCountries()
            return
        }
        
        _uiState.value = UiState.Loading
        
        searchJob = viewModelScope.launch {
            delay(500)
            
            if (_searchQuery.value != query) {
                return@launch
            }
            
            try {
                val countries = repository.searchCountries(query.trim())
                if (_searchQuery.value == query.trim()) {
                    _uiState.value = if (countries.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(countries)
                    }
                }
            } catch (e: Exception) {
                if (_searchQuery.value == query.trim()) {
                    _uiState.value = UiState.Error(
                        e.message ?: "Ошибка поиска. Проверьте подключение к интернету."
                    )
                }
            }
        }
    }
    
    fun refresh() {
        searchJob?.cancel()
        searchJob = null
        _searchQuery.value = ""
        loadCountries()
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        searchJob?.cancel()
        searchJob = null
        _uiState.value = UiState.Loading
        loadCountries()
    }
}
