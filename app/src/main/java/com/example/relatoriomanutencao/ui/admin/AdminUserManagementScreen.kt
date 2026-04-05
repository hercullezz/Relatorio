package com.example.relatoriomanutencao.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import com.parse.ParseUser
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun AdminUserContent(
    viewModel: MainViewModel
) {
    val users by viewModel.adminUsers.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("America/Porto_Velho") }

    LaunchedEffect(Unit) { viewModel.fetchUsersForAdmin() }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

        if (users.isEmpty()) {
            Text("Nenhum usuário encontrado.", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                items(users) { user ->
                    UserRow(user, viewModel)
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun UserRow(user: ParseUser, viewModel: MainViewModel) {
    val context = LocalContext.current
    val name = user.getString("name") ?: user.username
    val email = user.getString("email") ?: ""
    val isApproved = user.getBoolean("isApproved")
    val isAdmin = user.getBoolean("isAdmin")
    val createdAt = user.createdAt
    val dateStr = createdAt?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("America/Porto_Velho") }.format(it) } ?: "-"

    // Safe objectId extraction (ParseUser may have getObjectId())
    val objectId = try {
        user.objectId ?: user.getObjectId()
    } catch (e: Exception) {
        null
    }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text("@${user.username} • $email", style = MaterialTheme.typography.bodySmall)
                Text("Criado: $dateStr", style = MaterialTheme.typography.bodySmall)
            }

            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Row {
                    TextButton(onClick = {
                        if (objectId.isNullOrBlank()) {
                            Toast.makeText(context, "ID do usuário não disponível.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.approveUser(objectId, !isApproved)
                        }
                    }) {
                        Text(if (isApproved) "Reprovar" else "Aprovar")
                    }
                }
                Row {
                    TextButton(onClick = {
                        if (objectId.isNullOrBlank()) {
                            Toast.makeText(context, "ID do usuário não disponível.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.toggleAdmin(objectId, !isAdmin)
                        }
                    }) {
                        Text(if (isAdmin) "Remover Admin" else "Tornar Admin")
                    }
                }
                Row {
                    TextButton(onClick = {
                        if (email.isBlank()) {
                            Toast.makeText(context, "E-mail não disponível para reset.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.sendPasswordReset(email)
                            Toast.makeText(context, "Solicitação de reset enviada para $email", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Enviar Reset")
                    }
                }
            }
        }
    }
}
