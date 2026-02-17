package com.example.proyecto_rinconcito.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.auth.LoginActivity
import com.example.proyecto_rinconcito.cliente.adapters.FavoritosAdapter
import com.example.proyecto_rinconcito.cliente.adapters.PlatosAdapter
import com.example.proyecto_rinconcito.databinding.ActivityClienteHomeBinding
import com.example.proyecto_rinconcito.models.ItemPedido
import com.example.proyecto_rinconcito.models.Plato
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClienteHomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityClienteHomeBinding

    private lateinit var favoritosAdapter: FavoritosAdapter
    private lateinit var platosAdapter: PlatosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupToolbar()
        setupAdapters()
        setupRecyclerViews()
        setupListeners()

        cargarFavoritos()
        cargarMenu()
    }

    private fun setupToolbar() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_mis_pedidos -> {
                    startActivity(Intent(this, MisPedidosActivity::class.java))
                    true
                }
                R.id.action_logout -> {
                    mostrarDialogoDeLogout()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupAdapters() {
        favoritosAdapter = FavoritosAdapter(mutableListOf()) { plato ->
            Toast.makeText(this, "${plato.nombre} agregado desde favoritos", Toast.LENGTH_SHORT).show()
            agregarAlCarrito(plato)
        }

        platosAdapter = PlatosAdapter(mutableListOf()) { plato ->
            Toast.makeText(this, "${plato.nombre} agregado al carrito 🛒", Toast.LENGTH_SHORT).show()
            agregarAlCarrito(plato)
        }
    }

    private fun setupRecyclerViews() {
        binding.rvFavoritos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFavoritos.adapter = favoritosAdapter

        binding.rvMenuPlatos.layoutManager = LinearLayoutManager(this)
        binding.rvMenuPlatos.adapter = platosAdapter
    }

    private fun setupListeners() {
        // El listener del botón de logout ahora está en la Toolbar
        binding.btnCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }
    }

    private fun cargarFavoritos() {
        db.collection("platos")
            .whereEqualTo("favorito", true)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.map { d ->
                    Plato(
                        id = d.id,
                        nombre = d.getString("nombre") ?: "",
                        descripcion = d.getString("descripcion") ?: "",
                        precio = d.getDouble("precio") ?: 0.0,
                        categoria = d.getString("categoria") ?: "",
                        favorito = d.getBoolean("favorito") ?: false,
                        imagenUrl = d.getString("imagenUrl") ?: ""
                    )
                }
                favoritosAdapter.setData(list)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar favoritos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun cargarMenu() {
        db.collection("platos")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.map { d ->
                    Plato(
                        id = d.id,
                        nombre = d.getString("nombre") ?: "",
                        descripcion = d.getString("descripcion") ?: "",
                        precio = d.getDouble("precio") ?: 0.0,
                        categoria = d.getString("categoria") ?: "",
                        favorito = d.getBoolean("favorito") ?: false,
                        imagenUrl = d.getString("imagenUrl") ?: ""
                    )
                }
                platosAdapter.setData(list)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar el menú: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun agregarAlCarrito(plato: Plato) {
        val item = ItemPedido(
            nombre = plato.nombre,
            cantidad = 1,
            precio = plato.precio
        )
        CarritoManager.agregarItem(item)
    }

    private fun mostrarDialogoDeLogout() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que deseas cerrar sesión?")
            .setCancelable(false)
            .setPositiveButton("Sí") { _, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
