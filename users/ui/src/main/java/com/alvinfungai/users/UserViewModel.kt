package com.alvinfungai.users

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.app.core.ThemePreferences
import com.alvinfungai.providers.domain.repository.ServiceProvidersRepository
import com.alvinfungai.users.domain.model.AuthUser
import com.alvinfungai.users.domain.model.UserProfile
import com.alvinfungai.users.domain.repository.UserRepository
import com.alvinfungai.users.domain.usecase.GetCurrentUserUseCase
import com.alvinfungai.users.domain.usecase.GetUserProfileUseCase
import com.alvinfungai.users.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val userRepository: UserRepository,
    private val serviceProvidersRepository: ServiceProvidersRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _user = MutableStateFlow<AuthUser?>(null)
    val user = _user.asStateFlow()

    private val _profile = MutableStateFlow<Result<UserProfile>?>(null)
    val profile = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _updateResult = MutableStateFlow<Result<Unit>?>(null)
    val updateResult = _updateResult.asStateFlow()

    private val _deleteAccountResult = MutableStateFlow<Result<Unit>?>(null)
    val deleteAccountResult = _deleteAccountResult.asStateFlow()

    val isDarkMode: Flow<Boolean> = themePreferences.isDarkMode


    init {
        viewModelScope.launch {
            _user.update { getCurrentUserUseCase() }
            loadCurrentProfile()
        }
    }

    fun loadCurrentProfile() {
        val uid = user.value?.uid ?: return

        Log.d("UserViewModel", "loadCurrentProfile: $uid")

        getUserProfileUseCase(uid)
            .flowOn(Dispatchers.IO)
            .onStart { _isLoading.update { true } }
            .onEach { result ->
                Log.d("UserViewModel", "loadCurrentProfile: $result")
                _isLoading.update { false }
                _profile.update { result as Result<UserProfile>? }
            }
            .catch { error ->
                _isLoading.update { false }
                _profile.update { Result.failure(error) }
            }.launchIn(viewModelScope)
    }

    fun updateProfile(profile: UserProfile, imageBytes: ByteArray? = null) {
        viewModelScope.launch {
            _isLoading.update { true }
            try {
                // Update lastActiveAt whenever profile is updated
                var updatedProfile = profile.copy(lastActiveAt = System.currentTimeMillis())
                
                // 1. If imageBytes is not null, upload to Supabase and get the URL
                if (imageBytes != null) {
                    val uploadResult = userRepository.uploadProfileImage(profile.uid, imageBytes).first()
                    uploadResult.onSuccess { downloadUrl ->
                        updatedProfile = updatedProfile.copy(profileImage = downloadUrl)
                        
                    }.onFailure { error ->
                        _isLoading.update { false }
                        _updateResult.update { Result.failure(error) }
                        return@launch
                    }
                }

                // 2. Perform the Firestore profile update
                performUpdate(updatedProfile)
                
                // 3. Sync with Service Provider profile if exists (updates lastActiveAt, image, and description)
                syncWithServiceProvider(updatedProfile)
                
            } catch (e: Exception) {
                _isLoading.update { false }
                _updateResult.update { Result.failure(e) }
            }
        }
    }

    private suspend fun syncWithServiceProvider(userProfile: UserProfile) {
        try {
            val providerResult = serviceProvidersRepository.getServiceProviderByUserId(userProfile.uid).first()
            providerResult.onSuccess { provider ->
                if (provider != null) {
                    val updatedProvider = provider.copy(
                        name = userProfile.displayName,
                        imageUrl = userProfile.profileImage ?: provider.imageUrl,
                        lastActiveAt = userProfile.lastActiveAt
                    )
                    serviceProvidersRepository.updateServiceProvider(updatedProvider).first()
                    Log.d("UserViewModel", "Successfully synced profile with Service Provider profile")
                }
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Failed to sync profile with Service Provider: ${e.message}")
        }
    }

    private suspend fun performUpdate(profile: UserProfile) {
        try {
            val result = updateProfileUseCase(profile).first()
            _isLoading.update { false }
            _updateResult.update { result }
            if (result.isSuccess) {
                loadCurrentProfile()
            }
        } catch (e: Exception) {
            _isLoading.update { false }
            _updateResult.update { Result.failure(e) }
        }
    }

    fun updateThemePreference(isDark: Boolean) {
        viewModelScope.launch {
            themePreferences.setDarkMode(isDark)
        }
    }

    fun clearUpdateResult() {
        _updateResult.update { null }
    }

    fun clearDeleteAccountResult() {
        _deleteAccountResult.update { null }
    }
}
