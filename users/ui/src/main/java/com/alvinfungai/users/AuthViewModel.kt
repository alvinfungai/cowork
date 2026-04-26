package com.alvinfungai.users

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.users.domain.model.AuthUser
import com.alvinfungai.users.domain.model.UserProfile
import com.alvinfungai.users.domain.model.UserRole
import com.alvinfungai.users.domain.usecase.CreateUserProfileUseCase
import com.alvinfungai.users.domain.usecase.LoginUseCase
import com.alvinfungai.users.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val createUserProfileUseCase: CreateUserProfileUseCase
) : ViewModel() {

    private val _user = MutableStateFlow<Result<AuthUser>?>(null)
    val user = _user.asStateFlow()

    private val _profileCreated = MutableStateFlow<Boolean>(false)
    val profileCreated = _profileCreated.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isLogin = MutableStateFlow(true)
    val isLogin = _isLogin.asStateFlow()

    fun onEmailChange(email: String) {
        _email.update { email }
    }

    fun onPasswordChange(password: String) {
        _password.update { password }
    }

    fun onToggleAuthMode() {
        _isLogin.update { !it }
    }

    fun login() {
        loginUseCase(_email.value, _password.value)
            .flowOn(Dispatchers.IO)
            .onStart { _isLoading.update { true } }
            .onEach { result ->
                _isLoading.update { false }
                result.onSuccess { data ->
                    _user.update { Result.success(data) }
                }.onFailure { error ->
                    _user.update { Result.failure(error) }
                }
            }.catch { exception ->
                _isLoading.update { false }
                _user.update { Result.failure(exception) }
            }.launchIn(viewModelScope)
    }

    fun register() {
        registerUseCase(_email.value, _password.value)
            .flowOn(Dispatchers.IO)
            .onStart { _isLoading.update { true } }
            .onEach { result ->
                result.onSuccess { authUser ->
                    val initialProfile = UserProfile(
                        uid = authUser.uid,
                        email = authUser.email,
                        displayName = "",
                        role = UserRole.CLIENT
                    )
                    Log.d("AuthViewModel", "register success, creating profile: $initialProfile")
                    createProfileOnRegisterSuccess(initialProfile)
                }.onFailure { error ->
                    _isLoading.update { false }
                    _user.update { Result.failure(error) }
                }
            }.catch { exception ->
                _isLoading.update { false }
                _user.update { Result.failure(exception) }
            }.launchIn(viewModelScope)
    }

    private fun createProfileOnRegisterSuccess(profile: UserProfile) {
        createUserProfileUseCase(profile)
            .flowOn(Dispatchers.IO)
            .onEach { result ->
                _isLoading.update { false }
                result.onSuccess {
                    Log.d("AuthViewModel", "profile created successfully")
                    _profileCreated.update { true }
                }.onFailure { error ->
                    Log.e("AuthViewModel", "failed to create profile", error)
                    _user.update { Result.failure(error) }
                }
            }
            .catch { exception -> 
                _isLoading.update { false }
                _user.update { Result.failure(exception) }
            }
            .launchIn(viewModelScope)
    }
}
