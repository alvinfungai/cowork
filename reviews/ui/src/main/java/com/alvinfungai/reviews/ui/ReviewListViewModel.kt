package com.alvinfungai.reviews.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.reviews.domain.GetReviewsForProviderUseCase
import com.alvinfungai.reviews.domain.Review
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewListViewModel @Inject constructor(
    private val getReviewsForProviderUseCase: GetReviewsForProviderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReviewListUiState>(ReviewListUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadReviews(providerId: String) {
        viewModelScope.launch {
            _uiState.update { ReviewListUiState.Loading }
            getReviewsForProviderUseCase(providerId).collect { result ->
                result.onSuccess { reviews ->
                    _uiState.update { ReviewListUiState.Success(reviews) }
                }.onFailure { error ->
                    _uiState.update { ReviewListUiState.Error(error.message ?: "Unknown error") }
                }
            }
        }
    }
}

sealed interface ReviewListUiState {
    object Loading : ReviewListUiState
    data class Success(val reviews: List<Review>) : ReviewListUiState
    data class Error(val message: String) : ReviewListUiState
}
