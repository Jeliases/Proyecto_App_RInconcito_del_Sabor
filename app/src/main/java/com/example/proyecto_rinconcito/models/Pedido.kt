package com.example.proyecto_rinconcito.models

import com.google.firebase.firestore.DocumentId

data class Pedido(
    @DocumentId var id: String = "", // ID del documento de Firestore
    var clienteId: String = "",
    var nombreCliente: String = "",
    var items: List<ItemPedido> = listOf(),
    var total: Double = 0.0,
    var estado: String = "PENDIENTE_PAGO",
    var fecha: Long = System.currentTimeMillis(),
    var codigoPedido: String = "",
    var comprobanteUrl: String? = null
)
