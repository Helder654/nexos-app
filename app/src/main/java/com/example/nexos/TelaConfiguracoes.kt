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

private val BgPrimary     = Color(0xFF1C1C1E)
private val BgSecondary   = Color(0xFF2C2C2E)
private val BorderColor   = Color(0xFF3A3A3C)
private val TextPrimary   = Color(0xFFEBEBF5)
private val TextSecondary = Color(0xFF8E8E93)
private val TextHint      = Color(0xFF48484A)

@Composable
fun TelaConfiguracoes(onVoltar: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var nome           by remember { mutableStateOf("") }
    var email          by remember { mutableStateOf("") }
    var garantiaPadrao by remember { mutableStateOf("90") }
    var codigoLoja     by remember { mutableStateOf("") }
    var carregando     by remember { mutableStateOf(true) }
    var salvando       by remember { mutableStateOf(false) }

    val uid = auth.currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        db.collection("tecnicos").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                nome           = doc.getString("nome") ?: ""
                email          = doc.getString("email") ?: ""
                garantiaPadrao = doc.getLong("garantiaPadrao")?.toString() ?: "90"
                codigoLoja     = doc.getString("lojaId") ?: ""
                carregando     = false
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
            Text("Configurações", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
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
                // Card código da loja
                if (codigoLoja.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgSecondary)
                            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text("Código da loja", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = codigoLoja,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 4.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Compartilhe com seus técnicos", fontSize = 12.sp, color = TextHint)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Dados do técnico", fontSize = 13.sp, color = TextSecondary)

                CampoFormulario(
                    label = "Nome",
                    valor = nome,
                    onValorChange = { nome = it },
                    icone = Icons.Default.Person,
                    placeholder = "Seu nome"
                )

                // E-mail desabilitado
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("E-mail", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgSecondary)
                            .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(email, fontSize = 14.sp, color = TextHint)
                    }
                }

                // Divisor
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(BorderColor))
                Text("Preferências", fontSize = 13.sp, color = TextSecondary)

                CampoFormulario(
                    label = "Garantia padrão (dias)",
                    valor = garantiaPadrao,
                    onValorChange = { if (it.all { c -> c.isDigit() }) garantiaPadrao = it },
                    icone = Icons.Default.Shield,
                    placeholder = "90"
                )

                Text(
                    "Esse valor será usado automaticamente ao finalizar uma OS",
                    fontSize = 12.sp,
                    color = TextHint,
                    modifier = Modifier.padding(top = 0.dp)
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
                            if (nome.isEmpty()) {
                                Toast.makeText(context, "Nome não pode ser vazio", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            salvando = true
                            db.collection("tecnicos").document(uid)
                                .update(
                                    mapOf(
                                        "nome"           to nome,
                                        "garantiaPadrao" to (garantiaPadrao.toLongOrNull() ?: 90L)
                                    )
                                )
                                .addOnSuccessListener {
                                    salvando = false
                                    Toast.makeText(context, "Configurações salvas!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    salvando = false
                                    Toast.makeText(context, "Erro ao salvar", Toast.LENGTH_SHORT).show()
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
                            Text("Salvar configurações", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}