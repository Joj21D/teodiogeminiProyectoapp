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
        // Asumiendo que tu botón se llama btnResumen (cambia el nombre si usaste otro)
        Button btnResumen = findViewById(R.id.btn_resumen_main);
        btnInstrucciones = findViewById(R.id.btn_instrucciones_main);

        // Evento: Abrir Calculadora
        btnCalculadora.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalculadoraActivity.class);
            startActivity(intent);
        });

        btnResumen.setOnClickListener(v -> {
            // El "Intent" es el vehículo que viaja de una pantalla a otra
            android.content.Intent intent = new android.content.Intent(MainActivity.this, ResumenActivity.class);
            startActivity(intent);
        });

        // Evento: Mostrar Instrucciones amigables para tu madre
        // (Ajusta el nombre de la variable de tu botón si lo llamaste diferente)
        Button btnInstrucciones = findViewById(R.id.btn_instrucciones_main);
        btnInstrucciones.setOnClickListener(v -> mostrarInstrucciones());
    }
    private void mostrarInstrucciones() {
        android.view.View viewDialogo = getLayoutInflater().inflate(R.layout.dialog_instrucciones, null);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(viewDialogo)
                .setPositiveButton("Entendido", null)
                .show();
    }
}