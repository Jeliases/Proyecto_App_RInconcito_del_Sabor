package com.example.proyecto_rinconcito.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_rinconcito.adapters.PedidoAdminAdapter
import com.example.proyecto_rinconcito.databinding.FragmentAdminPedidosBinding
import com.example.proyecto_rinconcito.models.PedidoAdmin
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminPedidosFragment : Fragment() {

    private var _binding: FragmentAdminPedidosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PedidoAdminAdapter
    private val listaPedidos = mutableListOf<PedidoAdmin>()
    private var pedidosListener: ListenerRegistration? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminPedidosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        escucharPedidos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pedidosListener?.remove()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = PedidoAdminAdapter(listaPedidos) { pedido ->
            val intent = Intent(requireContext(), AdminPedidoDetalleActivity::class.java)
            intent.putExtra("PEDIDO_ID", pedido.id)
            startActivity(intent)
        }
        binding.recyclerPedidos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPedidos.adapter = adapter
    }

    private fun escucharPedidos() {
        pedidosListener = db.collection("pedidos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreError", "Error al escuchar pedidos", error)
                    Toast.makeText(requireContext(), "Error al cargar pedidos.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    listaPedidos.clear()
                    snapshot.documents.forEach { doc ->
                        val pedido = PedidoAdmin(
                            id = doc.id,
                            codigo = doc.getString("codigoPedido") ?: "",
                            estado = doc.getString("estado") ?: "",
                            total = doc.getDouble("total") ?: 0.0,
                            fecha = doc.getLong("fecha") ?: 0L
                        )
                        listaPedidos.add(pedido)
                    }
                    // Ordenamos la lista aquí, en el cliente
                    listaPedidos.sortByDescending { it.fecha }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
