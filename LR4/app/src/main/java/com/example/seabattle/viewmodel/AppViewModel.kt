package com.example.seabattle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle.data.AuthRepository
import com.example.seabattle.data.ProfileRepository
import com.example.seabattle.model.AvatarChoice
import com.example.seabattle.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppUiState(
    val isFirebaseConfigured: Boolean = false,
    val isLoading: Boolean = false,
    val currentUserId: String? = null,
    val currentUserEmail: String? = null,
    val profile: Profile? = null,
    val errorMessage: String? = null,
    val notificationsEnabled: Boolean = false,
)

class AppViewModel(
    private val isFirebaseConfigured: Boolean,
    private val authRepository: AuthRepository?,
    private val profileRepository: ProfileRepository?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AppUiState(isFirebaseConfigured = isFirebaseConfigured)
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    fun initialize() {
        if (!isFirebaseConfigured || authRepository == null || profileRepository == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = if (isFirebaseConfigured) "Firebase repositories are unavailable" else null,
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                val uid = authRepository.currentUserId()
                if (uid == null) {
                    null to null
                } else {
                    uid to profileRepository.getProfile(uid)
                }
            }.onSuccess { (uid, profile) ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUserId = uid,
                    currentUserEmail = authRepository.currentUserEmail(),
                    profile = profile,
                    errorMessage = null,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to initialize app",
                )
            }
        }
    }

    fun signIn(email: String, password: String) {
        authenticate(email = email, password = password, createAccount = false)
    }

    fun register(email: String, password: String) {
        authenticate(email = email, password = password, createAccount = true)
    }

    private fun authenticate(email: String, password: String, createAccount: Boolean) {
        val auth = authRepository ?: return
        val profiles = profileRepository ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                val uid = if (createAccount) {
                    auth.register(email, password)
                } else {
                    auth.signIn(email, password)
                }
                val profile = profiles.getProfile(uid)
                Triple(uid, auth.currentUserEmail(), profile)
            }.onSuccess { (uid, currentEmail, profile) ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUserId = uid,
                    currentUserEmail = currentEmail,
                    profile = profile,
                    errorMessage = null,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: if (createAccount) {
                        "Failed to create account"
                    } else {
                        "Failed to sign in"
                    },
                )
            }
        }
    }

    fun saveProfile(nickname: String, avatarChoice: AvatarChoice) {
        val uid = _uiState.value.currentUserId ?: return
        val repository = profileRepository ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                repository.saveProfile(uid, nickname, avatarChoice)
            }.onSuccess { profile ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUserEmail = authRepository?.currentUserEmail(),
                    profile = profile,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to save profile",
                )
            }
        }
    }

    fun signOut() {
        authRepository?.signOut()
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            currentUserId = null,
            currentUserEmail = null,
            profile = null,
            errorMessage = null,
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }
}
