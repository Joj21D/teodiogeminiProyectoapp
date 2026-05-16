package com.example.lgw; // Verifica que coincida exactamente con tu paquete

import android.app.Dialog; // ¡Esta era la herramienta que faltaba para borrar lo rojo!
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CalculadoraActivity extends AppCompatActivity {

    // 1. Componentes de la Interfaz Visual
    private ImageButton btnToggleModo, btnVolver, btnExportar, btnImportar;
    private TextView tvModoFecha, tvMontoTotal;
    private RecyclerView rvListaCalculadora;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnAgregar;

    // 2. Componentes de Datos y Control
    private GastoAdapter adapter;
    private List<Gasto> listaGastosReales;
    private boolean esModoDiario = true;
    private String fechaRealGuardada = "Sin registros";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculadora);

        // --- PASO A: VINCULACIÓN DE VISTAS ---
        btnToggleModo = findViewById(R.id.btn_toggle_modo);
        btnVolver = findViewById(R.id.btn_volver);
        btnExportar = findViewById(R.id.btn_exportar);
        btnImportar = findViewById(R.id.btn_importar);
        tvModoFecha = findViewById(R.id.tv_modo_fecha);
        tvMontoTotal = findViewById(R.id.tv_monto_total);
        rvListaCalculadora = findViewById(R.id.rv_lista_calculadora);
        btnAgregar = findViewById(R.id.btn_agregar_gasto);

        // --- PASO B: CONEXIÓN A BASE DE DATO REAL Y CÁLCULOS ---
        DatabaseHelper db = new DatabaseHelper(CalculadoraActivity.this);
        listaGastosReales = db.obtenerTodosLosGastos();

        double totalGastado = 0.0;

        if (!listaGastosReales.isEmpty()) {
            fechaRealGuardada = listaGastosReales.get(listaGastosReales.size() - 1).getFecha();
            for (Gasto g : listaGastosReales) {
                totalGastado += g.getMonto();
            }
        }

        tvModoFecha.setText("Diario: " + fechaRealGuardada);
        java.text.DecimalFormat formateador = new java.text.DecimalFormat("#,###");
        tvMontoTotal.setText("Monto total gastado: $ " + formateador.format(totalGastado));

        // --- PASO C: CONFIGURACIÓN DEL RECYCLERVIEW (LISTA) ---
        rvListaCalculadora.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GastoAdapter(listaGastosReales);
        rvListaCalculadora.setAdapter(adapter);

        // --- PASO D: CONTROLADORES DE EVENTOS (LISTENERS) ---
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoAgregar();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CalculadoraActivity.this, "Función Exportar en construcción", Toast.LENGTH_SHORT).show();
            }
        });

        btnImportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CalculadoraActivity.this, "Función Importar en construcción", Toast.LENGTH_SHORT).show();
            }
        });

        btnToggleModo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (esModoDiario) {
                    tvModoFecha.setText("Semanal: " + fechaRealGuardada);
                    btnToggleModo.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2196F3")));
                    Toast.makeText(CalculadoraActivity.this, "Vista Semanal", Toast.LENGTH_SHORT).show();
                    esModoDiario = false;
                } else {
                    tvModoFecha.setText("Diario: " + fechaRealGuardada);
                    btnToggleModo.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                    Toast.makeText(CalculadoraActivity.this, "Vista Diaria", Toast.LENGTH_SHORT).show();
                    esModoDiario = true;
                }
            }
        });
    }

    private void mostrarDialogoAgregar() {
        View viewDialogo = getLayoutInflater().inflate(R.layout.dialog_agregar_gasto, null);

        android.widget.EditText etProveedor = viewDialogo.findViewById(R.id.et_nuevo_proveedor);
        android.widget.EditText etMonto = viewDialogo.findViewById(R.id.et_nuevo_monto);
        android.widget.EditText etCantidad = viewDialogo.findViewById(R.id.et_nueva_cantidad);
        android.widget.EditText etDescripcion = viewDialogo.findViewById(R.id.et_nueva_descripcion);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(viewDialogo)
                .setCancelable(false)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String proveedorStr = etProveedor.getText().toString().trim();
                    String montoStr = etMonto.getText().toString().trim();
                    String descStr = etDescripcion.getText().toString().trim();

                    if(proveedorStr.isEmpty() || montoStr.isEmpty()) {
                        Toast.makeText(CalculadoraActivity.this, "Faltan datos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double montoUnitario = Double.parseDouble(montoStr);
                    String cantStr = etCantidad.getText().toString().trim();
                    int cantidad = cantStr.isEmpty() ? 1 : Integer.parseInt(cantStr);

                    double montoTotalCalculado = montoUnitario * cantidad;

                    String descFinal = descStr;
                    if (cantidad > 1) {
                        descFinal = descStr + " (x" + cantidad + ")";
                    }

                    String fechaHoy = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());

                    Gasto nuevoGasto = new Gasto(fechaHoy, proveedorStr, montoTotalCalculado, descFinal);
                    DatabaseHelper db = new DatabaseHelper(CalculadoraActivity.this);
                    db.insertarGasto(nuevoGasto);

                    finish();
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);

                    Toast.makeText(CalculadoraActivity.this, "Gasto añadido", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Método para mostrar la tarjetita flotante
    // Método para mostrar la tarjetita flotante
    public void mostrarDialogoDetalle(final Gasto gastoSeleccionado) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_detalle_gasto);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        EditText etNombre = dialog.findViewById(R.id.etDetalleNombre);
        EditText etPrecio = dialog.findViewById(R.id.etDetallePrecio);
        EditText etDescripcion = dialog.findViewById(R.id.etDetalleDescripcion);

        Button btnBorrar = dialog.findViewById(R.id.btnDetalleBorrar);
        Button btnEditar = dialog.findViewById(R.id.btnDetalleEditar);
        Button btnGuardar = dialog.findViewById(R.id.btnDetalleGuardar);
        Button btnCerrar = dialog.findViewById(R.id.btnDetalleCerrar);

        etNombre.setText(gastoSeleccionado.getProveedor());
        etPrecio.setText(String.valueOf(gastoSeleccionado.getMonto()));
        etDescripcion.setText(gastoSeleccionado.getDescripcion());

        // BOTÓN CERRAR / CANCELAR
        btnCerrar.setOnClickListener(v -> {
            if (btnCerrar.getText().toString().equals("Cancelar")) {
                etNombre.setFocusable(false);
                etPrecio.setFocusable(false);
                etDescripcion.setFocusable(false);

                etNombre.setTextColor(android.graphics.Color.BLACK);
                etPrecio.setTextColor(android.graphics.Color.BLACK);
                etDescripcion.setTextColor(android.graphics.Color.BLACK);

                etNombre.setText(gastoSeleccionado.getProveedor());
                etPrecio.setText(String.valueOf(gastoSeleccionado.getMonto()));
                etDescripcion.setText(gastoSeleccionado.getDescripcion());

                btnEditar.setVisibility(View.VISIBLE);
                btnGuardar.setVisibility(View.GONE);
                btnCerrar.setText("Cerrar");
            } else {
                dialog.dismiss();
            }
        });

        // BOTÓN EDITAR (AHORA CON EL VIGILANTE INTELIGENTE)
        btnEditar.setOnClickListener(v -> {
            etNombre.setFocusableInTouchMode(true);
            etPrecio.setFocusableInTouchMode(true);
            etDescripcion.setFocusableInTouchMode(true);

            int colorAmarillo = android.graphics.Color.parseColor("#FBC02D");
            int colorNegro = android.graphics.Color.parseColor("#000000");

            // --- EL VIGILANTE DE ESTADO ALTERADO ---
            android.text.TextWatcher vigilante = new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(android.text.Editable s) {
                    if (!etNombre.getText().toString().equals(gastoSeleccionado.getProveedor())) {
                        etNombre.setTextColor(colorAmarillo);
                    } else { etNombre.setTextColor(colorNegro); }

                    if (!etPrecio.getText().toString().equals(String.valueOf(gastoSeleccionado.getMonto()))) {
                        etPrecio.setTextColor(colorAmarillo);
                    } else { etPrecio.setTextColor(colorNegro); }

                    if (!etDescripcion.getText().toString().equals(gastoSeleccionado.getDescripcion())) {
                        etDescripcion.setTextColor(colorAmarillo);
                    } else { etDescripcion.setTextColor(colorNegro); }
                }
            };

            etNombre.addTextChangedListener(vigilante);
            etPrecio.addTextChangedListener(vigilante);
            etDescripcion.addTextChangedListener(vigilante);

            btnEditar.setVisibility(View.GONE);
            btnGuardar.setVisibility(View.VISIBLE);
            btnCerrar.setText("Cancelar");
        });

        // --- EL NUEVO MOTOR DE GUARDADO ---
        // --- EL NUEVO MOTOR DE GUARDADO (VERSIÓN SIN ID) ---
        btnGuardar.setOnClickListener(v -> {
            try {
                // 1. Guardamos las "huellas digitales" antiguas para encontrar el archivo
                String proveedorAntiguo = gastoSeleccionado.getProveedor();
                double montoAntiguo = gastoSeleccionado.getMonto();

                // 2. Actualizamos los datos en el objeto con lo que tú escribiste
                gastoSeleccionado.setProveedor(etNombre.getText().toString());
                gastoSeleccionado.setMonto(Double.parseDouble(etPrecio.getText().toString()));
                gastoSeleccionado.setDescripcion(etDescripcion.getText().toString());

                // 3. Enviamos los datos nuevos y las huellas antiguas a la Base de Datos
                DatabaseHelper db = new DatabaseHelper(CalculadoraActivity.this);
                db.actualizarGastoSinId(gastoSeleccionado, proveedorAntiguo, montoAntiguo);

                Toast.makeText(CalculadoraActivity.this, "Cambios guardados", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                // Recargamos la pantalla
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);

            } catch (Exception e) {
                Toast.makeText(CalculadoraActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();

        // Parche de estiramiento visual
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}