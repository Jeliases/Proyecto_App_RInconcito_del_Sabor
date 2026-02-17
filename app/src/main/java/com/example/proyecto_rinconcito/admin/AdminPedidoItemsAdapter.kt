package com.example.proyecto_rinconcito.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.databinding.ItemCarritoBinding
import com.example.proyecto_rinconcito.models.ItemPedido

class AdminPedidoItemsAdapter(private val items: List<ItemPedido>) : RecyclerView.Adapter<AdminPedidoItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemCarritoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemPedido) {
            binding.tvNombre.text = item.nombre
            binding.tvCantidad.text = "x${item.cantidad}"
            binding.tvPrecio.text = String.format("S/ %.2f", item.precio * item.cantidad)

            // Ocultamos los botones que el admin no necesita
            binding.btnEliminar.visibility = View.GONE
            binding.btnMenos.visibility = View.GONE
            binding.btnMas.visibility = View.GONE
        }
    }
}
