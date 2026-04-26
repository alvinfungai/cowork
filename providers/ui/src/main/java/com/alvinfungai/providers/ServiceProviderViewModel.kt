package com.alvinfungai.providers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.usecase.RegisterServiceProviderUseCase
import com.alvinfungai.users.domain.model.UserRole
import com.alvinfungai.users.domain.usecase.GetCurrentUserUseCase
import com.alvinfungai.users.domain.usecase.GetUserProfileUseCase
import com.alvinfungai.users.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class ServiceProviderViewModel @Inject constructor(
    private val registerServiceProviderUseCase: RegisterServiceProviderUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<Result<Unit>?>(null)
    val uiState = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.asStateFlow()

    fun registerProvider(
        name: String,
        profession: String,
        category: String,
        description: String,
        hourlyRate: Double,
        serviceRadiusKm: Double,
        services: List<String>,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            Log.d("COWORK_DEBUG", "VM: registerProvider called")
            _isLoading.value = true
            _uiState.value = null
            
            try {
                val authUser = getCurrentUserUseCase()
                if (authUser == null) {
                    Log.e("COWORK_DEBUG", "VM: User not authenticated")
                    _uiState.value = Result.failure(Exception("User not authenticated"))
                    return@launch
                }

                val newProvider = ServiceProvider(
                    userId = authUser.uid,
                    name = name,
                    profession = profession,
                    category = category,
                    description = description,
                    hourlyRate = hourlyRate,
                    serviceRadiusKm = serviceRadiusKm,
                    services = services,
                    latitude = latitude,
                    longitude = longitude,
                    ratingAvg = 0.0,
                    ratingCount = 0
                )

                Log.d("COWORK_DEBUG", "VM: Registering provider ${newProvider.name} for UID ${authUser.uid}")
                
                // Collect the flow using first() to get the single Result emission
                val registrationResult = registerServiceProviderUseCase(newProvider).first()
                
                Log.d("COWORK_DEBUG", "VM: registrationResult received: Success=${registrationResult.isSuccess}")
                
                if (registrationResult.isSuccess) {
                    Log.d("COWORK_DEBUG", "VM: Supabase registration successful. Updating Firestore role...")
                    
                    try {
                        val profileResult = getUserProfileUseCase(authUser.uid).first()
                        profileResult.onSuccess { profile ->
                            if (profile != null) {
                                val updatedProfile = profile.copy(role = UserRole.PROVIDER)
                                updateProfileUseCase(updatedProfile).first()
                                Log.d("COWORK_DEBUG", "VM: Firestore role updated to PROVIDER")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("COWORK_DEBUG", "VM: Error during Firestore role update", e)
                    }
                    _uiState.value = Result.success(Unit)
                } else {
                    val error = registrationResult.exceptionOrNull()
                    Log.e("COWORK_DEBUG", "VM: Supabase registration failed: ${error?.message}", error)
                    _uiState.value = registrationResult
                }

            } catch (e: Exception) {
                Log.e("COWORK_DEBUG", "VM: Unexpected error during registration: ${e.message}", e)
                _uiState.value = Result.failure(e)
            } finally {
                _isLoading.value = false
                Log.d("COWORK_DEBUG", "VM: registerProvider finished. UI State is: ${_uiState.value}")
            }
        }
    }
    
    fun resetUiState() {
        _uiState.value = null
    }
}
