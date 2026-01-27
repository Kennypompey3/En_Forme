package com.example.enforme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
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

private fun openCustomTab(context: Context, url: String) {
    val activity = context.findActivity()
    if (activity != null) {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(activity, Uri.parse(url))
    }
}

@Composable
fun HomeScreen(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()

    // --- Payment Logic ---
    val paymentViewModel: PaymentViewModel = viewModel()
    val paymentState by paymentViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(paymentState) {
        val currentState = paymentState
        if (currentState is PaymentUiState.NeedsAuthorization) {
            openCustomTab(context, currentState.authorizationUrl)
            paymentViewModel.reset()
        }
    }
    // --- End of Payment Logic ---

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(id = screen.iconResId), contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
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
    )  { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { EnFormeHomeScreen() }
            composable(Screen.Merchandise.route) { MerchandiseScreen() }
            composable(Screen.Programs.route) {
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(authViewModel))
                val profileState by profileViewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    ProgramsScreen(
                        modifier = Modifier.padding(paddingValues),
                        paymentState = paymentState,
                        onProgramClicked = { program ->
                            val bankAccountNumber = profileState.bankAccountNumber
                            val phoneNumber = profileState.phoneNumber
                            if (bankAccountNumber.isNotBlank() && phoneNumber.isNotBlank()) {
                                paymentViewModel.startSubscription(
                                    planCode = program.planCode,
                                    accountNumber = bankAccountNumber,
                                    phoneNumber = phoneNumber
                                )
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Please add your bank account and phone number in the Account tab first.",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    )
                }
            }
            composable(Screen.Account.route) {
                AccountScreen(
                    authViewModel = authViewModel,
                    onSignOut = { authViewModel.signOut() }
                )
            }
        }
    }
}