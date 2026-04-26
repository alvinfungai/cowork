package com.alvinfungai.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.usecase.GetServiceProviderDetailsUseCase
import com.alvinfungai.reviews.domain.GetReviewsForProviderUseCase
import com.alvinfungai.reviews.domain.Review
import com.alvinfungai.users.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceProviderDetailsViewModel @Inject constructor(
    private val getServiceProviderDetailsUseCase: GetServiceProviderDetailsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getReviewsForProviderUseCase: GetReviewsForProviderUseCase
) : ViewModel() {

    private val _provider = MutableStateFlow<Result<ServiceProvider>?>(null)
    val provider = _provider.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()

    init {
        viewModelScope.launch {
            _currentUserId.value = getCurrentUserUseCase()?.uid
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadProviderDetails(providerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            getServiceProviderDetailsUseCase(providerId)
                .flatMapLatest { result ->
                    result.fold(
                        onSuccess = { provider ->
                            // Correct ID mapping: USE provider.userId (Firebase UID) to fetch Firestore reviews
                            getReviewsForProviderUseCase(provider.userId).map { reviewsResult ->
                                val reviews: List<Review> = reviewsResult.getOrDefault(emptyList())
                                if (reviews.isNotEmpty()) {
                                    val avg = reviews.map { it.rating }.average()
                                    val count = reviews.size
                                    Result.success(provider.copy(
                                        ratingAvg = avg,
                                        ratingCount = count
                                    ))
                                } else {
                                    Result.success(provider)
                                }
                            }
                        },
                        onFailure = { error -> flowOf(Result.failure(error)) }
                    )
                }
                .onEach { result ->
                    _isLoading.value = false
                    _provider.value = result
                }
                .catch { error ->
                    _isLoading.value = false
                    _provider.value = Result.failure(error)
                }
                .collect { }
        }
    }
}
