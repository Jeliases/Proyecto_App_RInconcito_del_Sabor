package com.example.proyecto_rinconcito.models

data class Plato(
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var precio: Double = 0.0,
    var categoria: String = "",
    var favorito: Boolean = false,
    var imagenUrl: String = "",
    var disponible: Boolean = true // Nuevo campo para gestionar la disponibilidad
)
