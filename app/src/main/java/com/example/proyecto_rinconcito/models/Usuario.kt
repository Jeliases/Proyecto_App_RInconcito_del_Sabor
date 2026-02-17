package com.example.proyecto_rinconcito.models

data class Usuario(
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "",
    val enabled: Boolean = true
)
