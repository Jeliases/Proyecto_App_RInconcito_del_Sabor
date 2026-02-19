package com.example.proyecto_rinconcito.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.auth.LoginActivity
import com.example.proyecto_rinconcito.databinding.ActivityAdminBinding
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var restaurantStatusListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupBottomNavigation()
        setupFab()

        if (savedInstanceState == null) {
            replaceFragment(AdminPedidosFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_pedidos
            binding.fab.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        restaurantStatusListener?.remove()
    }

    private fun setupToolbar() {
        val switchItem = binding.topAppBar.menu.findItem(R.id.action_toggle_status)
        val statusSwitch = switchItem.actionView as SwitchMaterial

        restaurantStatusListener = db.collection("restaurante").document("estado")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    db.collection("restaurante").document("estado").set(mapOf("abierto" to false))
                    statusSwitch.isChecked = false
                    return@addSnapshotListener
                }
                val isOpen = snapshot.getBoolean("abierto") ?: false
                statusSwitch.isChecked = isOpen
            }

        statusSwitch.setOnCheckedChangeListener { _, isChecked ->
            db.collection("restaurante").document("estado").update("abierto", isChecked)
                .addOnSuccessListener { Toast.makeText(this, if (isChecked) "Restaurante ABIERTO" else "Restaurante CERRADO", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { Toast.makeText(this, "Error al cambiar el estado", Toast.LENGTH_SHORT).show() }
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_gestionar_horario -> {
                    startActivity(Intent(this, HorarioActivity::class.java))
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

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_pedidos -> {
                    replaceFragment(AdminPedidosFragment())
                    binding.topAppBar.title = "Pedidos"
                    binding.fab.visibility = View.GONE
                    true
                }
                R.id.nav_menu -> {
                    replaceFragment(AdminMenuFragment())
                    binding.topAppBar.title = "Gestión de Menú"
                    binding.fab.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddEditPlatoActivity::class.java))
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_admin, fragment)
            .commit()
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
