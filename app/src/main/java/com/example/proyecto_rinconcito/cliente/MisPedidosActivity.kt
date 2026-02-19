package com.example.proyecto_rinconcito.cliente

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_rinconcito.adapters.MisPedidosAdapter
import com.example.proyecto_rinconcito.databinding.ActivityMisPedidosBinding
import com.example.proyecto_rinconcito.models.Pedido
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MisPedidosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMisPedidosBinding
    private lateinit var adapter: MisPedidosAdapter
    private var pedidosListener: ListenerRegistration? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisPedidosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        escucharCambiosEnPedidos()
    }

    override fun onDestroy() {
        super.onDestroy()
        pedidosListener?.remove()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = MisPedidosAdapter(emptyList()) { pedido ->
            if (pedido.estado == "PENDIENTE_PAGO") {
                val intent = Intent(this, PagoActivity::class.java)
                intent.putExtra("pedidoId", pedido.id)
                intent.putExtra("total", pedido.total)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Este pedido ya fue procesado.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerMisPedidos.layoutManager = LinearLayoutManager(this)
        binding.recyclerMisPedidos.adapter = adapter
    }

    private fun escucharCambiosEnPedidos() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no identificado.", Toast.LENGTH_LONG).show()
            return
        }

        pedidosListener = db.collection("pedidos")
            .whereEqualTo("clienteId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreError", "Error al escuchar cambios en pedidos", error)
                    Toast.makeText(this, "Error al cargar los pedidos.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val pedidosConId = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Pedido::class.java)?.apply { id = doc.id } 
                    }
                    val pedidosOrdenados = pedidosConId.sortedByDescending { it.fecha }
                    adapter.setData(pedidosOrdenados)
                    binding.recyclerMisPedidos.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                } else {
                    adapter.setData(emptyList())
                    binding.recyclerMisPedidos.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                }
            }
    }
}
