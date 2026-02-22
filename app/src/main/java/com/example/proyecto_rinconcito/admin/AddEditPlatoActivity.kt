package com.example.proyecto_rinconcito.admin

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
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

    private val imagePickerLauncher: ActivityResultLauncher<String> = 
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> 
            uri?.let {
                imagenUri = it
                Glide.with(this).load(imagenUri).into(binding.ivPlatoPreview)
            }
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
        val categorias = listOf("Broaster", "Parrilla", "Ensaladas", "Bebidas", "Postres")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categorias)
        binding.actvCategoria.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnSeleccionarImagen.setOnClickListener { abrirSelectorDeImagen() }
        binding.btnGuardar.setOnClickListener { guardarPlato() }

        // Limpiar errores de validación al escribir
        binding.etNombre.doOnTextChanged { _, _, _, _ -> binding.tilNombre.error = null }
        binding.etPrecio.doOnTextChanged { _, _, _, _ -> binding.tilPrecio.error = null }
        binding.actvCategoria.doOnTextChanged { _, _, _, _ -> binding.tilCategoria.error = null }
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
                            if (plato.imagenUrl.isNotEmpty()) {
                                Glide.with(this).load(plato.imagenUrl).into(binding.ivPlatoPreview)
                            }
                        }
                    } else {
                        Toast.makeText(this, "No se encontraron los datos del plato.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .addOnFailureListener { 
                    Toast.makeText(this, "Error al cargar los datos del plato.", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    private fun abrirSelectorDeImagen() {
        imagePickerLauncher.launch("image/*")
    }

    private fun guardarPlato() {
        if (!validarCampos()) return

        binding.btnGuardar.isEnabled = false

        val urlImagenExistente = platoActual?.imagenUrl

        if (imagenUri != null) {
            subirImagenYGuardarDatos()
        } else if (urlImagenExistente != null) {
            guardarDatos(urlImagenExistente)
        } else {
            Toast.makeText(this, "Por favor, selecciona una imagen.", Toast.LENGTH_SHORT).show()
            binding.btnGuardar.isEnabled = true
        }
    }

    private fun subirImagenYGuardarDatos() {
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("platos/$filename")

        imagenUri?.let {
            ref.putFile(it)
                .addOnSuccessListener { ref.downloadUrl.addOnSuccessListener { url -> guardarDatos(url.toString()) } }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnGuardar.isEnabled = true
                }
        }
    }

    private fun guardarDatos(imageUrl: String) {
        val nombre = binding.etNombre.text.toString()
        val descripcion = binding.etDescripcion.text.toString()
        val precio = binding.etPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val categoria = binding.actvCategoria.text.toString()
        
        val id = platoId ?: db.collection("platos").document().id

        val plato = Plato(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            categoria = categoria,
            imagenUrl = imageUrl,
            // Preservar los valores existentes al editar, o usar defaults al crear
            favorito = platoActual?.favorito ?: false,
            activo = platoActual?.activo ?: true 
        )

        db.collection("platos").document(id).set(plato)
            .addOnSuccessListener {
                Toast.makeText(this, "Plato guardado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el plato: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnGuardar.isEnabled = true
            }
    }

    private fun validarCampos(): Boolean {
        if (binding.etNombre.text.isNullOrBlank()) {
            binding.tilNombre.error = "El nombre es obligatorio"
            return false
        }
        if (binding.etPrecio.text.isNullOrBlank()) {
            binding.tilPrecio.error = "El precio es obligatorio"
            return false
        }
        if (binding.actvCategoria.text.isNullOrBlank()) {
            binding.tilCategoria.error = "La categoría es obligatoria"
            return false
        }
        return true
    }
}
