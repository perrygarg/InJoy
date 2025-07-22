package com.perrygarg.injoyapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.perrygarg.injoyapp.ui.theme.InJoyTheme
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InJoyTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val homeViewModel: HomeViewModel = koinViewModel()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = homeViewModel,
                                contentPadding = innerPadding
                            )
                            val navigationEvent by homeViewModel.navigationEvent.collectAsStateWithLifecycle(null)
                            LaunchedEffect(navigationEvent) {
                                navigationEvent?.let { movieId ->
                                    navController.navigate("detail/$movieId")
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
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}