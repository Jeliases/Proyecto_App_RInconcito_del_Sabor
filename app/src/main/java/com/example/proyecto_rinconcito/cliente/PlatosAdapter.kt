package com.example.proyecto_rinconcito.cliente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.models.Plato
import com.google.android.material.button.MaterialButton

class PlatosAdapter(
    private val items: MutableList<Plato>,
    private val onAgregar: (Plato) -> Unit
) : RecyclerView.Adapter<PlatosAdapter.PlatoVH>() {

    class PlatoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        val btnAgregar: MaterialButton = itemView.findViewById(R.id.btnAgregar)
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

        holder.btnAgregar.setOnClickListener { onAgregar(p) }
    }

    fun setData(newItems: List<Plato>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
