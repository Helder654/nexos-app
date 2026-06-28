package com.example.nexos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

private val BgPrimary     = Color(0xFF1C1C1E)
private val BgSecondary   = Color(0xFF2C2C2E)
private val BorderColor   = Color(0xFF3A3A3C)
private val TextPrimary   = Color(0xFFEBEBF5)
private val TextSecondary = Color(0xFF8E8E93)
private val TextHint      = Color(0xFF48484A)

fun calcularVencimentoGarantia(dataEntrega: String, diasGarantia: Int): String {
    return try {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val data = formato.parse(dataEntrega) ?: return ""
        val calendario = Calendar.getInstance()
        calendario.time = data
        calendario.add(Calendar.DAY_OF_YEAR, diasGarantia)
        formato.format(calendario.time)
    } catch (e: Exception) { "" }
}

@Composable
fun TelaRelatorioOS(
    lojaId: String,
    os: OrdemServico,
    onVoltar: () -> Unit,
    onSalvar: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var descricaoServico        by remember { mutableStateOf(os.descricaoServico) }
    var gastos                  by remember { mutableStateOf(if (os.gastos > 0) os.gastos.toString() else "") }
    var dataEntrega             by remember { mutableStateOf(os.dataEntrega.filter { it.isDigit() }) } // só dígitos
    var diasGarantia            by remember { mutableStateOf("") }
    var dataVencimentoGarantia  by remember { mutableStateOf(os.dataVencimentoGarantia) }
    var salvando                by remember { mutableStateOf(false) }
    var carregando              by remember { mutableStateOf(true) }

    val uid = auth.currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        db.collection("tecnicos").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                diasGarantia = (doc.getLong("garantiaPadrao") ?: 90L).toString()
                carregando = false
            }
            .addOnFailureListener {
                diasGarantia = "90"
                carregando = false
            }
    }

    // Calcula vencimento da garantia quando data ou dias mudam
    LaunchedEffect(dataEntrega, diasGarantia) {
        if (dataEntrega.length == 8 && diasGarantia.isNotEmpty()) {
            val dataFormatada = buildString {
                dataEntrega.forEachIndexed { i, c -> if (i == 2 || i == 4) append('/'); append(c) }
            }
            dataVencimentoGarantia = calcularVencimentoGarantia(dataFormatada, diasGarantia.toIntOrNull() ?: 90)
        } else {
            dataVencimentoGarantia = ""
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
            Text("Relatório da OS", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        if (carregando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TextSecondary, strokeWidth = 2.dp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card resumo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSecondary)
                        .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text("Resumo da OS", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(os.cliente.nome, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${os.console.marca} ${os.console.modelo}".trim(), fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Status: ${os.status}", fontSize = 12.sp, color = TextHint)
                        Text("R$ ${"%.2f".format(os.valorTotal)}", fontSize = 12.sp, color = TextHint)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Relatório", fontSize = 13.sp, color = TextSecondary)

                CampoFormulario(
                    label = "Descrição do serviço realizado",
                    valor = descricaoServico,
                    onValorChange = { descricaoServico = it },
                    icone = Icons.Default.Description,
                    placeholder = "Descreva o que foi feito",
                    linhasMaximas = 5
                )

                CampoFormulario(
                    label = "Gastos com peças/serviço (R$)",
                    valor = gastos,
                    onValorChange = { gastos = it },
                    icone = Icons.Default.AttachMoney,
                    placeholder = "0.00"
                )

                // Divisor
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(BorderColor))
                Text("Entrega e Garantia", fontSize = 13.sp, color = TextSecondary)

                CampoFormulario(
                    label = "Data de entrega ao cliente",
                    valor = dataEntrega,
                    onValorChange = { dataEntrega = it.filter { c -> c.isDigit() }.take(8) },
                    icone = Icons.Default.CalendarToday,
                    placeholder = "DD/MM/AAAA",
                    visualTransformation = DataVisualTransformation()
                )

                CampoFormulario(
                    label = "Dias de garantia",
                    valor = diasGarantia,
                    onValorChange = { if (it.all { c -> c.isDigit() }) diasGarantia = it },
                    icone = Icons.Default.Shield,
                    placeholder = "90"
                )

                // Card vencimento garantia
                if (dataVencimentoGarantia.isNotEmpty()) {
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
                        Text("Vencimento da garantia", fontSize = 13.sp, color = TextSecondary)
                        Text(dataVencimentoGarantia, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botão salvar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (salvando) BgSecondary else TextPrimary),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.Button(
                        onClick = {
                            if (dataEntrega.length < 8) {
                                Toast.makeText(context, "Preencha a data de entrega completa", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            salvando = true
                            val dataFormatada = buildString {
                                dataEntrega.forEachIndexed { i, c -> if (i == 2 || i == 4) append('/'); append(c) }
                            }
                            db.collection("lojas").document(lojaId)
                                .collection("ordens_servico")
                                .document(os.id)
                                .update(
                                    mapOf(
                                        "descricaoServico"       to descricaoServico,
                                        "gastos"                 to (gastos.toDoubleOrNull() ?: 0.0),
                                        "dataEntrega"            to dataFormatada,
                                        "dataVencimentoGarantia" to dataVencimentoGarantia
                                    )
                                )
                                .addOnSuccessListener {
                                    salvando = false
                                    Toast.makeText(context, "Relatório salvo!", Toast.LENGTH_SHORT).show()
                                    onSalvar()
                                }
                                .addOnFailureListener {
                                    salvando = false
                                    Toast.makeText(context, "Erro ao salvar relatório", Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = TextPrimary,
                            contentColor = BgPrimary,
                            disabledContainerColor = BgSecondary,
                            disabledContentColor = TextSecondary
                        ),
                        enabled = !salvando
                    ) {
                        if (salvando) {
                            CircularProgressIndicator(color = TextSecondary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Salvar relatório", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}