package com.example.proyecto_rinconcito.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.auth.LoginActivity
import com.example.proyecto_rinconcito.databinding.ActivityAdminBinding
import com.google.firebase.auth.FirebaseAuth

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val auth = FirebaseAuth.getInstance()

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

    private fun setupToolbar() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
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
            val intent = Intent(this, AddEditPlatoActivity::class.java)
            startActivity(intent)
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
