package com.example.enforme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.enforme.ui.theme.EnFormeTheme

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel = viewModel(),
    onSignUpSuccess: () -> Unit,
    onSignInClicked: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onSignUpSuccess()
        }
    }

    SignUpScreenContent(
        authState = authState,
        onSignUp = { email, password -> authViewModel.signUp(email, password) },
        onSignInClicked = onSignInClicked
    )
}

@Composable
private fun SignUpScreenContent(
    authState: AuthState,
    onSignUp: (email: String, password: String) -> Unit,
    onSignInClicked: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMismatch by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.en_forme_logo),
            contentDescription = "En Forme Logo",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 16.dp)
        )

        // Left-aligned content block (matches your design)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "SIGN UP",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                placeholder = { Text("Enter First Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                placeholder = { Text("Enter Last Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("eMail Address") },
                placeholder = { Text("Enter e-Mail address") },
                modifier = Modifier.fillMaxWidth(),
                isError = authState is AuthState.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordMismatch = false },
                label = { Text("Password") },
                placeholder = { Text("Enter Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = authState is AuthState.Error || passwordMismatch
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; passwordMismatch = false },
                label = { Text("Confirm Password") },
                placeholder = { Text("verify password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordMismatch
            )

            if (passwordMismatch) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 6.dp, start = 16.dp)
                        .fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (authState is AuthState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (password.isNotEmpty() && password == confirmPassword) {
                        onSignUp(email, password)
                    } else {
                        passwordMismatch = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.72f) // centered pill-like button
                    .height(52.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Continue", fontSize = 16.sp)
            }
        }

        if (authState is AuthState.Error) {
            Text(
                text = authState.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        // If you still want the "Already a user? Sign In" row like before,
        // tell me and I'll add it back in a way that matches your layout.
    }
}