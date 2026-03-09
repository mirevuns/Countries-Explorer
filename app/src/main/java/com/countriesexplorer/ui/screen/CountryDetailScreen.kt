package com.countriesexplorer.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.countriesexplorer.R
import com.countriesexplorer.ui.state.UiState
import com.countriesexplorer.ui.viewmodel.CountryDetailViewModel
import com.countriesexplorer.util.CountryCodeHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryDetailScreen(
    countryCode: String,
    onNavigateBack: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    viewModel: CountryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(countryCode) {
        viewModel.loadCountry(countryCode)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали страны") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.error_occurred),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadCountry(countryCode) }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            is UiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_countries_found))
                }
            }
            is UiState.Success -> {
                val country = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(CountryCodeHelper.getFlagUrl(country))
                            .crossfade(true)
                            .build(),
                        contentDescription = country.displayName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = country.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DetailRow(
                        label = stringResource(R.string.capital),
                        value = country.capital?.joinToString() ?: "Нет данных"
                    )
                    
                    DetailRow(
                        label = stringResource(R.string.population),
                        value = String.format("%,d", country.population)
                    )
                    
                    country.area?.let {
                        DetailRow(
                            label = stringResource(R.string.area),
                            value = "${String.format("%.2f", it)} км²"
                        )
                    }
                    
                    DetailRow(
                        label = stringResource(R.string.region),
                        value = country.region
                    )
                    
                    country.subregion?.let {
                        DetailRow(
                            label = stringResource(R.string.subregion),
                            value = it
                        )
                    }
                    
                    country.languages?.let { languages ->
                        DetailRow(
                            label = stringResource(R.string.languages),
                            value = languages.values.joinToString()
                        )
                    }
                    
                    country.currencies?.let { currencies ->
                        DetailRow(
                            label = stringResource(R.string.currencies),
                            value = currencies.values.joinToString { 
                                "${it.name} (${it.symbol ?: ""})"
                            }
                        )
                    }
                    
                    country.timezones?.let { timezones ->
                        DetailRow(
                            label = stringResource(R.string.timezones),
                            value = timezones.joinToString()
                        )
                    }
                    
                    country.borders?.let { borders ->
                        if (borders.isNotEmpty()) {
                            DetailRow(
                                label = stringResource(R.string.borders),
                                value = borders.joinToString()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
