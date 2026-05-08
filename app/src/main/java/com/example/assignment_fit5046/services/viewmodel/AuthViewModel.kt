package com.example.assignment_fit5046.services.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment_fit5046.datamodels.User
import com.example.assignment_fit5046.services.FirebaseService
import com.example.assignment_fit5046.services.UserService
import com.example.assignment_fit5046.services.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    data object Loading : AuthState()
    data class LoggedIn(val user: User) : AuthState()
    data object LoggedOut : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getInstance(application).userDao()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch { checkAuthState() }
    }

    private suspend fun checkAuthState() {
        if (!FirebaseService.isLoggedIn()) {
            _authState.value = AuthState.LoggedOut
            return
        }
        val cached = userDao.getUser()
        if (cached != null) {
            _authState.value = AuthState.LoggedIn(cached)
            return
        }
        UserService.getCurrentUser()
            .onSuccess { user ->
                userDao.insertUser(user)
                _authState.value = AuthState.LoggedIn(user)
            }
            .onFailure {
                _authState.value = AuthState.Error(it.message ?: "Failed to restore session")
            }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            UserService.loginUser(email, password)
                .onSuccess { user ->
                    Log.d("AUTH_DEBUG", "Got user: $user")
                    try {
                        userDao.insertUser(user)
                        Log.d("AUTH_DEBUG", "Room insert done")
                        _authState.value = AuthState.LoggedIn(user)
                        Log.d("AUTH_DEBUG", "State updated")
                    } catch (e: Exception) {
                        Log.e("AUTH_DEBUG", "Failed after login: ${e::class.simpleName} — ${e.message}", e)
                        _authState.value = AuthState.Error(e.message ?: "Post-login error")
                    }
                }
                .onFailure {
                    Log.e("AUTH_DEBUG", "Login failed: ${it::class.simpleName} — ${it.message}", it)
                    _authState.value = AuthState.Error(it.message ?: "Login failed")
                }
        }
    }

    fun register(
        email: String,
        password: String,
        name: String,
        role: com.example.assignment_fit5046.datamodels.UserRole,
        phoneNumber: String = "",
        bio: String = "",
        ngoName: String = "",
        ngoDescription: String = ""
    ) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            UserService.registerUser(
                email = email,
                password = password,
                name = name,
                role = role,
                phoneNumber = phoneNumber,
                bio = bio,
                ngoName = ngoName,
                ngoDescription = ngoDescription
            )
                .onSuccess { user ->
                    try {
                        userDao.insertUser(user)
                        _authState.value = AuthState.LoggedIn(user)
                    } catch (e: Exception) {
                        Log.e("AUTH_DEBUG", "Room insert failed after register: ${e.message}", e)
                        _authState.value = AuthState.Error(e.message ?: "Post-register error")
                    }
                }
                .onFailure {
                    Log.e("AUTH_DEBUG", "Register failed: ${it.message}", it)
                    _authState.value = AuthState.Error(it.message ?: "Registration failed")
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            UserService.logoutUser()
            userDao.clearUser()
            _authState.value = AuthState.LoggedOut
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.LoggedOut
        }
    }
}
