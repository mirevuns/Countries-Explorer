package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.preferences.ListPreferences
import com.countriesexplorer.data.preferences.ListPreferencesRepository
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.data.repository.FavoriteRepository
import com.countriesexplorer.data.repository.ProfileRepository
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
import kotlinx.coroutines.flow.first
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
    private val favoriteRepository: FavoriteRepository,
    private val listPreferencesRepository: ListPreferencesRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchRetryNonce = MutableStateFlow(0)

    private val refreshSignals = MutableSharedFlow<Boolean>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        viewModelScope.launch {
            val favoriteCodes = favoriteRepository.favoriteCodes.first()
            val prefs = listPreferencesRepository.listPreferences.first()
            if (prefs.showFavoritesOnly && favoriteCodes.isEmpty()) {
                listPreferencesRepository.setShowFavoritesOnly(false)
            }
        }
    }

    val listPreferences: StateFlow<ListPreferences> = listPreferencesRepository.listPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListPreferences()
        )

    val activeProfileName: StateFlow<String> = profileRepository.activeProfile
        .map { it?.name ?: ProfileRepository.DEFAULT_PROFILE_NAME }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileRepository.DEFAULT_PROFILE_NAME
        )

    private val debouncedSearchQuery = _searchQuery
        .debounce(500L)
        .distinctUntilChanged()

    private val searchRequests = combine(debouncedSearchQuery, _searchRetryNonce) { query, _ ->
        query
    }

    private val remoteListState: Flow<UiState<List<Country>>> = merge(
        flowOf(false),
        refreshSignals
    ).flatMapLatest { forceRefresh ->
        flow {
            emit(UiState.Loading)
            try {
                val result = repository.getAllCountries(forceRefresh = forceRefresh)
                emit(result.toUiState())
            } catch (e: Exception) {
                emit(UiState.Error(e.message ?: "Неизвестная ошибка"))
            }
        }
    }

    private val remoteAfterSearch: Flow<UiState<List<Country>>> =
        combine(searchRequests, remoteListState) { query, remote -> query to remote }
            .flatMapLatest { (query, remote) ->
                flow {
                    if (query.isBlank()) {
                        emit(remote)
                    } else {
                        emit(UiState.Loading)
                        try {
                            val result = repository.searchCountries(query.trim())
                            emit(result.toUiState())
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
        favoriteRepository.favoriteCodes
    ) { state, prefs, favoriteCodes ->
        applyListPreferences(state, prefs, favoriteCodes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

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
        return if (list.isEmpty()) {
            UiState.Empty
        } else {
            state.copy(data = list)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        viewModelScope.launch {
            if (_searchQuery.value.isNotBlank()) {
                _searchRetryNonce.value++
            } else {
                refreshSignals.emit(true)
            }
        }
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

    private fun com.countriesexplorer.data.repository.CountriesLoadResult.toUiState(): UiState<List<Country>> {
        return if (countries.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(
                data = countries,
                isStale = isStale,
                lastUpdatedAt = lastSyncAt,
                isOffline = isOffline,
                syncBlocked = syncBlocked
            )
        }
    }
}
