package com.example.proyecto_rinconcito.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.models.Plato
import com.google.android.material.button.MaterialButton

class PlatosAdapter(
    private val items: MutableList<Plato>,
    private var isRestaurantOpen: Boolean,
    private val onAgregar: (Plato) -> Unit
) : RecyclerView.Adapter<PlatosAdapter.PlatoVH>() {

    class PlatoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        val btnAgregar: MaterialButton = itemView.findViewById(R.id.btnAgregar)
        val imgPlato: ImageView = itemView.findViewById(R.id.imgPlato)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatoVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_plato, parent, false)
        return PlatoVH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PlatoVH, position: Int) {
        val p = items[position]
        holder.tvNombre.text = p.nombre
        holder.tvDesc.text = p.descripcion
        holder.tvPrecio.text = "S/ %.2f".format(p.precio)

        Glide.with(holder.itemView.context).load(p.imagenUrl).into(holder.imgPlato)

        val isAvailable = p.activo&& isRestaurantOpen

        holder.btnAgregar.isEnabled = isAvailable
        holder.itemView.alpha = if (isAvailable) 1.0f else 0.6f
        
        if(isAvailable){
            holder.btnAgregar.text = "Agregar"
            holder.btnAgregar.setOnClickListener { onAgregar(p) }
        } else {
            holder.btnAgregar.text = if (!p.activo) "Agotado" else "Cerrado"
            holder.btnAgregar.setOnClickListener(null)
        }
    }

    fun setData(newItems: List<Plato>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setRestaurantOpen(isOpen: Boolean) {
        isRestaurantOpen = isOpen
        notifyDataSetChanged()
    }
}
