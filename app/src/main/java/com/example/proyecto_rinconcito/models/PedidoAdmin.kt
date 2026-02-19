package com.example.proyecto_rinconcito.models

data class PedidoAdmin(
    var id: String = "",
    var codigo: String = "",
    var estado: String = "",
    var total: Double = 0.0,
    var fecha: Long = 0L // Nuevo campo para ordenar
)
