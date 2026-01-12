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

data class RegistrationUiState(
    val userId: String = "",
    val fullName: String = "",
    val title: String = "Prof.",
    val registrationType: Int = 1,
    val photoPath: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val duplicateIdWarning: String? = null
)

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ParticipantRepository
    
    init {
        val database = ConferenceDatabase.getDatabase(application)
        repository = ParticipantRepository(database.participantDao())
    }
    
    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()
    
    fun updateUserId(userId: String) {
        _uiState.value = _uiState.value.copy(userId = userId, duplicateIdWarning = null)
        // Check for duplicate ID as user types
        if (userId.isNotBlank()) {
            checkDuplicateId(userId)
        }
    }
    
    fun updateFullName(fullName: String) {
        _uiState.value = _uiState.value.copy(fullName = fullName)
    }
    
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }
    
    fun updateRegistrationType(type: Int) {
        _uiState.value = _uiState.value.copy(registrationType = type)
    }
    
    fun updatePhotoPath(photoPath: String?) {
        _uiState.value = _uiState.value.copy(photoPath = photoPath)
    }
    
    private fun checkDuplicateId(userId: String) {
        viewModelScope.launch {
            try {
                val idInt = userId.toIntOrNull()
                if (idInt != null) {
                    val exists = repository.checkUserIdExists(idInt)
                    if (exists) {
                        _uiState.value = _uiState.value.copy(
                            duplicateIdWarning = "Warning: User ID $userId already exists. Please use a different ID."
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore errors during duplicate check
            }
        }
    }
    
    fun registerParticipant() {
        val currentState = _uiState.value
        
        // Validation
        if (currentState.userId.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "User ID is required")
            return
        }
        
        val userIdInt = currentState.userId.toIntOrNull()
        if (userIdInt == null) {
            _uiState.value = currentState.copy(errorMessage = "User ID must be a valid number")
            return
        }
        
        if (currentState.fullName.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Full Name is required")
            return
        }
        
        // Check for duplicate again before saving
        viewModelScope.launch {
            try {
                val exists = repository.checkUserIdExists(userIdInt)
                if (exists) {
                    _uiState.value = currentState.copy(
                        errorMessage = "User ID $userIdInt already exists. Please use a different ID."
                    )
                    return@launch
                }
                
                _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
                
                val participant = Participant(
                    userId = userIdInt,
                    fullName = currentState.fullName,
                    title = currentState.title,
                    registrationType = currentState.registrationType,
                    photoPath = currentState.photoPath
                )
                
                repository.insertParticipant(participant)
                
                _uiState.value = currentState.copy(
                    isLoading = false,
                    successMessage = "Participant registered successfully!",
                    userId = "",
                    fullName = "",
                    title = "Prof.",
                    registrationType = 1,
                    photoPath = null,
                    duplicateIdWarning = null
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
