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

package dev.galaxius.mnrega.view.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.galaxius.mnrega.core.repository.MnregaUserRepository
import dev.galaxius.mnrega.core.session.SessionManager
import dev.galaxius.mnrega.utils.validator.AuthValidator
import dev.galaxius.mnrega.view.state.LoginState
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val mnregaUserRepository: MnregaUserRepository,
    private val sessionManager: SessionManager
) : BaseViewModel<LoginState>(initialState = LoginState()) {

    fun setUsername(username: String) {
        setState { state -> state.copy(username = username) }
    }

    fun setPassword(password: String) {
        setState { state -> state.copy(password = password) }
    }

    fun login() {
        if (!validateCredentials()) return

        viewModelScope.launch {
            val username = currentState.username
            val password = currentState.password

            setState { state -> state.copy(isLoading = true) }

            val response = mnregaUserRepository.getUserByUsernameAndPassword(username, password)

            response.onSuccess { authCredential ->
                sessionManager.saveToken(authCredential.token)
                setState { state ->
                    state.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        error = null
                    )
                }
            }.onFailure { message ->
                setState { state ->
                    state.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        error = message
                    )
                }
            }
        }
    }

    private fun validateCredentials(): Boolean {
        val isValidUsername = AuthValidator.isValidUsername(currentState.username)
        val isValidPassword = AuthValidator.isValidPassword(currentState.password)

        setState { state ->
            state.copy(
                isValidUsername = isValidUsername,
                isValidPassword = isValidPassword
            )
        }

        return isValidUsername && isValidPassword
    }
}
