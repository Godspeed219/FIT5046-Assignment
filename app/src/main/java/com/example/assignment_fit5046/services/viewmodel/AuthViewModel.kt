package com.example.assignment_fit5046.services.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment_fit5046.datamodels.User
import com.example.assignment_fit5046.datamodels.UserRole
import com.example.assignment_fit5046.services.remote.firebase.FirebaseService
import com.example.assignment_fit5046.services.remote.firebase.UserService
import com.example.assignment_fit5046.services.local.AppDatabase
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

sealed class AuthState {
    data object Loading : AuthState()
    data class LoggedIn(val user: User) : AuthState()
    data object LoggedOut : AuthState()
    data class Error(val message: String) : AuthState()
}

data class PendingGoogleUser(val uid: String, val email: String, val name: String)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getInstance(application).userDao()
    private val driveDao = AppDatabase.getInstance(application).driveDao()
    private val applicationDao = AppDatabase.getInstance(application).applicationDao()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _pendingGoogleUser = MutableStateFlow<PendingGoogleUser?>(null)
    val pendingGoogleUser: StateFlow<PendingGoogleUser?> = _pendingGoogleUser.asStateFlow()

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

    private fun formatAuthError(message: String?): String {
        if (message == null) return "Something went wrong. Please try again."
        return when {
            message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            message.contains("invalid-credential", ignoreCase = true) ||
            message.contains("INVALID_PASSWORD", ignoreCase = true) ->
                "Incorrect email or password. Please try again."
            message.contains("EMAIL_NOT_FOUND", ignoreCase = true) ||
            message.contains("user-not-found", ignoreCase = true) ->
                "No account found with this email address."
            message.contains("INVALID_EMAIL", ignoreCase = true) ||
            message.contains("invalid-email", ignoreCase = true) ->
                "Please enter a valid email address."
            message.contains("USER_DISABLED", ignoreCase = true) ||
            message.contains("user-disabled", ignoreCase = true) ->
                "This account has been disabled. Please contact support."
            message.contains("TOO_MANY_REQUESTS", ignoreCase = true) ||
            message.contains("too-many-requests", ignoreCase = true) ->
                "Too many failed attempts. Please wait a moment and try again."
            message.contains("NETWORK_ERROR", ignoreCase = true) ||
            message.contains("network", ignoreCase = true) ->
                "Network error. Please check your connection and try again."
            message.contains("EMAIL_EXISTS", ignoreCase = true) ||
            message.contains("email-already-in-use", ignoreCase = true) ->
                "An account with this email already exists. Try logging in instead."
            message.contains("WEAK_PASSWORD", ignoreCase = true) ||
            message.contains("weak-password", ignoreCase = true) ->
                "Password is too weak. Please use at least 6 characters."
            message.contains("CREDENTIAL_TYPE_NOT_SUPPORTED", ignoreCase = true) ->
                "Google Sign-In is not supported on this device configuration."
            message.contains("No credentials available", ignoreCase = true) ->
                "No Google account found. Please add a Google account in device settings first."
            message.contains("cancelled", ignoreCase = true) ->
                "Sign-in was cancelled."
            message.contains("Unexpected credential", ignoreCase = true) ->
                "Unexpected error during Google Sign-In. Please try again."
            else -> "Something went wrong. Please try again."
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
                    _authState.value = AuthState.Error(formatAuthError(it.message))
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
        ngoDescription: String = "",
        ngoMetadata: String = "",
        ngoAddress: String = ""
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
                ngoDescription = ngoDescription,
                ngoMetadata = ngoMetadata,
                ngoAddress = ngoAddress
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
                    _authState.value = AuthState.Error(formatAuthError(it.message))
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            UserService.logoutUser()
            userDao.clearUser()
            try {
                CredentialManager.create(getApplication<Application>())
                    .clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {}
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    driveDao.clearDrives()
                    applicationDao.clearApplications()
                } catch (_: Exception) {}
            }
            try {
                getApplication<Application>()
                    .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .edit().clear().apply()
                getApplication<Application>()
                    .getSharedPreferences("volunteerlink_prefs", Context.MODE_PRIVATE)
                    .edit().clear().apply()
            } catch (_: Exception) {}
            _authState.value = AuthState.LoggedOut
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.LoggedOut
        }
    }

    fun updateCurrentUser(user: User) {
        _authState.value = AuthState.LoggedIn(user)
    }

    fun clearPendingGoogleUser() {
        _pendingGoogleUser.value = null
    }

    fun signInWithGoogle(context: Context, webClientId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val credentialManager = CredentialManager.create(context)
            try {
                val option = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()

                val credential = credentialManager.getCredential(context, request).credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val checkResult = UserService.checkGoogleUser(idToken)
                    if (checkResult.isFailure) {
                        _authState.value = AuthState.Error(
                            formatAuthError(checkResult.exceptionOrNull()?.message)
                        )
                    } else {
                        val existingUser = checkResult.getOrNull()
                        if (existingUser != null) {
                            userDao.insertUser(existingUser)
                            _authState.value = AuthState.LoggedIn(existingUser)
                        } else {
                            val firebaseUser = com.google.firebase.auth.FirebaseAuth
                                .getInstance().currentUser
                            _pendingGoogleUser.value = PendingGoogleUser(
                                uid = firebaseUser?.uid ?: "",
                                email = firebaseUser?.email ?: "",
                                name = firebaseUser?.displayName ?: ""
                            )
                            _authState.value = AuthState.LoggedOut
                        }
                    }
                } else {
                    _authState.value = AuthState.Error("Unexpected error during Google Sign-In. Please try again.")
                }
            } catch (e: GetCredentialCancellationException) {
                _authState.value = AuthState.Error("Sign-in was cancelled.")
            } catch (e: Exception) {
                // Credential Manager failed — signal UI to fall back to legacy Google Sign-In intent
                Log.w("AUTH_DEBUG", "Credential Manager failed, falling back to legacy GSI: ${e.message}")
                _authState.value = AuthState.Error("FALLBACK_GOOGLE_SIGNIN")
            }
        }
    }

    fun registerFcmToken(uid: String) {
        viewModelScope.launch {
            try {
                FirebaseMessaging.getInstance().token.await().let { token ->
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .update("fcmToken", token)
                        .await()
                    val user = userDao.getUser()
                    if (user != null) {
                        userDao.insertUser(user.copy(fcmToken = token))
                    }
                }
            } catch (e: Exception) {
                // Fail silently — token registration is non-critical
            }
        }
    }

    fun completeGoogleRegistration(role: UserRole) {
        viewModelScope.launch {
            val pending = _pendingGoogleUser.value ?: return@launch
            _authState.value = AuthState.Loading
            val result = UserService.registerGoogleUser(
                uid = pending.uid,
                email = pending.email,
                name = pending.name,
                role = role
            )
            if (result.isSuccess) {
                val user = result.getOrThrow()
                userDao.insertUser(user)
                _pendingGoogleUser.value = null
                _authState.value = AuthState.LoggedIn(user)
            } else {
                _authState.value = AuthState.Error(
                    formatAuthError(result.exceptionOrNull()?.message)
                )
            }
        }
    }

    fun getGoogleSignInIntent(context: Context, webClientId: String): android.content.Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    fun handleGoogleSignInResult(data: android.content.Intent?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                val idToken = account.idToken
                if (idToken == null) {
                    _authState.value = AuthState.Error("Google Sign-In failed. Please try again.")
                    return@launch
                }
                val checkResult = UserService.checkGoogleUser(idToken)
                if (checkResult.isFailure) {
                    _authState.value = AuthState.Error(formatAuthError(checkResult.exceptionOrNull()?.message))
                    return@launch
                }
                val existingUser = checkResult.getOrNull()
                if (existingUser != null) {
                    userDao.insertUser(existingUser)
                    _authState.value = AuthState.LoggedIn(existingUser)
                } else {
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    _pendingGoogleUser.value = PendingGoogleUser(
                        uid = firebaseUser?.uid ?: "",
                        email = firebaseUser?.email ?: "",
                        name = firebaseUser?.displayName ?: ""
                    )
                    _authState.value = AuthState.LoggedOut
                }
            } catch (e: com.google.android.gms.common.api.ApiException) {
                if (e.statusCode == com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                    _authState.value = AuthState.Error("Sign-in was cancelled.")
                } else {
                    _authState.value = AuthState.Error(formatAuthError(e.message))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(formatAuthError(e.message))
            }
        }
    }
}
