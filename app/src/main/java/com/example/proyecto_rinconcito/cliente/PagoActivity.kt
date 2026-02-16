package com.example.proyecto_rinconcito.cliente

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_rinconcito.R
import com.google.firebase.firestore.FirebaseFirestore

class PagoActivity : AppCompatActivity() {

    private lateinit var tvTotal: TextView
    private lateinit var btnYaPague: Button

    private val db = FirebaseFirestore.getInstance()
    private var pedidoId: String = ""
    private var total: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago)

        tvTotal = findViewById(R.id.tvTotalPago)
        btnYaPague = findViewById(R.id.btnYaPague)

        pedidoId = intent.getStringExtra("pedidoId") ?: ""
        total = intent.getDoubleExtra("total", 0.0)

        tvTotal.text = "Total: S/ $total"

        btnYaPague.setOnClickListener {
            marcarComoEnVerificacion()
        }
    }

    private fun marcarComoEnVerificacion() {

        db.collection("pedidos")
            .document(pedidoId)
            .update("estado", "PAGO_EN_VERIFICACION")
            .addOnSuccessListener {
                Toast.makeText(this, "Esperando verificación de cocina", Toast.LENGTH_LONG).show()
                finish()
            }
    }
}
