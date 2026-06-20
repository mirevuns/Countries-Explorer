package com.countriesexplorer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.countriesexplorer.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
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
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        InstrumentedTestSupport.ensureWorkManagerInitialized()
        TestApiHolder.resetAlphaErrorArm()
        InstrumentedTestSupport.resetPreferences()
    }

    private fun waitForTestland(timeoutMs: Long = 15_000) {
        composeRule.waitUntil(timeoutMs) {
            composeRule.onAllNodesWithText("Testland").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun list_click_navigates_to_detail_shows_capital() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentedTestSupport.launchNavGraph(composeRule)
        waitForTestland()
        composeRule.onNodeWithText("Testland").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText(ctx.getString(R.string.country_detail_title))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Capital City").assertIsDisplayed()
    }

    @Test
    fun detail_network_error_shows_cached_country() {
        TestApiHolder.armNextAlphaErrors(1)
        InstrumentedTestSupport.launchNavGraph(composeRule)
        waitForTestland()
        composeRule.onNodeWithText("Testland").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Capital City").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Capital City").assertIsDisplayed()
    }

    @Test
    fun search_filters_list_to_matching_country() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentedTestSupport.launchNavGraph(composeRule)
        waitForTestland()
        composeRule.onNodeWithText(ctx.getString(R.string.search_hint)).performTextInput("Test")
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Testland").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Testland").assertIsDisplayed()
    }
}
