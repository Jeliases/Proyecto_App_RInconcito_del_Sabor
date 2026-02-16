package com.example.proyecto_rinconcito.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.models.PedidoAdmin
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: PedidoAdminAdapter
    private val listaPedidos = mutableListOf<PedidoAdmin>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        recycler = findViewById(R.id.recyclerPedidos)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = PedidoAdminAdapter(listaPedidos)
        recycler.adapter = adapter

        escucharPedidos()
    }

    private fun escucharPedidos() {

        db.collection("pedidos")
            .addSnapshotListener { snapshot, _ ->

                listaPedidos.clear()

                snapshot?.documents?.forEach { doc ->

                    val pedido = PedidoAdmin(
                        id = doc.id,
                        codigo = doc.getString("codigoPedido") ?: "",
                        estado = doc.getString("estado") ?: "",
                        total = doc.getDouble("total") ?: 0.0
                    )

                    listaPedidos.add(pedido)
                }

                adapter.notifyDataSetChanged()
            }
    }
}