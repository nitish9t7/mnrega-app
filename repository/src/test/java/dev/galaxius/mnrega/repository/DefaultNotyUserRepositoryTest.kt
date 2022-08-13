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

import io.kotest.core.spec.style.BehaviorSpec

class DefaultMnregaUserRepositoryTest : BehaviorSpec({

//    val authService = spyk(FakeAuthService())
//    val repository = DefaultMnregaUserRepository(authService)

//    Given("A user") {
//        When("New user is added") {
//            And("Credentials are valid") {
//                val response = repository.addUser(username = "admin", password = "admin")
//
//                Then("User should be get added") {
//                    coVerify { authService.register(AuthRequest("admin", "admin")) }
//                }
//
//                Then("Valid response with token should be returned") {
//                    val credentials = (response as Either.Success).data
//                    credentials.token shouldBe "Bearer ABCD"
//                }
//            }
//
//            And("Credentials are invalid") {
//                val response = repository.addUser(username = "john", password = "doe")
//
//                Then("User should be get added") {
//                    coVerify { authService.register(AuthRequest("john", "doe")) }
//                }
//
//                Then("Valid response with error message should be returned") {
//                    val message = (response as Either.Error).message
//                    message shouldBe "Invalid credentials"
//                }
//            }
//        }
//
//        When("A user is retrieved by credentials") {
//            And("Credentials are valid") {
//                val response = repository.getUserByUsernameAndPassword(
//                    username = "admin",
//                    password = "admin"
//                )
//
//                Then("User login should be get requested") {
//                    coVerify { authService.login(AuthRequest("admin", "admin")) }
//                }
//
//                Then("Valid response with token should be returned") {
//                    val credentials = (response as Either.Success).data
//                    credentials.token shouldBe "Bearer ABCD"
//                }
//            }
//
//            And("Credentials are invalid") {
//                val response = repository.getUserByUsernameAndPassword(
//                    username = "john",
//                    password = "doe"
//                )
//
//                Then("User login should be get requested") {
//                    coVerify { authService.login(AuthRequest("john", "doe")) }
//                }
//
//                Then("Valid response with error message should be returned") {
//                    val message = (response as Either.Error).message
//                    message shouldBe "Invalid credentials"
//                }
//            }
//        }
//    }
})

//class FakeAuthService : MnregaAuthService {
//    override suspend fun register(authRequest: AuthRequest): Response<AuthResponse> {
//        return fakeAuthResponse(authRequest)
//    }
//
//    override suspend fun login(authRequest: AuthRequest): Response<AuthResponse> {
//        return fakeAuthResponse(authRequest)
//    }
//
//    @OptIn(ExperimentalStdlibApi::class)
//    private fun fakeAuthResponse(authRequest: AuthRequest): Response<AuthResponse> {
//        return if (authRequest.username == "admin" && authRequest.password == "admin") {
//            Response.success(AuthResponse(State.SUCCESS, "Success", "Bearer ABCD"))
//        } else {
//            val response = AuthResponse(State.UNAUTHORIZED, "Invalid credentials", null)
//            val body = ResponseBody.create(
//                MediaType.parse("application/json"),
//                moshi.adapter<AuthResponse>().toJson(response)
//            )
//            Response.error(401, body)
//        }
//    }
//}
