package com.example.lgw;

public class Gasto {
    private String fecha;
    private String proveedor;
    private double monto;
    private String descripcion;
    private int fueEditado;
    private int esVenta; // NUEVO: 0 = Compra (Gasto), 1 = Venta (Ingreso)

    // Constructor completo (usado por la Bóveda)
    public Gasto(String fecha, String proveedor, double monto, String descripcion, int fueEditado, int esVenta) {
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fueEditado = fueEditado;
        this.esVenta = esVenta;
    }

    // Constructor para crear uno nuevo desde la pantalla
    public Gasto(String fecha, String proveedor, double monto, String descripcion, int esVenta) {
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fueEditado = 0;
        this.esVenta = esVenta;
    }

    // Constructor antiguo (por si lo usa el Lector CSV para no romperlo, asume Compra por defecto)
    public Gasto(String fecha, String proveedor, double monto, String descripcion) {
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fueEditado = 0;
        this.esVenta = 0;
    }

    public String getFecha() { return fecha; }
    public String getProveedor() { return proveedor; }
    public double getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
    public int getFueEditado() { return fueEditado; }
    public int getEsVenta() { return esVenta; }

    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public void setMonto(double monto) { this.monto = monto; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFueEditado(int fueEditado) { this.fueEditado = fueEditado; }
    public void setEsVenta(int esVenta) { this.esVenta = esVenta; }
}