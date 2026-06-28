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
import androidx.compose.material.icons.filled.Build
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

// Cores de status
private val StatusAguardando  = Color(0xFF636366) // cinza
private val StatusOrcamento   = Color(0xFF9B6B00) // âmbar escuro
private val StatusAprovacao   = Color(0xFF1A4A8A) // azul escuro
private val StatusReparo      = Color(0xFF1A5C2A) // verde escuro
private val StatusCancelada   = Color(0xFF7A1A1A) // vermelho escuro

private fun corStatus(status: String): Color = when (status) {
    "Aguardando análise"    -> StatusAguardando
    "Em orçamento"          -> StatusOrcamento
    "Aguardando aprovação"  -> StatusAprovacao
    "Em reparo"             -> StatusReparo
    "Cancelada"             -> StatusCancelada
    else                    -> StatusAguardando
}

private fun corTextoStatus(status: String): Color = when (status) {
    "Aguardando análise"    -> Color(0xFFAEAEB2)
    "Em orçamento"          -> Color(0xFFFFD060)
    "Aguardando aprovação"  -> Color(0xFF6EB0FF)
    "Em reparo"             -> Color(0xFF6EDF8A)
    "Cancelada"             -> Color(0xFFFF6B6B)
    else                    -> Color(0xFFAEAEB2)
}

@Composable
fun ListaOSAbertasScreen(
    lojaId: String,
    onVoltar: () -> Unit,
    onNovaOS: () -> Unit,
    onAbrirOS: (OrdemServico) -> Unit
) {
    var listaOS by remember { mutableStateOf<List<OrdemServico>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("lojas").document(lojaId)
            .collection("ordens_servico")
            .whereNotIn("status", listOf("Pronto", "Entregue", "Cancelada"))
            .get()
            .addOnSuccessListener { resultado ->
                listaOS = resultado.documents.mapNotNull { doc ->
                    doc.toObject(OrdemServico::class.java)?.copy(id = doc.id)
                }
                carregando = false
            }
            .addOnFailureListener { carregando = false }
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
                    text = "OS Abertas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (!carregando) {
                    Text(
                        text = "${listaOS.size}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
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
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = TextHint,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Nenhuma OS aberta", color = TextSecondary, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Toque no + para criar uma nova", color = TextHint, fontSize = 13.sp)
                        }
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
                        items(listaOS) { os ->
                            CardOS(os = os, onClick = { onAbrirOS(os) })
                        }
                    }
                }
            }
        }

        // Botão flutuante nova OS
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(TextPrimary)
                .clickable { onNovaOS() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Nova OS",
                tint = BgPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CardOS(os: OrdemServico, onClick: () -> Unit) {
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
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = os.cliente.nome,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                StatusBadge(status = os.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${os.console.marca} ${os.console.modelo}".trim(),
                fontSize = 13.sp,
                color = TextSecondary
            )
            if (os.dataEntrada.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Entrada: ${os.dataEntrada}",
                    fontSize = 12.sp,
                    color = TextHint
                )
            }
            if (os.defeitoReclamado.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = os.defeitoReclamado,
                    fontSize = 12.sp,
                    color = TextHint,
                    maxLines = 2
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null,
            tint = TextHint,
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer { scaleX = -1f }
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(corStatus(status))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = corTextoStatus(status)
        )
    }
}