package com.example.nexos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

private val BgPrimary     = Color(0xFF1C1C1E)
private val BgSecondary   = Color(0xFF2C2C2E)
private val BorderColor   = Color(0xFF3A3A3C)
private val TextPrimary   = Color(0xFFEBEBF5)
private val TextSecondary = Color(0xFF8E8E93)
private val TextHint      = Color(0xFF48484A)

@Composable
fun ListaClientesScreen(
    lojaId: String,
    onVoltar: () -> Unit,
    onNovoCliente: () -> Unit,
    onAbrirCliente: (Cliente) -> Unit
) {
    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    var busca by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("lojas").document(lojaId)
            .collection("clientes")
            .get()
            .addOnSuccessListener { resultado ->
                clientes = resultado.documents.mapNotNull { doc ->
                    doc.toObject(Cliente::class.java)?.copy(id = doc.id)
                }
                carregando = false
            }
            .addOnFailureListener { carregando = false }
    }

    val clientesFiltrados = if (busca.isBlank()) clientes
    else clientes.filter {
        it.nome.contains(busca, ignoreCase = true) ||
                it.telefone.contains(busca, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgSecondary)
                        .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Clientes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Campo de busca
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BgSecondary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp
                    ),
                    singleLine = true,
                    decorationBox = { inner ->
                        Box(modifier = Modifier.weight(1f)) {
                            if (busca.isEmpty()) {
                                Text("Buscar por nome ou telefone", color = TextHint, fontSize = 14.sp)
                            }
                            inner()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Conteúdo
            when {
                carregando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TextSecondary, strokeWidth = 2.dp)
                    }
                }

                clientes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextHint,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nenhum cliente cadastrado",
                                color = TextSecondary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toque no + para adicionar",
                                color = TextHint,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                clientesFiltrados.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhum resultado para \"$busca\"",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(clientesFiltrados) { cliente ->
                            CardCliente(cliente = cliente, onClick = { onAbrirCliente(cliente) })
                        }
                    }
                }
            }
        }

        // Botão flutuante novo cliente
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(TextPrimary)
                .clickable { onNovoCliente() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Novo Cliente",
                tint = BgPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CardCliente(cliente: Cliente, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgSecondary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BgPrimary)
                .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cliente.nome,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            if (cliente.telefone.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cliente.telefone,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null,
            tint = TextHint,
            modifier = Modifier
                .size(16.dp)
                .padding(0.dp)
                // Espelha o ícone para virar uma seta pra direita
                .then(Modifier.graphicsLayer { scaleX = -1f })
        )
    }
}