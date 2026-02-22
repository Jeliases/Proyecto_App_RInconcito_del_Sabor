package com.example.proyecto_rinconcito.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto_rinconcito.databinding.ItemDetallePedidoAdminBinding
import com.example.proyecto_rinconcito.models.ItemPedido
import com.google.firebase.firestore.FirebaseFirestore

class AdminPedidoItemsAdapter(private val items: List<ItemPedido>) : RecyclerView.Adapter<AdminPedidoItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 1. Usamos el nuevo diseño hermoso
        val binding = ItemDetallePedidoAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemDetallePedidoAdminBinding) : RecyclerView.ViewHolder(binding.root) {

        // 2. Instanciamos Firebase para buscar la foto
        private val db = FirebaseFirestore.getInstance()

        fun bind(item: ItemPedido) {
            // Textos básicos
            binding.tvItemNombre.text = item.nombre
            binding.tvItemCantidad.text = "x${item.cantidad}"
            binding.tvItemPrecio.text = String.format("S/ %.2f", item.precio * item.cantidad)

            // 3. Magia: Buscamos la foto en la colección 'platos'
            db.collection("platos")
                .whereEqualTo("nombre", item.nombre)
                .limit(1)
                .get()
                .addOnSuccessListener { documentos ->
                    if (!documentos.isEmpty) {
                        // OJO: Asegúrate de que el campo en tu Firebase se llame "imagenUrl"
                        val urlFoto = documentos.documents[0].getString("imagenUrl")

                        if (urlFoto != null) {
                            Glide.with(itemView.context)
                                .load(urlFoto)
                                .centerCrop()
                                .into(binding.imgItemPlato)
                        }
                    }
                }
        }
    }
}