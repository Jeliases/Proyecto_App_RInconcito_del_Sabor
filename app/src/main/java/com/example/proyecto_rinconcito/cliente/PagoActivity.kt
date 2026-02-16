package com.example.proyecto_rinconcito.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_rinconcito.databinding.ActivityPagoBinding
import com.google.firebase.firestore.FirebaseFirestore

class PagoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPagoBinding
    private val db = FirebaseFirestore.getInstance()
    private var pedidoId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        getIntentData()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun getIntentData() {
        pedidoId = intent.getStringExtra("pedidoId") ?: ""
        val total = intent.getDoubleExtra("total", 0.0)

        if (pedidoId.isEmpty()) {
            Toast.makeText(this, "Error: ID de pedido no encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.tvTotalPago.text = String.format("S/ %.2f", total)
    }

    private fun setupListeners() {
        binding.btnYaPague.setOnClickListener {
            marcarComoEnVerificacion()
        }
    }

    private fun marcarComoEnVerificacion() {
        binding.btnYaPague.isEnabled = false // Evitar clics múltiples

        db.collection("pedidos")
            .document(pedidoId)
            .update("estado", "PAGO_EN_VERIFICACION")
            .addOnSuccessListener {
                Toast.makeText(this, "¡Gracias! Tu pago está siendo verificado.", Toast.LENGTH_LONG).show()

                // Limpiar la pila de actividades y volver a la home del cliente
                val intent = Intent(this, ClienteHomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar el pedido: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnYaPague.isEnabled = true // Habilitar el botón de nuevo si falla
            }
    }
}
