package com.alvinfungai.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.usecase.GetServiceProviderByUserIdUseCase
import com.alvinfungai.providers.domain.usecase.UpdateServiceProviderUseCase
import com.alvinfungai.users.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderSettingsViewModel @Inject constructor(
    private val getServiceProviderByUserIdUseCase: GetServiceProviderByUserIdUseCase,
    private val updateServiceProviderUseCase: UpdateServiceProviderUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderSettingsUiState>(ProviderSettingsUiState.Loading)
    val uiState: StateFlow<ProviderSettingsUiState> = _uiState.asStateFlow()

    private val _updateSuccess = MutableSharedFlow<Unit>()
    val updateSuccess = _updateSuccess.asSharedFlow()

    init {
        loadProviderProfile()
    }

    private fun loadProviderProfile() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                getServiceProviderByUserIdUseCase(user.uid).collect { result ->
                    result.onSuccess { provider ->
                        if (provider != null) {
                            _uiState.value = ProviderSettingsUiState.Success(provider)
                        } else {
                            _uiState.value = ProviderSettingsUiState.Error("Provider profile not found")
                        }
                    }.onFailure { error ->
                        _uiState.value = ProviderSettingsUiState.Error(error.message ?: "Failed to load profile")
                    }
                }
            } else {
                _uiState.value = ProviderSettingsUiState.Error("User not logged in")
            }
        }
    }

    fun updateProfile(updatedProvider: ServiceProvider) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ProviderSettingsUiState.Success) {
                _uiState.value = ProviderSettingsUiState.Updating(updatedProvider)
                updateServiceProviderUseCase(updatedProvider).collect { result ->
                    result.onSuccess {
                        _uiState.value = ProviderSettingsUiState.Success(updatedProvider)
                        _updateSuccess.emit(Unit)
                    }.onFailure { error ->
                        _uiState.value = ProviderSettingsUiState.Error(error.message ?: "Update failed")
                    }
                }
            }
        }
    }
}

sealed interface ProviderSettingsUiState {
    data object Loading : ProviderSettingsUiState
    data class Success(val provider: ServiceProvider) : ProviderSettingsUiState
    data class Updating(val provider: ServiceProvider) : ProviderSettingsUiState
    data class Error(val message: String) : ProviderSettingsUiState
}
