package com.countriesexplorer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.ui.screen.CountriesListScreen
import com.countriesexplorer.ui.screen.CountryDetailScreen
import com.countriesexplorer.ui.screen.FavoritesScreen
import com.countriesexplorer.ui.viewmodel.CountriesListViewModel
import com.countriesexplorer.ui.viewmodel.CountryDetailViewModel
import com.countriesexplorer.ui.viewmodel.FavoritesSharedViewModel

sealed class Screen(val route: String) {
    object CountriesList : Screen("countries_list")
    object Favorites : Screen("favorites")
    object CountryDetail : Screen("country_detail/{code}") {
        fun createRoute(code: String) = "country_detail/$code"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    favoritesViewModel: FavoritesSharedViewModel,
    favoritesSet: Set<String>,
    onFavoriteToggle: (String, Country?) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.CountriesList.route
    ) {
        composable(Screen.CountriesList.route) {
            val viewModel: CountriesListViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val listPrefs by viewModel.listPreferences.collectAsStateWithLifecycle()

            CountriesListScreen(
                uiState = uiState,
                searchQuery = searchQuery,
                listPrefs = listPrefs,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onRefresh = viewModel::refresh,
                onShowFavoritesOnlyChange = viewModel::setShowFavoritesOnly,
                onSortByNameChange = viewModel::setSortByName,
                onNavigateToDetail = { code ->
                    navController.navigate(Screen.CountryDetail.createRoute(code))
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                },
                favoritesSet = favoritesSet,
                onFavoriteToggle = onFavoriteToggle
            )
        }

        composable(Screen.Favorites.route) {
            val entries by favoritesViewModel.favoriteEntries.collectAsStateWithLifecycle()

            FavoritesScreen(
                favoriteEntries = entries,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { code ->
                    navController.navigate(Screen.CountryDetail.createRoute(code))
                },
                favoritesSet = favoritesSet,
                onFavoriteToggle = onFavoriteToggle
            )
        }

        composable(
            route = Screen.CountryDetail.route,
            arguments = listOf(navArgument("code") { type = NavType.StringType })
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            val viewModel: CountryDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(code) {
                viewModel.loadCountry(code)
            }

            CountryDetailScreen(
                countryCode = code,
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() },
                onRetry = { viewModel.loadCountry(code) },
                isFavorite = favoritesSet.contains(code),
                onFavoriteToggle = onFavoriteToggle
            )
        }
    }
}
