package com.example.relatoriomanutencao.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parse.ParseACL
import com.parse.ParseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Define os possíveis estados da UI para o cadastro
sealed interface SignUpUiState {
    object Idle : SignUpUiState // Estado inicial
    object Loading : SignUpUiState // Carregando
    object Success : SignUpUiState // Sucesso no cadastro
    data class Error(val message: String) : SignUpUiState // Erro
}

class SignUpViewModel : ViewModel() {

    // 2. StateFlow para gerenciar o estado da UI de forma reativa
    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // 3. Propriedades que a UI vai preencher (Two-way data binding)
    var name by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("") // Novo campo para confirmação
    var selectedShift by mutableStateOf<Pair<String, Int>?>(null)

    // 4. Função principal que a UI chamará para iniciar o cadastro
    fun signUp() {
        // Validação básica para garantir que todos os campos foram preenchidos
        if (name.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank() || selectedShift == null) {
            _uiState.value = SignUpUiState.Error("Por favor, preencha todos os campos.")
            return
        }

        // Nova validação: Verifica se as senhas são iguais
        if (password != confirmPassword) {
            _uiState.value = SignUpUiState.Error("As senhas não conferem.")
            return
        }

        viewModelScope.launch {
            _uiState.value = SignUpUiState.Loading // Muda o estado para Carregando

            try {
                val user = ParseUser()
                user.username = username
                user.setPassword(password)
                user.put("name", name)
                user.put("ShiftId", selectedShift!!.second) // Usa o ID numérico do turno

                user.put("isApproved", false) // O usuário começa como não aprovado
                user.put("isAdmin", false)   // E também não é um admin

                // ** A CORREÇÃO DA ACL (LISTA DE CONTROLE DE ACESSO) **
                val acl = ParseACL()
                acl.publicReadAccess = true
                user.acl = acl

                // A chamada para o backend é assíncrona
                user.signUp()

                // Se a chamada acima não lançar exceção, o cadastro foi bem-sucedido
                _uiState.value = SignUpUiState.Success

            } catch (e: Exception) {
                // Se ocorrer um erro (ex: usuário já existe, sem internet), captura a exceção
                _uiState.value = SignUpUiState.Error(e.message ?: "Ocorreu um erro desconhecido.")
            }
        }
    }
}