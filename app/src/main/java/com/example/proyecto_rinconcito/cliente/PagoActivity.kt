package com.example.proyecto_rinconcito.cliente

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.proyecto_rinconcito.databinding.ActivityPagoBinding
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
