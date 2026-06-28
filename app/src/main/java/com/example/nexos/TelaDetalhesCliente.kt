package com.example.nexos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.platform.LocalContext
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
fun TelaDetalhesCliente(
    lojaId: String,
    cliente: Cliente,
    onVoltar: () -> Unit,
    onAbrirOS: (OrdemServico) -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var nome     by remember { mutableStateOf(cliente.nome) }
    var telefone by remember { mutableStateOf(cliente.telefone) }
    var email    by remember { mutableStateOf(cliente.email) }
    var endereco by remember { mutableStateOf(cliente.endereco) }
    var salvando by remember { mutableStateOf(false) }

    var historicoOS    by remember { mutableStateOf<List<OrdemServico>>(emptyList()) }
    var carregandoOS   by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("lojas").document(lojaId)
            .collection("ordens_servico")
            .whereEqualTo("cliente.id", cliente.id)
            .get()
            .addOnSuccessListener { resultado ->
                historicoOS = resultado.documents.mapNotNull { doc ->
                    doc.toObject(OrdemServico::class.java)?.copy(id = doc.id)
                }
                carregandoOS = false
            }
            .addOnFailureListener { carregandoOS = false }
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
                text = "Detalhes do Cliente",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Seção: Dados do Cliente
            item {
                Text(
                    text = "Dados do cliente",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            item {
                CampoFormulario(
                    label = "Nome completo",
                    valor = nome,
                    onValorChange = { nome = it },
                    icone = Icons.Default.Person,
                    placeholder = "Nome do cliente"
                )
            }

            item {
                CampoFormulario(
                    label = "Telefone",
                    valor = telefone,
                    onValorChange = { telefone = aplicarMascaraTelefone(it) },
                    icone = Icons.Default.Phone,
                    placeholder = "(00) 00000-0000"
                )
            }

            item {
                CampoFormulario(
                    label = "E-mail",
                    valor = email,
                    onValorChange = { email = it },
                    icone = Icons.Default.Email,
                    placeholder = "email@exemplo.com"
                )
            }

            item {
                CampoFormulario(
                    label = "Endereço",
                    valor = endereco,
                    onValorChange = { endereco = it },
                    icone = Icons.Default.LocationOn,
                    placeholder = "Rua, número, bairro"
                )
            }

            // Botão salvar
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (salvando) BgSecondary else TextPrimary)
                        .clickable(enabled = !salvando) {
                            if (nome.isEmpty() || telefone.isEmpty()) {
                                Toast.makeText(context, "Nome e telefone são obrigatórios", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            salvando = true
                            db.collection("lojas").document(lojaId)
                                .collection("clientes")
                                .document(cliente.id)
                                .update(
                                    mapOf(
                                        "nome"     to nome,
                                        "telefone" to telefone,
                                        "email"    to email,
                                        "endereco" to endereco
                                    )
                                )
                                .addOnSuccessListener {
                                    salvando = false
                                    Toast.makeText(context, "Cliente atualizado!", Toast.LENGTH_SHORT).show()
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
                        CircularProgressIndicator(
                            color = TextSecondary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Salvar alterações",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = BgPrimary
                        )
                    }
                }
            }

            // Divisor + seção histórico
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(BorderColor)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Histórico de OS${if (!carregandoOS) " (${historicoOS.size})" else ""}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Estados do histórico
            if (carregandoOS) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = TextSecondary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else if (historicoOS.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgSecondary)
                            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma OS encontrada para este cliente",
                            fontSize = 14.sp,
                            color = TextHint
                        )
                    }
                }
            } else {
                items(historicoOS) { os ->
                    CardOS(os = os, onClick = { onAbrirOS(os) })
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

        }
    }
}