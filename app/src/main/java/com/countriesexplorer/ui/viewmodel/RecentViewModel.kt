package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.local.VisitHistoryEntity
import com.countriesexplorer.data.repository.VisitHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentViewModel @Inject constructor(
    private val visitHistoryRepository: VisitHistoryRepository
) : ViewModel() {

    val recentVisits: StateFlow<List<VisitHistoryEntity>> = visitHistoryRepository
        .getRecentVisits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() {
        viewModelScope.launch {
            visitHistoryRepository.clearHistory()
        }
    }
}
