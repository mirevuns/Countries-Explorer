package com.countriesexplorer.ui.state

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Error(
        val message: String,
        val retryable: Boolean = true
    ) : UiState<Nothing>()
    data class Success<T>(
        val data: T,
        val isStale: Boolean = false,
        val lastUpdatedAt: Long? = null,
        val isOffline: Boolean = false
    ) : UiState<T>()
    object Empty : UiState<Nothing>()
}
