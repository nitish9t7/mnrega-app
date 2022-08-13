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

import dev.galaxius.mnrega.base.ViewModelBehaviorSpec
import dev.galaxius.mnrega.core.model.AuthCredential
import dev.galaxius.mnrega.core.repository.Either
import dev.galaxius.mnrega.core.repository.MnregaUserRepository
import dev.galaxius.mnrega.core.session.SessionManager
import dev.galaxius.mnrega.testUtils.currentStateShouldBe
import dev.galaxius.mnrega.testUtils.withState
import dev.galaxius.mnrega.view.state.RegisterState
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify

class RegisterViewModelTest : ViewModelBehaviorSpec({
    val repository: MnregaUserRepository = mockk()
    val sessionManager: SessionManager = mockk(relaxUnitFun = true)

    val viewModel = RegisterViewModel(repository, sessionManager)

    Given("The ViewModel") {
        When("Initialized") {
            val expectedState = RegisterState(
                isLoading = false,
                isLoggedIn = false,
                error = null,
                username = "",
                password = "",
                confirmPassword = "",
                isValidUsername = null,
                isValidPassword = null,
                isValidConfirmPassword = null
            )
            Then("Initial state should be valid") {
                viewModel currentStateShouldBe expectedState
            }
        }
    }

    Given("A username, password and confirm password") {
        val username = "galaxius"
        val password = "eodnhoj"
        val confirmPassword = "eodnhojabcd"

        When("Username is set") {
            viewModel.setUsername(username)

            Then("Username should be updated in the current state") {
                viewModel.withState { this.username shouldBe username }
            }
        }

        When("Password is set") {
            viewModel.setPassword(password)

            Then("Password should be updated in the current state") {
                viewModel.withState { this.password shouldBe password }
            }
        }

        When("Confirm Password is set") {
            viewModel.setConfirmPassword(confirmPassword)

            Then("Confirm Password should be updated in the current state") {
                viewModel.withState { this.confirmPassword shouldBe confirmPassword }
            }
        }
    }

    Given("A user for registration") {
        And("User provides incomplete credentials") {
            val username = "joh"
            val password = "doe"
            val confirmPassword = ""

            viewModel.setUsername(username)
            viewModel.setPassword(password)
            viewModel.setConfirmPassword(confirmPassword)

            When("User registers") {
                viewModel.register()

                Then("User should NOT be get created") {
                    coVerify(exactly = 0) { repository.addUser(username, password) }
                }

                Then("State should include invalid credentials") {
                    viewModel.withState {
                        isValidUsername shouldBe false
                        isValidPassword shouldBe false
                        isValidConfirmPassword shouldBe false
                    }
                }
            }
        }

        And("Repository fails to fulfil the request") {
            val username = "john"
            val password = "doe12345"

            viewModel.setUsername(username)
            viewModel.setPassword(password)
            viewModel.setConfirmPassword(password)

            coEvery { repository.addUser(username, password) }
                .returns(Either.error("Invalid credentials"))

            When("User logs in") {
                viewModel.register()

                Then("User should be get created") {
                    coVerify { repository.addUser(username, password) }
                }

                Then("UI state should include the error message") {
                    viewModel.withState {
                        error shouldBe "Invalid credentials"
                    }
                }
            }
        }

        And("Credentials are valid") {
            val username = "galaxius"
            val password = "eodnhoj1234"

            viewModel.setUsername(username)
            viewModel.setPassword(password)
            viewModel.setConfirmPassword(password)

            val token = "Bearer TOKEN_ABC"

            coEvery { repository.addUser(username, password) }
                .returns(Either.success(AuthCredential(token)))

            When("User logs in") {
                viewModel.register()

                Then("User should be get created") {
                    coVerify { repository.addUser(username, password) }
                }

                Then("Authentication token should be get saved") {
                    verify { sessionManager.saveToken(eq(token)) }
                }

                Then("UI state should have user logged in status") {
                    viewModel.withState {
                        isLoggedIn shouldBe true
                        error shouldBe null
                    }
                }
            }
        }
    }
})
