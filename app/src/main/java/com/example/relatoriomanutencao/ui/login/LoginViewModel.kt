package com.example.relatoriomanutencao.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parse.ParseException
import com.parse.ParseQuery
import com.parse.ParseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {

    var users by mutableStateOf<List<ParseUser>>(emptyList())
    var selectedUser by mutableStateOf<ParseUser?>(null)
    var password by mutableStateOf("")

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                val userList = withContext(Dispatchers.IO) {
                    val query = ParseQuery.getQuery(ParseUser::class.java)
                    query.orderByAscending("username")
                    query.find()
                }
                users = userList
            } catch (e: ParseException) {
                _uiState.value = LoginUiState.Error("Falha ao buscar usuários: ${e.message}")
            }
        }
    }

    fun login() {
        val userToLogin = selectedUser ?: run {
            _uiState.value = LoginUiState.Error("Por favor, selecione um usuário.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                withContext(Dispatchers.IO) {
                    // O login no Parse é feito com o username, não com o nome.
                    ParseUser.logIn(userToLogin.username, password)
                }
                _uiState.value = LoginUiState.Success
            } catch (e: ParseException) {
                _uiState.value = LoginUiState.Error(e.message ?: "Ocorreu um erro desconhecido.")
            }
        }
    }
}
