package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.local.CollectionEntity
import com.countriesexplorer.data.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    val collections: StateFlow<List<CollectionEntity>> = collectionRepository.collections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createCollection(name: String) {
        viewModelScope.launch {
            collectionRepository.createCollection(name)
        }
    }

    fun deleteCollection(id: Long) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(id)
        }
    }
}
