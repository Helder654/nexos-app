package com.example.nexos

data class OrdemServico(
    var id: String = "",
    var cliente: Cliente = Cliente(),
    var console: Console = Console(),
    var defeitoReclamado: String = "",
    var sintomasAnalisados: String = "",
    var fotos: List<String> = emptyList(),
    var dataEntrada: String = "",
    var previsaoOrcamento: String = "",
    var previsaoEntrega: String = "",
    var valorTotal: Double = 0.0,
    var garantia: String = "",
    var status: String = "Aguardando análise",
    // campos do relatório
    var descricaoServico: String = "",
    var gastos: Double = 0.0,
    var dataEntrega: String = "",
    var dataVencimentoGarantia: String = ""
)