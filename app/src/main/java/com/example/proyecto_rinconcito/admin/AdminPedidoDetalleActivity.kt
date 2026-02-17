package com.example.proyecto_rinconcito.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_rinconcito.databinding.ActivityAdminPedidoDetalleBinding
import com.example.proyecto_rinconcito.models.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class AdminPedidoDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPedidoDetalleBinding
    private val db = FirebaseFirestore.getInstance()
    private var pedidoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPedidoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pedidoId = intent.getStringExtra("PEDIDO_ID")

        if (pedidoId == null) {
            Toast.makeText(this, "Error: No se encontró el ID del pedido", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupToolbar()
        cargarDetallesPedido()
        setupActionButtons()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun cargarDetallesPedido() {
        pedidoId?.let {
            db.collection("pedidos").document(it)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        Toast.makeText(this, "Error al cargar el pedido", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    snapshot.toObject(Pedido::class.java)?.let {
                        mostrarDatos(it)
                    }
                }
        }
    }

    private fun mostrarDatos(pedido: Pedido) {
        binding.tvCodigoPedido.text = "Pedido #${pedido.codigoPedido}"
        binding.tvNombreCliente.text = "Cliente: ${pedido.nombreCliente}"

        val sdf = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault())
        binding.tvFechaPedido.text = "Fecha: ${sdf.format(pedido.fecha)}"

        binding.tvTotal.text = String.format("Total: S/ %.2f", pedido.total)

        binding.chipEstado.text = pedido.estado

        // Configurar RecyclerView para los items
        binding.recyclerItemsPedido.layoutManager = LinearLayoutManager(this)
        binding.recyclerItemsPedido.adapter = AdminPedidoItemsAdapter(pedido.items)

        actualizarVisibilidadBotones(pedido.estado)
    }

    private fun setupActionButtons() {
        binding.btnConfirmarPago.setOnClickListener { cambiarEstado("EN_PREPARACION") }
        binding.btnPrepararPedido.setOnClickListener { cambiarEstado("LISTO_PARA_RECOGER") }
        binding.btnPedidoListo.setOnClickListener { cambiarEstado("ENTREGADO") }
        binding.btnEntregarPedido.setOnClickListener { cambiarEstado("COMPLETADO") } 
    }

    private fun actualizarVisibilidadBotones(estado: String) {
        binding.btnConfirmarPago.visibility = if (estado == "PAGO_EN_VERIFICACION") View.VISIBLE else View.GONE
        binding.btnPrepararPedido.visibility = if (estado == "EN_PREPARACION") View.VISIBLE else View.GONE
        binding.btnPedidoListo.visibility = if (estado == "LISTO_PARA_RECOGER") View.VISIBLE else View.GONE
        binding.btnEntregarPedido.visibility = if (estado == "ENTREGADO") View.VISIBLE else View.GONE
    }

    private fun cambiarEstado(nuevoEstado: String) {
        pedidoId?.let {
            db.collection("pedidos").document(it)
                .update("estado", nuevoEstado)
                .addOnSuccessListener {
                    Toast.makeText(this, "Estado actualizado a $nuevoEstado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
