package com.example.nexos

fun aplicarMascaraTelefone(telefone: String): String {
    val digits = telefone.filter { it.isDigit() }
    return when {
        digits.length <= 10 -> digits.replace(
            Regex("(\\d{2})(\\d{4})(\\d{0,4})"),
            "($1) $2-$3"
        )
        else -> digits.replace(
            Regex("(\\d{2})(\\d{5})(\\d{0,4})"),
            "($1) $2-$3"
        )
    }
}