package com.example.nexos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

private val BgPrimary = Color(0xFF1C1C1E)
private val BgSecondary = Color(0xFF2C2C2E)
private val BorderColor = Color(0xFF3A3A3C)
private val TextPrimary = Color(0xFFEBEBF5)
private val TextSecondary = Color(0xFF8E8E93)
private val TextHint = Color(0xFF48484A)

@Composable
fun TelaCadastro(
    onCadastroSucesso: () -> Unit,
    onVoltar: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var codigoLoja by remember { mutableStateOf("") }
    var criarNovaLoja by remember { mutableStateOf(false) }
    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }
    var carregando by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NexOS",
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Text(
                text = "Criar conta",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo Nome
            CampoCadastro(
                label = "Nome",
                valor = nome,
                onValorChange = { nome = it },
                icone = Icons.Default.Person,
                placeholder = "Seu nome"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Email
            CampoCadastro(
                label = "E-mail",
                valor = email,
                onValorChange = { email = it },
                icone = Icons.Default.Email,
                placeholder = "seu@email.com"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Senha
            CampoCadastro(
                label = "Senha",
                valor = senha,
                onValorChange = { senha = it },
                icone = Icons.Default.Lock,
                placeholder = "••••••••",
                isSenha = true,
                senhaVisivel = senhaVisivel,
                onToggleSenha = { senhaVisivel = !senhaVisivel }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Confirmar Senha
            CampoCadastro(
                label = "Confirmar senha",
                valor = confirmarSenha,
                onValorChange = { confirmarSenha = it },
                icone = Icons.Default.Lock,
                placeholder = "••••••••",
                isSenha = true,
                senhaVisivel = confirmarSenhaVisivel,
                onToggleSenha = { confirmarSenhaVisivel = !confirmarSenhaVisivel }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle criar nova loja
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BgSecondary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                    .clickable { criarNovaLoja = !criarNovaLoja }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Criar nova loja",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = criarNovaLoja,
                    onCheckedChange = { criarNovaLoja = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BgPrimary,
                        checkedTrackColor = TextPrimary,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = BorderColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo código da loja (se não for criar nova)
            if (!criarNovaLoja) {
                CampoCadastro(
                    label = "Código da loja",
                    valor = codigoLoja,
                    onValorChange = { codigoLoja = it.uppercase() },
                    icone = Icons.Default.VpnKey,
                    placeholder = "Ex: A1B2C3"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botão criar conta
            Button(
                onClick = {
                    when {
                        nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty() -> {
                            Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                        }
                        senha != confirmarSenha -> {
                            Toast.makeText(context, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                        }
                        senha.length < 6 -> {
                            Toast.makeText(context, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                        }
                        !criarNovaLoja && codigoLoja.isEmpty() -> {
                            Toast.makeText(context, "Informe o código da loja", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            carregando = true

                            val continuarCadastro = { uid: String, lojaId: String ->
                                val tecnico = hashMapOf(
                                    "nome" to nome,
                                    "email" to email,
                                    "lojaId" to lojaId
                                )
                                db.collection("tecnicos").document(uid)
                                    .set(tecnico)
                                    .addOnSuccessListener {
                                        carregando = false
                                        Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                        onCadastroSucesso()
                                    }
                                    .addOnFailureListener {
                                        carregando = false
                                        Toast.makeText(context, "Erro ao salvar dados", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            auth.createUserWithEmailAndPassword(email, senha)
                                .addOnSuccessListener { resultado ->
                                    val uid = resultado.user?.uid ?: ""
                                    if (criarNovaLoja) {
                                        val lojaId = UUID.randomUUID().toString().take(6).uppercase()
                                        val loja = hashMapOf(
                                            "codigo" to lojaId,
                                            "donoUid" to uid
                                        )
                                        db.collection("lojas").document(lojaId)
                                            .set(loja)
                                            .addOnSuccessListener { continuarCadastro(uid, lojaId) }
                                            .addOnFailureListener {
                                                carregando = false
                                                Toast.makeText(context, "Erro ao criar loja", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        db.collection("lojas").document(codigoLoja)
                                            .get()
                                            .addOnSuccessListener { doc ->
                                                if (doc.exists()) {
                                                    continuarCadastro(uid, codigoLoja)
                                                } else {
                                                    carregando = false
                                                    Toast.makeText(context, "Código de loja inválido", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .addOnFailureListener {
                                                carregando = false
                                                Toast.makeText(context, "Erro ao verificar código", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    carregando = false
                                    val mensagem = when {
                                        e.message?.contains("email address is already in use") == true -> "Este e-mail já está cadastrado"
                                        e.message?.contains("badly formatted") == true -> "E-mail inválido"
                                        else -> "Erro ao criar conta"
                                    }
                                    Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextPrimary,
                    contentColor = BgPrimary
                ),
                enabled = !carregando
            ) {
                if (carregando) {
                    CircularProgressIndicator(
                        color = BgPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Criar conta",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(
                    text = "Já tenho uma conta. ",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Entrar",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onVoltar() }
                )
            }
        }
    }
}

@Composable
fun CampoCadastro(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit,
    icone: ImageVector,
    placeholder: String,
    isSenha: Boolean = false,
    senhaVisivel: Boolean = false,
    onToggleSenha: (() -> Unit)? = null
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
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BgSecondary)
                .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = valor,
                onValueChange = onValorChange,
                visualTransformation = if (isSenha && !senhaVisivel) PasswordVisualTransformation() else VisualTransformation.None,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
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
            if (isSenha && onToggleSenha != null) {
                IconButton(
                    onClick = onToggleSenha,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Mostrar senha",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}