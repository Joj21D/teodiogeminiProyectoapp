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

    // Esta herramienta toma la lista de la bóveda y la transforma en un archivo real
    public String exportarCsv(CalculadoraActivity calculadoraActivity, List<Gasto> listaGastos) {
        try {
            OutputStream os;
            String rutaFinal = "Descargas";

            // Le agregamos la hora exacta al nombre para que si exportas dos veces, no choquen ni se borren
            String nombreArchivo = "gastos_tienda_" + System.currentTimeMillis() + ".csv";

            // Android 10 o superior: Usamos MediaStore (El VIP Pass para que sea visible)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues resolver = new ContentValues();
                resolver.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
                resolver.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                resolver.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                // Insertamos el archivo vacío en las descargas y obtenemos la llave (URI)
                Uri uri = calculadoraActivity.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, resolver);
                if (uri != null) {
                    os = calculadoraActivity.getContentResolver().openOutputStream(uri);
                } else {
                    return null; // Falló la creación
                }
            } else {
                // Teléfonos antiguos (Método clásico)
                java.io.File carpetaDescargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                java.io.File archivoFinal = new java.io.File(carpetaDescargas, nombreArchivo);
                os = new java.io.FileOutputStream(archivoFinal);
                rutaFinal = archivoFinal.getAbsolutePath();
            }

            // 3. Encendemos tu máquina de escritura industrial, conectada al nuevo flujo
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(os));

            // 4. (Opcional pero elegante) Escribimos los títulos
            String[] cabeceras = {"Fecha", "Proveedor", "Monto", "Descripcion"};
            writer.writeNext(cabeceras);

            // 5. Recorremos los gastos
            for (Gasto g : listaGastos) {
                String[] fila = {
                        g.getFecha(),
                        g.getProveedor(),
                        String.valueOf(g.getMonto()),
                        g.getDescripcion()
                };
                writer.writeNext(fila);
            }

            // Limpieza de memoria
            writer.close();
            os.close();

            return rutaFinal;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}