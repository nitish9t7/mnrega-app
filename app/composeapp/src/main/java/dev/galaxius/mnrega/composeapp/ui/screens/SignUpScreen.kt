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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.galaxius.mnrega.composeapp.component.dialog.FailureDialog
import dev.galaxius.mnrega.composeapp.component.dialog.LoaderDialog
import dev.galaxius.mnrega.composeapp.component.text.NameTextField
import dev.galaxius.mnrega.composeapp.component.text.PasswordTextField
import dev.galaxius.mnrega.composeapp.component.text.UsernameTextField
import dev.galaxius.mnrega.composeapp.ui.theme.typography
import dev.galaxius.mnrega.composeapp.utils.MnregaPreview
import dev.galaxius.mnrega.composeapp.utils.collectState
import dev.galaxius.mnrega.view.viewmodel.RegisterViewModel

@Composable
fun SignUpScreen(
    viewModel: RegisterViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToNotes: () -> Unit
) {
    val state by viewModel.collectState()

    SignUpContent(
        isLoading = state.isLoading,

        name = state.name,
        username = state.username,
        password = state.password,
        confirmPassword = state.confirmPassword,
        isValidName = state.isValidName ?: true,
        isValidUsername = state.isValidUsername ?: true,
        isValidPassword = state.isValidPassword ?: true,
        isValidConfirmPassword = state.isValidConfirmPassword ?: true,
        onNameChange = viewModel::setName,
        onUsernameChange = viewModel::setUsername,
        onPasswordChange = viewModel::setPassword,
        onConfirmPasswordChanged = viewModel::setConfirmPassword,
        onSignUpClick = viewModel::register,
        onNavigateUp = onNavigateUp,
        error = state.error
    )

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            onNavigateToNotes()
        }
    }
}

@Composable
fun SignUpContent(
    isLoading: Boolean,

    name: String,
    username: String,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    password: String,
    confirmPassword: String,
    onConfirmPasswordChanged: (String) -> Unit,
    isValidConfirmPassword: Boolean,
    onNavigateUp: () -> Unit,
    onSignUpClick: () -> Unit,
    isValidName: Boolean,
    isValidUsername: Boolean,
    isValidPassword: Boolean,
    error: String?
) {
    if (isLoading) {
        LoaderDialog()
    }

    if (error != null) {
        FailureDialog(error)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Create\naccount",
            style = typography.h4,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 60.dp, bottom = 16.dp)
        )

        SignUpForm(
            name = name,
            username = username,
            onNameChange = onNameChange,
            onUsernameChange = onUsernameChange,
            isValidName = isValidName,
            isValidUsername = isValidUsername,
            password = password,
            onPasswordChange = onPasswordChange,
            isValidPassword = isValidPassword,
            confirmPassword = confirmPassword,
            onConfirmPasswordChanged = onConfirmPasswordChanged,
            isValidConfirmPassword = isValidConfirmPassword,
            onSignUpClick = onSignUpClick
        )

        LoginLink(Modifier.align(Alignment.CenterHorizontally), onLoginClick = onNavigateUp)
    }
}

@Composable
private fun SignUpForm(
    name: String,
    username: String,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    isValidName: Boolean,
    isValidUsername: Boolean,
    password: String,
    onPasswordChange: (String) -> Unit,
    isValidPassword: Boolean,
    confirmPassword: String,
    onConfirmPasswordChanged: (String) -> Unit,
    isValidConfirmPassword: Boolean,
    onSignUpClick: () -> Unit
) {
    Column(
        Modifier.padding(
            start = 16.dp,
            top = 32.dp,
            end = 16.dp,
            bottom = 16.dp
        )
    ) {

        NameTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colors.background),
            value = name,
            onValueChange = onNameChange,
            isError = !isValidName,
        )
        UsernameTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colors.background),
            value = username,
            onValueChange = onUsernameChange,
            isError = !isValidUsername,
        )

        PasswordTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colors.background),
            value = password,
            onValueChange = onPasswordChange,
            isError = !isValidPassword
        )

        PasswordTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colors.background),
            value = confirmPassword,
            label = "Confirm Password",
            onValueChange = onConfirmPasswordChanged,
            isError = !isValidConfirmPassword
        )

        Button(
            onClick = onSignUpClick,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(style = typography.subtitle1, color = Color.White, text = "Create account")
        }
    }
}

@Composable
private fun LoginLink(modifier: Modifier, onLoginClick: () -> Unit) {
    Text(
        text = buildAnnotatedString {
            append("Already have an account? Login")
            addStyle(SpanStyle(color = MaterialTheme.colors.primary), 24, this.length)
        },
        style = typography.subtitle1,
        modifier = modifier
            .padding(vertical = 24.dp, horizontal = 16.dp)
            .clickable(onClick = onLoginClick)
    )
}

@Preview
@Composable
fun PreviewSignupContent() = MnregaPreview {
    SignUpContent(
        isLoading = false,
        name = "Nitish",
        username = "galaxius",
        onNameChange = {},
        onUsernameChange = {},
        onPasswordChange = {},
        password = "password",
        confirmPassword = "password",
        onConfirmPasswordChanged = {},
        isValidConfirmPassword = false,
        onNavigateUp = {},
        onSignUpClick = {},
        isValidName = false,
        isValidUsername = false,
        isValidPassword = false,
        error = null
    )
}
