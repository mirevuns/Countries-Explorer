package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.local.JournalEntryEntity
import com.countriesexplorer.data.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    val entries: StateFlow<List<JournalEntryEntity>> = journalRepository.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(text: String) {
        viewModelScope.launch {
            journalRepository.addManualEntry(text)
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            journalRepository.deleteEntry(id)
        }
    }

    fun clearEntries() {
        viewModelScope.launch {
            journalRepository.clearEntries()
        }
    }
}
