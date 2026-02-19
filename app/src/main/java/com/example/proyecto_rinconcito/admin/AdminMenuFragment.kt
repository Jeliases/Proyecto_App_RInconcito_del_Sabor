package com.example.proyecto_rinconcito.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_rinconcito.adapters.AdminPlatosAdapter
import com.example.proyecto_rinconcito.databinding.FragmentAdminMenuBinding
import com.example.proyecto_rinconcito.models.Plato
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage

class AdminMenuFragment : Fragment() {

    private var _binding: FragmentAdminMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdminPlatosAdapter
    private val listaPlatos = mutableListOf<Plato>()
    private var platosListener: ListenerRegistration? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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
                mostrarDialogoDeEliminacion(plato)
            },
            onDisponibilidadChanged = { plato, isChecked ->
                actualizarDisponibilidad(plato, isChecked)
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

    private fun mostrarDialogoDeEliminacion(plato: Plato) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Seguro que deseas eliminar el plato '${plato.nombre}'? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarPlato(plato)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarPlato(plato: Plato) {
        db.collection("platos").document(plato.id).delete()
            .addOnSuccessListener {
                val imagenRef = storage.getReferenceFromUrl(plato.imagenUrl)
                imagenRef.delete()
                    .addOnSuccessListener { Toast.makeText(requireContext(), "'${plato.nombre}' eliminado con éxito.", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(requireContext(), "Plato eliminado, pero hubo un error al borrar la imagen.", Toast.LENGTH_LONG).show() }
            }
            .addOnFailureListener { e -> Toast.makeText(requireContext(), "Error al eliminar el plato: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    private fun actualizarDisponibilidad(plato: Plato, disponible: Boolean) {
        db.collection("platos").document(plato.id)
            .update("disponible", disponible)
            .addOnFailureListener { e ->
                // Si falla, revertir el switch y mostrar un error
                Toast.makeText(requireContext(), "Error al actualizar la disponibilidad: ${e.message}", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged() // Para que el switch vuelva a su estado original
            }
    }
}
