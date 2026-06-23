package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.preferences.AppSettings
import com.countriesexplorer.data.preferences.AppSettingsRepository
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.data.repository.SyncResult
import com.countriesexplorer.data.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val countriesRepository: CountriesRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = appSettingsRepository.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    fun setCacheTtlHours(hours: Int) {
        viewModelScope.launch {
            appSettingsRepository.setCacheTtlHours(hours)
            syncScheduler.schedulePeriodicSync()
        }
    }

    fun setAutoRefreshEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setAutoRefreshEnabled(enabled)
            syncScheduler.schedulePeriodicSync()
        }
    }

    fun setWifiOnlySync(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setWifiOnlySync(enabled)
        }
    }

    fun setPreloadOnWifi(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsRepository.setPreloadOnWifi(enabled)
        }
    }

    fun preloadNow() {
        viewModelScope.launch {
            _syncMessage.value = when (val result = countriesRepository.preloadForOffline()) {
                SyncResult.Success -> "Предзагрузка завершена"
                is SyncResult.Skipped -> "Предзагрузка пропущена: ${result.reason}"
                is SyncResult.Failed -> "Ошибка: ${result.message}"
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _syncMessage.value = when (val result = countriesRepository.syncCacheIfNeeded(force = true)) {
                SyncResult.Success -> "Синхронизация завершена"
                is SyncResult.Skipped -> "Синхронизация пропущена: ${result.reason}"
                is SyncResult.Failed -> "Ошибка: ${result.message}"
            }
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }
}
