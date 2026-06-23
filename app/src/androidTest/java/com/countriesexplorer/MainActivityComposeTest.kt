package com.countriesexplorer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
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
class MainActivityComposeTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun startMockServer() {
            InstrumentedTestSupport.clearDataStoreFiles()
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

    @Test
    fun list_screen_shows_title_and_loaded_country() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentedTestSupport.launchNavGraph(composeRule)
        val title = ctx.getString(R.string.countries_list)
        composeRule.onNodeWithText(title).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("Testland").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Testland").assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.search_hint)).assertIsDisplayed()
    }
}
