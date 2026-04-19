package com.example.relatoriomanutencao.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.relatoriomanutencao.R
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Consistência com Login)
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "App Logo",
                        modifier = Modifier.padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Criar Nova Conta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Cartão de Cadastro
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState is SignUpUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        } else {
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
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = viewModel.name,
        onValueChange = { viewModel.name = it },
        label = { Text("Nome Completo") },
        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = viewModel.username,
        onValueChange = { viewModel.username = it },
        label = { Text("Usuário") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = viewModel.email,
        onValueChange = { viewModel.email = it },
        label = { Text("E-mail") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = viewModel.password,
        onValueChange = { viewModel.password = it },
        label = { Text("Senha") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = viewModel.confirmPassword,
        onValueChange = { viewModel.confirmPassword = it },
        label = { Text("Confirmar Senha") },
        leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                Icon(
                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = onDropdownExpandedChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            value = viewModel.selectedShift?.first ?: "Turno de Trabalho",
            onValueChange = {},
            readOnly = true,
            label = { Text("Turno") },
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
            shape = RoundedCornerShape(12.dp)
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
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = viewModel.name.isNotBlank() &&
                viewModel.username.isNotBlank() &&
                viewModel.email.isNotBlank() &&
                viewModel.password.isNotBlank() &&
                viewModel.confirmPassword.isNotBlank() &&
                viewModel.selectedShift != null
    ) {
        Text("SOLICITAR CADASTRO", fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) {
        Text("Já tem uma conta? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Faça o login", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}


@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    RelatorioManutencaoTheme {
        SignUpScreen(onSignUpSuccess = {}, onNavigateToLogin = {})
    }
}
