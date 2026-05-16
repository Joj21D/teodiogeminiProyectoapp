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

    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Aquí "inflamos" tu diseño visual (item_gasto.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gasto, parent, false);
        return new GastoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        Gasto gasto = listaGastos.get(position);

        // Formateador de miles
        java.text.DecimalFormat formateador = new java.text.DecimalFormat("#,###");

        // Asignamos los textos reales
        holder.tvIndice.setText((position + 1) + ".");
        holder.tvNombre.setText(gasto.getProveedor());
        holder.tvPrecio.setText("$ " + formateador.format(gasto.getMonto()));

        // CORRECCIÓN: Al presionar "detalle", extrae la descripción real del archivo
        holder.btnDetalle.setOnClickListener(v -> {
            String detalleReal = gasto.getDescripcion();
            // Si no tiene descripción, ponemos un aviso amigable
            if (detalleReal == null || detalleReal.trim().isEmpty()) {
                detalleReal = "Sin descripción registrada";
            }
            Toast.makeText(v.getContext(), "Detalle: " + detalleReal, Toast.LENGTH_SHORT).show();
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