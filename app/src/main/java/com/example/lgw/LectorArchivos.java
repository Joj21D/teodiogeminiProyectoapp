package com.example.lgw;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LectorArchivos {

    public List<Gasto> procesarArchivo(InputStream inputStream) {
        List<Gasto> listaGastos = new ArrayList<>();

        try {
            // REGLA DE NEGOCIO 0: El Estándar Industrial toma el control.
            // CSVReader sabe perfectamente cómo lidiar con comas engañosas y comillas.
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
            String[] partes;

            // readNext() lee la línea y la separa inteligentemente de forma automática
            while ((partes = reader.readNext()) != null) {

                // REGLA DE NEGOCIO 1: Si la línea está vacía, saltar
                if (partes.length == 0 || (partes.length == 1 && partes[0].trim().isEmpty())) continue;

                // REGLA DE NEGOCIO 2: Validación de estructura mínima (4 columnas)
                if (partes.length >= 4) {
                    String fecha = partes[0].trim();
                    String proveedor = partes[1].trim();
                    String descripcion = partes[3].trim();

                    try {
                        // REGLA DE NEGOCIO 3: Transformación matemática estricta
                        double monto = Double.parseDouble(partes[2].trim());

                        // REGLA DE NEGOCIO 4: Estandarización de valores absolutos
                        if (monto < 0) {
                            monto = monto * -1;
                        }

                        Gasto nuevoGasto = new Gasto(fecha, proveedor, monto, descripcion);
                        listaGastos.add(nuevoGasto);

                    } catch (NumberFormatException e) {
                        // Excepción controlada: Se ignora la fila con datos corruptos
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaGastos;
    }

}
