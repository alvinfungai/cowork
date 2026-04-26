package com.alvinfungai.users

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * This is the "Stateful" wrapper.
 * MainNavGraph calls THIS, and THIS calls the UI.
 */
@Composable
fun ProfileRoute(
    viewModel: UserViewModel = hiltViewModel(),
    onRegisterAsServiceProvider: () -> Unit,
    onProviderDashboardClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    // 1. Listen to the flows from the ViewModel with lifecycle awareness
    val profileResult by viewModel.profile.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    // 2. Local theme preference
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)

    // 3. Trigger a fresh fetch whenever the user enters this screen
    LaunchedEffect(Unit) {
        Log.d("ProfileRoute", "ProfileRoute: Screen entered, refreshing profile...")
        viewModel.loadCurrentProfile()
    }

    // 4. Pass the data down to the stateless Screen
    ProfileScreen(
        profileResult = profileResult,
        isLoading = isLoading,
        isDarkMode = isDarkMode,
        onThemeToggle = { isDark ->
            viewModel.updateThemePreference(isDark)
        },
        onRegisterAsServiceProvider = onRegisterAsServiceProvider,
        onProviderDashboardClick = onProviderDashboardClick,
        onEditProfileClick = onEditProfileClick,
        onLogout = onLogout
    )
}
