package com.example.nexos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun ListaOSFinalizadasScreen(
    lojaId: String,
    onVoltar: () -> Unit,
    onAbrirOS: (OrdemServico) -> Unit
) {
    var listaOS by remember { mutableStateOf<List<OrdemServico>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("lojas").document(lojaId)
            .collection("ordens_servico")
            .whereIn("status", listOf("Pronto", "Entregue", "Cancelada"))
            .get()
            .addOnSuccessListener { resultado ->
                listaOS = resultado.documents.mapNotNull { doc ->
                    doc.toObject(OrdemServico::class.java)?.copy(id = doc.id)
                }
                carregando = false
            }
            .addOnFailureListener { carregando = false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
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
                text = "OS Finalizadas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (!carregando) {
                Text("${listaOS.size}", fontSize = 13.sp, color = TextSecondary)
            }
        }

        when {
            carregando -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TextSecondary, strokeWidth = 2.dp)
                }
            }

            listaOS.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TextHint,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Nenhuma OS finalizada", color = TextSecondary, fontSize = 15.sp)
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(listaOS) { os ->
                        CardOS(os = os, onClick = { onAbrirOS(os) })
                    }
                }
            }
        }
    }
}