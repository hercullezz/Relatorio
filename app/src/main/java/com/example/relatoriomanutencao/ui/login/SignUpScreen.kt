package com.example.relatoriomanutencao.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.relatoriomanutencao.ui.theme.RelatorioManutencaoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignUpViewModel = viewModel() // 1. Integra o ViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val shifts = listOf("1º Turno (A)" to 1, "2º Turno (B)" to 2, "3º Turno (C)" to 3)

    // 2. Escuta o estado da UI para mostrar mensagens ou navegar
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SignUpUiState.Success -> {
                snackbarHostState.showSnackbar("Solicitação de cadastro enviada com sucesso! Aguarde a aprovação.")
                onSignUpSuccess() // Navega de volta para o login
            }
            is SignUpUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> { /* Não faz nada para Idle ou Loading */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState is SignUpUiState.Loading) {
                CircularProgressIndicator() // Mostra o loading
            } else {
                // Esconde o formulário durante o loading
                SignUpForm(
                    viewModel = viewModel,
                    shifts = shifts,
                    isDropdownExpanded = isDropdownExpanded,
                    onDropdownExpandedChange = { isDropdownExpanded = it },
                    onSignUpClick = { viewModel.signUp() },
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignUpForm(
    viewModel: SignUpViewModel,
    shifts: List<Pair<String, Int>>,
    isDropdownExpanded: Boolean,
    onDropdownExpandedChange: (Boolean) -> Unit,
    onSignUpClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Text("Criar Nova Conta", style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.height(32.dp))

    // 3. Conecta os campos da UI às variáveis do ViewModel
    OutlinedTextField(
        value = viewModel.name,
        onValueChange = { viewModel.name = it },
        label = { Text("Nome Completo") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = viewModel.username,
        onValueChange = { viewModel.username = it },
        label = { Text("Usuário") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = viewModel.password,
        onValueChange = { viewModel.password = it },
        label = { Text("Senha") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))

    // Novo campo para confirmar a senha
    OutlinedTextField(
        value = viewModel.confirmPassword,
        onValueChange = { viewModel.confirmPassword = it },
        label = { Text("Confirmar Senha") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = onDropdownExpandedChange,
        modifier = Modifier.fillMaxWidth() // O modificador de preenchimento vai aqui
    ) {
        OutlinedTextField(
            // O modificador .menuAnchor() é aplicado diretamente aqui pelo escopo do Box
            modifier = Modifier.menuAnchor(),
            value = viewModel.selectedShift?.first ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Turno de Trabalho") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
        )
        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { onDropdownExpandedChange(false) },
        ) {
            shifts.forEach { shift ->
                DropdownMenuItem(
                    text = { Text(shift.first) },
                    onClick = {
                        viewModel.selectedShift = shift
                        onDropdownExpandedChange(false)
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onSignUpClick,
        modifier = Modifier.fillMaxWidth(),
        // Lógica do botão atualizada para incluir o novo campo
        enabled = viewModel.name.isNotBlank() &&
                viewModel.username.isNotBlank() &&
                viewModel.password.isNotBlank() &&
                viewModel.confirmPassword.isNotBlank() &&
                viewModel.selectedShift != null
    ) {
        Text("SOLICITAR CADASTRO")
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = onNavigateToLogin) {
        Text("Já tem uma conta? Faça o login")
    }
}


@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    RelatorioManutencaoTheme {
        SignUpScreen(onSignUpSuccess = {}, onNavigateToLogin = {})
    }
}
