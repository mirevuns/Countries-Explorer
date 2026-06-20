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
import com.countriesexplorer.ui.screen.RecentScreen
import com.countriesexplorer.ui.screen.SettingsScreen
import com.countriesexplorer.ui.viewmodel.CountriesListViewModel
import com.countriesexplorer.ui.viewmodel.CountryDetailViewModel
import com.countriesexplorer.ui.viewmodel.FavoritesSharedViewModel
import com.countriesexplorer.ui.viewmodel.RecentViewModel
import com.countriesexplorer.ui.viewmodel.SettingsViewModel

sealed class Screen(val route: String) {
    object CountriesList : Screen("countries_list")
    object Favorites : Screen("favorites")
    object Recent : Screen("recent")
    object Settings : Screen("settings")
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
                onNavigateToRecent = {
                    navController.navigate(Screen.Recent.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
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

        composable(Screen.Recent.route) {
            val viewModel: RecentViewModel = hiltViewModel()
            val visits by viewModel.recentVisits.collectAsStateWithLifecycle()

            RecentScreen(
                visits = visits,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { code ->
                    navController.navigate(Screen.CountryDetail.createRoute(code))
                },
                onClearHistory = viewModel::clearHistory
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val settings by viewModel.appSettings.collectAsStateWithLifecycle()
            val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()

            SettingsScreen(
                settings = settings,
                syncMessage = syncMessage,
                onCacheTtlChanged = viewModel::setCacheTtlHours,
                onAutoRefreshChanged = viewModel::setAutoRefreshEnabled,
                onWifiOnlyChanged = viewModel::setWifiOnlySync,
                onPreloadOnWifiChanged = viewModel::setPreloadOnWifi,
                onPreloadNow = viewModel::preloadNow,
                onSyncNow = viewModel::syncNow,
                onClearSyncMessage = viewModel::clearSyncMessage,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CountryDetail.route,
            arguments = listOf(navArgument("code") { type = NavType.StringType })
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            val viewModel: CountryDetailViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val noteDraft by viewModel.noteDraft.collectAsStateWithLifecycle()

            LaunchedEffect(code) {
                viewModel.loadCountry(code)
            }

            CountryDetailScreen(
                countryCode = code,
                uiState = uiState,
                noteDraft = noteDraft,
                onNoteChanged = viewModel::onNoteChanged,
                onSaveNote = viewModel::saveNote,
                onDeleteNote = viewModel::deleteNote,
                onNavigateBack = { navController.popBackStack() },
                onRetry = { viewModel.loadCountry(code) },
                isFavorite = favoritesSet.contains(code),
                onFavoriteToggle = onFavoriteToggle
            )
        }
    }
}
