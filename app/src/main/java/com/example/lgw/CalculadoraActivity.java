package com.example.lgw;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalculadoraActivity extends AppCompatActivity {

    private ImageButton btnVolver;
    private View btnExportar, btnImportar;
    private TextView tvModoFecha, tvMontoTotal;
    private RecyclerView rvListaCalculadora;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnAgregar;

    private GastoAdapter adapter;
    private List<Gasto> listaGastosReales;
    private List<Gasto> listaFiltrada;

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

        DatabaseHelper db = new DatabaseHelper(this);
        listaGastosReales = db.obtenerTodosLosGastos();

        actualizarPantalla(listaGastosReales, "Todos los registros");

        tvModoFecha.setOnClickListener(v -> mostrarCalendarioNativo());
        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar());
        btnVolver.setOnClickListener(v -> finish());

        if (btnExportar != null) {
            btnExportar.setOnClickListener(v -> {
                try {
                    ExportadorArchivos exportador = new ExportadorArchivos();
                    exportador.exportarExcel(CalculadoraActivity.this, listaGastosReales);
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

    private void actualizarPantalla(List<Gasto> listaAmostrar, String textoCabecera) {
        double totalCompras = 0.0;
        double totalVentas = 0.0;

        for (Gasto g : listaAmostrar) {
            if (g.getEsVenta() == 1) {
                totalVentas += g.getMonto();
            } else {
                totalCompras += g.getMonto();
            }
        }

        double totalNeto = totalVentas - totalCompras;

        tvModoFecha.setText(textoCabecera + " 📅");

        java.text.DecimalFormat formateador = new java.text.DecimalFormat("#,###");

        tvMontoTotal.setText("Compras: $" + formateador.format(totalCompras) +
                " | Ventas: $" + formateador.format(totalVentas) +
                "\nTotal Neto: $" + formateador.format(totalNeto));

        rvListaCalculadora.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GastoAdapter(listaAmostrar);
        rvListaCalculadora.setAdapter(adapter);
    }

    private void mostrarCalendarioNativo() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String fechaElegida = String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);

                    listaFiltrada = new ArrayList<>();
                    for (Gasto g : listaGastosReales) {
                        if (g.getFecha().equals(fechaElegida)) {
                            listaFiltrada.add(g);
                        }
                    }

                    if (listaFiltrada.isEmpty()) {
                        Toast.makeText(CalculadoraActivity.this, "No hay registros para " + fechaElegida, Toast.LENGTH_SHORT).show();
                    }

                    actualizarPantalla(listaFiltrada, "Fecha: " + fechaElegida);

                }, year, month, day);

        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Ver Todos", (dialog, which) -> {
            actualizarPantalla(listaGastosReales, "Todos los registros");
        });

        datePickerDialog.show();
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

                    // Evitar crasheo por mal tipeo
                    double montoUnitario = 0;
                    try {
                        montoUnitario = Double.parseDouble(montoStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(CalculadoraActivity.this, "Monto inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String cantStr = etCantidad.getText().toString().trim();
                    int cantidad = cantStr.isEmpty() ? 1 : Integer.parseInt(cantStr);
                    double montoTotalCalculado = montoUnitario * cantidad;

                    String descFinal = descStr;
                    if (cantidad > 1) {
                        String precioIndStr = (montoUnitario % 1 == 0) ? String.valueOf((int)montoUnitario) : String.valueOf(montoUnitario);
                        descFinal = descStr + " (x" + cantidad + " a $" + precioIndStr + " c/u)";
                    }

                    // BUG FIX: Respeta la fecha del calendario si está filtrada
                    String fechaGuardado;
                    if (tvModoFecha.getText().toString().contains("Fecha:")) {
                        fechaGuardado = tvModoFecha.getText().toString().replace("Fecha: ", "").replace(" \uD83D\uDCC5", "").trim();
                    } else {
                        fechaGuardado = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
                    }

                    Gasto nuevoGasto = new Gasto(fechaGuardado, proveedorStr, montoTotalCalculado, descFinal, esVenta);

                    DatabaseHelper dbBaza = new DatabaseHelper(CalculadoraActivity.this);
                    dbBaza.insertarGasto(nuevoGasto);

                    finish();
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
//AQUI ESTAN TODOS LOS BOTONES EXTRAS
    public void mostrarDialogoDetalle(final Gasto gastoSeleccionado) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_detalle_gasto);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Muestra la fecha en el detalle
        TextView tvFechaDisplay = dialog.findViewById(R.id.tvDetalleFechaDisplay);
        tvFechaDisplay.setText("Registro del día: " + gastoSeleccionado.getFecha());

        EditText etNombre = dialog.findViewById(R.id.etDetalleNombre);
        EditText etPrecio = dialog.findViewById(R.id.etDetallePrecio);
        EditText etCantidad = dialog.findViewById(R.id.etDetalleCantidad);
        EditText etDescripcion = dialog.findViewById(R.id.etDetalleDescripcion);

        RadioGroup rgTipo = dialog.findViewById(R.id.rgDetalleTipoTransaccion);
        RadioButton rbCompra = dialog.findViewById(R.id.rbDetalleCompra);
        RadioButton rbVenta = dialog.findViewById(R.id.rbDetalleVenta);

        Button btnBorrar = dialog.findViewById(R.id.btnDetalleBorrar);
        Button btnEditar = dialog.findViewById(R.id.btnDetalleEditar);
        Button btnGuardar = dialog.findViewById(R.id.btnDetalleGuardar);
        Button btnCerrar = dialog.findViewById(R.id.btnDetalleCerrar);

        etNombre.setText(gastoSeleccionado.getProveedor());
        java.text.DecimalFormat formateador = new java.text.DecimalFormat("#,###");
        etPrecio.setText(formateador.format(gastoSeleccionado.getMonto()));
        etDescripcion.setText(gastoSeleccionado.getDescripcion());

        if (gastoSeleccionado.getEsVenta() == 1) {
            rbVenta.setChecked(true);
        } else {
            rbCompra.setChecked(true);
        }

        btnBorrar.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(CalculadoraActivity.this)
                    .setTitle("¿Borrar Registro?")
                    .setMessage("¿Estás seguro de que quieres eliminar esto de la lista?")
                    .setPositiveButton("Sí, borrar", (dialogInterno, which) -> {
                        DatabaseHelper database = new DatabaseHelper(CalculadoraActivity.this);

                        // NUEVO: Llamamos al borrado absoluto por ID
                        database.borrarGastoPorId(gastoSeleccionado.getId());

                        Toast.makeText(CalculadoraActivity.this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        finish();
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        btnEditar.setOnClickListener(v -> {
            etNombre.setFocusableInTouchMode(true);
            etPrecio.setFocusableInTouchMode(true);
            etCantidad.setFocusableInTouchMode(true);
            etDescripcion.setFocusableInTouchMode(true);

            // BUG FIX: Habilita interactividad real para que se pueda cambiar Gasto/Ingreso
            for (int i = 0; i < rgTipo.getChildCount(); i++) {
                rgTipo.getChildAt(i).setClickable(true);
                rgTipo.getChildAt(i).setFocusableInTouchMode(true);
            }

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

                // BUG FIX: Lee correctamente si tu mamá cambió el botón
                int esVentaNuevo = rbVenta.isChecked() ? 1 : 0;
                gastoSeleccionado.setEsVenta(esVentaNuevo);

                String precioNumerico = etPrecio.getText().toString().replaceAll("[^0-9.]", "");
                double montoUnitario = Double.parseDouble(precioNumerico);

                String cantStr = etCantidad.getText().toString().trim();
                int cantidad = cantStr.isEmpty() ? 1 : Integer.parseInt(cantStr);

                double montoTotalCalculado = montoUnitario * cantidad;
                gastoSeleccionado.setMonto(montoTotalCalculado);

                String descOriginal = etDescripcion.getText().toString();
                if (!cantStr.isEmpty() && cantidad > 1) {
                    String precioIndStr = (montoUnitario % 1 == 0) ? String.valueOf((int)montoUnitario) : String.valueOf(montoUnitario);
                    descOriginal = descOriginal + " (x" + cantidad + " a $" + precioIndStr + " c/u)";
                }
                gastoSeleccionado.setDescripcion(descOriginal);

                DatabaseHelper database = new DatabaseHelper(CalculadoraActivity.this);
                // Borra la línea de actualizarGastoSinId y usa esta:
                database.actualizarGastoPorId(gastoSeleccionado);

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
                // Justo antes del Toast de éxito
                if (inputStream != null) {
                    inputStream.close();
                }
                Toast.makeText(this, "¡" + agregados + " registros importados!", Toast.LENGTH_LONG).show();
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            } catch (Exception e) {
                Toast.makeText(this, "Error al procesar el archivo", Toast.LENGTH_SHORT).show();
            }
        }
    }
}