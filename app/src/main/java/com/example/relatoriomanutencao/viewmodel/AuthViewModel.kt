package com.example.relatoriomanutencao.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val user = auth.currentUser
        _authState.value = AuthState(isLoggedIn = user != null)
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState(error = "Email e senha não podem ser vazios")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                Log.d("AuthViewModel", "Login success")
                _authState.value = AuthState(isLoggedIn = true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error", e)
                _authState.value = AuthState(error = "Erro no login: ${e.message}")
            }
        }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState(error = "Email e senha não podem ser vazios")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                Log.d("AuthViewModel", "SignUp success")
                _authState.value = AuthState(isLoggedIn = true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "SignUp error", e)
                _authState.value = AuthState(error = "Erro no cadastro: ${e.message}")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                Log.d("AuthViewModel", "Google Login success")
                _authState.value = AuthState(isLoggedIn = true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google Login error", e)
                _authState.value = AuthState(error = "Erro no login com Google: ${e.message}")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState()
    }
}
