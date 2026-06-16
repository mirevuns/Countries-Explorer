package com.countriesexplorer.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.countriesexplorer.R
import com.countriesexplorer.data.local.FavoriteEntity
import com.countriesexplorer.data.model.Country

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoriteEntries: List<FavoriteEntity>,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    favoritesSet: Set<String>,
    onFavoriteToggle: (String, Country?) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (favoriteEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_favorites),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favoriteEntries, key = { it.code }) { entity ->
                    CountryItem(
                        displayName = entity.name,
                        region = entity.region,
                        flagUrl = entity.flagUrl,
                        isFavorite = favoritesSet.contains(entity.code),
                        onItemClick = { onNavigateToDetail(entity.code) },
                        onFavoriteClick = { onFavoriteToggle(entity.code, null) }
                    )
                }
            }
        }
    }
}
