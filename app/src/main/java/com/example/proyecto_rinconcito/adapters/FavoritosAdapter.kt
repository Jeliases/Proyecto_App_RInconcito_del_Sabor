package com.example.proyecto_rinconcito.cliente.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.models.Plato

class FavoritosAdapter(
    private val items: MutableList<Plato>,
    private val onClick: (Plato) -> Unit
) : RecyclerView.Adapter<FavoritosAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvNombre: TextView = v.findViewById(R.id.tvNombreFav)
        val tvPrecio: TextView = v.findViewById(R.id.tvPrecioFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_favorito, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.tvNombre.text = p.nombre
        holder.tvPrecio.text = "S/ %.2f".format(p.precio)

        holder.itemView.setOnClickListener { onClick(p) }
    }

    override fun getItemCount(): Int = items.size

    fun setData(newItems: List<Plato>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
