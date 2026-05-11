package com.countriesexplorer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MainActivityComposeTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun startMockServer() {
            TestApiHolder.ensureStarted()
        }
    }

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun inject() {
        hiltRule.inject()
        runBlocking {
            val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
            val prefs = EntryPointAccessors.fromApplication(app, InstrumentedTestEntryPoint::class.java)
                .listPreferencesRepository()
            prefs.setShowFavoritesOnly(false)
            prefs.setSortByName(true)
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
    fun `01_topBar_shows_list_title`() {
        composeRule.onNodeWithText("Список стран").assertIsDisplayed()
    }

    @Test
    fun `02_list_shows_mock_country_after_load`() {
        composeRule.waitUntil(timeoutMillis = 25_000) {
            composeRule.onAllNodesWithText("Testland").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Testland").assertIsDisplayed()
    }
}
