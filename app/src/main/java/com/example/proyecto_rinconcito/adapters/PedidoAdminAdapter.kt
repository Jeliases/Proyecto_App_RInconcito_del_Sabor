package com.example.proyecto_rinconcito.admin

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rinconcito.R
import com.example.proyecto_rinconcito.models.PedidoAdmin
import com.google.firebase.firestore.FirebaseFirestore

class PedidoAdminAdapter(
    private val lista: MutableList<PedidoAdmin>
) : RecyclerView.Adapter<PedidoAdminAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalAdmin)
        val btnAccion: Button = view.findViewById(R.id.btnAccion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido_admin, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val pedido = lista[position]

        holder.tvCodigo.text = "Código: ${pedido.codigo}"
        holder.tvEstado.text = "Estado: ${pedido.estado}"
        holder.tvTotal.text = "Total: S/ ${pedido.total}"

        when (pedido.estado) {

            "PAGO_EN_VERIFICACION" -> {
                holder.btnAccion.text = "Confirmar Pago"
                holder.btnAccion.isEnabled = true
                holder.btnAccion.setOnClickListener {
                    db.collection("pedidos")
                        .document(pedido.id)
                        .update("estado", "PAGADO")
                }
            }

            "PAGADO" -> {
                holder.btnAccion.text = "Marcar Listo"
                holder.btnAccion.isEnabled = true
                holder.btnAccion.setOnClickListener {
                    db.collection("pedidos")
                        .document(pedido.id)
                        .update("estado", "LISTO")
                }
            }

            "LISTO" -> {
                holder.btnAccion.text = "Esperando entrega..."
                holder.btnAccion.isEnabled = false
            }

            else -> {
                holder.btnAccion.text = "Finalizado"
                holder.btnAccion.isEnabled = false
            }
        }

    }

    private fun iniciarTimerEntrega(idPedido: String) {

        Handler(Looper.getMainLooper()).postDelayed({

            db.collection("pedidos")
                .document(idPedido)
                .update("estado", "ENTREGADO")

        }, 5 * 60 * 1000)
    }
}
