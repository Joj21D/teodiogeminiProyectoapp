package com.example.lgw;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GastoAdapter extends RecyclerView.Adapter<GastoAdapter.GastoViewHolder> {

    private List<Gasto> listaGastos;

    public GastoAdapter(List<Gasto> listaGastos) {
        this.listaGastos = listaGastos;
    }

    // NEW: Method to update the list without creating a new adapter
    public void actualizarLista(List<Gasto> nuevaLista) {
        this.listaGastos = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Aquí "inflamos" tu diseño visual (item_gasto.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gasto, parent, false);
        return new GastoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        Gasto gastoActual = listaGastos.get(position);

        // Formateador de miles
        java.text.DecimalFormat formateador = new java.text.DecimalFormat("#,###");

        // Asignamos los textos reales
        holder.tvIndice.setText((position + 1) + ".");
        holder.tvNombre.setText(gastoActual.getProveedor());
        holder.tvPrecio.setText("$ " + formateador.format(gastoActual.getMonto()));
// --- EL PINTOR DE MEMORIA ---
        if (gastoActual.getFueEditado() == 1) {
            int colorAmarillo = android.graphics.Color.parseColor("#FBC02D");
            holder.tvNombre.setTextColor(colorAmarillo);
            holder.tvPrecio.setTextColor(colorAmarillo);
        } else {
            holder.tvNombre.setTextColor(android.graphics.Color.BLACK);
            holder.tvPrecio.setTextColor(android.graphics.Color.BLACK);
        }
        // --- EL NUEVO CABLE MÁGICO (VERSIÓN ANTI-DISFRACES) ---
        holder.btnDetalle.setOnClickListener(v -> {
            android.content.Context contexto = v.getContext();

            // Bucle para quitarle los disfraces (ContextWrappers) al botón
            while (contexto instanceof android.content.ContextWrapper) {
                if (contexto instanceof CalculadoraActivity) {
                    break;
                }
                contexto = ((android.content.ContextWrapper) contexto).getBaseContext();
            }

            // Ahora sí, con la actividad real desenmascarada, abrimos la ventana
            if (contexto instanceof CalculadoraActivity) {
                CalculadoraActivity actividad = (CalculadoraActivity) contexto;
                actividad.mostrarDialogoDetalle(gastoActual);
            } else {
                // Seguro de vida: Si falla de nuevo, gritará un error en vez de quedarse callado
                android.widget.Toast.makeText(v.getContext(), "Error de contexto", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaGastos.size();
    }

    // Clase interna que conecta los IDs de tu XML
    public static class GastoViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndice, tvNombre, tvPrecio;
        Button btnDetalle;

        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndice = itemView.findViewById(R.id.tv_indice);
            tvNombre = itemView.findViewById(R.id.tv_nombre_gasto);
            tvPrecio = itemView.findViewById(R.id.tv_precio_gasto);
            btnDetalle = itemView.findViewById(R.id.btn_detalle);
        }
    }
}
