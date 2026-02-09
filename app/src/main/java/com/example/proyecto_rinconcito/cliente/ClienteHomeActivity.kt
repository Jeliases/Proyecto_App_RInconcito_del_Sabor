package com.example.proyecto_rinconcito.cliente

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.cliente.adapters.FavoritosAdapter
import com.example.proyecto_rinconcito.cliente.adapters.PlatosAdapter
import com.example.proyecto_rinconcito.models.Plato
import com.google.firebase.firestore.FirebaseFirestore

class ClienteHomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var rvFavoritos: RecyclerView
    private lateinit var rvMenu: RecyclerView

    private lateinit var favoritosAdapter: FavoritosAdapter
    private lateinit var platosAdapter: PlatosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente_home)

        db = FirebaseFirestore.getInstance()

        rvFavoritos = findViewById(R.id.rvFavoritos)
        rvMenu = findViewById(R.id.rvMenuPlatos)

        favoritosAdapter = FavoritosAdapter(mutableListOf()) { plato ->
            Toast.makeText(this, "Fav: ${plato.nombre}", Toast.LENGTH_SHORT).show()
        }

        platosAdapter = PlatosAdapter(mutableListOf()) { plato ->
            Toast.makeText(this, "Agregado: ${plato.nombre}", Toast.LENGTH_SHORT).show()
            // Luego aquí mandas al carrito
        }

        rvFavoritos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvFavoritos.adapter = favoritosAdapter

        rvMenu.layoutManager = LinearLayoutManager(this)
        rvMenu.adapter = platosAdapter

        cargarFavoritos()
        cargarMenu()
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
                        precio = (d.getDouble("precio") ?: 0.0),
                        categoria = d.getString("categoria") ?: "",
                        favorito = d.getBoolean("favorito") ?: false,
                        imagenUrl = d.getString("imagenUrl") ?: ""
                    )
                }
                favoritosAdapter.setData(list)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error favoritos: ${e.message}", Toast.LENGTH_LONG).show()
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
                        precio = (d.getDouble("precio") ?: 0.0),
                        categoria = d.getString("categoria") ?: "",
                        favorito = d.getBoolean("favorito") ?: false,
                        imagenUrl = d.getString("imagenUrl") ?: ""
                    )
                }
                platosAdapter.setData(list)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error menú: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
