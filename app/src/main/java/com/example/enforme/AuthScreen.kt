package com.example.enforme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.enforme.ui.theme.EnFormeTheme
import androidx.compose.runtime.collectAsState

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel(),
    onAuthenticated: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onAuthenticated()
        }
    }

    var showSignUp by remember { mutableStateOf(false) }

    if (showSignUp) {
        SignUpScreen(
            authViewModel = authViewModel,
            onSignUpSuccess = onAuthenticated,
            onSignInClicked = { showSignUp = false }
        )
    } else {
        SignInScreen(
            authViewModel = authViewModel,
            onSignInSuccess = onAuthenticated,
            onCreateAccountClicked = { showSignUp = true }
        )
    }
}