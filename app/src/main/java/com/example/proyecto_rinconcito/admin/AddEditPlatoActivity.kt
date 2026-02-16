package com.example.proyecto_rinconcito.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.proyecto_rinconcito.databinding.ActivityAddEditPlatoBinding
import com.example.proyecto_rinconcito.models.Plato
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddEditPlatoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditPlatoBinding
    private var platoId: String? = null
    private var platoActual: Plato? = null
    private var imagenUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditPlatoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        platoId = intent.getStringExtra("PLATO_ID")

        setupToolbar()
        setupCategoriaDropdown()
        setupListeners()

        if (platoId != null) {
            binding.topAppBar.title = "Editar Plato"
            cargarDatosDelPlato()
        } else {
            binding.topAppBar.title = "Añadir Plato"
        }
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener { finish() }
    }

    private fun setupCategoriaDropdown() {
        val categorias = listOf("Broaster", "Parrilla", "Ensaladas", "Bebidas", "Postres") // Puedes obtenerlas de otro lugar si quieres
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categorias)
        binding.actvCategoria.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnSeleccionarImagen.setOnClickListener {
            abrirSelectorDeImagen()
        }
        binding.btnGuardar.setOnClickListener {
            guardarPlato()
        }
    }

    private fun cargarDatosDelPlato() {
        platoId?.let {
            db.collection("platos").document(it).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        platoActual = doc.toObject(Plato::class.java)
                        platoActual?.let { plato ->
                            binding.etNombre.setText(plato.nombre)
                            binding.etDescripcion.setText(plato.descripcion)
                            binding.etPrecio.setText(plato.precio.toString())
                            binding.actvCategoria.setText(plato.categoria, false)
                            Glide.with(this).load(plato.imagenUrl).into(binding.ivPlatoPreview)
                        }
                    }
                }
        }
    }

    private fun abrirSelectorDeImagen() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imagenUri = data.data
            Glide.with(this).load(imagenUri).into(binding.ivPlatoPreview)
        }
    }

    private fun guardarPlato() {
        // TODO: Añadir validación de campos

        binding.btnGuardar.isEnabled = false // Deshabilitar para evitar guardados múltiples

        if (imagenUri != null) {
            // Si hay una nueva imagen, subirla primero
            subirImagenYGuardarDatos()
        } else if (platoId != null) {
            // Si no hay nueva imagen pero estamos editando, solo guardar datos
            guardarDatos(platoActual!!.imagenUrl) // Usamos la URL de la imagen que ya tenía
        } else {
            // Si estamos creando un plato nuevo, la imagen es obligatoria
            Toast.makeText(this, "Por favor, selecciona una imagen para el plato.", Toast.LENGTH_SHORT).show()
            binding.btnGuardar.isEnabled = true
        }
    }

    private fun subirImagenYGuardarDatos() {
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("platos/$filename")

        imagenUri?.let {
            ref.putFile(it)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        guardarDatos(url.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                    binding.btnGuardar.isEnabled = true
                }
        }
    }

    private fun guardarDatos(imageUrl: String) {
        val nombre = binding.etNombre.text.toString()
        val descripcion = binding.etDescripcion.text.toString()
        val precio = binding.etPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val categoria = binding.actvCategoria.text.toString()

        val plato = Plato(
            id = platoId ?: UUID.randomUUID().toString(), // Si estamos editando, usamos el ID existente
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            categoria = categoria,
            imagenUrl = imageUrl
        )

        val document = if (platoId != null) {
            db.collection("platos").document(platoId!!)
        } else {
            db.collection("platos").document(plato.id)
        }

        document.set(plato)
            .addOnSuccessListener {
                Toast.makeText(this, "Plato guardado con éxito", Toast.LENGTH_SHORT).show()
                finish() // Cerrar la actividad y volver a la lista
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar el plato", Toast.LENGTH_SHORT).show()
                binding.btnGuardar.isEnabled = true
            }
    }
}
