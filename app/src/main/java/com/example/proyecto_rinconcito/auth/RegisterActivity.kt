package com.example.proyecto_rinconcito.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto_rinconcito.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnRegister.setOnClickListener { doRegister() }

        binding.tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun doRegister() {
        val nombre = binding.etNombre.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim()
        val pass = binding.etPassword.text.toString()

        if (nombre.isBlank()) {
            binding.tilNombre.error = "Ingresa tu nombre"
            binding.etNombre.requestFocus()
            return
        } else {
            binding.tilNombre.error = null
        }

        if (!isValidEmail(correo)) {
            binding.tilCorreo.error = "Correo inválido"
            binding.etCorreo.requestFocus()
            return
        } else {
            binding.tilCorreo.error = null
        }

        if (pass.length < 6) {
            binding.tilPassword.error = "Mínimo 6 caracteres"
            binding.etPassword.requestFocus()
            return
        } else {
            binding.tilPassword.error = null
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
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
        binding.tvGoLogin.isEnabled = !loading
        binding.etNombre.isEnabled = !loading
        binding.etCorreo.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
    }
}
