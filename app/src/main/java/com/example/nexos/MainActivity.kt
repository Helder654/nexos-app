package com.example.nexos

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexos.ui.theme.NexOSTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val BgPrimary = Color(0xFF1C1C1E)
private val BgSecondary = Color(0xFF2C2C2E)
private val BorderColor = Color(0xFF3A3A3C)
private val TextPrimary = Color(0xFFEBEBF5)
private val TextSecondary = Color(0xFF8E8E93)
private val TextHint = Color(0xFF48484A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexOSTheme {
                var telaAtual by remember { mutableStateOf("login") }
                var clienteSelecionado by remember { mutableStateOf<Cliente?>(null) }
                var clienteAberto by remember { mutableStateOf<Cliente?>(null) }
                var osSelecionada by remember { mutableStateOf<OrdemServico?>(null) }
                var osParaRelatorio by remember { mutableStateOf<OrdemServico?>(null) }
                var lojaId by remember { mutableStateOf("") }

                when (telaAtual) {
                    "cadastro" -> TelaCadastro(
                        onCadastroSucesso = { telaAtual = "login" },
                        onVoltar = { telaAtual = "login" }
                    )
                    "login" -> TelaLogin(
                        onLoginSucesso = { id ->
                            lojaId = id
                            telaAtual = "home"
                        },
                        onIrParaCadastro = { telaAtual = "cadastro" }
                    )
                    "home" -> HomeScreen(
                        lojaId = lojaId,
                        onNavegar = { destino -> telaAtual = destino }
                    )
                    "lista_clientes" -> ListaClientesScreen(
                        lojaId = lojaId,
                        onVoltar = { telaAtual = "home" },
                        onNovoCliente = { telaAtual = "clientes" },
                        onAbrirCliente = { cliente ->
                            clienteAberto = cliente
                            telaAtual = "detalhes_cliente"
                        }
                    )
                    "clientes" -> ClienteScreen(
                        lojaId = lojaId,
                        onVoltar = { telaAtual = "lista_clientes" }
                    )
                    "detalhes_cliente" -> {
                        clienteAberto?.let { cliente ->
                            TelaDetalhesCliente(
                                lojaId = lojaId,
                                cliente = cliente,
                                onVoltar = { telaAtual = "lista_clientes" },
                                onAbrirOS = { os ->
                                    osSelecionada = os
                                    telaAtual = "detalhes_os"
                                }
                            )
                        }
                    }
                    "lista_os_abertas" -> ListaOSAbertasScreen(
                        lojaId = lojaId,
                        onVoltar = { telaAtual = "home" },
                        onNovaOS = { telaAtual = "nova_os" },
                        onAbrirOS = { os ->
                            osSelecionada = os
                            telaAtual = "detalhes_os"
                        }
                    )
                    "nova_os" -> NovaOSScreen(
                        lojaId = lojaId,
                        onVoltar = { telaAtual = "lista_os_abertas" },
                        onNovoCliente = { telaAtual = "clientes" },
                        onClienteSelecionado = { cliente ->
                            clienteSelecionado = cliente
                            telaAtual = "formulario_os"
                        }
                    )
                    "formulario_os" -> {
                        clienteSelecionado?.let { cliente ->
                            FormularioOSScreen(
                                lojaId = lojaId,
                                cliente = cliente,
                                onVoltar = { telaAtual = "nova_os" },
                                onSalvar = { telaAtual = "lista_os_abertas" }
                            )
                        }
                    }
                    "detalhes_os" -> {
                        osSelecionada?.let { os ->
                            DetalhesOSScreen(
                                lojaId = lojaId,
                                os = os,
                                onVoltar = { telaAtual = "lista_os_abertas" },
                                onFinalizada = { osAtualizada ->
                                    osParaRelatorio = osAtualizada
                                    telaAtual = "relatorio_os"
                                }
                            )
                        }
                    }
                    "lista_os_finalizadas" -> ListaOSFinalizadasScreen(
                        lojaId = lojaId,
                        onVoltar = { telaAtual = "home" },
                        onAbrirOS = { os ->
                            osSelecionada = os
                            telaAtual = "detalhes_os"
                        }
                    )
                    "relatorio_os" -> {
                        osParaRelatorio?.let { os ->
                            TelaRelatorioOS(
                                lojaId = lojaId,
                                os = os,
                                onVoltar = { telaAtual = "lista_os_finalizadas" },
                                onSalvar = { telaAtual = "lista_os_finalizadas" }
                            )
                        }
                    }
                    "configuracoes" -> TelaConfiguracoes(
                        onVoltar = { telaAtual = "home" }
                    )
                }
            }
        }
    }
}

@Composable
fun TelaLogin(
    onLoginSucesso: (String) -> Unit,
    onIrParaCadastro: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var carregando by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
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
                text = "Ordem de Serviço",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo e-mail
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "E-mail",
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
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = email,
                        onValueChange = { email = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        decorationBox = { inner ->
                            if (email.isEmpty()) {
                                Text("seu@email.com", color = TextHint, fontSize = 14.sp)
                            }
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo senha
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Senha",
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
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = senha,
                        onValueChange = { senha = it },
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        decorationBox = { inner ->
                            Box(modifier = Modifier.weight(1f)) {
                                if (senha.isEmpty()) {
                                    Text("••••••••", color = TextHint, fontSize = 14.sp)
                                }
                                inner()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { senhaVisivel = !senhaVisivel },
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

            Spacer(modifier = Modifier.height(28.dp))

            // Botão entrar
            Button(
                onClick = {
                    if (email.isEmpty() || senha.isEmpty()) {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                    } else {
                        carregando = true
                        auth.signInWithEmailAndPassword(email, senha)
                            .addOnSuccessListener { resultado ->
                                val uid = resultado.user?.uid ?: ""
                                db.collection("tecnicos").document(uid)
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        carregando = false
                                        val lojaId = doc.getString("lojaId") ?: ""
                                        if (lojaId.isEmpty()) {
                                            Toast.makeText(context, "Loja não encontrada", Toast.LENGTH_SHORT).show()
                                        } else {
                                            onLoginSucesso(lojaId)
                                        }
                                    }
                                    .addOnFailureListener {
                                        carregando = false
                                        Toast.makeText(context, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                carregando = false
                                Toast.makeText(context, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show()
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
                        text = "Entrar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(
                    text = "Não tem conta? ",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Cadastre-se",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onIrParaCadastro() }
                )
            }
        }
    }
}