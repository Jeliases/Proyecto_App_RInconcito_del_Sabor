package com.example.proyecto_rinconcito.cliente

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.databinding.ActivityPagoBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class PagoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPagoBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var pedidoId: String? = null
    private var imagenComprobanteUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        getIntentData()
        setupListeners()
        setupMetodoPago() // Inicia lógica de Tabs y Descarga
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener { finish() }
    }

    private fun getIntentData() {
        pedidoId = intent.getStringExtra("pedidoId")
        val total = intent.getDoubleExtra("total", 0.0)

        if (pedidoId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: ID de pedido no encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.tvTotalPago.text = String.format("S/ %.2f", total)
    }

    private fun setupListeners() {
        binding.btnSeleccionarComprobante.setOnClickListener {
            abrirSelectorDeImagen()
        }
        binding.btnEnviarComprobante.setOnClickListener {
            subirComprobanteYActualizarPedido()
        }
        binding.btnDescargarQR.setOnClickListener {
            descargarQR()
        }
    }

    private fun setupMetodoPago() {
        binding.tabLayoutMetodo.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> binding.imgQR.setImageResource(R.drawable.qr_yape)
                    1 -> binding.imgQR.setImageResource(R.drawable.qr_plin)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun descargarQR() {
        try {
            val drawable = binding.imgQR.drawable as BitmapDrawable
            val bitmap = drawable.bitmap

            val nombreArchivo = "QR_Rinconcito_${System.currentTimeMillis()}.jpg"
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Rinconcito")
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            imageUri?.let { uri ->
                val outputStream = resolver.openOutputStream(uri)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)
                outputStream.close()
                Toast.makeText(this, "QR guardado en Galería. Ábrelo desde tu App de pago.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al descargar: ${e.message}", Toast.LENGTH_SHORT).show()
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
            imagenComprobanteUri = data.data
            binding.ivComprobantePreview.visibility = View.VISIBLE
            Glide.with(this).load(imagenComprobanteUri).into(binding.ivComprobantePreview)
            binding.btnEnviarComprobante.isEnabled = true
        }
    }

    private fun subirComprobanteYActualizarPedido() {
        if (imagenComprobanteUri == null) {
            Toast.makeText(this, "Por favor, selecciona una imagen primero.", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnEnviarComprobante.isEnabled = false
        binding.btnEnviarComprobante.text = "Subiendo comprobante..."

        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("comprobantes/$filename")

        val uploadTask = ref.putFile(imagenComprobanteUri!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                actualizarPedido(downloadUri.toString())
            } else {
                Toast.makeText(this, "Error al subir: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                binding.btnEnviarComprobante.isEnabled = true
                binding.btnEnviarComprobante.text = "Enviar Comprobante"
            }
        }
    }

    private fun actualizarPedido(comprobanteUrl: String) {
        pedidoId?.let {
            db.collection("pedidos").document(it)
                .update(
                    "estado", "PAGO_EN_VERIFICACION",
                    "comprobanteUrl", comprobanteUrl
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Gracias! Tu pago está siendo verificado.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, ClienteHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar el pedido: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.btnEnviarComprobante.isEnabled = true
                }
        }
    }
}