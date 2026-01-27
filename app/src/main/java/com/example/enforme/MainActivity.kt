package com.example.enforme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.enforme.ui.theme.EnFormeTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnFormeTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen()
                        LaunchedEffect(Unit) {
                            delay(2000) // 2-second delay
                            val nextScreen = if (authViewModel.isUserAuthenticated()) "home" else "signIn"
                            navController.navigate(nextScreen) {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }
                    composable("signIn") {
                        SignInScreen(
                            authViewModel = authViewModel,
                            onSignInSuccess = {
                                navController.navigate("home") { popUpTo("signIn") { inclusive = true } }
                            },
                            onCreateAccountClicked = { navController.navigate("signUp") }
                        )
                    }
                    composable("signUp") {
                        SignUpScreen(
                            authViewModel = authViewModel,
                            onSignUpSuccess = {
                                navController.navigate("home") { popUpTo("signUp") { inclusive = true } }
                            },
                            onSignInClicked = { navController.navigateUp() }
                        )
                    }
                    composable("home") {
                        HomeScreen(authViewModel)
                    }
                }
            }
        }
    }
}
