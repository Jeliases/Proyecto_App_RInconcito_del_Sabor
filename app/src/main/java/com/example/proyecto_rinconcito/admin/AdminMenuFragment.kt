package com.example.proyecto_rinconcito.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_rinconcito.databinding.FragmentAdminMenuBinding
import com.example.proyecto_rinconcito.models.Plato
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminMenuFragment : Fragment() {

    private var _binding: FragmentAdminMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdminPlatosAdapter
    private val listaPlatos = mutableListOf<Plato>()
    private var platosListener: ListenerRegistration? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        escucharCambiosEnMenu()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        platosListener?.remove()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = AdminPlatosAdapter(
            listaPlatos,
            onEditClicked = { plato ->
                val intent = Intent(requireContext(), AddEditPlatoActivity::class.java)
                intent.putExtra("PLATO_ID", plato.id)
                startActivity(intent)
            },
            onDeleteClicked = { plato ->
                // TODO: Implementar la lógica para eliminar el plato
                Toast.makeText(requireContext(), "Eliminar ${plato.nombre}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerMenuAdmin.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMenuAdmin.adapter = adapter
    }

    private fun escucharCambiosEnMenu() {
        platosListener = db.collection("platos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error al cargar el menú", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    listaPlatos.clear()
                    val platos = snapshot.toObjects(Plato::class.java)
                    listaPlatos.addAll(platos)
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
