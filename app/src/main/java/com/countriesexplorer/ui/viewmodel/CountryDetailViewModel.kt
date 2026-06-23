package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.data.repository.CollectionRepository
import com.countriesexplorer.data.repository.CountryNoteRepository
import com.countriesexplorer.data.repository.CountryNotFoundException
import com.countriesexplorer.data.local.CollectionEntity
import com.countriesexplorer.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CountryDetailViewModel @Inject constructor(
    private val repository: CountriesRepository,
    private val countryNoteRepository: CountryNoteRepository,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _countryCode = MutableStateFlow("")
    private val _uiState = MutableStateFlow<UiState<com.countriesexplorer.data.model.Country>>(UiState.Loading)
    val uiState: StateFlow<UiState<com.countriesexplorer.data.model.Country>> = _uiState.asStateFlow()

    private val _noteDraft = MutableStateFlow("")
    val noteDraft: StateFlow<String> = _noteDraft.asStateFlow()

    val collections: StateFlow<List<CollectionEntity>> = collectionRepository.collections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _collectionMessage = MutableStateFlow<String?>(null)
    val collectionMessage: StateFlow<String?> = _collectionMessage.asStateFlow()

    val savedNote = _countryCode
        .flatMapLatest { code ->
            if (code.isBlank()) flowOf("")
            else countryNoteRepository.observeNote(code).map { it?.text.orEmpty() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun loadCountry(code: String) {
        _countryCode.value = code
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = repository.getCountryByCode(code)
                _uiState.value = UiState.Success(
                    data = result.country,
                    isStale = result.isStale,
                    isOffline = result.isOffline,
                    syncBlocked = result.syncBlocked
                )
                _noteDraft.value = countryNoteRepository.getNote(code)?.text.orEmpty()
            } catch (e: CountryNotFoundException) {
                _uiState.value = UiState.Error(
                    message = "Страна не найдена",
                    retryable = false
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Неизвестная ошибка",
                    retryable = true
                )
            }
        }
    }

    fun onNoteChanged(text: String) {
        _noteDraft.value = text
    }

    fun saveNote() {
        val code = _countryCode.value
        if (code.isBlank()) return
        viewModelScope.launch {
            countryNoteRepository.saveNote(code, _noteDraft.value)
        }
    }

    fun deleteNote() {
        val code = _countryCode.value
        if (code.isBlank()) return
        viewModelScope.launch {
            countryNoteRepository.deleteNote(code)
            _noteDraft.value = ""
        }
    }

    fun addToCollection(collectionId: Long) {
        val country = (_uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            collectionRepository.addCountryToCollection(collectionId, country)
            _collectionMessage.value = "added"
        }
    }

    fun clearCollectionMessage() {
        _collectionMessage.value = null
    }
}
