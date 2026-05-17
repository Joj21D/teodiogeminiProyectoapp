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

        // 1. Enlazamos los cables visuales
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

        // 2. Extraemos todo de tu Bóveda
        db = new DatabaseHelper(this);
        todosLosGastos = db.obtenerTodosLosGastos();

        // 3. Sistema de Seguridad: Si la base está vacía
        if (todosLosGastos.isEmpty()) {
            tvFecha.setText("No hay datos registrados 📅");
            return;
        }

        // 4. Por defecto, leemos la fecha del último día que tu mamá anotó algo
        String ultimaFecha = todosLosGastos.get(todosLosGastos.size() - 1).getFecha();
        calcularMetricas(ultimaFecha);

        // 5. El Calendario Nativo (A prueba de usuarios)
        tvFecha.setOnClickListener(v -> mostrarCalendarioNativo());
    }

    private void mostrarCalendarioNativo() {
        final Calendar c = Calendar.getInstance();
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

    // --- EL CEREBRO MATEMÁTICO ---
    private void calcularMetricas(String fechaTarget) {
        tvFecha.setText("Fecha: " + fechaTarget + " 📅");

        double ventasHoy = 0, comprasHoy = 0;
        double ventaMayor = 0, gastoMayor = 0;
        int cantVentas = 0, cantCompras = 0;
        String nombreVentaMayor = "Ninguna", nombreGastoMayor = "Ninguno";

        double ventasMes = 0, comprasMes = 0;
        double ventasAyer = 0, comprasAyer = 0;

        // Extraemos el Mes y calculamos matemáticamente cuál fue el día de ayer
        String mesAnoTarget = fechaTarget.substring(3); // Saca el "MM/yyyy"
        String fechaAyer = obtenerFechaAyer(fechaTarget);

        // Motor de búsqueda de ultra-alta velocidad
        for (Gasto g : todosLosGastos) {

            // A) Filtro del Día Exacto
            if (g.getFecha().equals(fechaTarget)) {
                if (g.getEsVenta() == 1) {
                    ventasHoy += g.getMonto();
                    cantVentas++;
                    if (g.getMonto() > ventaMayor) { // Guardamos el más grande
                        ventaMayor = g.getMonto();
                        nombreVentaMayor = g.getProveedor();
                    }
                } else {
                    comprasHoy += g.getMonto();
                    cantCompras++;
                    if (g.getMonto() > gastoMayor) { // Guardamos el gasto más doloroso
                        gastoMayor = g.getMonto();
                        nombreGastoMayor = g.getProveedor();
                    }
                }
            }

            // B) Filtro de Ayer (Para comparar)
            if (g.getFecha().equals(fechaAyer)) {
                if (g.getEsVenta() == 1) { ventasAyer += g.getMonto(); }
                else { comprasAyer += g.getMonto(); }
            }

            // C) Filtro del Mes (Acumulado)
            if (g.getFecha().endsWith(mesAnoTarget)) {
                if (g.getEsVenta() == 1) { ventasMes += g.getMonto(); }
                else { comprasMes += g.getMonto(); }
            }
        }

        // Resultados Finales
        double netoHoy = ventasHoy - comprasHoy;
        double netoAyer = ventasAyer - comprasAyer;
        double diferenciaAyer = netoHoy - netoAyer;
        double netoMes = ventasMes - comprasMes;

        // Inyección a los textos de la pantalla
        tvMontoTotal.setText("$ " + formateador.format(netoHoy));
        tvVentaMayor.setText("Venta Mayor: $" + formateador.format(ventaMayor) + " (" + nombreVentaMayor + ")");
        tvGastoMayor.setText("Gasto Mayor: $" + formateador.format(gastoMayor) + " (" + nombreGastoMayor + ")");

        String prefijoDif = diferenciaAyer > 0 ? "+" : "";
        tvDiferenciaAyer.setText("Diferencia de ayer: " + prefijoDif + "$" + formateador.format(diferenciaAyer));
        tvNetoMes.setText("Neto Acumulado mes (" + mesAnoTarget + "): $" + formateador.format(netoMes));

        tvCantCompras.setText("Compras: " + cantCompras);
        tvCantVentas.setText("Ventas: " + cantVentas);

        // UX de Semáforo (Verde = Dinero a favor, Rojo = Pérdida)
        tvMontoTotal.setTextColor(netoHoy >= 0 ? android.graphics.Color.parseColor("#4CAF50") : android.graphics.Color.parseColor("#F44336"));
        tvDiferenciaAyer.setTextColor(diferenciaAyer >= 0 ? android.graphics.Color.parseColor("#4CAF50") : android.graphics.Color.parseColor("#F44336"));
        tvNetoMes.setTextColor(netoMes >= 0 ? android.graphics.Color.parseColor("#2196F3") : android.graphics.Color.parseColor("#F44336"));
    }

    // Herramienta de Ingeniería de Tiempo (Resta 1 día al calendario)
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