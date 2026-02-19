package com.example.proyecto_rinconcito.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.databinding.ItemMiPedidoBinding
import com.example.proyecto_rinconcito.models.Pedido
import java.text.SimpleDateFormat
import java.util.Locale

class MisPedidosAdapter(
    private var pedidos: List<Pedido>,
    private val onPedidoClicked: (Pedido) -> Unit
) : RecyclerView.Adapter<MisPedidosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMiPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.bind(pedido)
        holder.itemView.setOnClickListener { onPedidoClicked(pedido) }
    }

    override fun getItemCount(): Int {
        return pedidos.size
    }

    fun setData(newPedidos: List<Pedido>) {
        this.pedidos = newPedidos
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemMiPedidoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pedido: Pedido) {
            binding.tvCodigoPedido.text = pedido.codigoPedido
            binding.tvTotalPedido.text = String.format("S/ %.2f", pedido.total)

            val sdf = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault())
            binding.tvFechaPedido.text = sdf.format(pedido.fecha)

            binding.chipEstado.text = pedido.estado.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() }

            val color = when (pedido.estado) {
                "PENDIENTE_PAGO" -> "#FFC107"
                "PAGO_EN_VERIFICACION" -> "#FF9800"
                "EN_PREPARACION" -> "#2196F3"
                "LISTO_PARA_RECOGER" -> "#4CAF50"
                "ENTREGADO" -> "#BDBDBD"
                "CANCELADO" -> "#F44336"
                else -> "#E0E0E0"
            }
            binding.chipEstado.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(color)))
        }
    }
}
