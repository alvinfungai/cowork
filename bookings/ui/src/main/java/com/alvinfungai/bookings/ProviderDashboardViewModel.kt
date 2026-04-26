package com.alvinfungai.bookings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.bookings.domain.model.BookingStatus
import com.alvinfungai.bookings.domain.repository.BookingsRepository
import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.usecase.GetNotificationsUseCase
import com.alvinfungai.providers.domain.usecase.GetServiceProviderByUserIdUseCase
import com.alvinfungai.providers.domain.usecase.UpdateServiceProviderUseCase
import com.alvinfungai.users.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardStats(
    val totalRequests: Int = 0,
    val totalEarnings: Double = 0.0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProviderDashboardViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val authRepository: AuthRepository,
    private val getServiceProviderByUserIdUseCase: GetServiceProviderByUserIdUseCase,
    private val updateServiceProviderUseCase: UpdateServiceProviderUseCase,
    private val bookingsRepository: BookingsRepository
) : ViewModel() {

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _providerProfile = MutableStateFlow<ServiceProvider?>(null)
    val providerProfile: StateFlow<ServiceProvider?> = _providerProfile.asStateFlow()

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _updateSuccess = MutableSharedFlow<Unit>()
    val updateSuccess = _updateSuccess.asSharedFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            
            // Observe unread notifications
            getNotificationsUseCase(user.uid)
                .onEach { result ->
                    result.onSuccess { notifications ->
                        _unreadCount.value = notifications.count { !it.read }
                    }
                }
                .launchIn(viewModelScope)

            // Load provider profile
            _isLoading.value = true
            getServiceProviderByUserIdUseCase(user.uid)
                .onEach { result ->
                    result.onSuccess { provider ->
                        _providerProfile.value = provider
                        if (provider != null) {
                            // USE CASE: In Firestore 'bookings', the 'providerId' field is the provider's Firebase UID.
                            // We must use provider.userId (Firebase UID) here, NOT the Supabase UUID (provider.id).
                            loadStats(provider.userId)
                        } else {
                            _isLoading.value = false
                        }
                    }.onFailure { e ->
                        _error.value = e.message
                        _isLoading.value = false
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun loadStats(providerId: String) {
        bookingsRepository.getBookingsForProvider(providerId)
            .onEach { result ->
                result.onSuccess { bookings ->
                    // Exclude canceled and rejected bookings from the "Requests" count
                    val activeRequests = bookings.filter { 
                        it.status != BookingStatus.CANCELLED && it.status != BookingStatus.REJECTED 
                    }.size
                    
                    // Improved earnings calculation: Sum of stored amountDue for COMPLETED bookings
                    // This ensures accuracy even if hourlyRate changed over time
                    val earnings = bookings.filter { it.status == BookingStatus.COMPLETED }.sumOf { it.amountDue }
                    
                    Log.d("DASHBOARD", "loadStats: Recalculated earnings for provider $providerId: $earnings from ${bookings.size} total bookings")
                    
                    _stats.value = DashboardStats(activeRequests, earnings)
                    _isLoading.value = false
                }
                result.onFailure {
                    _isLoading.value = false
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateProfile(updatedProvider: ServiceProvider) {
        viewModelScope.launch {
            _isLoading.value = true
            updateServiceProviderUseCase(updatedProvider)
                .collect { result ->
                    result.onSuccess {
                        _updateSuccess.emit(Unit)
                        _isLoading.value = false
                    }.onFailure { e ->
                        _error.value = e.message
                        _isLoading.value = false
                    }
                }
        }
    }

    fun resetError() {
        _error.value = null
    }
}
