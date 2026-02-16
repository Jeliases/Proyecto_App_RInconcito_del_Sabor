package com.example.proyecto_rinconcito.cliente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.cliente.adapters.FavoritosAdapter
import com.example.proyecto_rinconcito.cliente.adapters.PlatosAdapter
import com.example.proyecto_rinconcito.models.ItemPedido
import com.example.proyecto_rinconcito.models.Plato
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore


import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;
import com.example.proyecto_rinconcito.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth


class ClienteHomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var rvFavoritos: RecyclerView
    private lateinit var rvMenu: RecyclerView
    private lateinit var btnCarrito: FloatingActionButton

    private lateinit var favoritosAdapter: FavoritosAdapter
    private lateinit var platosAdapter: PlatosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente_home)

        db = FirebaseFirestore.getInstance()

        // Referencias
        rvFavoritos = findViewById(R.id.rvFavoritos)
        rvMenu = findViewById(R.id.rvMenuPlatos)
        btnCarrito = findViewById(R.id.btnCarrito)

        // Configurar adapters
        favoritosAdapter = FavoritosAdapter(mutableListOf()) { plato ->
            Toast.makeText(this, "Fav: ${plato.nombre}", Toast.LENGTH_SHORT).show()
        }

        platosAdapter = PlatosAdapter(mutableListOf()) { plato ->

            val item = ItemPedido(
                nombre = plato.nombre,
                cantidad = 1,
                precio = plato.precio
            )

            CarritoManager.agregarItem(item)

            Toast.makeText(
                this,
                "${plato.nombre} agregado al carrito 🛒",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Configurar RecyclerViews
        rvFavoritos.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvFavoritos.adapter = favoritosAdapter

        rvMenu.layoutManager = LinearLayoutManager(this)
        rvMenu.adapter = platosAdapter

        // Botón carrito
        btnCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        // Cargar datos
        cargarFavoritos()
        cargarMenu()
        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que deseas cerrar sesión?")
                .setCancelable(false)
                .setPositiveButton("Sí") { _, _ ->

                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
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
                Toast.makeText(
                    this,
                    "Error favoritos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
                Toast.makeText(
                    this,
                    "Error menú: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
