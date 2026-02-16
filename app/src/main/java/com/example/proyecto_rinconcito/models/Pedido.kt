package com.example.proyecto_rinconcito.models

data class Pedido(
    var clienteId: String = "",
    var nombreCliente: String = "",
    var items: List<ItemPedido> = listOf(),
    var total: Double = 0.0,
    var estado: String = "PENDIENTE_PAGO",
    var fecha: Long = System.currentTimeMillis(),
    var codigoPedido: String = ""
)
