package com.example.proyecto_rinconcito.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_rinconcito.admin.AdminActivity
import com.example.proyecto_rinconcito.cliente.ClienteHomeActivity
import com.example.proyecto_rinconcito.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnLogin.setOnClickListener { doLogin() }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser
        if (user != null) {
            goByRole(user.uid)
        }
    }

    private fun doLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString()

        if (!isValidEmail(email)) {
            binding.tilEmail.error = "Correo inválido"
            binding.etEmail.requestFocus()
            return
        } else {
            binding.tilEmail.error = null
        }

        if (pass.length < 6) {
            binding.tilPassword.error = "Mínimo 6 caracteres"
            binding.etPassword.requestFocus()
            return
        } else {
            binding.tilPassword.error = null
        }

        setLoading(true)

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    setLoading(false)
                    Toast.makeText(this, "No se pudo obtener el usuario.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                goByRole(uid)
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun goByRole(uid: String) {
        setLoading(true)

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                setLoading(false)

                if (!doc.exists()) {
                    Toast.makeText(this, "No existe perfil en Firestore. Regístrate otra vez.", Toast.LENGTH_LONG).show()
                    auth.signOut()
                    return@addOnSuccessListener
                }

                val rol = (doc.getString("rol") ?: "cliente").lowercase()

                when (rol) {
                    "admin" -> {
                        startActivity(Intent(this, AdminActivity::class.java))
                    }
                    "cliente" -> {
                        startActivity(Intent(this, ClienteHomeActivity::class.java))
                    }
                    else -> {
                        Toast.makeText(this, "Rol no válido: $rol", Toast.LENGTH_LONG).show()
                        auth.signOut()
                        return@addOnSuccessListener
                    }
                }

                finish() // para que no regrese al login con "atrás"
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Error leyendo rol: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.tvGoRegister.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
    }
}
