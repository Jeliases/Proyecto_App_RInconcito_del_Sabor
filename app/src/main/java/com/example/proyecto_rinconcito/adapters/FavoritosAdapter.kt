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

class FavoritosAdapter(
    private val items: MutableList<Plato>,
    private var isRestaurantOpen: Boolean,
    private val onClick: (Plato) -> Unit
) : RecyclerView.Adapter<FavoritosAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvNombre: TextView = v.findViewById(R.id.tvNombreFav)
        val tvPrecio: TextView = v.findViewById(R.id.tvPrecioFav)
        val imgPlato: ImageView = v.findViewById(R.id.imgPlatoFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_favorito, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.tvNombre.text = p.nombre
        holder.tvPrecio.text = "S/ %.2f".format(p.precio)

        Glide.with(holder.itemView.context).load(p.imagenUrl).into(holder.imgPlato)

        val isAvailable = p.activo && isRestaurantOpen

        holder.itemView.alpha = if (isAvailable) 1.0f else 0.6f
        holder.itemView.isClickable = isAvailable

        if (isAvailable) {
            holder.itemView.setOnClickListener { onClick(p) }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = items.size

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
