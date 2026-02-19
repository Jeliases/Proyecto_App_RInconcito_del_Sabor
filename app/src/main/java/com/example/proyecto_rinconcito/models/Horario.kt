package com.example.proyecto_rinconcito.models

// Representa el horario para un día específico
data class DiaHorario(
    val abierto: Boolean = false,
    val apertura: String = "09:00 AM",
    val cierre: String = "10:00 PM"
)

// Representa el horario de toda la semana
data class Horario(
    val lunes: DiaHorario = DiaHorario(),
    val martes: DiaHorario = DiaHorario(),
    val miercoles: DiaHorario = DiaHorario(),
    val jueves: DiaHorario = DiaHorario(),
    val viernes: DiaHorario = DiaHorario(),
    val sabado: DiaHorario = DiaHorario(true, "10:00 AM", "11:00 PM"), // Ejemplo: abierto por defecto
    val domingo: DiaHorario = DiaHorario(true, "10:00 AM", "09:00 PM")  // Ejemplo: abierto por defecto
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(DiaHorario(), DiaHorario(), DiaHorario(), DiaHorario(), DiaHorario(), DiaHorario(), DiaHorario())
}
