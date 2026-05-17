package com.example.lgw;

import com.opencsv.CSVReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LectorArchivos {

    public List<Gasto> procesarArchivo(InputStream is) {
        List<Gasto> listaImportada = new ArrayList<>();
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(is));
            String[] fila;

            while ((fila = reader.readNext()) != null) {
                // Filtro de seguridad: La fila debe tener 5 columnas y empezar con una fecha válida (contiene "/")
                if (fila.length >= 5 && fila[0].contains("/")) {

                    // Si es la fila de los títulos de las columnas, la saltamos
                    if (fila[0].equalsIgnoreCase("Fecha")) continue;

                    try {
                        String fecha = fila[0].trim();
                        String tipoStr = fila[1].trim();
                        String proveedor = fila[2].trim();
                        double monto = Double.parseDouble(fila[3].trim());
                        String descripcion = fila[4].trim();

                        // Detectamos si la palabra dice Ingreso o Gasto
                        int esVenta = tipoStr.contains("Ingreso") ? 1 : 0;

                        // Metemos el dato rescatado a la lista
                        listaImportada.add(new Gasto(fecha, proveedor, monto, descripcion, esVenta));

                    } catch (Exception e) {
                        // Si una fila está rota, la ignora y sigue con la siguiente sin crashear la app
                        continue;
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaImportada;
    }
}