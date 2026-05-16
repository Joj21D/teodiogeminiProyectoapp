package com.example.lgw; // Verifica que coincida exactamente con tu paquete

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
        btnAgregar = findViewById(R.id.btn_agregar_gasto); // Extraído al nivel principal

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

                    // --- LA TRANSFORMACIÓN MATEMÁTICA ESTÁ AQUÍ ---
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
    private void mostrarDialogoDetalle(final Gasto gastoSeleccionado) {
        // 1. Crear el cuadro de diálogo conectado a tu XML
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_detalle_gasto);

        // (Opcional) Hacer el fondo transparente por si quieres redondear los bordes del XML luego
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 2. Vincular los elementos del XML a Java
        EditText etNombre = dialog.findViewById(R.id.etDetalleNombre);
        EditText etPrecio = dialog.findViewById(R.id.etDetallePrecio);
        EditText etDescripcion = dialog.findViewById(R.id.etDetalleDescripcion);

        Button btnBorrar = dialog.findViewById(R.id.btnDetalleBorrar);
        Button btnEditar = dialog.findViewById(R.id.btnDetalleEditar);
        Button btnGuardar = dialog.findViewById(R.id.btnDetalleGuardar);
        Button btnCerrar = dialog.findViewById(R.id.btnDetalleCerrar);

        // 3. Cargar los datos del gasto que se clickeó (Estado de Lectura)
        etNombre.setText(gastoSeleccionado.getNombre());
        etPrecio.setText(String.valueOf(gastoSeleccionado.getPrecio()));
        etDescripcion.setText(gastoSeleccionado.getDescripcion());

        // 4. Lógica de los Botones

        // Botón Cerrar (Normal o cuando dice Cancelar)
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        // Botón Editar (Activa el Estado Amarillo)
        btnEditar.setOnClickListener(v -> {
            // Desbloquear los textos para que aparezca el teclado
            etNombre.setFocusableInTouchMode(true);
            etPrecio.setFocusableInTouchMode(true);
            etDescripcion.setFocusableInTouchMode(true);

            // Tu directiva brillante: Cambiar el color a amarillo oscuro (Mostaza) para indicar edición
            int colorAmarillo = android.graphics.Color.parseColor("#FBC02D");
            etNombre.setTextColor(colorAmarillo);
            etPrecio.setTextColor(colorAmarillo);
            etDescripcion.setTextColor(colorAmarillo);

            // Cambiar la interfaz de botones
            btnEditar.setVisibility(View.GONE);
            btnGuardar.setVisibility(View.VISIBLE);
            btnCerrar.setText("Cancelar");
        });

        // 5. Mostrar la tarjetita en pantalla
        dialog.show();
    }
}