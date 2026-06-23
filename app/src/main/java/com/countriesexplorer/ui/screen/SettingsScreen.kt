package com.countriesexplorer.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.countriesexplorer.R
import com.countriesexplorer.data.preferences.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    syncMessage: String?,
    onCacheTtlChanged: (Int) -> Unit,
    onAutoRefreshChanged: (Boolean) -> Unit,
    onWifiOnlyChanged: (Boolean) -> Unit,
    onPreloadOnWifiChanged: (Boolean) -> Unit,
    onPreloadNow: () -> Unit,
    onSyncNow: () -> Unit,
    onClearSyncMessage: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToProfiles: (() -> Unit)? = null,
    onNavigateToJournal: (() -> Unit)? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearSyncMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onNavigateToProfiles != null || onNavigateToJournal != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    onNavigateToProfiles?.let { navigate ->
                        OutlinedButton(onClick = navigate) {
                            Text(stringResource(R.string.profiles))
                        }
                    }
                    onNavigateToJournal?.let { navigate ->
                        OutlinedButton(onClick = navigate) {
                            Text(stringResource(R.string.journal))
                        }
                    }
                }
            }

            Text(stringResource(R.string.cache_ttl_label), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.cache_ttl_value, settings.cacheTtlHours))
            Slider(
                value = settings.cacheTtlHours.toFloat(),
                onValueChange = { onCacheTtlChanged(it.toInt()) },
                valueRange = AppSettings.MIN_CACHE_TTL_HOURS.toFloat()..AppSettings.MAX_CACHE_TTL_HOURS.toFloat(),
                steps = 6
            )

            SettingSwitch(
                title = stringResource(R.string.auto_refresh),
                checked = settings.autoRefreshEnabled,
                onCheckedChange = onAutoRefreshChanged
            )
            SettingSwitch(
                title = stringResource(R.string.wifi_only_sync),
                checked = settings.wifiOnlySync,
                onCheckedChange = onWifiOnlyChanged
            )
            SettingSwitch(
                title = stringResource(R.string.preload_on_wifi),
                checked = settings.preloadOnWifi,
                onCheckedChange = onPreloadOnWifiChanged
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSyncNow) {
                    Text(stringResource(R.string.sync_now))
                }
                Button(onClick = onPreloadNow) {
                    Text(stringResource(R.string.preload_now))
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
