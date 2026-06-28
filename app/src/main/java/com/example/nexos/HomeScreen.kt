package com.example.nexos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

private val BgPrimary = Color(0xFF1C1C1E)
private val BgSecondary = Color(0xFF2C2C2E)
private val BgTertiary = Color(0xFF3A3A3C)
private val BorderColor = Color(0xFF3A3A3C)
private val TextPrimary = Color(0xFFEBEBF5)
private val TextSecondary = Color(0xFF8E8E93)

@Composable
fun HomeScreen(lojaId: String, onNavegar: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val db = FirebaseFirestore.getInstance()

    var totalClientes by remember { mutableStateOf(0) }
    var totalOSAbertas by remember { mutableStateOf(0) }
    var totalOSFinalizadas by remember { mutableStateOf(0) }
    var faturamentoBruto by remember { mutableStateOf(0.0) }
    var totalGastos by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        val lojaRef = db.collection("lojas").document(lojaId)

        lojaRef.collection("clientes").get()
            .addOnSuccessListener { totalClientes = it.size() }

        lojaRef.collection("ordens_servico")
            .whereNotIn("status", listOf("Pronto", "Entregue", "Cancelada"))
            .get()
            .addOnSuccessListener { totalOSAbertas = it.size() }

        lojaRef.collection("ordens_servico")
            .whereIn("status", listOf("Pronto", "Entregue"))
            .get()
            .addOnSuccessListener { resultado ->
                totalOSFinalizadas = resultado.size()
                faturamentoBruto = resultado.documents.sumOf { it.getDouble("valorTotal") ?: 0.0 }
                totalGastos = resultado.documents.sumOf { it.getDouble("gastos") ?: 0.0 }
            }
    }

    val lucroLiquido = faturamentoBruto - totalGastos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Olá, Técnico!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "Bem-vindo ao NexOS",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BgSecondary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                    .clickable { onNavegar("configuracoes") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configurações",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Botão nova OS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TextPrimary)
                .clickable { onNavegar("nova_os") }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = BgPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nova Ordem de Serviço",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = BgPrimary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Menu
        Text(
            text = "Menu",
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BotaoMenu("Clientes", Icons.Default.Person, Modifier.weight(1f)) { onNavegar("lista_clientes") }
            BotaoMenu("OS Abertas", Icons.Default.Build, Modifier.weight(1f)) { onNavegar("lista_os_abertas") }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BotaoMenu("Finalizadas", Icons.Default.Check, Modifier.weight(1f)) { onNavegar("lista_os_finalizadas") }
            BotaoMenu("Orçamentos", Icons.Default.List, Modifier.weight(1f)) {}
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Estatísticas
        Text(
            text = "Estatísticas",
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CardEstatistica("Clientes", "$totalClientes", Modifier.weight(1f))
            CardEstatistica("OS Abertas", "$totalOSAbertas", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CardEstatistica("Finalizadas", "$totalOSFinalizadas", Modifier.weight(1f))
            CardEstatistica("Faturamento", "R$ ${"%.2f".format(faturamentoBruto)}", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CardEstatistica("Gastos", "R$ ${"%.2f".format(totalGastos)}", Modifier.weight(1f))
            CardEstatistica("Lucro Líquido", "R$ ${"%.2f".format(lucroLiquido)}", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun BotaoMenu(
    titulo: String,
    icone: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BgSecondary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icone,
            contentDescription = titulo,
            tint = TextPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = titulo,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun CardEstatistica(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgSecondary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = valor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = titulo,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}