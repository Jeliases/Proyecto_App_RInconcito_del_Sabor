package com.example.proyecto_rinconcito.cliente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_rinconcito.databinding.ActivityCarritoBinding
import com.example.proyecto_rinconcito.models.Pedido
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private lateinit var adapter: CarritoAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()

        actualizarVista()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            // Cierra la actividad y vuelve a la anterior
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = CarritoAdapter(CarritoManager.listaItems) {
            actualizarVista()
        }
        binding.recyclerCarrito.layoutManager = LinearLayoutManager(this)
        binding.recyclerCarrito.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnConfirmarPedido.setOnClickListener {
            if (CarritoManager.listaItems.isEmpty()) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            } else {
                confirmarPedido()
            }
        }
    }

    private fun actualizarVista() {
        val total = CarritoManager.obtenerTotal()
        binding.tvTotal.text = String.format("S/ %.2f", total)

        if (CarritoManager.listaItems.isEmpty()) {
            binding.recyclerCarrito.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.recyclerCarrito.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
        // Notificar al adapter por si se eliminó un item
        adapter.notifyDataSetChanged()
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
