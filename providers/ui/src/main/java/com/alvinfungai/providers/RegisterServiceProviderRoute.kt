package com.alvinfungai.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

//@Composable
//fun ExploreRoute(
//    viewModel: ExploreViewModel = hiltViewModel(),
//    onProviderClick: (String) -> Unit
//) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
//
//    ExploreScreen(
//        uiState = uiState,
//        selectedCategory = selectedCategory,
//        onSelectCategory = {viewModel.getProviders(it)},
//        onProviderClick = onProviderClick
//    )
//}

