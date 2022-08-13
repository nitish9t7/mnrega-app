/*
 * Copyright 2022 Team GALAXIUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.galaxius.mnrega.composeapp.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import dev.galaxius.mnrega.composeapp.BuildConfig
import dev.galaxius.mnrega.composeapp.MnregaScreenTest
import org.junit.Assert.assertTrue
import org.junit.Test

@HiltAndroidTest
class AboutScreenTest : MnregaScreenTest() {
    @Test
    fun testAboutScreen() = runTest {
        var navigatingUp = false
        setMnregaContent { AboutScreen(onNavigateUp = { navigatingUp = true }) }

        // Check if version info is displayed
        val versionInfo = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        onNodeWithText(versionInfo).assertIsDisplayed()

        // Check if repository link is displayed
        onNodeWithText("https://github.com/PatilShreyas/MnregaKT").assertIsDisplayed()

        // When Back navigation icon is clicked
        onNodeWithContentDescription("Back").performClick()
        waitForIdle()

        // Then should be navigated up
        assertTrue(navigatingUp)
    }
}
