package com.alvinfungai.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.bookings.domain.model.ProofOfWork
import com.alvinfungai.bookings.domain.model.VerificationStatus
import com.alvinfungai.bookings.domain.usecase.GetProofOfWorkUseCase
import com.alvinfungai.bookings.domain.usecase.SubmitProofOfWorkUseCase
import com.alvinfungai.users.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProofOfWorkViewModel @Inject constructor(
    private val submitProofOfWorkUseCase: SubmitProofOfWorkUseCase,
    private val getProofOfWorkUseCase: GetProofOfWorkUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProofOfWorkUiState>(ProofOfWorkUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _proofOfWork = MutableStateFlow<ProofOfWork?>(null)
    val proofOfWork = _proofOfWork.asStateFlow()

    fun loadProofOfWork(bookingId: String) {
        viewModelScope.launch {
            getProofOfWorkUseCase(bookingId).collect { result ->
                result.onSuccess { proof ->
                    _proofOfWork.value = proof
                }
            }
        }
    }

    fun submitProof(bookingId: String, description: String, imageUrls: List<String>) {
        viewModelScope.launch {
            _uiState.update { ProofOfWorkUiState.Loading }
            val user = getCurrentUserUseCase()
            if (user == null) {
                _uiState.update { ProofOfWorkUiState.Error("User not authenticated") }
                return@launch
            }

            val proof = ProofOfWork(
                bookingId = bookingId,
                providerId = user.uid,
                description = description,
                imageUrls = imageUrls,
                status = VerificationStatus.PENDING
            )

            submitProofOfWorkUseCase(proof).collect { result ->
                result.onSuccess {
                    _uiState.update { ProofOfWorkUiState.Success }
                    loadProofOfWork(bookingId)
                }.onFailure { error ->
                    _uiState.update { ProofOfWorkUiState.Error(error.message ?: "Failed to submit") }
                }
            }
        }
    }
}

sealed interface ProofOfWorkUiState {
    object Idle : ProofOfWorkUiState
    object Loading : ProofOfWorkUiState
    object Success : ProofOfWorkUiState
    data class Error(val message: String) : ProofOfWorkUiState
}
