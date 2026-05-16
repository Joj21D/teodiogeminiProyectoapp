package com.example.lgw;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CalculadoraActivity extends AppCompatActivity {

    private ImageButton btnVolver;
    private View btnExportar, btnImportar;
    private TextView tvModoFecha, tvMontoTotal;
    private RecyclerView rvListaCalculadora;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnAgregar;

    private GastoAdapter adapter;
    private List<Gasto> listaGastosReales;
    private String fechaRealGuardada = "Sin registros";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculadora);

        btnVolver = findViewById(R.id.btn_volver);
        tvModoFecha = findViewById(R.id.tv_modo_fecha);
        tvMontoTotal = findViewById(R.id.tv_monto_total);
        rvListaCalculadora = findViewById(R.id.rv_lista_calculadora);
        btnAgregar = findViewById(R.id.btn_agregar_gasto);
        btnExportar = findViewById(R.id.btn_exportar);
        btnImportar = findViewById(R.id.btn_importar);

        DatabaseHelper db = new DatabaseHelper(CalculadoraActivity.this);
        listaGastosReales = db.obtenerTodosLosGastos();

        // --- MATEMÁTICA ESTABILIZADA: COMPRAS VS VENTAS ---
        double totalCompras = 0.0;
        double totalVentas = 0.0;
        if (!listaGastosReales.isEmpty()) {
            fechaRealGuardada = listaGastosReales.get(listaGastosReales.size() - 1).getFecha();
            for (Gasto g : listaGastosReales) {
                if (g.getEsVenta() == 1) {
                    totalVentas += g.getMonto();
                } else {
                    totalCompras += g.getMonto();
                }
            }
        }

        double totalNeto = totalVentas - totalCompras;

        tvModoFecha.setText("Registros: " + fechaRealGuardada);
        java.text.DecimalFormat formateador = new java.text.DecimalFormat("#,###");

        // Renderizado único de balance en el componente de texto
        tvMontoTotal.setText("Compras: $" + formateador.format(totalCompras) +
                " | Ventas: $" + formateador.format(totalVentas) +
                "\nTotal Neto: $" + formateador.format(totalNeto));

        rvListaCalculadora.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GastoAdapter(listaGastosReales);
        rvListaCalculadora.setAdapter(adapter);

        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar());
        btnVolver.setOnClickListener(v -> finish());

        // Módulos I/O de Comunicación Externa
        if (btnExportar != null) {
            btnExportar.setOnClickListener(v -> {
                try {
                    ExportadorArchivos exportador = new ExportadorArchivos();
                    exportador.exportarCsv(CalculadoraActivity.this, listaGastosReales);
                    Toast.makeText(CalculadoraActivity.this, "¡Archivo exportado con éxito!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(CalculadoraActivity.this, "Error al exportar", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnImportar != null) {
            btnImportar.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, 200);
            });
        }
    }

    private void mostrarDialogoAgregar() {
        View viewDialogo = getLayoutInflater().inflate(R.layout.dialog_agregar_gasto, null);
        EditText etProveedor = viewDialogo.findViewById(R.id.et_nuevo_proveedor);
        EditText etMonto = viewDialogo.findViewById(R.id.et_nuevo_monto);
        EditText etCantidad = viewDialogo.findViewById(R.id.et_nueva_cantidad);
        EditText etDescripcion = viewDialogo.findViewById(R.id.et_nueva_descripcion);

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

                    RadioGroup rgTipoTransaccion = viewDialogo.findViewById(R.id.rg_tipo_transaccion);
                    int esVenta = 0;
                    if (rgTipoTransaccion.getCheckedRadioButtonId() == R.id.rb_venta) {
                        esVenta = 1;
                    }

                    double montoUnitario = Double.parseDouble(montoStr);
                    String cantStr = etCantidad.getText().toString().trim();
                    int cantidad = cantStr.isEmpty() ? 1 : Integer.parseInt(cantStr);
                    double montoTotalCalculado = montoUnitario * cantidad;

                    String descFinal = descStr;
                    if (cantidad > 1) {
                        String precioIndStr = (montoUnitario % 1 == 0) ? String.valueOf((int)montoUnitario) : String.valueOf(montoUnitario);
                        descFinal = descStr + " (x" + cantidad + " a $" + precioIndStr + " c/u)";
                    }

                    String fechaHoy = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());

                    Gasto nuevoGasto = new Gasto(fechaHoy, proveedorStr, montoTotalCalculado, descFinal, esVenta);

                    DatabaseHelper dbBaza = new DatabaseHelper(CalculadoraActivity.this);
                    dbBaza.insertarGasto(nuevoGasto);

                    finish();
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

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
        java.text.DecimalFormat formateador = new java.text.DecimalFormat("#,###");
        etPrecio.setText(formateador.format(gastoSeleccionado.getMonto()));
        etDescripcion.setText(gastoSeleccionado.getDescripcion());

        btnBorrar.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(CalculadoraActivity.this)
                    .setTitle("¿Borrar Gasto?")
                    .setMessage("¿Estás seguro de que quieres eliminar esto de la lista?")
                    .setPositiveButton("Sí, borrar", (dialogInterno, which) -> {
                        DatabaseHelper database = new DatabaseHelper(CalculadoraActivity.this);
                        database.borrarGastoSinId(gastoSeleccionado);
                        Toast.makeText(CalculadoraActivity.this, "Gasto eliminado", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        finish();
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        btnCerrar.setOnClickListener(v -> {
            if (btnCerrar.getText().toString().equals("Cancelar")) {
                etNombre.setFocusable(false);
                etPrecio.setFocusable(false);
                etDescripcion.setFocusable(false);
                etNombre.setTextColor(android.graphics.Color.BLACK);
                etPrecio.setTextColor(android.graphics.Color.BLACK);
                etDescripcion.setTextColor(android.graphics.Color.BLACK);
                etNombre.setText(gastoSeleccionado.getProveedor());
                etPrecio.setText(formateador.format(gastoSeleccionado.getMonto()));
                etDescripcion.setText(gastoSeleccionado.getDescripcion());
                btnEditar.setVisibility(View.VISIBLE);
                btnGuardar.setVisibility(View.GONE);
                btnCerrar.setText("Cerrar");
            } else {
                dialog.dismiss();
            }
        });

        btnEditar.setOnClickListener(v -> {
            etNombre.setFocusableInTouchMode(true);
            etPrecio.setFocusableInTouchMode(true);
            etDescripcion.setFocusableInTouchMode(true);

            int colorAmarillo = android.graphics.Color.parseColor("#FBC02D");
            int colorNegro = android.graphics.Color.parseColor("#000000");

            android.text.TextWatcher vigilante = new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(android.text.Editable s) {
                    if (!etNombre.getText().toString().equals(gastoSeleccionado.getProveedor())) {
                        etNombre.setTextColor(colorAmarillo);
                    } else { etNombre.setTextColor(colorNegro); }

                    String precioLimpio = etPrecio.getText().toString().replaceAll("[^0-9]", "");
                    String precioOriginalLimpio = String.valueOf((int)gastoSeleccionado.getMonto());
                    if (!precioLimpio.equals(precioOriginalLimpio)) {
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

        btnGuardar.setOnClickListener(v -> {
            try {
                String proveedorAntiguo = gastoSeleccionado.getProveedor();
                double montoAntiguo = gastoSeleccionado.getMonto();

                gastoSeleccionado.setProveedor(etNombre.getText().toString());
                String precioNumerico = etPrecio.getText().toString().replaceAll("[^0-9.]", "");
                gastoSeleccionado.setMonto(Double.parseDouble(precioNumerico));
                gastoSeleccionado.setDescripcion(etDescripcion.getText().toString());

                DatabaseHelper database = new DatabaseHelper(CalculadoraActivity.this);
                database.actualizarGastoSinId(gastoSeleccionado, proveedorAntiguo, montoAntiguo);

                Toast.makeText(CalculadoraActivity.this, "Cambios guardados", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            } catch (Exception e) {
                Toast.makeText(CalculadoraActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            try {
                java.io.InputStream inputStream = getContentResolver().openInputStream(data.getData());
                LectorArchivos lector = new LectorArchivos();
                java.util.List<Gasto> gastosImportados = lector.procesarArchivo(inputStream);
                DatabaseHelper db = new DatabaseHelper(this);
                int agregados = 0;
                for (Gasto nuevoGasto : gastosImportados) {
                    db.insertarGasto(nuevoGasto);
                    agregados++;
                }
                Toast.makeText(this, "¡" + agregados + " gastos importados con éxito!", Toast.LENGTH_LONG).show();
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            } catch (Exception e) {
                Toast.makeText(this, "Error al procesar el archivo", Toast.LENGTH_SHORT).show();
            }
        }
    }
}