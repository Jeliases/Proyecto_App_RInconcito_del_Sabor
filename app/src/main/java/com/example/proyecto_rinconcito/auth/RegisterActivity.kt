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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoLogin: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoLogin = findViewById(R.id.tvGoLogin)
        progressBar = findViewById(R.id.progressBar)

        btnRegister.setOnClickListener { doRegister() }

        tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun doRegister() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val pass = etPassword.text.toString()

        if (nombre.isBlank()) {
            etNombre.error = "Ingresa tu nombre"
            etNombre.requestFocus()
            return
        }

        if (!isValidEmail(correo)) {
            etCorreo.error = "Correo inválido"
            etCorreo.requestFocus()
            return
        }

        if (pass.length < 6) {
            etPassword.error = "Mínimo 6 caracteres"
            etPassword.requestFocus()
            return
        }

        setLoading(true)

        auth.createUserWithEmailAndPassword(correo, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    setLoading(false)
                    Toast.makeText(this, "No se pudo obtener UID.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Perfil (NO guardar contraseña aquí)
                val userDoc = hashMapOf(
                    "nombre" to nombre,
                    "correo" to correo,
                    "rol" to "cliente",        // por defecto cliente
                    "enabled" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("usuarios")
                    .document(uid) // <-- aquí está el "auto id": uid real del usuario
                    .set(userDoc)
                    .addOnSuccessListener {
                        setLoading(false)
                        Toast.makeText(this, "Registro OK", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        setLoading(false)
                        Toast.makeText(this, "Error guardando perfil: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Error registro: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !loading
        tvGoLogin.isEnabled = !loading
        etNombre.isEnabled = !loading
        etCorreo.isEnabled = !loading
        etPassword.isEnabled = !loading
    }
}
