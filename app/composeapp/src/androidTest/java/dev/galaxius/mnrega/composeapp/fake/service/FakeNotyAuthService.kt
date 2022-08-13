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

package dev.galaxius.mnrega.composeapp.fake.service

import dev.galaxius.mnrega.composeapp.testUtil.errorResponse
import dev.galaxius.mnrega.composeapp.testUtil.successResponse
import dev.galaxius.mnrega.core.model.AuthCredential
import dev.galaxius.mnrega.data.remote.api.MnregaAuthService
import dev.galaxius.mnrega.data.remote.model.request.AuthRequest
import dev.galaxius.mnrega.data.remote.model.response.AuthResponse
import dev.galaxius.mnrega.data.remote.model.response.State
import retrofit2.Response
import javax.inject.Inject

data class UserCredentials(val username: String, val password: String, val token: String)

/**
 * Fake implementation for user service
 *
 * This stored credentials in memory
 */
class FakeMnregaAuthService @Inject constructor() : MnregaAuthService {
    private val users = mutableListOf<UserCredentials>()

    init {
        // Seed one user
        users.add(
            UserCredentials(
                username = "galaxius",
                password = "galaxius1234",
                token = "galaxiusgalaxius"
            )
        )
    }

    override suspend fun register(authRequest: AuthRequest): Response<AuthResponse> {
        val (username, password) = authRequest
        if (users.any { it.username == username }) {
            return errorResponse(400, AuthResponse(State.FAILED, "User already exist", null))
        }
        val credential = AuthCredential("$username-$password")
        users.add(
            UserCredentials(
                username = username,
                password = password,
                token = credential.token
            )
        )
        return successResponse(AuthResponse(State.SUCCESS, "", credential.token))
    }

    override suspend fun login(authRequest: AuthRequest): Response<AuthResponse> {
        val (username, password) = authRequest
        return users.find { it.username == username && it.password == password }.let {
            if (it != null) {
                successResponse(AuthResponse(State.SUCCESS, "", it.token))
            } else {
                errorResponse(401, AuthResponse(State.UNAUTHORIZED, "User not exist", null))
            }
        }
    }
}
