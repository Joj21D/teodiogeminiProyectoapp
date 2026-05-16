package com.example.lgw;

public class Gasto {
    private String fecha;
    private String proveedor;
    private double monto;
    private String descripcion;
    private int fueEditado; // Súper Memoria: 0 = Normal, 1 = Editado (Amarillo)

    // Constructor completo (usado por la Base de Datos para leer)
    public Gasto(String fecha, String proveedor, double monto, String descripcion, int fueEditado) {
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fueEditado = fueEditado;
    }

    // Constructor original (usado cuando agregas un gasto por primera vez)
    public Gasto(String fecha, String proveedor, double monto, String descripcion) {
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fueEditado = 0; // Al nacer, nunca está editado
    }

    public String getFecha() { return fecha; }
    public String getProveedor() { return proveedor; }
    public double getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
    public int getFueEditado() { return fueEditado; }

    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public void setMonto(double monto) { this.monto = monto; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFueEditado(int fueEditado) { this.fueEditado = fueEditado; }
}