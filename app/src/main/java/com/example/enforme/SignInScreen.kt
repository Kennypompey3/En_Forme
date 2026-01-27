package com.example.enforme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.enforme.ui.theme.EnFormeTheme

@Composable
fun SignInScreen(
    authViewModel: AuthViewModel = viewModel(),
    onSignInSuccess: () -> Unit,
    onCreateAccountClicked: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onSignInSuccess()
        }
    }

    SignInScreenContent(
        authState = authState,
        onSignIn = { email, password -> authViewModel.signIn(email, password) },
        onCreateAccountClicked = onCreateAccountClicked
    )
}

@Composable
private fun SignInScreenContent(
    authState: AuthState,
    onSignIn: (email: String, password: String) -> Unit,
    onCreateAccountClicked: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "SIGN IN",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
                Text("New User?  ")
                val linkColor = MaterialTheme.colorScheme.primary
                ClickableText(
                    text = buildAnnotatedString {
                        pushStyle(
                            SpanStyle(
                                color = linkColor,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                        append("Create Account")
                        pop()
                    },
                    onClick = { onCreateAccountClicked() }
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                isError = authState is AuthState.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = authState is AuthState.Error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (authState is AuthState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { onSignIn(email, password) },
                modifier = Modifier
                    .fillMaxWidth(0.72f)
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            Divider(modifier = Modifier.weight(1f))
            Text(" or ", modifier = Modifier.padding(horizontal = 8.dp))
            Divider(modifier = Modifier.weight(1f))
        }

        OutlinedButton(
            onClick = { /* TODO: Implement Google Sign-In */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.25f)),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Continue with Google",
                color = Color.Black,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}
