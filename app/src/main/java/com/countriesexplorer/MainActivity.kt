package com.countriesexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.countriesexplorer.ui.navigation.NavGraph
import com.countriesexplorer.ui.theme.CountriesExplorerTheme
import com.countriesexplorer.ui.viewmodel.FavoritesSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CountriesExplorerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val favoritesViewModel: FavoritesSharedViewModel = hiltViewModel()
                    val favoritesSet by favoritesViewModel.favorites.collectAsState()
                    
                    NavGraph(
                        navController = navController,
                        favoritesSet = favoritesSet,
                        onFavoriteToggle = { code ->
                            favoritesViewModel.toggleFavorite(code)
                        }
                    )
                }
            }
        }
    }
}
