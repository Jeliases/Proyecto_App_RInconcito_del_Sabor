package com.example.proyecto_rinconcito.cliente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.models.ItemPedido

class CarritoAdapter(
    private val lista: MutableList<ItemPedido>,
    private val actualizarTotal: () -> Unit
) : RecyclerView.Adapter<CarritoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidad)
        val btnMas: Button = view.findViewById(R.id.btnMas)
        val btnMenos: Button = view.findViewById(R.id.btnMenos)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = lista[position]

        holder.tvNombre.text = item.nombre
        holder.tvPrecio.text = "S/ ${item.precio}"
        holder.tvCantidad.text = item.cantidad.toString()

        holder.btnMas.setOnClickListener {
            CarritoManager.aumentarCantidad(item)
            notifyDataSetChanged()
            actualizarTotal()
        }

        holder.btnMenos.setOnClickListener {
            CarritoManager.disminuirCantidad(item)
            notifyDataSetChanged()
            actualizarTotal()
        }

        holder.btnEliminar.setOnClickListener {
            CarritoManager.eliminarItem(item)
            notifyDataSetChanged()
            actualizarTotal()
        }
    }
}
