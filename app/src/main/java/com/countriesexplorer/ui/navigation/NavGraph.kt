package com.countriesexplorer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.countriesexplorer.ui.screen.CountriesListScreen
import com.countriesexplorer.ui.screen.CountryDetailScreen
import com.countriesexplorer.ui.screen.FavoritesScreen

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
    favoritesSet: Set<String>,
    onFavoriteToggle: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.CountriesList.route
    ) {
        composable(Screen.CountriesList.route) {
            CountriesListScreen(
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
            FavoritesScreen(
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
            CountryDetailScreen(
                countryCode = code,
                onNavigateBack = { navController.popBackStack() },
                isFavorite = favoritesSet.contains(code),
                onFavoriteToggle = { onFavoriteToggle(code) }
            )
        }
    }
}
