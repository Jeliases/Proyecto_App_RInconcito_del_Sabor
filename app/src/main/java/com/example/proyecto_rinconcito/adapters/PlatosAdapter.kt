package com.example.proyecto_rinconcito.cliente.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.models.Plato
import com.google.android.material.button.MaterialButton
import com.bumptech.glide.Glide


class PlatosAdapter(
    private val items: MutableList<Plato>,
    private val onAgregar: (Plato) -> Unit
) : RecyclerView.Adapter<PlatosAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvNombre: TextView = v.findViewById(R.id.tvNombre)
        val tvDesc: TextView = v.findViewById(R.id.tvDesc)
        val tvPrecio: TextView = v.findViewById(R.id.tvPrecio)
        val btnAgregar: MaterialButton = v.findViewById(R.id.btnAgregar)
        val imgPlato: ImageView = v.findViewById(R.id.imgPlato)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_plato, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.tvNombre.text = p.nombre
        holder.tvDesc.text = p.descripcion
        holder.tvPrecio.text = "S/ %.2f".format(p.precio)

        Glide.with(holder.itemView.context)
            .load(p.imagenUrl)
            .into(holder.imgPlato)

        holder.btnAgregar.setOnClickListener { onAgregar(p) }
    }


    override fun getItemCount(): Int = items.size

    fun setData(newItems: List<Plato>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }


}
