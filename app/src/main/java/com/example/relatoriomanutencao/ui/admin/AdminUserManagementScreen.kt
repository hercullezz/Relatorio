package com.example.relatoriomanutencao.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import com.parse.ParseUser
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun AdminUserContent(
    viewModel: MainViewModel
) {
    val users by viewModel.adminUsers.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchUsersForAdmin() }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

        if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Nenhum usuário encontrado.", 
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(users) { user ->
                    UserCard(user, viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserCard(user: ParseUser, viewModel: MainViewModel) {
    val context = LocalContext.current
    val name = user.getString("name") ?: user.username
    val email = user.getString("email") ?: ""
    val isApproved = user.getBoolean("isApproved")
    val isAdmin = user.getBoolean("isAdmin")
    val createdAt = user.createdAt
    val dateStr = createdAt?.let { 
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("America/Porto_Velho") }.format(it) 
    } ?: "-"

    val objectId = try { user.objectId ?: user.getObjectId() } catch (e: Exception) { null }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Excluir Usuário") },
            text = { Text("Tem certeza que deseja excluir o usuário $name? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (!objectId.isNullOrBlank()) {
                            viewModel.deleteUser(objectId)
                        }
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name and badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isAdmin) {
                        Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                            Text("ADMIN", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (isApproved) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("APROVADO", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("PENDENTE", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Info
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = email.ifBlank { "Sem e-mail" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Criado em: $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Aprovar/Reprovar
                Button(
                    onClick = {
                        if (objectId.isNullOrBlank()) Toast.makeText(context, "ID indisponível.", Toast.LENGTH_SHORT).show()
                        else viewModel.approveUser(objectId, !isApproved)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApproved) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
                        contentColor = if (isApproved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        if (isApproved) Icons.Default.Close else Icons.Default.CheckCircle, 
                        contentDescription = null, 
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isApproved) "Reprovar" else "Aprovar")
                }

                // Admin
                OutlinedButton(
                    onClick = {
                        if (objectId.isNullOrBlank()) Toast.makeText(context, "ID indisponível.", Toast.LENGTH_SHORT).show()
                        else viewModel.toggleAdmin(objectId, !isAdmin)
                    },
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isAdmin) "Remover Admin" else "Tornar Admin")
                }

                // Reset Password
                OutlinedButton(
                    onClick = {
                        if (email.isBlank()) Toast.makeText(context, "E-mail não disponível.", Toast.LENGTH_SHORT).show()
                        else {
                            viewModel.sendPasswordReset(email)
                            Toast.makeText(context, "Reset enviado para $email", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }

                // Delete
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
