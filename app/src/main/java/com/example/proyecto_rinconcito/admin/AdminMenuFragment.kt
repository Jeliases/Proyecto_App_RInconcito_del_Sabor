package com.example.proyecto_rinconcito.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import java.lang.Exception

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
                    Log.w("AdminMenuFragment", "Error al escuchar cambios en el menú", error)
                    Toast.makeText(requireContext(), "Error al cargar el menú", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val nuevosPlatos = snapshot.documents.mapNotNull { doc ->
                        try {
                            // Construcción manual y segura del objeto Plato para garantizar el ID
                            Plato(
                                id = doc.id, // <--
                                nombre = doc.getString("nombre") ?: "",
                                descripcion = doc.getString("descripcion") ?: "",
                                precio = (doc.get("precio") as? Number)?.toDouble() ?: 0.0,
                                categoria = doc.getString("categoria") ?: "",
                                favorito = doc.getBoolean("favorito") ?: false,
                                imagenUrl = doc.getString("imagenUrl") ?: "",
                                activo = doc.getBoolean("activo") ?: true
                            )
                        } catch (e: Exception) {
                            Log.e("AdminMenuFragment", "Error al procesar el plato ${doc.id}", e)
                            null
                        }
                    }
                    listaPlatos.clear()
                    listaPlatos.addAll(nuevosPlatos)
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
        if (plato.id.isEmpty()) {
            Toast.makeText(requireContext(), "Error: ID de plato inválido.", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("platos").document(plato.id).delete()
            .addOnSuccessListener {
                if (plato.imagenUrl.isNotEmpty()){
                    try {
                        val imagenRef = storage.getReferenceFromUrl(plato.imagenUrl)
                        imagenRef.delete()
                            .addOnSuccessListener { 
                                Toast.makeText(requireContext(), "'${plato.nombre}' eliminado con éxito.", Toast.LENGTH_SHORT).show() 
                            }
                            .addOnFailureListener { 
                                Toast.makeText(requireContext(), "Plato eliminado, pero hubo un error al borrar la imagen.", Toast.LENGTH_LONG).show() 
                            }
                    } catch(e: Exception) {
                        Log.e("AdminMenuFragment", "URL de imagen inválida al eliminar: ${plato.imagenUrl}", e)
                        Toast.makeText(requireContext(), "'${plato.nombre}' eliminado. No se pudo borrar la imagen (URL inválida).", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "'${plato.nombre}' eliminado con éxito.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e -> Toast.makeText(requireContext(), "Error al eliminar el plato: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    private fun actualizarDisponibilidad(plato: Plato, activo: Boolean) {
        if (plato.id.isEmpty()) {
            Toast.makeText(requireContext(), "Error: ID de plato inválido.", Toast.LENGTH_SHORT).show()
            adapter.notifyDataSetChanged() // Revertir visualmente el switch
            return
        }
        db.collection("platos").document(plato.id)
            .update("activo", activo)
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
            }
    }
}
