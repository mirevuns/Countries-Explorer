package com.countriesexplorer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.countriesexplorer.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FinalProjectComposeInstrumentedTest {

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

    private fun openTestlandDetail() {
        waitForTestland()
        composeRule.onNodeWithText("Testland").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Capital City").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun settings_icon_opens_settings_screen() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentedTestSupport.launchNavGraph(composeRule)
        waitForTestland()
        composeRule.onNodeWithContentDescription(ctx.getString(R.string.settings)).performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(ctx.getString(R.string.sync_now)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(ctx.getString(R.string.settings)).assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.auto_refresh)).assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.sync_now)).assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.preload_now)).assertIsDisplayed()
    }

    @Test
    fun opening_country_adds_it_to_recent_screen() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentedTestSupport.launchNavGraph(composeRule, clearHistory = true)
        openTestlandDetail()
        composeRule.onNodeWithContentDescription(ctx.getString(R.string.back)).performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Testland").fetchSemanticsNodes().size >= 1
        }
        composeRule.onNodeWithContentDescription(ctx.getString(R.string.recent)).performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Testland").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(ctx.getString(R.string.recent)).assertIsDisplayed()
        composeRule.onNodeWithText("Testland").assertIsDisplayed()
    }

    @Test
    fun detail_save_note_persists_in_room() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val noteText = "Планирую поездку"
        InstrumentedTestSupport.launchNavGraph(composeRule)
        openTestlandDetail()
        composeRule.onNodeWithText(ctx.getString(R.string.country_note)).performScrollTo()
        composeRule.onNode(hasSetTextAction()).performScrollTo().performTextInput(noteText)
        composeRule.onNodeWithText(ctx.getString(R.string.save_note)).performScrollTo().performClick()
        composeRule.waitUntil(5_000) {
            runBlocking(Dispatchers.IO) {
                InstrumentedTestSupport.entryPoint()
                    .countryNoteDao()
                    .getByCountryCode("TL")
                    ?.text == noteText
            }
        }
        val saved = runBlocking(Dispatchers.IO) {
            InstrumentedTestSupport.entryPoint().countryNoteDao().getByCountryCode("TL")
        }
        assertEquals(noteText, saved?.text)
        assertEquals("TL", saved?.countryCode)
    }
}
