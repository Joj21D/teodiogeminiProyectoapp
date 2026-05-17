package com.example.lgw;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import com.opencsv.CSVWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class ExportadorArchivos {

    public String exportarCsv(CalculadoraActivity calculadoraActivity, List<Gasto> listaGastos) {
        try {
            OutputStream os;
            String rutaFinal = "Descargas";
            String nombreArchivo = "Reporte_Tienda_" + System.currentTimeMillis() + ".csv";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues resolver = new ContentValues();
                resolver.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
                resolver.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                resolver.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = calculadoraActivity.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, resolver);
                if (uri != null) {
                    os = calculadoraActivity.getContentResolver().openOutputStream(uri);
                } else {
                    return null;
                }
            } else {
                java.io.File carpetaDescargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                java.io.File archivoFinal = new java.io.File(carpetaDescargas, nombreArchivo);
                os = new java.io.FileOutputStream(archivoFinal);
                rutaFinal = archivoFinal.getAbsolutePath();
            }

            // --- MAGIA ANTI-EXCEL ROTO ---
            // Le decimos a la máquina que NO use comillas nunca (NO_QUOTE_CHARACTER)
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(os),
                    ',',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            // --- BLOQUE 1: CABECERA SEGÚN TU FOTO (Sin tildes) ---
            String fechaActual = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
            String[] titulo = {"Reporte " + fechaActual, "", "", "", ""};
            String[] lineaVacia = {"", "", "", "", ""};

            writer.writeNext(titulo);
            writer.writeNext(lineaVacia);

            // --- BLOQUE 2: COLUMNAS SEGÚN TU FOTO (Sin tildes) ---
            String[] cabeceras = {"Fecha", "Tipo de (Gasto/Ingreso)", "Detalle/Proveedor", "Monto", "Descripcion"};
            writer.writeNext(cabeceras);

            double acumuladoCompras = 0;
            double acumuladoVentas = 0;

            // --- BLOQUE 3: REGISTROS ---
            for (Gasto g : listaGastos) {
                String tipoStr;
                if (g.getEsVenta() == 1) {
                    tipoStr = "Ingreso (Venta)";
                    acumuladoVentas += g.getMonto();
                } else {
                    tipoStr = "Gasto (Compra)";
                    acumuladoCompras += g.getMonto();
                }

                String montoFormateado = (g.getMonto() % 1 == 0) ? String.valueOf((int)g.getMonto()) : String.valueOf(g.getMonto());

                // Limpieza de texto de usuario por si ponen tildes o saltos de línea (protege el CSV)
                String descLimpia = g.getDescripcion().replace("\n", " ").replace(",", ".");
                String provLimpio = g.getProveedor().replace("\n", " ").replace(",", ".");

                String[] fila = {
                        g.getFecha(),
                        tipoStr,
                        provLimpio,
                        montoFormateado,
                        descLimpia
                };
                writer.writeNext(fila);
            }

            // --- BLOQUE 4: TOTALES SEGÚN TU FOTO (Sin tildes) ---
            double balanceNeto = acumuladoVentas - acumuladoCompras;

            writer.writeNext(lineaVacia);
            // Ponemos los resultados en la columna 3 y 4 para que queden bajo "Proveedor" y "Monto"
            writer.writeNext(new String[]{"", "", "Total Gasto (Compras):", String.valueOf((int)acumuladoCompras), ""});
            writer.writeNext(new String[]{"", "", "Total Ingreso (Ventas):", String.valueOf((int)acumuladoVentas), ""});
            writer.writeNext(new String[]{"", "", "Balance Total:", String.valueOf((int)balanceNeto), ""});

            writer.close();
            os.close();

            return rutaFinal;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}