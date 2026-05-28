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
        // A lista ainda é carregada na primeira vez que o ViewModel é criado.
        refreshUsers()
    }

    // A função agora é pública para poder ser chamada pela UI.
    fun refreshUsers() {
        viewModelScope.launch {
            try {
                val userList = withContext(Dispatchers.IO) {
                    val query = ParseQuery.getQuery(ParseUser::class.java)

                    // ** A CORREÇÃO ESSENCIAL **
                    // Força a query a buscar os dados da rede primeiro.
                    query.cachePolicy = ParseQuery.CachePolicy.NETWORK_ELSE_CACHE

                    // Filtra para mostrar apenas usuários aprovados na lista
                    query.whereEqualTo("isApproved", true)
                    query.orderByAscending("username")
                    query.find()
                }
                users = userList
            } catch (e: ParseException) {
                _uiState.value = LoginUiState.Error("Falha ao buscar usuários: ${e.message}")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }

    fun login() {
        val userToLogin = selectedUser ?: run {
            _uiState.value = LoginUiState.Error("Por favor, selecione um usuário.")
            return
        }

        if (password.isBlank()) {
            _uiState.value = LoginUiState.Error("Por favor, digite a senha.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                // 1. Tenta fazer a autenticação com usuário e senha
                val loggedInUser = withContext(Dispatchers.IO) {
                    ParseUser.logIn(userToLogin.username, password)
                }

                // 2. Verifica se a conta está aprovada (esta verificação continua importante)
                val isApproved = loggedInUser.getBoolean("isApproved")

                if (isApproved) {
                    // 3. Se aprovado, o login é um sucesso
                    _uiState.value = LoginUiState.Success
                } else {
                    // 4. Se não aprovado, desloga imediatamente e mostra o erro
                    withContext(Dispatchers.IO) {
                        ParseUser.logOut()
                    }
                    _uiState.value = LoginUiState.Error("Sua conta aguarda aprovação de um administrador.")
                }

            } catch (e: ParseException) {
                val errorMessage = when (e.code) {
                    ParseException.OBJECT_NOT_FOUND -> "Usuário ou senha incorretos."
                    ParseException.CONNECTION_FAILED -> "Sem conexão com a internet."
                    else -> "Erro ao fazer login: ${e.message}"
                }
                _uiState.value = LoginUiState.Error(errorMessage)
            }
        }
    }
}
