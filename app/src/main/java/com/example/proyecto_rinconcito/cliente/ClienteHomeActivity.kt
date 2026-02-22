package com.example.proyecto_rinconcito.cliente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.auth.LoginActivity
import com.example.proyecto_rinconcito.adapters.FavoritosAdapter
import com.example.proyecto_rinconcito.adapters.PlatosAdapter
import com.example.proyecto_rinconcito.databinding.ActivityClienteHomeBinding
import com.example.proyecto_rinconcito.models.Horario
import com.example.proyecto_rinconcito.models.ItemPedido
import com.example.proyecto_rinconcito.models.Plato
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ClienteHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClienteHomeBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var favoritosAdapter: FavoritosAdapter
    private lateinit var platosAdapter: PlatosAdapter
    private var statusListener: ListenerRegistration? = null
    private var horarioListener: ListenerRegistration? = null
    private var isRestaurantOpen = true

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

        escucharEstadoDelRestaurante()
        cargarFavoritos()
        cargarMenu()
    }

    override fun onDestroy() {
        super.onDestroy()
        statusListener?.remove()
        horarioListener?.remove()
    }

    private fun escucharEstadoDelRestaurante() {
        val statusRef = db.collection("restaurante").document("estado")
        val horarioRef = db.collection("restaurante").document("horario")

        statusListener = statusRef.addSnapshotListener { snapshot, _ ->
            val manualOpen = snapshot?.getBoolean("abierto") ?: false
            horarioRef.get().addOnSuccessListener { horarioDoc ->
                val horario = horarioDoc.toObject(Horario::class.java)
                verificarDisponibilidad(manualOpen, horario)
            }
        }

        horarioListener = horarioRef.addSnapshotListener { _, _ ->
            statusRef.get().addOnSuccessListener { statusDoc ->
                val manualOpen = statusDoc.getBoolean("abierto") ?: false
                horarioRef.get().addOnSuccessListener { horarioDoc ->
                    val horario = horarioDoc.toObject(Horario::class.java)
                    verificarDisponibilidad(manualOpen, horario)
                }
            }
        }
    }

    private fun verificarDisponibilidad(manualOpen: Boolean, horario: Horario?) {

        val ahora = Calendar.getInstance()
        val dayOfWeek = ahora.get(Calendar.DAY_OF_WEEK)

        val diaActual = when (dayOfWeek) {
            Calendar.MONDAY -> horario?.lunes
            Calendar.TUESDAY -> horario?.martes
            Calendar.WEDNESDAY -> horario?.miercoles
            Calendar.THURSDAY -> horario?.jueves
            Calendar.FRIDAY -> horario?.viernes
            Calendar.SATURDAY -> horario?.sabado
            Calendar.SUNDAY -> horario?.domingo
            else -> null
        }

        var horarioOpen = false

        if (diaActual?.abierto == true) {
            try {
                val sdf = SimpleDateFormat("hh:mm a", Locale.US)

                val aperturaDate = sdf.parse(diaActual.apertura)
                val cierreDate = sdf.parse(diaActual.cierre)

                val calApertura = Calendar.getInstance().apply { time = aperturaDate!! }
                val calCierre = Calendar.getInstance().apply { time = cierreDate!! }


                val ahoraMinutos = ahora.get(Calendar.HOUR_OF_DAY) * 60 + ahora.get(Calendar.MINUTE)
                val aperturaMinutos = calApertura.get(Calendar.HOUR_OF_DAY) * 60 + calApertura.get(Calendar.MINUTE)
                val cierreMinutos = calCierre.get(Calendar.HOUR_OF_DAY) * 60 + calCierre.get(Calendar.MINUTE)

                if (aperturaMinutos <= cierreMinutos) {

                    horarioOpen = ahoraMinutos in aperturaMinutos..cierreMinutos
                } else {

                    horarioOpen = ahoraMinutos >= aperturaMinutos || ahoraMinutos <= cierreMinutos
                }

            } catch (e: Exception) {
                e.printStackTrace()
                horarioOpen = false
            }
        }

        isRestaurantOpen = manualOpen && horarioOpen
        actualizarUI(isRestaurantOpen)
    }
    private fun actualizarUI(isOpen: Boolean) {
        binding.tvRestauranteCerrado.visibility = if (isOpen) View.GONE else View.VISIBLE
        binding.btnCarrito.visibility = if (isOpen) View.VISIBLE else View.GONE

        if (::platosAdapter.isInitialized) platosAdapter.setRestaurantOpen(isOpen)
        if (::favoritosAdapter.isInitialized) favoritosAdapter.setRestaurantOpen(isOpen)
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
        favoritosAdapter = FavoritosAdapter(mutableListOf(), isRestaurantOpen) { plato ->
            if (isRestaurantOpen) agregarAlCarrito(plato)
        }

        platosAdapter = PlatosAdapter(mutableListOf(), isRestaurantOpen) { plato ->
            if (isRestaurantOpen) agregarAlCarrito(plato)
        }
    }

    private fun setupRecyclerViews() {
        binding.rvFavoritos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFavoritos.adapter = favoritosAdapter
        binding.rvMenuPlatos.layoutManager = LinearLayoutManager(this)
        binding.rvMenuPlatos.adapter = platosAdapter
    }

    private fun setupListeners() {
        binding.btnCarrito.setOnClickListener {
            if(isRestaurantOpen) startActivity(Intent(this, CarritoActivity::class.java))
        }
    }

    private fun cargarFavoritos() {
        db.collection("platos").whereEqualTo("favorito", true).get()
            .addOnSuccessListener { snap ->
                val list = snap.toObjects(Plato::class.java)
                favoritosAdapter.setData(list)
            }
    }

    private fun cargarMenu() {
        db.collection("platos").get()
            .addOnSuccessListener { snap ->
                val list = snap.toObjects(Plato::class.java)
                platosAdapter.setData(list)
            }
    }

    private fun agregarAlCarrito(plato: Plato) {
        val item = ItemPedido(plato.nombre, 1, plato.precio)
        CarritoManager.agregarItem(item)
        Toast.makeText(this, "${plato.nombre} agregado al carrito 🛒", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarDialogoDeLogout() {
        AlertDialog.Builder(this).setTitle("Cerrar sesión")
            .setMessage("¿Seguro que deseas cerrar sesión?")
            .setCancelable(false)
            .setPositiveButton("Sí") { _, _ ->

                statusListener?.remove()
                horarioListener?.remove()


                auth.signOut()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null).show()
    }
}
