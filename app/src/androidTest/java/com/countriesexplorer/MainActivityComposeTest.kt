package com.countriesexplorer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.countriesexplorer.R
import com.countriesexplorer.ui.navigation.NavGraph
import com.countriesexplorer.ui.theme.CountriesExplorerTheme
import com.countriesexplorer.ui.viewmodel.FavoritesSharedViewModel
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityComposeTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun startMockServer() {
            TestApiHolder.ensureStarted()
        }

        @JvmStatic
        @AfterClass
        fun stopMockServer() {
            try {
                TestApiHolder.server.shutdown()
            } catch (_: Throwable) {
            }
        }
    }

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun inject() {
        hiltRule.inject()
        TestApiHolder.resetAlphaErrorArm()
        runBlocking {
            val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
            val prefs = EntryPointAccessors.fromApplication(app, InstrumentedTestEntryPoint::class.java)
                .listPreferencesRepository()
            prefs.setShowFavoritesOnly(false)
            prefs.setSortByName(true)
        }

        composeRule.setContent {
            CountriesExplorerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val favoritesViewModel: FavoritesSharedViewModel =
                        androidx.hilt.navigation.compose.hiltViewModel()
                    val favoritesSet by favoritesViewModel.favorites
                        .collectAsStateWithLifecycle()

                    NavGraph(
                        navController = androidx.navigation.compose.rememberNavController(),
                        favoritesViewModel = favoritesViewModel,
                        favoritesSet = favoritesSet,
                        onFavoriteToggle = { code, country ->
                            favoritesViewModel.toggleFavorite(code, country)
                        }
                    )
                }
            }
        }
    }

    @After
    fun closeActivity() {
        try {
            composeRule.activityRule.scenario.close()
        } catch (_: Throwable) {
        }
    }

    @Test
    fun list_screen_shows_title_and_loaded_country() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val title = ctx.getString(R.string.countries_list)
        composeRule.onNodeWithText(title).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("Testland").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Testland").assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.search_hint)).assertIsDisplayed()
    }
}
