package com.alvinfungai.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.bookings.domain.model.ProofOfWork
import com.alvinfungai.bookings.domain.model.VerificationStatus
import com.alvinfungai.bookings.domain.usecase.GetPendingProofOfWorksUseCase
import com.alvinfungai.bookings.domain.usecase.VerifyProofOfWorkUseCase
import com.alvinfungai.users.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModeratorDashboardViewModel @Inject constructor(
    private val getPendingProofOfWorksUseCase: GetPendingProofOfWorksUseCase,
    private val verifyProofOfWorkUseCase: VerifyProofOfWorkUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _pendingProofs = MutableStateFlow<List<ProofOfWork>>(emptyList())
    val pendingProofs = _pendingProofs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadPendingProofs()
    }

    fun loadPendingProofs() {
        viewModelScope.launch {
            _isLoading.value = true
            getPendingProofOfWorksUseCase().collect { result ->
                _isLoading.value = false
                result.onSuccess { proofs ->
                    _pendingProofs.value = proofs
                }
            }
        }
    }

    fun approveProof(proofId: String, notes: String?) {
        verify(proofId, VerificationStatus.APPROVED, notes)
    }

    fun rejectProof(proofId: String, notes: String?) {
        verify(proofId, VerificationStatus.REJECTED, notes)
    }

    private fun verify(proofId: String, status: VerificationStatus, notes: String?) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase() ?: return@launch
            verifyProofOfWorkUseCase(proofId, user.uid, status.name, notes).collect { result ->
                if (result.isSuccess) {
                    loadPendingProofs()
                }
            }
        }
    }
}
