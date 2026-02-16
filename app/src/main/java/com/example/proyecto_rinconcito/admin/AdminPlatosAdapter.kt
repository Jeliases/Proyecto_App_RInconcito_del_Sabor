package com.example.proyecto_rinconcito.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto_rinconcito.databinding.ItemPlatoAdminBinding
import com.example.proyecto_rinconcito.models.Plato

class AdminPlatosAdapter(
    private var platos: List<Plato>,
    private val onEditClicked: (Plato) -> Unit,
    private val onDeleteClicked: (Plato) -> Unit
) : RecyclerView.Adapter<AdminPlatosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlatoAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plato = platos[position]
        holder.bind(plato)
        holder.binding.btnEditar.setOnClickListener { onEditClicked(plato) }
        holder.binding.btnEliminar.setOnClickListener { onDeleteClicked(plato) }
    }

    override fun getItemCount(): Int = platos.size

    fun setData(newPlatos: List<Plato>){
        this.platos = newPlatos
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemPlatoAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(plato: Plato) {
            binding.tvNombrePlato.text = plato.nombre
            binding.tvPrecioPlato.text = String.format("S/ %.2f", plato.precio)

            Glide.with(itemView.context)
                .load(plato.imagenUrl)
                .into(binding.ivPlato)
        }
    }
}
