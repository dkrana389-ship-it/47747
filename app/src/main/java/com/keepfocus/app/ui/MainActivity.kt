package com.keepfocus.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.keepfocus.app.ui.screens.*
import com.keepfocus.app.ui.theme.KeepFocusTheme
import com.keepfocus.app.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KeepFocusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = hiltViewModel()
                    val profile by viewModel.userProfile.collectAsState()

                    if (profile == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val navController = rememberNavController()
                        val startDest = if (profile!!.onboardingCompleted) "home" else "welcome"

                        NavHost(navController = navController, startDestination = startDest) {
                            composable("welcome") {
                                WelcomeScreen(onContinue = { navController.navigate("profile_setup") })
                            }
                            composable("profile_setup") {
                                ProfileSetupScreen(
                                    viewModel = viewModel,
                                    isEditing = false,
                                    onSaved = { navController.navigate("permissions") }
                                )
                            }
                            composable("permissions") {
                                PermissionsConsentScreen(
                                    onContinue = { navController.navigate("done") },
                                    onDeny = { navController.navigate("done") }
                                )
                            }
                            composable("done") {
                                DoneScreen(onFinish = {
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                })
                            }
                            composable("home") {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onOpenSettings = { navController.navigate("settings") }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
