package com.countriesexplorer.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.countriesexplorer.R

val bottomNavRoutes = setOf(
    Screen.CountriesList.route,
    Screen.Collections.route,
    Screen.Recent.route,
    Screen.Settings.route
)

private data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: @Composable () -> Unit
)

private val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.CountriesList,
        labelRes = R.string.tab_countries,
        icon = { Icon(Icons.Default.Public, contentDescription = null) }
    ),
    BottomNavItem(
        screen = Screen.Collections,
        labelRes = R.string.tab_collections,
        icon = { Icon(Icons.Default.CollectionsBookmark, contentDescription = null) }
    ),
    BottomNavItem(
        screen = Screen.Recent,
        labelRes = R.string.tab_history,
        icon = { Icon(Icons.Default.History, contentDescription = null) }
    ),
    BottomNavItem(
        screen = Screen.Settings,
        labelRes = R.string.tab_settings,
        icon = { Icon(Icons.Default.Settings, contentDescription = null) }
    )
)

@Composable
fun AppBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen.route) },
                icon = item.icon,
                label = { Text(label) }
            )
        }
    }
}

fun navigateToBottomTab(navController: androidx.navigation.NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(Screen.CountriesList.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
