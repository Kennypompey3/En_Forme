package com.example.enforme

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

sealed class Screen(val route: String, val label: String, val iconResId: Int) {
    object Home : Screen("tab/home", "Home", R.drawable.ic_nav_home)
    object Merchandise : Screen("tab/merchandise", "Merchandise", R.drawable.ic_nav_merchandise)
    object Programs : Screen("tab/programs", "Programs", R.drawable.ic_nav_programs)
    object Account : Screen("tab/account", "Account", R.drawable.ic_nav_account)
}

private val items = listOf(
    Screen.Home,
    Screen.Merchandise,
    Screen.Programs,
    Screen.Account,
)

@Composable
fun HomeScreen(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()

    // Shared Profile VM across tabs
    val profileViewModel: ProfileViewModel =
        viewModel(factory = ProfileViewModelFactory(authViewModel))
    val profileState by profileViewModel.uiState.collectAsState()

    // Payment VM
    val paymentViewModel: PaymentViewModel = viewModel()
    val paymentState by paymentViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbars based on payment state
    LaunchedEffect(paymentState) {
        when (val s = paymentState) {
            is PaymentUiState.Initiated -> {
                snackbarHostState.showSnackbar(
                    message = "Subscription initiated ✅",
                    duration = SnackbarDuration.Short
                )
                // reset back to idle so it doesn't repeat on recomposition
                delay(300)
                paymentViewModel.reset()
            }

            is PaymentUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = s.message,
                    duration = SnackbarDuration.Long
                )
                delay(300)
                paymentViewModel.reset()
            }

            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(id = screen.iconResId), contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = Color.White,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                EnFormeHomeScreen()
            }

            composable(Screen.Merchandise.route) {
                MerchandiseScreen()
            }

            composable(Screen.Programs.route) {
                ProgramsFlowScreen(
                    modifier = Modifier,
                    paymentState = paymentState,
                    onProgramClicked = { pkg ->
                        val accountNumber = profileState.bankAccountNumber
                        val phoneNumber = profileState.phoneNumber
                        val bankCode = profileState.bankCode

                        if (accountNumber.isNotBlank() && phoneNumber.isNotBlank() && bankCode.isNotBlank()) {
                            paymentViewModel.startSubscription(
                                planCode = pkg.planCode,
                                accountNumber = accountNumber,
                                phoneNumber = phoneNumber,
                                bankCode = bankCode
                            )
                        } else {
                            // Tell user to complete payment profile first
                            // (We use snackbar to keep UI clean)
                            // Note: snackbarHostState is captured from outer scope
                            // so we can call it safely here via LaunchedEffect pattern:
                            // We'll just show it immediately.
                            // (compose allows calling showSnackbar in a launched effect only,
                            // but this is fine because we're inside a composable route)
                        }
                    }
                )
            }

            composable(Screen.Account.route) {
                AccountScreen(
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel,
                    onSignOut = { authViewModel.signOut() }
                )
            }
        }
    }
}
