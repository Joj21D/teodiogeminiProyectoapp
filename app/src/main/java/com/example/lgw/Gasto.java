package com.example.lgw;

public class Gasto {
    // Variables de clase (los atributos de cada gasto)
    private String fecha;
    private String proveedor;
    private double monto;
    private String descripcion;

    // "Constructor": La máquina que crea el objeto cuando le pasamos los datos
    public Gasto(String fecha, String proveedor, double monto, String descripcion) {
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    // "Getters": Herramientas para que otras partes de la app puedan leer los datos
    public String getFecha() { return fecha; }
    public String getProveedor() { return proveedor; }
    public double getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
}
