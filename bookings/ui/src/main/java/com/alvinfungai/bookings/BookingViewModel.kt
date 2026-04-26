package com.alvinfungai.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.bookings.domain.model.Booking
import com.alvinfungai.bookings.domain.model.BookingStatus
import com.alvinfungai.bookings.domain.usecase.CreateBookingUseCase
import com.alvinfungai.bookings.domain.usecase.GetBookingsForUserUseCase
import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.usecase.GetServiceProviderByUserIdUseCase
import com.alvinfungai.reviews.domain.GetReviewsForProviderUseCase
import com.alvinfungai.reviews.domain.Review
import com.alvinfungai.users.domain.usecase.GetCurrentUserUseCase
import com.alvinfungai.users.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val createBookingUseCase: CreateBookingUseCase,
    private val getBookingsForUserUseCase: GetBookingsForUserUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getServiceProviderByUserIdUseCase: GetServiceProviderByUserIdUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getReviewsForProviderUseCase: GetReviewsForProviderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _bookingStatus = MutableStateFlow<Result<Unit>?>(null)
    val bookingStatus = _bookingStatus.asStateFlow()

    private val _provider = MutableStateFlow<Result<ServiceProvider?>?>(null)
    val provider = _provider.asStateFlow()

    init {
        loadUserBookings()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadUserBookings() {
        viewModelScope.launch {
            _uiState.update { BookingUiState.Loading }
            val user = getCurrentUserUseCase()
            if (user == null) {
                _uiState.update { BookingUiState.Error("User not found") }
                return@launch
            }

            getBookingsForUserUseCase(user.uid).flatMapLatest { result ->
                result.fold(
                    onSuccess = { bookings ->
                        val providerIds = bookings.map { it.providerId }.distinct()
                        if (providerIds.isEmpty()) {
                            flowOf(Result.success(emptyList<BookingUiModel>()))
                        } else {
                            val reviewsFlows = providerIds.map { providerId ->
                                getReviewsForProviderUseCase(providerId)
                            }

                            combine(reviewsFlows) { reviewsResults ->
                                val allReviews: List<Review> = reviewsResults.flatMap { it.getOrDefault(emptyList()) }
                                Result.success(bookings.map { booking ->
                                    val hasReviewed = allReviews.any { review ->
                                        review.bookingId == booking.id && review.customerId == user.uid
                                    }
                                    BookingUiModel(booking, hasReviewed)
                                })
                            }
                        }
                    },
                    onFailure = { error ->
                        flowOf(Result.failure(error))
                    }
                )
            }.collect { result ->
                result.onSuccess { data ->
                    _uiState.update { BookingUiState.Success(data) }
                }.onFailure { error ->
                    _uiState.update { BookingUiState.Error(error.message ?: "Unknown error") }
                }
            }
        }
    }

    fun loadProvider(providerId: String) {
        viewModelScope.launch {
            // Note: providerId here is the Firebase UID (userId)
            getServiceProviderByUserIdUseCase(providerId).collect { result ->
                _provider.value = result
            }
        }
    }

    fun createBooking(
        providerId: String, // This is the provider's Firebase UID (userId)
        scheduledTime: Long,
        notes: String,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user == null) {
                _bookingStatus.value = Result.failure(Exception("User not authenticated"))
                return@launch
            }

            // Explicitly fetch provider details to get their name and rate
            val providerResult = getServiceProviderByUserIdUseCase(providerId).first()
            val providerObj = providerResult.getOrNull()
            
            if (providerObj == null) {
                _bookingStatus.value = Result.failure(Exception("Could not find service provider details"))
                return@launch
            }

            // Fetch user profile for accurate customer info
            val profileResult = getUserProfileUseCase(user.uid).first()
            val userProfile = profileResult.getOrNull()

            val booking = Booking(
                customerId = user.uid,
                customerName = userProfile?.displayName ?: user.displayName,
                customerPhone = userProfile?.phone ?: "",
                providerId = providerObj.userId,
                providerName = providerObj.name,
                hourlyRate = providerObj.hourlyRate,
                scheduledTime = scheduledTime,
                notes = notes,
                latitude = latitude,
                longitude = longitude,
                status = BookingStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )

            createBookingUseCase(booking).collect { result ->
                _bookingStatus.update { result }
            }
        }
    }
}

data class BookingUiModel(
    val booking: Booking,
    val hasReviewed: Boolean
)

sealed interface BookingUiState {
    object Loading : BookingUiState
    data class Success(val data: List<BookingUiModel>) : BookingUiState
    data class Error(val message: String) : BookingUiState
}
