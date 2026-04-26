package com.alvinfungai.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ExploreRoute(
    onProviderClick: (String) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel(),
    initialCategory: String? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val cameraPosition by viewModel.cameraPosition.collectAsStateWithLifecycle()

    LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            viewModel.onCategoryChange(initialCategory)
        }
    }

    // Trigger refresh when entering the screen to ensure fresh data
    LaunchedEffect(Unit) {
        viewModel.getProviders(selectedCategory, searchQuery)
    }

    ExploreScreen(
        uiState = uiState,
        selectedCategory = selectedCategory,
        onSearchChange = viewModel::onSearchChange,
        onCategoryChange = viewModel::onCategoryChange,
        searchQuery = searchQuery,
        onProviderClick = onProviderClick,
        cameraPosition = cameraPosition
    )
}
