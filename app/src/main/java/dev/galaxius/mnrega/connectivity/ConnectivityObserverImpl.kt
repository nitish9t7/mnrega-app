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

package dev.galaxius.mnrega.connectivity

import android.net.ConnectivityManager
import dev.galaxius.mnrega.core.connectivity.ConnectionState
import dev.galaxius.mnrega.core.connectivity.ConnectivityObserver
import dev.galaxius.mnrega.utils.currentConnectivityState
import dev.galaxius.mnrega.utils.observeConnectivityAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityObserverImpl(
    private val connectivityManager: ConnectivityManager
) : ConnectivityObserver {

    override val connectionState: Flow<ConnectionState>
        get() = connectivityManager.observeConnectivityAsFlow()

    override val currentConnectionState: ConnectionState
        get() = connectivityManager.currentConnectivityState
}
