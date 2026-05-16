package com.example.lgw;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Constantes de configuración de la Bóveda
    private static final String DATABASE_NAME = "TiendaMamaDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_GASTOS = "gastos";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Se ejecuta solo la primera vez que se instala la app para crear la tabla
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_GASTOS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "fecha TEXT,"
                + "proveedor TEXT,"
                + "monto REAL,"
                + "descripcion TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    // Se ejecuta si en el futuro cambias la versión de la base de datos
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GASTOS);
        onCreate(db);
    }

    // Herramienta para guardar un objeto Gasto en la tabla
    public void insertarGasto(Gasto gasto) {
        SQLiteDatabase db = this.getWritableDatabase(); // Abrimos la bóveda en modo escritura
        ContentValues values = new ContentValues(); // Empaquetamos los datos

        values.put("fecha", gasto.getFecha());
        values.put("proveedor", gasto.getProveedor());
        values.put("monto", gasto.getMonto());
        values.put("descripcion", gasto.getDescripcion());

        // Insertamos la fila y cerramos
        db.insert(TABLE_GASTOS, null, values);
        db.close();
    }
    // Herramienta para sacar todos los gastos de la bóveda
    public java.util.List<Gasto> obtenerTodosLosGastos() {
        java.util.List<Gasto> listaGastos = new java.util.ArrayList<>();

        // Consulta en lenguaje SQL puro: "Tráeme todo de la tabla gastos"
        String selectQuery = "SELECT * FROM " + TABLE_GASTOS;

        SQLiteDatabase db = this.getReadableDatabase(); // Abrimos en modo lectura
        android.database.Cursor cursor = db.rawQuery(selectQuery, null);

        // Si el cursor encuentra al menos una fila, empezamos a recorrer
        if (cursor.moveToFirst()) {
            do {
                // El índice 0 es el ID autoincremental, el 1 es fecha, 2 proveedor, etc.
                Gasto gasto = new Gasto(
                        cursor.getString(1), // fecha
                        cursor.getString(2), // proveedor
                        cursor.getDouble(3), // monto
                        cursor.getString(4)  // descripcion
                );
                listaGastos.add(gasto);
            } while (cursor.moveToNext());
        }

        // Limpieza de memoria
        cursor.close();
        db.close();

        return listaGastos;
    }
}
