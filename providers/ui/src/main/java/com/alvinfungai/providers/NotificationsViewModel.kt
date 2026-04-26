package com.alvinfungai.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.providers.domain.model.AppNotification
import com.alvinfungai.providers.domain.usecase.GetNotificationsUseCase
import com.alvinfungai.providers.domain.usecase.MarkNotificationAsReadUseCase
import com.alvinfungai.users.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        flow {
            emit(authRepository.getCurrentUser())
        }
            .filterNotNull()
            .flatMapLatest { user ->
                getNotificationsUseCase(user.uid)
            }
            .onEach { result ->
                result.onSuccess { notifications ->
                    _uiState.value = NotificationsUiState.Success(notifications)
                }
                result.onFailure { error ->
                    _uiState.value = NotificationsUiState.Error(error.message ?: "Unknown error")
                }
            }
            .catch { error ->
                _uiState.value = NotificationsUiState.Error(error.message ?: "Connection error")
            }
            .launchIn(viewModelScope)
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            markNotificationAsReadUseCase(notificationId).collect { result ->
                // UI will be updated via real-time flow from getNotificationsUseCase
            }
        }
    }
}

sealed interface NotificationsUiState {
    data object Loading : NotificationsUiState
    data class Success(val notifications: List<AppNotification>) : NotificationsUiState
    data class Error(val message: String) : NotificationsUiState
}
