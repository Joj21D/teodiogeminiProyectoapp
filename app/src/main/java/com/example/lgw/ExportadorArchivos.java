package com.example.lgw;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExportadorArchivos {

    public static void exportarExcel(Context context, List<Gasto> listaGastos) {
        // 1. Creamos el libro de Excel y la primera hoja
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Resumen Financiero");

        // 2. Configuramos el tamaño de las columnas (como pediste)
        sheet.setColumnWidth(1, 4000); // Columna B: Fecha
        sheet.setColumnWidth(2, 8000); // Columna C: Descripción (Más ancha)
        sheet.setColumnWidth(3, 4000); // Columna D: Venta/Gasto
        sheet.setColumnWidth(4, 5000); // Columna E: Proveedor
        sheet.setColumnWidth(5, 4000); // Columna F: Monto

        // 3. Estilos de Colores
        CellStyle estiloCabecera = workbook.createCellStyle();
        Font fuenteCabecera = workbook.createFont();
        fuenteCabecera.setBold(true);
        estiloCabecera.setFont(fuenteCabecera);
        estiloCabecera.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloCabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle estiloGasto = workbook.createCellStyle();
        estiloGasto.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        estiloGasto.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle estiloVenta = workbook.createCellStyle();
        estiloVenta.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        estiloVenta.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 4. Creamos la Cabecera en la Fila 5 (Índice 4 en Java)
        Row cabecera = sheet.createRow(4);
        String[] titulos = {"", "Fecha", "Descripcion", "Venta/Gasto", "Proveedor", "Monto"};
        for (int i = 1; i <= 5; i++) {
            Cell celda = cabecera.createCell(i);
            celda.setCellValue(titulos[i]);
            celda.setCellStyle(estiloCabecera);
        }

        // 5. Llenamos los datos desde SQLite
        int filaActual = 5;
        double ventaMayor = 0, gastoMayor = 0, neto = 0;

        for (Gasto gasto : listaGastos) {
            Row fila = sheet.createRow(filaActual++);

            fila.createCell(1).setCellValue(gasto.getFecha());
            fila.createCell(2).setCellValue(gasto.getDescripcion());

            String tipo = gasto.getEsVenta() == 1 ? "Venta" : "Gasto";
            Cell celdaTipo = fila.createCell(3);
            celdaTipo.setCellValue(tipo);
            celdaTipo.setCellStyle(gasto.getEsVenta() == 1 ? estiloVenta : estiloGasto);

            fila.createCell(4).setCellValue(gasto.getProveedor());
            fila.createCell(5).setCellValue(gasto.getMonto());

            // Cálculos para el panel lateral
            if (gasto.getEsVenta() == 1) {
                if (gasto.getMonto() > ventaMayor) ventaMayor = gasto.getMonto();
                neto += gasto.getMonto();
            } else {
                if (gasto.getMonto() > gastoMayor) gastoMayor = gasto.getMonto();
                neto -= gasto.getMonto();
            }
        }

        // 6. El Panel de Resumen Lateral (Combinando Celdas)
        // Combinamos celdas J5:K5 para el título del resumen
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 9, 10));
        Row filaResumenTitulo = sheet.getRow(4);
        if (filaResumenTitulo == null) filaResumenTitulo = sheet.createRow(4);
        Cell celdaResumen = filaResumenTitulo.createCell(9);
        celdaResumen.setCellValue("Resumen general");
        celdaResumen.setCellStyle(estiloCabecera);

        // Datos del Resumen
        sheet.getRow(5).createCell(9).setCellValue("Venta mayor:");
        sheet.getRow(5).createCell(10).setCellValue(ventaMayor);

        sheet.getRow(6).createCell(9).setCellValue("Gasto mayor:");
        sheet.getRow(6).createCell(10).setCellValue(gastoMayor);

        sheet.getRow(7).createCell(9).setCellValue("Acumulado neto:");
        sheet.getRow(7).createCell(10).setCellValue(neto);

        // 7. Guardar el archivo en el teléfono
        try {
            File directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File archivo = new File(directorio, "ReporteFinanciero.xlsx");
            FileOutputStream outputStream = new FileOutputStream(archivo);
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();
            Toast.makeText(context, "Excel profesional creado en Descargas", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al crear Excel", Toast.LENGTH_SHORT).show();
        }
    }
}