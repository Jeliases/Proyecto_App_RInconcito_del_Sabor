package com.example.proyecto_rinconcito.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.models.Pedido
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CarritoActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: CarritoAdapter
    private lateinit var tvTotal: TextView
    private lateinit var btnConfirmar: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        recycler = findViewById(R.id.recyclerCarrito)
        tvTotal = findViewById(R.id.tvTotal)
        btnConfirmar = findViewById(R.id.btnConfirmarPedido)

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = CarritoAdapter(CarritoManager.listaItems) {
            actualizarTotal()
        }

        recycler.adapter = adapter

        actualizarTotal()

        btnConfirmar.setOnClickListener {
            if (CarritoManager.listaItems.isEmpty()) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            } else {
                confirmarPedido()
            }
        }
    }

    private fun actualizarTotal() {
        val total = CarritoManager.obtenerTotal()
        tvTotal.text = "Total: S/ $total"
    }

    private fun confirmarPedido() {

        val user = auth.currentUser ?: return
        val total = CarritoManager.obtenerTotal()
        val codigo = (100000..999999).random().toString()

        val pedido = Pedido(
            clienteId = user.uid,
            nombreCliente = user.email ?: "Cliente",
            items = CarritoManager.listaItems.toList(),
            total = total,
            estado = "PENDIENTE_PAGO",
            fecha = System.currentTimeMillis(),
            codigoPedido = codigo
        )

        db.collection("pedidos")
            .add(pedido)
            .addOnSuccessListener { documentReference ->

                val intent = Intent(this, PagoActivity::class.java)
                intent.putExtra("pedidoId", documentReference.id)
                intent.putExtra("total", total)
                startActivity(intent)

                CarritoManager.limpiarCarrito()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar pedido", Toast.LENGTH_SHORT).show()
            }
    }

}
