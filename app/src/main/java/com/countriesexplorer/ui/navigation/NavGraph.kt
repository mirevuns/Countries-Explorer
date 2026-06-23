package com.countriesexplorer.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.ui.screen.CollectionDetailScreen
import com.countriesexplorer.ui.screen.CollectionsScreen
import com.countriesexplorer.ui.screen.CountriesListScreen
import com.countriesexplorer.ui.screen.CountryDetailScreen
import com.countriesexplorer.ui.screen.FavoritesScreen
import com.countriesexplorer.ui.screen.JournalScreen
import com.countriesexplorer.ui.screen.ProfilesScreen
import com.countriesexplorer.ui.screen.RecentScreen
import com.countriesexplorer.ui.screen.SettingsScreen
import com.countriesexplorer.ui.viewmodel.CollectionDetailViewModel
import com.countriesexplorer.ui.viewmodel.CollectionsViewModel
import com.countriesexplorer.ui.viewmodel.CountriesListViewModel
import com.countriesexplorer.ui.viewmodel.CountryDetailViewModel
import com.countriesexplorer.ui.viewmodel.FavoritesSharedViewModel
import com.countriesexplorer.ui.viewmodel.JournalViewModel
import com.countriesexplorer.ui.viewmodel.ProfilesViewModel
import com.countriesexplorer.ui.viewmodel.RecentViewModel
import com.countriesexplorer.ui.viewmodel.SettingsViewModel

sealed class Screen(val route: String) {
    object CountriesList : Screen("countries_list")
    object Favorites : Screen("favorites")
    object Recent : Screen("recent")
    object Settings : Screen("settings")
    object Profiles : Screen("profiles")
    object Journal : Screen("journal")
    object Collections : Screen("collections")
    object CollectionDetail : Screen("collection_detail/{id}") {
        fun createRoute(id: Long) = "collection_detail/$id"
    }
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navigateToBottomTab(navController, route) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.CountriesList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.CountriesList.route) {
                val viewModel: CountriesListViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val listPrefs by viewModel.listPreferences.collectAsStateWithLifecycle()
                val activeProfileName by viewModel.activeProfileName.collectAsStateWithLifecycle()

                CountriesListScreen(
                    uiState = uiState,
                    searchQuery = searchQuery,
                    listPrefs = listPrefs,
                    activeProfileName = activeProfileName,
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
                    onNavigateToProfiles = {
                        navController.navigate(Screen.Profiles.route)
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
                    onNavigateToDetail = { code ->
                        navController.navigate(Screen.CountryDetail.createRoute(code))
                    },
                    onClearHistory = viewModel::clearHistory
                )
            }

            composable(Screen.Profiles.route) {
                val viewModel: ProfilesViewModel = hiltViewModel()
                val profiles by viewModel.profiles.collectAsStateWithLifecycle()
                val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()

                ProfilesScreen(
                    profiles = profiles,
                    activeProfileId = activeProfileId,
                    onNavigateBack = { navController.popBackStack() },
                    onSetActive = viewModel::setActiveProfile,
                    onCreateProfile = viewModel::createProfile,
                    onDeleteProfile = viewModel::deleteProfile
                )
            }

            composable(Screen.Journal.route) {
                val viewModel: JournalViewModel = hiltViewModel()
                val entries by viewModel.entries.collectAsStateWithLifecycle()

                JournalScreen(
                    entries = entries,
                    onNavigateBack = { navController.popBackStack() },
                    onAddEntry = viewModel::addEntry,
                    onDeleteEntry = viewModel::deleteEntry,
                    onClearEntries = viewModel::clearEntries
                )
            }

            composable(Screen.Collections.route) {
                val viewModel: CollectionsViewModel = hiltViewModel()
                val collections by viewModel.collections.collectAsStateWithLifecycle()

                CollectionsScreen(
                    collections = collections,
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.CollectionDetail.createRoute(id))
                    },
                    onCreateCollection = viewModel::createCollection,
                    onDeleteCollection = viewModel::deleteCollection
                )
            }

            composable(
                route = Screen.CollectionDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                val viewModel: CollectionDetailViewModel = hiltViewModel()
                val collection by viewModel.collection.collectAsStateWithLifecycle()
                val countries by viewModel.countries.collectAsStateWithLifecycle()

                CollectionDetailScreen(
                    collection = collection,
                    countries = countries,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCountry = { code ->
                        navController.navigate(Screen.CountryDetail.createRoute(code))
                    },
                    onRemoveCountry = viewModel::removeCountry,
                    onDeleteCollection = {
                        viewModel.deleteCollection { navController.popBackStack() }
                    }
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
                    onNavigateToProfiles = {
                        navController.navigate(Screen.Profiles.route)
                    },
                    onNavigateToJournal = {
                        navController.navigate(Screen.Journal.route)
                    }
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
                val collections by viewModel.collections.collectAsStateWithLifecycle()
                val collectionMessage by viewModel.collectionMessage.collectAsStateWithLifecycle()

                LaunchedEffect(code) {
                    viewModel.loadCountry(code)
                }

                CountryDetailScreen(
                    countryCode = code,
                    uiState = uiState,
                    noteDraft = noteDraft,
                    collections = collections,
                    collectionMessage = collectionMessage,
                    onNoteChanged = viewModel::onNoteChanged,
                    onSaveNote = viewModel::saveNote,
                    onDeleteNote = viewModel::deleteNote,
                    onAddToCollection = viewModel::addToCollection,
                    onClearCollectionMessage = viewModel::clearCollectionMessage,
                    onNavigateBack = { navController.popBackStack() },
                    onRetry = { viewModel.loadCountry(code) },
                    isFavorite = favoritesSet.contains(code),
                    onFavoriteToggle = onFavoriteToggle
                )
            }
        }
    }
}
