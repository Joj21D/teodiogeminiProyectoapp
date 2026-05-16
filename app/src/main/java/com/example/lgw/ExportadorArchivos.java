package com.example.lgw;

import android.os.Environment;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class ExportadorArchivos {

    // Esta herramienta toma la lista de la bóveda y la transforma en un archivo real
    public String exportarCsv(List<Gasto> listaGastos) {
        try {
            // 1. Buscamos la carpeta pública de "Descargas" del teléfono
            File carpetaDescargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // 2. Nombramos el nuevo archivo
            File archivoFinal = new File(carpetaDescargas, "gastos_tienda_exportado.csv");

            // 3. Encendemos la máquina de escritura industrial
            CSVWriter writer = new CSVWriter(new FileWriter(archivoFinal));

            // 4. (Opcional pero elegante) Escribimos los títulos de las columnas arriba del todo
            String[] cabeceras = {"Fecha", "Proveedor", "Monto", "Descripcion"};
            writer.writeNext(cabeceras);

            // 5. Recorremos los gastos y los vamos escribiendo línea por línea
            for (Gasto g : listaGastos) {
                String[] fila = {
                        g.getFecha(),
                        g.getProveedor(),
                        String.valueOf(g.getMonto()),
                        g.getDescripcion()
                };
                writer.writeNext(fila);
            }

            writer.close();

            // Devolvemos la "dirección" física donde quedó el archivo para avisarle al usuario
            return archivoFinal.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Si algo falla, devolvemos un vacío
        }
    }
}
