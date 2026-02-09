package com.example.proyecto_rinconcito.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_rinconcito.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoLogin: TextView
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNombre = findViewById(R.id.etNombre)
        etEmail = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoLogin = findViewById(R.id.tvGoLogin)
        progressBar = findViewById(R.id.progressBar)

        btnRegister.setOnClickListener { doRegister() }

        tvGoLogin.setOnClickListener {
            finish()
        }
    }

    private fun doRegister() {
        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val pass = etPassword.text.toString()

        if (nombre.isEmpty()) {
            etNombre.error = "Ingrese su nombre"
            etNombre.requestFocus()
            return
        }
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

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    setLoading(false)
                    Toast.makeText(this, "No se pudo obtener UID.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // PERFIL EN FIRESTORE (sin password!)
                val data = hashMapOf(
                    "nombre" to nombre,
                    "correo" to email,
                    "rol" to "cliente", // por defecto: cliente
                    "createdAt" to Timestamp.now()
                )

                db.collection("usuarios").document(uid).set(data)
                    .addOnSuccessListener {
                        setLoading(false)
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setLoading(loading: Boolean) {
        progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !loading
        tvGoLogin.isEnabled = !loading
        etNombre.isEnabled = !loading
        etEmail.isEnabled = !loading
        etPassword.isEnabled = !loading
    }
}
