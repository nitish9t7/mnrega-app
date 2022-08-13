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

package dev.galaxius.mnrega.composeapp

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dev.galaxius.mnrega.composeapp.rule.WorkManagerRule
import dev.galaxius.mnrega.composeapp.ui.MainActivity
import dev.galaxius.mnrega.composeapp.ui.theme.MnregaTheme
import dev.galaxius.mnrega.view.state.State
import dev.galaxius.mnrega.view.viewmodel.BaseViewModel
import org.junit.Rule

/**
 * Base spec for testing Jetpack Compose screens
 *
 * This takes care of instantiating Hilt, WorkManager.
 */
@Suppress("LeakingThis")
abstract class MnregaScreenTest {
    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val workManagerRule = WorkManagerRule()

    @JvmField
    @Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    fun inject() = hiltRule.inject()

    inline fun <reified T : BaseViewModel<out State>> viewModel() =
        composeTestRule.activity.viewModels<T>().value

    fun runTest(
        body: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.() -> Unit
    ) = composeTestRule.run(body)

    fun setMnregaContent(content: @Composable () -> Unit) = composeTestRule.setContent {
        MnregaTheme {
            content()
        }
    }
}
