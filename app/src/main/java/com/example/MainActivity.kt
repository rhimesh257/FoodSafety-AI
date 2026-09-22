package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.FoodSafeTheme
import com.example.ui.viewmodel.FoodSafetyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodSafeTheme {
                FoodSafeApp()
            }
        }
    }
}

@Composable
fun FoodSafeApp(
    viewModel: FoodSafetyViewModel = viewModel()
) {
    val currentCitizen by viewModel.currentCitizen.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedGrievance by viewModel.selectedGrievance.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    // Determine initial screen: If citizen registered -> HOME, else AUTH
    val activeScreen = if (currentCitizen == null && currentScreen != "OFFICIAL_DASHBOARD") {
        "AUTH"
    } else {
        currentScreen
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Crossfade(
            targetState = activeScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                "AUTH" -> {
                    AuthScreen(
                        viewModel = viewModel,
                        onAuthSuccess = {
                            viewModel.navigateTo("CITIZEN_HOME")
                        }
                    )
                }
                "CITIZEN_HOME" -> {
                    CitizenHomeScreen(
                        viewModel = viewModel,
                        onNavigateNewGrievance = {
                            viewModel.navigateTo("NEW_GRIEVANCE")
                        },
                        onNavigateVerificationDetails = { grievance ->
                            viewModel.selectGrievance(grievance)
                            viewModel.navigateTo("VERIFICATION_DETAILS")
                        },
                        onNavigateOfficialDashboard = {
                            viewModel.navigateTo("OFFICIAL_DASHBOARD")
                        },
                        onNavigateWallet = {
                            viewModel.navigateTo("WALLET")
                        },
                        onNavigateStateAuthority = {
                            viewModel.navigateTo("STATE_AUTHORITY")
                        }
                    )
                }
                "NEW_GRIEVANCE" -> {
                    NewGrievanceScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            viewModel.navigateTo("CITIZEN_HOME")
                        },
                        onGrievanceSubmitted = { submittedGrievance ->
                            viewModel.selectGrievance(submittedGrievance)
                            viewModel.navigateTo("VERIFICATION_DETAILS")
                        }
                    )
                }
                "VERIFICATION_DETAILS" -> {
                    val grievance = selectedGrievance
                    if (grievance != null) {
                        VerificationPipelineScreen(
                            viewModel = viewModel,
                            grievance = grievance,
                            onNavigateBack = {
                                viewModel.navigateTo("CITIZEN_HOME")
                            },
                            onNavigateOfficialDashboard = {
                                viewModel.navigateTo("OFFICIAL_DASHBOARD")
                            },
                            onNavigateWallet = {
                                viewModel.navigateTo("WALLET")
                            }
                        )
                    } else {
                        viewModel.navigateTo("CITIZEN_HOME")
                    }
                }
                "OFFICIAL_DASHBOARD" -> {
                    OfficialDashboardScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            viewModel.navigateTo("CITIZEN_HOME")
                        },
                        onNavigateStateAuthority = {
                            viewModel.navigateTo("STATE_AUTHORITY")
                        },
                        onNavigateVerificationDetails = { grievance ->
                            viewModel.selectGrievance(grievance)
                            viewModel.navigateTo("VERIFICATION_DETAILS")
                        }
                    )
                }
                "STATE_AUTHORITY" -> {
                    StateAuthorityScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            viewModel.navigateTo("CITIZEN_HOME")
                        },
                        onViewGrievanceDetails = { grievance ->
                            viewModel.selectGrievance(grievance)
                            viewModel.navigateTo("VERIFICATION_DETAILS")
                        }
                    )
                }
                "WALLET" -> {
                    WalletScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            viewModel.navigateTo("CITIZEN_HOME")
                        }
                    )
                }
            }
        }
    }
}
