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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

private val BgPrimary    = Color(0xFF1C1C1E)
private val BgSecondary  = Color(0xFF2C2C2E)
private val BorderColor  = Color(0xFF3A3A3C)
private val TextPrimary  = Color(0xFFEBEBF5)
private val TextSecondary = Color(0xFF8E8E93)
private val TextHint     = Color(0xFF48484A)

// Formata dígitos puros -> "(11) 99999-9999"
fun formatarTelefone(digits: String): String {
    return when {
        digits.length <= 2  -> "(${digits}"
        digits.length <= 7  -> "(${digits.substring(0, 2)}) ${digits.substring(2)}"
        digits.length <= 11 -> "(${digits.substring(0, 2)}) ${digits.substring(2, 7)}-${digits.substring(7)}"
        else                -> "(${digits.substring(0, 2)}) ${digits.substring(2, 7)}-${digits.substring(7, 11)}"
    }
}

// VisualTransformation que aplica a máscara sem alterar o estado
class TelefoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(11)
        val formatted = formatarTelefone(digits)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // calcula quantos chars de máscara existem até a posição original
                val transformed = formatarTelefone(digits.take(offset))
                return transformed.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                // conta só os dígitos até a posição transformada
                return formatted.take(offset).count { it.isDigit() }
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@Composable
fun ClienteScreen(lojaId: String, onVoltar: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var nome     by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }   // armazena só dígitos
    var email    by remember { mutableStateOf("") }
    var endereco by remember { mutableStateOf("") }
    var salvando by remember { mutableStateOf(false) }

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
                text = "Novo Cliente",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            CampoFormulario(
                label = "Nome completo",
                valor = nome,
                onValorChange = { nome = it },
                icone = Icons.Default.Person,
                placeholder = "Nome do cliente"
            )

            // Campo telefone com VisualTransformation
            CampoFormulario(
                label = "Telefone",
                valor = telefone,
                onValorChange = { telefone = it.filter { c -> c.isDigit() }.take(11) },
                icone = Icons.Default.Phone,
                placeholder = "(00) 00000-0000",
                visualTransformation = TelefoneVisualTransformation()
            )

            CampoFormulario(
                label = "E-mail",
                valor = email,
                onValorChange = { email = it },
                icone = Icons.Default.Email,
                placeholder = "email@exemplo.com"
            )

            CampoFormulario(
                label = "Endereço",
                valor = endereco,
                onValorChange = { endereco = it },
                icone = Icons.Default.LocationOn,
                placeholder = "Rua, número, bairro"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (salvando) BgSecondary else TextPrimary),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (nome.isEmpty() || telefone.isEmpty()) {
                            Toast.makeText(context, "Nome e telefone são obrigatórios", Toast.LENGTH_SHORT).show()
                        } else {
                            salvando = true
                            val novoCliente = Cliente(
                                id = "",
                                nome = nome,
                                telefone = formatarTelefone(telefone), // salva formatado no Firestore
                                email = email,
                                endereco = endereco
                            )
                            db.collection("lojas").document(lojaId)
                                .collection("clientes")
                                .add(novoCliente)
                                .addOnSuccessListener {
                                    salvando = false
                                    Toast.makeText(context, "Cliente salvo!", Toast.LENGTH_SHORT).show()
                                    onVoltar()
                                }
                                .addOnFailureListener {
                                    salvando = false
                                    Toast.makeText(context, "Erro ao salvar cliente", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextPrimary,
                        contentColor = BgPrimary,
                        disabledContainerColor = BgSecondary,
                        disabledContentColor = TextSecondary
                    ),
                    enabled = !salvando
                ) {
                    if (salvando) {
                        CircularProgressIndicator(
                            color = TextSecondary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Salvar Cliente",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CampoFormulario(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit,
    icone: ImageVector,
    placeholder: String,
    linhasMaximas: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (linhasMaximas == 1) Modifier.height(48.dp) else Modifier.heightIn(min = 48.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(BgSecondary)
                .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = if (linhasMaximas > 1) 12.dp else 0.dp),
            verticalAlignment = if (linhasMaximas == 1) Alignment.CenterVertically else Alignment.Top
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier
                    .size(18.dp)
                    .then(if (linhasMaximas > 1) Modifier.padding(top = 2.dp) else Modifier)
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = valor,
                onValueChange = onValorChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextPrimary,
                    fontSize = 14.sp
                ),
                maxLines = linhasMaximas,
                visualTransformation = visualTransformation,
                decorationBox = { inner ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (valor.isEmpty()) {
                            Text(placeholder, color = TextHint, fontSize = 14.sp)
                        }
                        inner()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}