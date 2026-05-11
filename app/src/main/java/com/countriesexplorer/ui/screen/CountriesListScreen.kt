package com.countriesexplorer.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.ui.state.UiState
import com.countriesexplorer.ui.viewmodel.CountriesListViewModel
import com.countriesexplorer.util.CountryCodeHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    favoritesSet: Set<String>,
    onFavoriteToggle: (String, Country) -> Unit,
    viewModel: CountriesListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val listPrefs by viewModel.listPreferences.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.countries_list)) },
                actions = {
                    IconButton(onClick = { onNavigateToFavorites() }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = stringResource(R.string.favorites)
                        )
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true
            )

            var searchTipsExpanded by remember { mutableStateOf(false) }
            TextButton(
                onClick = { searchTipsExpanded = !searchTipsExpanded },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(stringResource(R.string.search_tips_toggle))
            }
            if (searchTipsExpanded) {
                Text(
                    text = stringResource(R.string.search_tips_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = listPrefs.showFavoritesOnly,
                    onClick = {
                        viewModel.setShowFavoritesOnly(!listPrefs.showFavoritesOnly)
                    },
                    label = { Text(stringResource(R.string.filter_favorites_only)) }
                )
                FilterChip(
                    selected = listPrefs.sortByName,
                    onClick = { viewModel.setSortByName(true) },
                    label = { Text(stringResource(R.string.sort_by_name)) }
                )
                FilterChip(
                    selected = !listPrefs.sortByName,
                    onClick = { viewModel.setSortByName(false) },
                    label = { Text(stringResource(R.string.sort_by_population)) }
                )
            }

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f, fill = true),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.loading))
                        }
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f, fill = true),
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
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
                is UiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f, fill = true),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_countries_found))
                    }
                }
                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f, fill = true),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data) { country ->
                            CountryItem(
                                country = country,
                                isFavorite = favoritesSet.contains(CountryCodeHelper.getCountryCode(country)),
                                onItemClick = {
                                    viewModel.clearSearch()
                                    onNavigateToDetail(CountryCodeHelper.getCountryCode(country))
                                },
                                onFavoriteClick = {
                                    onFavoriteToggle(CountryCodeHelper.getCountryCode(country), country)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryItem(
    country: Country,
    isFavorite: Boolean,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        onClick = onItemClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(CountryCodeHelper.getFlagUrl(country))
                    .crossfade(true)
                    .build(),
                contentDescription = country.displayName,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = country.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = country.region,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
