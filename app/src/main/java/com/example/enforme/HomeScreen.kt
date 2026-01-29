package com.example.enforme

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.launch

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

    // Shared profile VM (keeps bank/phone across tabs)
    val profileViewModel: ProfileViewModel =
        viewModel(factory = ProfileViewModelFactory(authViewModel))
    val profileState by profileViewModel.uiState.collectAsState()

    // Payment VM
    val paymentViewModel: PaymentViewModel = viewModel()
    val paymentState by paymentViewModel.uiState.collectAsState()

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // React to payment state changes
    LaunchedEffect(paymentState) {
        when (val s = paymentState) {
            is PaymentUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(s.message) }
                paymentViewModel.reset()
            }

            is PaymentUiState.SubscriptionInitiated -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Subscription initiated. Check WhatsApp/SMS/Email to approve."
                    )
                }
                paymentViewModel.reset()
            }

            is PaymentUiState.MerchInitiated -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Merch payment initiated. Follow the prompt sent to you."
                    )
                }
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
            composable(Screen.Home.route) { EnFormeHomeScreen() }

            composable(Screen.Merchandise.route) {

                // Pick ONE biller code for merch payments (use the one OnePipe gave you)
                val merchBillerCode = "000739"

                MerchandiseScreen(
                    onCheckout = { totalKobo, cartItems ->

                        val bankAccountNumber = profileState.bankAccountNumber
                        val phoneNumber = profileState.phoneNumber
                        val bankCode = profileState.bankCode

                        if (bankAccountNumber.isNotBlank() && phoneNumber.isNotBlank() && bankCode.isNotBlank()) {

                            paymentViewModel.startMerchPayment(
                                totalKobo = totalKobo,
                                billerCode = merchBillerCode,
                                accountNumber = bankAccountNumber,
                                bankCode = bankCode,
                                phoneNumber = phoneNumber,
                                items = cartItems
                            )

                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Please set Bank, Account Number, and Phone in the Account tab first."
                                )
                            }
                        }
                    }
                )
            }


            composable(Screen.Programs.route) {
                ProgramsFlowScreen(
                    paymentState = paymentState,
                    onProgramClicked = { pkg ->
                        val bankAccountNumber = profileState.bankAccountNumber
                        val phoneNumber = profileState.phoneNumber
                        val bankCode = profileState.bankCode

                        if (bankAccountNumber.isNotBlank() && phoneNumber.isNotBlank() && bankCode.isNotBlank()) {
                            paymentViewModel.startSubscription(
                                planCode = pkg.planCode,
                                accountNumber = bankAccountNumber,
                                phoneNumber = phoneNumber,
                                bankCode = bankCode
                            )
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Please set Bank, Account Number, and Phone in the Account tab first."
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
