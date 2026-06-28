package com.example.nexos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore

private val BgPrimary     = Color(0xFF1C1C1E)
private val BgSecondary   = Color(0xFF2C2C2E)
private val BorderColor   = Color(0xFF3A3A3C)
private val TextPrimary   = Color(0xFFEBEBF5)
private val TextSecondary = Color(0xFF8E8E93)
private val TextHint      = Color(0xFF48484A)
private val ColorErro     = Color(0xFFFF453A)

@Composable
fun DetalhesOSScreen(
    lojaId: String,
    os: OrdemServico,
    onVoltar: () -> Unit,
    onFinalizada: (OrdemServico) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var sintomasAnalisados by remember { mutableStateOf(os.sintomasAnalisados) }
    var previsaoEntrega    by remember { mutableStateOf(os.previsaoEntrega) }
    var valorTotal         by remember { mutableStateOf(if (os.valorTotal > 0) os.valorTotal.toString() else "") }
    var garantia           by remember { mutableStateOf(os.garantia) }
    var salvando           by remember { mutableStateOf(false) }
    var mostrarDialogoCancelar by remember { mutableStateOf(false) }

    val sequenciaStatus = listOf("Aguardando análise", "Em orçamento", "Aguardando aprovação", "Em reparo", "Pronto", "Entregue")
    val indexAtual = sequenciaStatus.indexOf(os.status)
    val proximoStatus = if (indexAtual < sequenciaStatus.size - 1) sequenciaStatus[indexAtual + 1] else null

    val osRef = db.collection("lojas").document(lojaId).collection("ordens_servico").document(os.id)

    // Diálogo cancelar
    if (mostrarDialogoCancelar) {
        Dialog(onDismissRequest = { mostrarDialogoCancelar = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgSecondary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Text("Cancelar OS", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tem certeza? A OS será movida para finalizadas com status Cancelada.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Botão voltar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgPrimary)
                            .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                            .clickable { mostrarDialogoCancelar = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Voltar", fontSize = 14.sp, color = TextPrimary)
                    }
                    // Botão confirmar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF3A1A1A))
                            .border(0.5.dp, Color(0xFF5A2A2A), RoundedCornerShape(10.dp))
                            .clickable {
                                mostrarDialogoCancelar = false
                                salvando = true
                                osRef.update("status", "Cancelada")
                                    .addOnSuccessListener {
                                        salvando = false
                                        Toast.makeText(context, "OS cancelada", Toast.LENGTH_SHORT).show()
                                        onFinalizada(os.copy(status = "Cancelada"))
                                    }
                                    .addOnFailureListener {
                                        salvando = false
                                        Toast.makeText(context, "Erro ao cancelar", Toast.LENGTH_SHORT).show()
                                    }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Confirmar", fontSize = 14.sp, color = ColorErro, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
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
            Text("Detalhes da OS", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status atual
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgSecondary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Status atual", fontSize = 13.sp, color = TextSecondary)
                StatusBadge(status = os.status)
            }

            // Card cliente
            InfoCard(titulo = "Cliente") {
                Text(os.cliente.nome, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                if (os.cliente.telefone.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(os.cliente.telefone, fontSize = 13.sp, color = TextSecondary)
                }
            }

            // Card console
            InfoCard(titulo = "Console") {
                Text("${os.console.marca} ${os.console.modelo}".trim(), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                if (os.console.numeroSerie.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("S/N: ${os.console.numeroSerie}", fontSize = 13.sp, color = TextSecondary)
                }
            }

            // Card defeito
            InfoCard(titulo = "Defeito reclamado") {
                Text(os.defeitoReclamado, fontSize = 14.sp, color = TextPrimary)
            }

            // Divisor
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(BorderColor))
            Spacer(modifier = Modifier.height(4.dp))

            Text("Informações editáveis", fontSize = 13.sp, color = TextSecondary)

            CampoFormulario(
                label = "Sintomas analisados pelo técnico",
                valor = sintomasAnalisados,
                onValorChange = { sintomasAnalisados = it },
                icone = Icons.Default.Search,
                placeholder = "Descreva os sintomas observados",
                linhasMaximas = 4
            )
            CampoFormulario(
                label = "Previsão de entrega",
                valor = previsaoEntrega,
                onValorChange = { previsaoEntrega = it },
                icone = Icons.Default.CalendarToday,
                placeholder = "DD/MM/AAAA"
            )
            CampoFormulario(
                label = "Valor total (R$)",
                valor = valorTotal,
                onValorChange = { valorTotal = it },
                icone = Icons.Default.AttachMoney,
                placeholder = "0.00"
            )
            CampoFormulario(
                label = "Garantia",
                valor = garantia,
                onValorChange = { garantia = it },
                icone = Icons.Default.Shield,
                placeholder = "Ex: 90 dias"
            )

            // Botão salvar alterações
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (salvando) BgSecondary else TextPrimary)
                    .clickable(enabled = !salvando) {
                        salvando = true
                        osRef.update(
                            mapOf(
                                "sintomasAnalisados" to sintomasAnalisados,
                                "previsaoEntrega"    to previsaoEntrega,
                                "valorTotal"         to (valorTotal.toDoubleOrNull() ?: 0.0),
                                "garantia"           to garantia
                            )
                        )
                            .addOnSuccessListener {
                                salvando = false
                                Toast.makeText(context, "OS atualizada!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                salvando = false
                                Toast.makeText(context, "Erro ao salvar", Toast.LENGTH_SHORT).show()
                            }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (salvando) {
                    CircularProgressIndicator(color = TextSecondary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Salvar alterações", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = BgPrimary)
                }
            }

            // Divisor
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(BorderColor))

            // Botão avançar status
            if (proximoStatus != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSecondary)
                        .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = !salvando) {
                            salvando = true
                            osRef.update("status", proximoStatus)
                                .addOnSuccessListener {
                                    salvando = false
                                    Toast.makeText(context, "Status: $proximoStatus", Toast.LENGTH_SHORT).show()
                                    if (proximoStatus == "Entregue") {
                                        onFinalizada(os.copy(status = "Entregue"))
                                    } else {
                                        onVoltar()
                                    }
                                }
                                .addOnFailureListener {
                                    salvando = false
                                    Toast.makeText(context, "Erro ao atualizar status", Toast.LENGTH_SHORT).show()
                                }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Avançar para: $proximoStatus",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            // Botão cancelar OS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A1A1A))
                    .border(0.5.dp, Color(0xFF3A2A2A), RoundedCornerShape(12.dp))
                    .clickable(enabled = !salvando) { mostrarDialogoCancelar = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Cancelar OS", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = ColorErro)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InfoCard(titulo: String, conteudo: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgSecondary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(titulo, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
        conteudo()
    }
}