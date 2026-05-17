package com.example.lgw;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResumenActivity extends AppCompatActivity {

    private TextView tvFecha, tvMontoTotal, tvVentaMayor, tvGastoMayor;
    private TextView tvDiferenciaAyer, tvNetoMes, tvCantCompras, tvCantVentas;
    private ImageButton btnVolver;

    private DatabaseHelper db;
    private List<Gasto> todosLosGastos;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private DecimalFormat formateador = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen);

        tvFecha = findViewById(R.id.tv_resumen_fecha);
        tvMontoTotal = findViewById(R.id.tv_resumen_monto_total);
        tvVentaMayor = findViewById(R.id.tv_resumen_venta_mayor);
        tvGastoMayor = findViewById(R.id.tv_resumen_gasto_mayor);
        tvDiferenciaAyer = findViewById(R.id.tv_resumen_diferencia_ayer);
        tvNetoMes = findViewById(R.id.tv_resumen_neto_mes);
        tvCantCompras = findViewById(R.id.tv_resumen_cant_compras);
        tvCantVentas = findViewById(R.id.tv_resumen_cant_ventas);
        btnVolver = findViewById(R.id.btn_volver_resumen);

        btnVolver.setOnClickListener(v -> finish());

        db = new DatabaseHelper(this);
        todosLosGastos = db.obtenerTodosLosGastos();

        if (todosLosGastos.isEmpty()) {
            tvFecha.setText("No hay datos registrados 📅");
            return;
        }

        String ultimaFecha = todosLosGastos.get(todosLosGastos.size() - 1).getFecha();
        calcularMetricas(ultimaFecha);

        tvFecha.setOnClickListener(v -> mostrarCalendarioNativo());
    }

    private void mostrarCalendarioNativo() {
        final Calendar c = Calendar.getInstance();

        try {
            String cabecera = tvFecha.getText().toString();
            if (cabecera.contains("Fecha:")) {
                String fechaLimpia = cabecera.replaceAll("[^0-9/]", "");
                c.setTime(sdf.parse(fechaLimpia));
            }
        } catch (Exception e) {}

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String fechaElegida = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                    calcularMetricas(fechaElegida);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void calcularMetricas(String fechaTarget) {
        String fechaTargetLimpia = fechaTarget.replaceAll("[^0-9/]", "");
        tvFecha.setText("Fecha: " + fechaTargetLimpia + " 📅");

        double ventasHoy = 0, comprasHoy = 0;
        double ventaMayor = 0, gastoMayor = 0;
        int cantVentas = 0, cantCompras = 0;
        String nombreVentaMayor = "Ninguna", nombreGastoMayor = "Ninguno";

        double ventasMes = 0, comprasMes = 0;
        double ventasAyer = 0, comprasAyer = 0;

        String mesAnoTarget = fechaTargetLimpia.length() >= 7 ? fechaTargetLimpia.substring(3) : "";
        String fechaAyerLimpia = obtenerFechaAyer(fechaTargetLimpia).replaceAll("[^0-9/]", "");

        for (Gasto g : todosLosGastos) {
            // DOBLE KATANA PARA LAS MATEMÁTICAS
            String fechaDB = g.getFecha() != null ? g.getFecha().replaceAll("[^0-9/]", "") : "";

            if (fechaDB.equals(fechaTargetLimpia)) {
                if (g.getEsVenta() == 1) {
                    ventasHoy += g.getMonto();
                    cantVentas++;
                    if (g.getMonto() > ventaMayor) {
                        ventaMayor = g.getMonto();
                        nombreVentaMayor = g.getProveedor();
                    }
                } else {
                    comprasHoy += g.getMonto();
                    cantCompras++;
                    if (g.getMonto() > gastoMayor) {
                        gastoMayor = g.getMonto();
                        nombreGastoMayor = g.getProveedor();
                    }
                }
            }

            if (fechaDB.equals(fechaAyerLimpia)) {
                if (g.getEsVenta() == 1) { ventasAyer += g.getMonto(); }
                else { comprasAyer += g.getMonto(); }
            }

            if (!mesAnoTarget.isEmpty() && fechaDB.endsWith(mesAnoTarget)) {
                if (g.getEsVenta() == 1) { ventasMes += g.getMonto(); }
                else { comprasMes += g.getMonto(); }
            }
        }

        double netoHoy = ventasHoy - comprasHoy;
        double netoAyer = ventasAyer - comprasAyer;
        double diferenciaAyer = netoHoy - netoAyer;
        double netoMes = ventasMes - comprasMes;

        tvMontoTotal.setText("$ " + formateador.format(netoHoy));
        tvVentaMayor.setText("Venta Mayor: $" + formateador.format(ventaMayor) + " (" + nombreVentaMayor + ")");
        tvGastoMayor.setText("Gasto Mayor: $" + formateador.format(gastoMayor) + " (" + nombreGastoMayor + ")");

        String prefijoDif = diferenciaAyer > 0 ? "+$" : (diferenciaAyer < 0 ? "-$" : "$");
        tvDiferenciaAyer.setText("Diferencia de ayer: " + prefijoDif + formateador.format(Math.abs(diferenciaAyer)));

        String prefijoMes = netoMes < 0 ? "-$" : "$";
        tvNetoMes.setText("Neto Acumulado mes (" + mesAnoTarget + "): " + prefijoMes + formateador.format(Math.abs(netoMes)));

        tvCantCompras.setText("Compras: " + cantCompras);
        tvCantVentas.setText("Ventas: " + cantVentas);

        tvMontoTotal.setTextColor(netoHoy >= 0 ? android.graphics.Color.parseColor("#4CAF50") : android.graphics.Color.parseColor("#F44336"));
        tvDiferenciaAyer.setTextColor(diferenciaAyer >= 0 ? android.graphics.Color.parseColor("#4CAF50") : android.graphics.Color.parseColor("#F44336"));
        tvNetoMes.setTextColor(netoMes >= 0 ? android.graphics.Color.parseColor("#2196F3") : android.graphics.Color.parseColor("#F44336"));
    }

    private String obtenerFechaAyer(String fechaHoyStr) {
        try {
            Date fecha = sdf.parse(fechaHoyStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return "";
        }
    }
}