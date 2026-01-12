package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ConferenceDatabase
import com.example.myapplication.data.Participant
import com.example.myapplication.data.ParticipantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VerificationUiState(
    val searchUserId: String = "",
    val participant: Participant? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val backgroundColorType: BackgroundColorType = BackgroundColorType.Default
)

enum class BackgroundColorType {
    Default, NotFound, Full, Student, None
}

class VerificationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ParticipantRepository
    
    init {
        val database = ConferenceDatabase.getDatabase(application)
        repository = ParticipantRepository(database.participantDao())
    }
    
    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()
    
    fun updateSearchUserId(userId: String) {
        _uiState.value = _uiState.value.copy(searchUserId = userId)
    }
    
    fun verifyParticipant() {
        val currentState = _uiState.value
        val userId = currentState.searchUserId.trim()
        
        if (userId.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a User ID",
                participant = null,
                backgroundColorType = BackgroundColorType.Default
            )
            return
        }
        
        val userIdInt = userId.toIntOrNull()
        if (userIdInt == null) {
            _uiState.value = currentState.copy(
                errorMessage = "User ID must be a valid number",
                participant = null,
                backgroundColorType = BackgroundColorType.Default
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            
            try {
                val participant = repository.getParticipantById(userIdInt)
                
                if (participant == null) {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        participant = null,
                        errorMessage = "User ID $userIdInt not found",
                        backgroundColorType = BackgroundColorType.NotFound
                    )
                } else {
                    val bgType = when (participant.registrationType) {
                        1 -> BackgroundColorType.Full
                        2 -> BackgroundColorType.Student
                        3 -> BackgroundColorType.None
                        else -> BackgroundColorType.Default
                    }
                    
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        participant = participant,
                        errorMessage = null,
                        backgroundColorType = bgType
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}",
                    participant = null,
                    backgroundColorType = BackgroundColorType.Default
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
