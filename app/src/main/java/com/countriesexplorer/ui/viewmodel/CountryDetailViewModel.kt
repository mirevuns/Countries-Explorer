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
class CountryDetailViewModel @Inject constructor(
    private val repository: CountriesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<Country>>(UiState.Loading)
    val uiState: StateFlow<UiState<Country>> = _uiState.asStateFlow()
    
    fun loadCountry(code: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val country = repository.getCountryByCode(code)
                _uiState.value = if (country != null) {
                    UiState.Success(country)
                } else {
                    UiState.Error("Страна не найдена")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}
