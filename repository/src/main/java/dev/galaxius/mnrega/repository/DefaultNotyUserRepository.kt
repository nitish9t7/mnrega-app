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

package dev.galaxius.mnrega.repository

import android.util.Log
import dev.galaxius.mnrega.core.model.AuthCredential
import dev.galaxius.mnrega.core.repository.MnregaUserRepository
import dev.galaxius.mnrega.core.repository.Either
import dev.galaxius.mnrega.data.remote.api.MnregaAuthService
import dev.galaxius.mnrega.data.remote.model.response2.State
import dev.galaxius.mnrega.data.remote.util.getResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of data of User of the app.
 */
@Singleton
class DefaultMnregaUserRepository @Inject internal constructor(
    private val authService: MnregaAuthService
) : MnregaUserRepository {

    override suspend fun addUser(
        name: String,
        username: String,
        password: String
    ): Either<AuthCredential> {
        return runCatching {
            val params = HashMap<String?, String?>()
            params["name"] = name
            params["email"] = username
            params["password"] = password
            val authResponse = authService.register(params).getResponse()
            Log.d("TAG", authResponse.toString())

            when (authResponse.status) {
                State.SUCCESS -> Either.success(AuthCredential(authResponse.token!!))
                else -> Either.error(authResponse.errors?.msg.toString())
            }
        }.onFailure {
            Log.e("TAG", it.message.toString())

        }.getOrDefault(Either.error("Something went wrong!"))
    }

    override suspend fun getUserByUsernameAndPassword(
        username: String,
        password: String
    ): Either<AuthCredential> {
        return runCatching {
            val params = HashMap<String?, String?>()
            params["email"] = username
            params["password"] = password
            val authResponse = authService.login(params).getResponse()
            Log.d("TAG", authResponse.toString())
            when (authResponse.status) {
                State.SUCCESS -> Either.success(AuthCredential(authResponse.token!!))
                else -> Either.error(authResponse.errors?.msg.toString())
            }
        }.onFailure {
            Log.e("TAG", it.message.toString())

        }.getOrDefault(Either.error("Something went wrong!"))
    }
}
