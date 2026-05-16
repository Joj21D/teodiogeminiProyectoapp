package com.example.lgw;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TiendaMamaDB";
    private static final int DATABASE_VERSION = 3; // ¡NUEVA VERSIÓN!
    private static final String TABLE_GASTOS = "gastos";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_GASTOS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "fecha TEXT,"
                + "proveedor TEXT,"
                + "monto REAL,"
                + "descripcion TEXT,"
                + "fue_editado INTEGER DEFAULT 0,"
                + "es_venta INTEGER DEFAULT 0" + ")"; // <--- Nueva columna para saber si es venta
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GASTOS);
        onCreate(db);
    }

    public void insertarGasto(Gasto gasto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("fecha", gasto.getFecha());
        values.put("proveedor", gasto.getProveedor());
        values.put("monto", gasto.getMonto());
        values.put("descripcion", gasto.getDescripcion());
        values.put("fue_editado", gasto.getFueEditado());
        values.put("es_venta", gasto.getEsVenta()); // Guardamos si es venta o compra

        db.insert(TABLE_GASTOS, null, values);
        db.close();
    }

    public java.util.List<Gasto> obtenerTodosLosGastos() {
        java.util.List<Gasto> listaGastos = new java.util.ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GASTOS;

        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Gasto gasto = new Gasto(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getString(4),
                        cursor.getInt(5),    // fue_editado
                        cursor.getInt(6)     // es_venta
                );
                listaGastos.add(gasto);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listaGastos;
    }

    public void actualizarGastoSinId(Gasto gastoNuevo, String proveedorAntiguo, double montoAntiguo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();

        valores.put("proveedor", gastoNuevo.getProveedor());
        valores.put("monto", gastoNuevo.getMonto());
        valores.put("descripcion", gastoNuevo.getDescripcion());
        valores.put("fue_editado", 1);
        // No actualizamos es_venta porque si era venta, sigue siendo venta

        try {
            db.update(TABLE_GASTOS, valores, "proveedor = ? AND monto = ? AND fecha = ?",
                    new String[]{proveedorAntiguo, String.valueOf(montoAntiguo), gastoNuevo.getFecha()});
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
    }

    public void borrarGastoSinId(Gasto gastoTarget) {
        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete("gastos", "proveedor = ? AND monto = ? AND fecha = ?",
                    new String[]{gastoTarget.getProveedor(), String.valueOf(gastoTarget.getMonto()), gastoTarget.getFecha()});
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
    }
}