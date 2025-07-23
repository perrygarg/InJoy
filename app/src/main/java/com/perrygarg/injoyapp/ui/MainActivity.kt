package com.perrygarg.injoyapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.perrygarg.injoyapp.ui.theme.InJoyTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.android.get
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val offlineStateFlow = get<MutableStateFlow<Boolean>>()
            val context = LocalContext.current
            LaunchedEffect(context) {
                observeNetworkStatus(context)
                    .collect { isOnline ->
                        offlineStateFlow.value = !isOnline
                    }
            }
            InJoyTheme {
                val navController = rememberNavController()
                val bottomNavItems = listOf(
                    BottomNavItem("Home", "home", Icons.Filled.Home),
                    BottomNavItem("Search", "search", Icons.Filled.Search),
                    BottomNavItem("Saved", "saved", Icons.Filled.Favorite)
                )
                val currentRoute = navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null).value?.destination?.route
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            bottomNavItems.forEach { item ->
                                NavigationBarItem(
                                    selected = currentRoute == item.route || (item.route == "home" && currentRoute == null),
                                    onClick = {
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            val homeViewModel: HomeViewModel = koinViewModel()
                            HomeScreen(
                                viewModel = homeViewModel,
                                contentPadding = PaddingValues(0.dp)
                            )
                            val navigationEvent by homeViewModel.navigationEvent.collectAsStateWithLifecycle(null)
                            LaunchedEffect(navigationEvent) {
                                navigationEvent?.let { movieId ->
                                    navController.navigate("detail/$movieId") {
                                        launchSingleTop = true
                                        popUpTo("home") { inclusive = false }
                                    }
                                }
                            }
                        }
                        composable("search") {
                            SearchScreen { movieId ->
                                navController.navigate("detail/$movieId") {
                                    launchSingleTop = true
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        }
                        composable("saved") {
                            SavedMoviesScreen { movieId ->
                                navController.navigate("detail/$movieId") {
                                    launchSingleTop = true
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        }
                        composable(
                            route = "detail/{movieId}",
                            arguments = listOf(navArgument("movieId") { type = NavType.IntType }),
                            deepLinks = listOf(
                                navDeepLink { uriPattern = "injoy://movie/{movieId}" }
                            )
                        ) { backStackEntry ->
                            val movieId = backStackEntry.arguments?.getInt("movieId") ?: -1
                            MovieDetailScreen(
                                movieId = movieId,
                                onBack = {
                                    // If only detail is in stack, pop to home
                                    if (navController.previousBackStackEntry?.destination?.route == null) {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)