package com.example.proyecto_rinconcito.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.databinding.ItemPedidoAdminBinding
import com.example.proyecto_rinconcito.models.PedidoAdmin

class PedidoAdminAdapter(
    private val pedidos: List<PedidoAdmin>,
    private val onPedidoClicked: (PedidoAdmin) -> Unit
) : RecyclerView.Adapter<PedidoAdminAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPedidoAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.bind(pedido)
        holder.itemView.setOnClickListener { onPedidoClicked(pedido) }
    }

    override fun getItemCount(): Int = pedidos.size

    class ViewHolder(private val binding: ItemPedidoAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pedido: PedidoAdmin) {
            binding.tvCodigoPedido.text = pedido.codigo
            binding.tvTotal.text = String.format("S/ %.2f", pedido.total)
            binding.chipEstado.text = pedido.estado

            val color = when (pedido.estado) {
                "PENDIENTE_PAGO" -> "#FFC107"
                "PAGO_EN_VERIFICACION" -> "#FF9800"
                "EN_PREPARACION" -> "#2196F3"
                "LISTO_PARA_RECOGER" -> "#4CAF50"
                "ENTREGADO" -> "#BDBDBD"
                "CANCELADO" -> "#F44336"
                else -> "#E0E0E0"
            }
            binding.chipEstado.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(color))
        }
    }
}
