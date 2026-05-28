package com.example.relatoriomanutencao.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parse.ParseACL
import com.parse.ParseException
import com.parse.ParseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("") // Novo campo para confirmação
    var selectedShift by mutableStateOf<Pair<String, Int>?>(null)

    fun clearError() {
        if (_uiState.value is SignUpUiState.Error) {
            _uiState.value = SignUpUiState.Idle
        }
    }

    // 4. Função principal que a UI chamará para iniciar o cadastro
    fun signUp() {
        // Validação básica para garantir que todos os campos foram preenchidos
        if (name.isBlank() || username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || selectedShift == null) {
            _uiState.value = SignUpUiState.Error("Por favor, preencha todos os campos.")
            return
        }

        // Nova validação: Verifica se as senhas são iguais
        if (password != confirmPassword) {
            _uiState.value = SignUpUiState.Error("As senhas não conferem.")
            return
        }

        viewModelScope.launch {
            _uiState.value = SignUpUiState.Loading

            try {
                val user = ParseUser()
                user.username = username.trim()
                user.email = email.trim()
                user.setPassword(password)
                user.put("name", name.trim())
                // O schema do Back4App tem DOIS campos de turno:
                // - "ShiftId" (S maiúsculo) = required:true  → OBRIGATÓRIO
                // - "shiftId" (s minúsculo) = required:false → opcional
                // Ambos precisam ser enviados para o cadastro não ser rejeitado.
                val shiftIdValue = selectedShift!!.second as Int
                user.put("ShiftId", shiftIdValue)
                user.put("shiftId", shiftIdValue)

                user.put("isApproved", false)
                user.put("isAdmin", false)

                val acl = ParseACL()
                acl.publicReadAccess = true
                user.acl = acl

                withContext(Dispatchers.IO) {
                    user.signUp()
                }

                _uiState.value = SignUpUiState.Success

            } catch (e: ParseException) {
                android.util.Log.e("SignUpViewModel", "Parse error code=${e.code} msg=${e.message}")
                val errorMessage = when (e.code) {
                    ParseException.USERNAME_TAKEN      -> "Este usuário já está cadastrado."
                    ParseException.EMAIL_TAKEN         -> "Este e-mail já está sendo usado."
                    ParseException.CONNECTION_FAILED   -> "Sem conexão com a internet."
                    ParseException.INVALID_QUERY       -> "Erro de configuração no servidor. Contate o administrador."
                    142 /* VALIDATION_ERROR */         -> "Dados inválidos. Verifique o turno selecionado e tente novamente."
                    else -> "Erro ao criar conta (${e.code}): ${e.message}"
                }
                _uiState.value = SignUpUiState.Error(errorMessage)
            } catch (e: Exception) {
                android.util.Log.e("SignUpViewModel", "Generic error: ${e.message}", e)
                _uiState.value = SignUpUiState.Error(e.message ?: "Ocorreu um erro desconhecido.")
            }
        }
    }
}