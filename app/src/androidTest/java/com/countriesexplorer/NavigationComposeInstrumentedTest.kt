package com.countriesexplorer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.countriesexplorer.R
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class NavigationComposeInstrumentedTest {

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
    fun setup() {
        hiltRule.inject()
        TestApiHolder.resetAlphaErrorArm()
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

    private fun waitForTestland(timeoutMs: Long = 25_000) {
        composeRule.waitUntil(timeoutMs) {
            composeRule.onAllNodesWithText("Testland").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `01_list_click_navigates_to_detail_shows_capital`() {
        waitForTestland()
        composeRule.onNodeWithText("Testland").performClick()
        composeRule.waitUntil(15_000) {
            composeRule.onAllNodesWithText("Детали страны").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Capital City").assertIsDisplayed()
    }

    @Test
    fun `02_detail_error_then_retry_shows_country`() {
        TestApiHolder.armNextAlphaErrors(1)
        waitForTestland()
        composeRule.onNodeWithText("Testland").performClick()
        composeRule.waitUntil(12_000) {
            composeRule.onAllNodesWithText(
                InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.retry)
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.retry)
        ).performClick()
        composeRule.waitUntil(15_000) {
            composeRule.onAllNodesWithText("Capital City").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Capital City").assertIsDisplayed()
    }

    @Test
    fun `03_search_tips_mutableState_toggle`() {
        waitForTestland()
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val toggle = ctx.getString(R.string.search_tips_toggle)
        val body = ctx.getString(R.string.search_tips_body)
        composeRule.onNodeWithText(toggle).performClick()
        composeRule.onNodeWithText(body).assertIsDisplayed()
        composeRule.onNodeWithText(toggle).performClick()
        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText(body).fetchSemanticsNodes().isEmpty()
        }
        assertTrue(composeRule.onAllNodesWithText(body).fetchSemanticsNodes().isEmpty())
    }
}
