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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

private val BgPrimary     = Color(0xFF1C1C1E)
private val BgSecondary   = Color(0xFF2C2C2E)
private val BorderColor   = Color(0xFF3A3A3C)
private val TextPrimary   = Color(0xFFEBEBF5)
private val TextSecondary = Color(0xFF8E8E93)
private val TextHint      = Color(0xFF48484A)

// Máscara de data via VisualTransformation — estado armazena só dígitos
class DataVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(8)
        val formatted = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 2 || i == 4) append('/')
                append(c)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                return o + when {
                    o > 4 -> 2
                    o > 2 -> 1
                    else  -> 0
                }
            }
            override fun transformedToOriginal(offset: Int): Int {
                val o = offset.coerceIn(0, formatted.length)
                return (o - formatted.take(o).count { it == '/' }).coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

// Máscara de valor monetário via VisualTransformation — estado armazena só dígitos
class ValorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(10)
        val numero = digits.toLongOrNull() ?: 0L
        val reais = numero / 100
        val centavos = numero % 100
        val formatted = "R$ %d,%02d".format(reais, centavos)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formatted.length
            override fun transformedToOriginal(offset: Int): Int = digits.length
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@Composable
fun FormularioOSScreen(
    lojaId: String,
    cliente: Cliente,
    onVoltar: () -> Unit,
    onSalvar: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var marca            by remember { mutableStateOf("") }
    var modelo           by remember { mutableStateOf("") }
    var numeroSerie      by remember { mutableStateOf("") }
    var defeitoReclamado by remember { mutableStateOf("") }
    var sintomasAnalisados by remember { mutableStateOf("") }
    var previsaoEntrega  by remember { mutableStateOf("") } // só dígitos
    var valorTotal       by remember { mutableStateOf("") } // só dígitos (centavos)
    var garantia         by remember { mutableStateOf("") }
    var salvando         by remember { mutableStateOf(false) }

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
            Column {
                Text(
                    text = "Nova OS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = cliente.nome,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Card resumo cliente
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgSecondary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgPrimary)
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = cliente.nome, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    if (cliente.telefone.isNotEmpty()) {
                        Text(text = cliente.telefone, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Seção Console
            Text(text = "Dados do console", fontSize = 13.sp, color = TextSecondary)

            CampoFormulario(
                label = "Marca",
                valor = marca,
                onValorChange = { marca = it },
                icone = Icons.Default.Gamepad,
                placeholder = "Ex: Sony, Microsoft, Nintendo"
            )
            CampoFormulario(
                label = "Modelo",
                valor = modelo,
                onValorChange = { modelo = it },
                icone = Icons.Default.Gamepad,
                placeholder = "Ex: PlayStation 5, Xbox Series X"
            )
            CampoFormulario(
                label = "Número de série",
                valor = numeroSerie,
                onValorChange = { numeroSerie = it },
                icone = Icons.Default.Tag,
                placeholder = "S/N (opcional)"
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Seção OS
            Text(text = "Dados da OS", fontSize = 13.sp, color = TextSecondary)

            CampoFormulario(
                label = "Defeito reclamado pelo cliente",
                valor = defeitoReclamado,
                onValorChange = { defeitoReclamado = it },
                icone = Icons.Default.Warning,
                placeholder = "Descreva o defeito relatado",
                linhasMaximas = 4
            )
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
                onValorChange = { previsaoEntrega = it.filter { c -> c.isDigit() }.take(8) },
                icone = Icons.Default.CalendarToday,
                placeholder = "DD/MM/AAAA",
                visualTransformation = DataVisualTransformation()
            )
            CampoFormulario(
                label = "Valor total",
                valor = valorTotal,
                onValorChange = { valorTotal = it.filter { c -> c.isDigit() }.take(10) },
                icone = Icons.Default.AttachMoney,
                placeholder = "R$ 0,00",
                visualTransformation = ValorVisualTransformation()
            )
            CampoFormulario(
                label = "Garantia",
                valor = garantia,
                onValorChange = { garantia = it },
                icone = Icons.Default.Shield,
                placeholder = "Ex: 90 dias"
            )

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
                        if (marca.isEmpty() || modelo.isEmpty() || defeitoReclamado.isEmpty()) {
                            Toast.makeText(context, "Preencha marca, modelo e defeito", Toast.LENGTH_SHORT).show()
                        } else {
                            salvando = true
                            val console = Console(marca = marca, modelo = modelo, numeroSerie = numeroSerie)
                            val dataEntrada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                            val valorDouble = (valorTotal.toLongOrNull() ?: 0L) / 100.0
                            val novaOS = OrdemServico(
                                cliente = cliente,
                                console = console,
                                defeitoReclamado = defeitoReclamado,
                                sintomasAnalisados = sintomasAnalisados,
                                dataEntrada = dataEntrada,
                                previsaoEntrega = previsaoEntrega.let {
                                    // converte dígitos para DD/MM/AAAA para salvar
                                    buildString {
                                        it.forEachIndexed { i, c -> if (i == 2 || i == 4) append('/'); append(c) }
                                    }
                                },
                                valorTotal = valorDouble,
                                garantia = garantia,
                                status = "Aguardando análise"
                            )
                            db.collection("lojas").document(lojaId)
                                .collection("ordens_servico")
                                .add(novaOS)
                                .addOnSuccessListener {
                                    salvando = false
                                    Toast.makeText(context, "OS criada com sucesso!", Toast.LENGTH_SHORT).show()
                                    onSalvar()
                                }
                                .addOnFailureListener {
                                    salvando = false
                                    Toast.makeText(context, "Erro ao criar OS", Toast.LENGTH_SHORT).show()
                                }
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
                        Text("Salvar OS", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}