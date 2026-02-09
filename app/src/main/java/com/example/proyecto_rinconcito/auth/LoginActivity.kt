package com.example.proyecto_rinconcito.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.cliente.ClienteHomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoRegister: TextView
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoRegister = findViewById(R.id.tvGoRegister)
        progressBar = findViewById(R.id.progressBar)

        btnLogin.setOnClickListener { doLogin() }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Auto-login si ya hay sesión
        val user = auth.currentUser
        if (user != null) {
            goByRole(user.uid)
        }
    }

    private fun doLogin() {
        val email = etEmail.text.toString().trim()
        val pass = etPassword.text.toString()

        if (!isValidEmail(email)) {
            etEmail.error = "Correo inválido"
            etEmail.requestFocus()
            return
        }
        if (pass.length < 6) {
            etPassword.error = "Mínimo 6 caracteres"
            etPassword.requestFocus()
            return
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
                    // Si existe auth pero no existe doc en Firestore
                    Toast.makeText(this, "No existe perfil en Firestore. Regístrate otra vez.", Toast.LENGTH_LONG).show()
                    auth.signOut()
                    return@addOnSuccessListener
                }

                val rol = (doc.getString("rol") ?: "cliente").lowercase()

                when (rol) {
                    "admin" -> {
                        // TODO: cambia esto cuando crees tu AdminActivity real
                        // startActivity(Intent(this, AdminHomeActivity::class.java))
                        startActivity(Intent(this, ClienteHomeActivity::class.java)) // temporal
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
        progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
        tvGoRegister.isEnabled = !loading
        etEmail.isEnabled = !loading
        etPassword.isEnabled = !loading
    }
}
