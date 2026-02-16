package com.example.proyecto_rinconcito.cliente

import com.example.proyecto_rinconcito.models.ItemPedido


object CarritoManager {

    val listaItems = mutableListOf<ItemPedido>()

    fun agregarItem(nuevo: ItemPedido) {

        val existente = listaItems.find { it.nombre == nuevo.nombre }

        if (existente != null) {
            existente.cantidad++
        } else {
            listaItems.add(nuevo)
        }
    }

    fun eliminarItem(item: ItemPedido) {
        listaItems.remove(item)
    }

    fun aumentarCantidad(item: ItemPedido) {
        item.cantidad++
    }

    fun disminuirCantidad(item: ItemPedido) {
        if (item.cantidad > 1) {
            item.cantidad--
        }
    }

    fun obtenerTotal(): Double {
        return listaItems.sumOf { it.precio * it.cantidad }
    }

    fun limpiarCarrito() {
        listaItems.clear()
    }
}
