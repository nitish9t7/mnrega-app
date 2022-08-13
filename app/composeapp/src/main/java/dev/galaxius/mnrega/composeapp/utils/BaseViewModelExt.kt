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

package dev.galaxius.mnrega.composeapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import dev.galaxius.mnrega.view.state.State
import dev.galaxius.mnrega.view.viewmodel.BaseViewModel

/**
 * Collects values from this ViewModel's state and represents its latest value via State.
 * Every time there would be new value posted into the state the returned State will be
 * updated causing recomposition of every State.value usage.
 */
@Composable
fun <S : State, VM : BaseViewModel<S>> VM.collectState() = state.collectAsState()
