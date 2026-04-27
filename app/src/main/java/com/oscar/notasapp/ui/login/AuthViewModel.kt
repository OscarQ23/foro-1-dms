package com.oscar.notasapp.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oscar.notasapp.data.AppDatabase
import com.oscar.notasapp.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado UI para las pantallas de autenticación.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedUserId: Long? = null
)

/**
 * Gestiona el flujo de login y registro.
 * Expone un [StateFlow] inmutable que la UI observa para renderizar.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getInstance(application).userDao()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Completa todos los campos")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val user = userDao.login(username.trim(), password)
            _uiState.value = if (user != null) {
                AuthUiState(loggedUserId = user.id)
            } else {
                AuthUiState(errorMessage = "Usuario o contraseña incorrectos")
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Completa todos los campos")
            return
        }
        if (password.length < 4) {
            _uiState.value = _uiState.value.copy(errorMessage = "La contraseña debe tener al menos 4 caracteres")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val existing = userDao.findByUsername(username.trim())
            if (existing != null) {
                _uiState.value = AuthUiState(errorMessage = "El usuario ya existe")
                return@launch
            }
            val newId = userDao.insert(
                User(username = username.trim(), email = email.trim(), password = password)
            )
            _uiState.value = AuthUiState(loggedUserId = newId)
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
