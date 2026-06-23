package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.local.CollectionCountryEntity
import com.countriesexplorer.data.local.CollectionEntity
import com.countriesexplorer.data.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val collectionId: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L

    val collection: StateFlow<CollectionEntity?> = flow {
        emit(collectionRepository.getOwnedCollectionById(collectionId))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val countries: StateFlow<List<CollectionCountryEntity>> =
        collectionRepository.getCountriesInCollection(collectionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeCountry(countryCode: String) {
        viewModelScope.launch {
            collectionRepository.removeCountryFromCollection(collectionId, countryCode)
        }
    }

    fun deleteCollection(onDeleted: () -> Unit) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(collectionId)
            onDeleted()
        }
    }
}
