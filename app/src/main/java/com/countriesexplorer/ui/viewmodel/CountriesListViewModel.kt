package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.local.FavoriteDao
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.preferences.ListPreferences
import com.countriesexplorer.data.preferences.ListPreferencesRepository
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.ui.state.UiState
import com.countriesexplorer.util.CountryCodeHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CountriesListViewModel @Inject constructor(
    private val repository: CountriesRepository,
    private val favoriteDao: FavoriteDao,
    private val listPreferencesRepository: ListPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _remoteListState = MutableStateFlow<UiState<List<Country>>>(UiState.Loading)

    private val refreshSignals = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val listPreferences: StateFlow<ListPreferences> = listPreferencesRepository.listPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListPreferences()
        )

    private val debouncedSearchQuery = _searchQuery
        .debounce(500L)
        .distinctUntilChanged()

    private val remoteAfterSearch: Flow<UiState<List<Country>>> =
        combine(debouncedSearchQuery, _remoteListState) { query, remote -> query to remote }
            .flatMapLatest { (query, remote) ->
                flow {
                    if (query.isBlank()) {
                        emit(remote)
                    } else {
                        emit(UiState.Loading)
                        try {
                            val countries = repository.searchCountries(query.trim())
                            emit(
                                if (countries.isEmpty()) UiState.Empty
                                else UiState.Success(countries)
                            )
                        } catch (e: Exception) {
                            val msg = e.message ?: ""
                            emit(
                                UiState.Error(
                                    if (msg.isNotBlank()) msg
                                    else "Ошибка поиска. Проверьте подключение к интернету."
                                )
                            )
                        }
                    }
                }
            }

    val uiState: StateFlow<UiState<List<Country>>> = combine(
        remoteAfterSearch,
        listPreferencesRepository.listPreferences,
        favoriteDao.getAllFavoriteCodes().map { it.toSet() }
    ) { state, prefs, favoriteCodes ->
        applyListPreferences(state, prefs, favoriteCodes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    init {
        viewModelScope.launch {
            merge(
                flowOf(Unit),
                refreshSignals
            ).collect {
                loadRemoteCountries()
            }
        }
    }

    private suspend fun loadRemoteCountries() {
        _remoteListState.value = UiState.Loading
        try {
            val countries = repository.getAllCountries()
            _remoteListState.value = if (countries.isEmpty()) {
                UiState.Empty
            } else {
                UiState.Success(countries)
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: ""
            _remoteListState.value = if (errorMessage.contains("400") || errorMessage.contains("Bad Request")) {
                UiState.Empty
            } else {
                UiState.Error(errorMessage)
            }
        }
    }

    private fun applyListPreferences(
        state: UiState<List<Country>>,
        prefs: ListPreferences,
        favoriteCodes: Set<String>
    ): UiState<List<Country>> {
        if (state !is UiState.Success) return state
        var list = state.data
        if (prefs.showFavoritesOnly) {
            list = list.filter { CountryCodeHelper.getCountryCode(it) in favoriteCodes }
        }
        list = if (prefs.sortByName) {
            list.sortedBy { it.displayName.lowercase() }
        } else {
            list.sortedByDescending { it.population }
        }
        return if (list.isEmpty()) UiState.Empty else UiState.Success(list)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        viewModelScope.launch {
            _searchQuery.value = ""
            refreshSignals.emit(Unit)
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun setShowFavoritesOnly(show: Boolean) {
        viewModelScope.launch {
            listPreferencesRepository.setShowFavoritesOnly(show)
        }
    }

    fun setSortByName(byName: Boolean) {
        viewModelScope.launch {
            listPreferencesRepository.setSortByName(byName)
        }
    }
}
