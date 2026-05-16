package com.example.lgw;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnCalculadora, btnResumen, btnInstrucciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vinculación con los nuevos IDs limpios
        btnCalculadora = findViewById(R.id.btn_abrir_calculadora);
        btnResumen = findViewById(R.id.btn_resumen_main);
        btnInstrucciones = findViewById(R.id.btn_instrucciones_main);

        // Evento: Abrir Calculadora
        btnCalculadora.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalculadoraActivity.class);
            startActivity(intent);
        });

        // Evento: Resumen General (Módulo en construcción)
        btnResumen.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Módulo de Resumen General en construcción", Toast.LENGTH_SHORT).show();
        });

        // Evento: Mostrar Instrucciones amigables para tu madre
        btnInstrucciones.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this,
                    "Manual: Presiona 'Tabla de datos' para registrar compras. Toca 'Detalle' en cualquier elemento para modificarlo o ver notas.",
                    Toast.LENGTH_LONG).show();
        });
    }
}