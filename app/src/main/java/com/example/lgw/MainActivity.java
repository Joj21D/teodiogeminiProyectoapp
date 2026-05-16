package com.example.lgw;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 1. Enlazamos los 3 botones del XML
        Button btnAbrirCalculadora = findViewById(R.id.btn_abrir_calculadora);
        Button btnExportar = findViewById(R.id.btn_exportar);
        Button btnImportar = findViewById(R.id.btn_importar);

        // 2. Lógica: ABRIR CALCULADORA
        btnAbrirCalculadora.setOnClickListener(v -> {
            // Viajamos desde MainActivity hacia CalculadoraActivity
            Intent intent = new Intent(MainActivity.this, CalculadoraActivity.class);
            startActivity(intent);
        });
        // 3. Lógica: EXPORTAR INFO
        btnExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instanciamos la base de datos y el exportador
                DatabaseHelper db = new DatabaseHelper(MainActivity.this);
                ExportadorArchivos exportador = new ExportadorArchivos();

                // Obtenemos la lista y la exportamos
                java.util.List<Gasto> listaGastos = db.obtenerTodosLosGastos();
                String rutaGuardada = exportador.exportarCsv(listaGastos);

                // Verificamos si funcionó para mostrar el mensaje
                if (rutaGuardada != null) {
                    Toast.makeText(MainActivity.this, "Datos guardados en: " + rutaGuardada, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error al exportar (Revisa permisos de almacenamiento)", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 4. Lógica: IMPORTAR INFO (Tu código original)
        btnImportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 1);
            }
        });
    }

    // --- MANTENEMOS TU BACKEND ORIGINAL INTACTO ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            leerArchivo(uri);
        }
    }

    private void leerArchivo(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String linea;
            StringBuilder textoLeido = new StringBuilder();
            int contador = 0;
            while ((linea = reader.readLine()) != null && contador < 5) {
                textoLeido.append(linea).append("\n");
                contador++;
            }
            reader.close();
            inputStream.close();
            Toast.makeText(this, "Contenido:\n" + textoLeido.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al intentar leer el archivo", Toast.LENGTH_SHORT).show();
        }
    }
}