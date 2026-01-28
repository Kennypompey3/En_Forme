package com.example.enforme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp

sealed class Screen(val route: String, val label: String, val iconResId: Int) {
    object Home : Screen("tab/home", "Home", R.drawable.ic_nav_home)
    object Merchandise : Screen("tab/merchandise", "Merchandise", R.drawable.ic_nav_merchandise)
    object Programs : Screen("tab/programs", "Programs", R.drawable.ic_nav_programs)
    object Account : Screen("tab/account", "Account", R.drawable.ic_nav_account)
}

val items = listOf(
    Screen.Home,
    Screen.Merchandise,
    Screen.Programs,
    Screen.Account,
)

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun HomeScreen(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()

    // ✅ Shared Profile VM (fixes "not saving across tabs")
    val profileViewModel: ProfileViewModel =
        viewModel(factory = ProfileViewModelFactory(authViewModel))
    val profileState by profileViewModel.uiState.collectAsState()

    // --- Payment Logic ---
    val paymentViewModel: PaymentViewModel = viewModel()
    val paymentState by paymentViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Snackbar for showing payment status messages
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is PaymentUiState.Initiated -> {
                // Show a success message (no authorizationUrl flow anymore)
                snackbarHostState.showSnackbar(
                    message = "Subscription ${state.status ?: "initiated"} • Ref: ${state.reference}",
                    duration = SnackbarDuration.Short
                )
                paymentViewModel.reset()
            }

            is PaymentUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                paymentViewModel.reset()
            }

            else -> Unit
        }
    }
    // --- End of Payment Logic ---

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painterResource(id = screen.iconResId),
                                contentDescription = null
                            )
                        },
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
            composable(Screen.Home.route) { EnFormeHomeScreen() }
            composable(Screen.Merchandise.route) { MerchandiseScreen() }

            composable(Screen.Programs.route) {
                val scope = rememberCoroutineScope()

                ProgramsScreen(
                    modifier = Modifier.padding(0.dp),
                    paymentState = paymentState,
                    onProgramClicked = { program ->
                        val bankAccountNumber = profileState.bankAccountNumber
                        val phoneNumber = profileState.phoneNumber
                        val bankCode = profileState.bankCode

                        if (bankAccountNumber.isNotBlank() && phoneNumber.isNotBlank() && bankCode.isNotBlank()) {
                            paymentViewModel.startSubscription(
                                planCode = program.planCode,
                                accountNumber = bankAccountNumber,
                                phoneNumber = phoneNumber,
                                bankCode = bankCode
                            )
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Please set Bank, Account Number, and Phone in the Account tab first.",
                                    duration = SnackbarDuration.Short
                                )
                            }
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
