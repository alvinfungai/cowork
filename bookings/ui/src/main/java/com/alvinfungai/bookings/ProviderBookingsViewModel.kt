package com.alvinfungai.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.bookings.domain.model.Booking
import com.alvinfungai.bookings.domain.model.BookingStatus
import com.alvinfungai.bookings.domain.usecase.GetBookingsForProviderUseCase
import com.alvinfungai.bookings.domain.usecase.UpdateBookingStatusUseCase
import com.alvinfungai.providers.domain.usecase.GetServiceProviderByUserIdUseCase
import com.alvinfungai.users.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProviderBookingsViewModel @Inject constructor(
    private val getBookingsForProviderUseCase: GetBookingsForProviderUseCase,
    private val updateBookingStatusUseCase: UpdateBookingStatusUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getServiceProviderByUserIdUseCase: GetServiceProviderByUserIdUseCase
) : ViewModel() {

    private val _bookings = MutableStateFlow<Result<List<Booking>>?>(null)
    val bookings = _bookings.asStateFlow()

    fun loadProviderBookings() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user != null) {
                // Use flatMapLatest to avoid nested collections and handle updates properly
                getServiceProviderByUserIdUseCase(user.uid)
                    .flatMapLatest { result ->
                        result.fold(
                            onSuccess = { provider ->
                                if (provider != null) {
                                    // We use provider.userId (Firebase UID) because Firestore bookings 
                                    // are keyed by the Firebase UID, not the Supabase UUID.
                                    getBookingsForProviderUseCase(provider.userId)
                                } else {
                                    flowOf(Result.success(emptyList()))
                                }
                            },
                            onFailure = { error ->
                                flowOf(Result.failure(error))
                            }
                        )
                    }
                    .onEach { result ->
                        _bookings.value = result
                    }
                    .launchIn(viewModelScope)
            }
        }
    }

    fun acceptBooking(bookingId: String) {
        updateStatus(bookingId, BookingStatus.ACCEPTED)
    }

    fun rejectBooking(bookingId: String) {
        updateStatus(bookingId, BookingStatus.REJECTED)
    }

    fun startBooking(bookingId: String) {
        updateStatus(bookingId, BookingStatus.STARTED)
    }

    fun completeBooking(bookingId: String) {
        updateStatus(bookingId, BookingStatus.COMPLETED)
    }

    private fun updateStatus(bookingId: String, status: BookingStatus) {
        viewModelScope.launch {
            updateBookingStatusUseCase(bookingId, status.name).collect { result ->
                // The list will refresh automatically if we use a real-time snapshot listener in the repo
            }
        }
    }
}
