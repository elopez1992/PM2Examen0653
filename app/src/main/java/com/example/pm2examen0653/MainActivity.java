package com.example.pm2examen0653;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pm2examen0653.Configuracion.DBHelper;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    ImageView imgFoto;
    Button btnCapturar, btnGuardar;
    Spinner spinnerPais;
    EditText txtNombre, txtNumero, txtNota;
    String imagenBase64 = "";

    String[] paises = {"Honduras +504", "El Salvador +503", "Guatemala +502", "Costa Rica +506", "Nicaragua +505"};
    final int REQUEST_CAMARA = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgFoto = findViewById(R.id.imgFoto);
        btnCapturar = findViewById(R.id.btnCapturarFoto);
        btnGuardar = findViewById(R.id.btnGuardar);
        spinnerPais = findViewById(R.id.spinnerPais);
        txtNombre = findViewById(R.id.txtNombre);
        txtNumero = findViewById(R.id.txtNumero);
        txtNota = findViewById(R.id.txtNota);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPais.setAdapter(adapter);

        btnCapturar.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMARA);
            } else {
                abrirCamara();
            }
        });

        btnGuardar.setOnClickListener(v -> {
            guardarContacto();
        });
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMARA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CAMARA && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imgFoto.setImageBitmap(bitmap);
            imagenBase64 = convertirImagenBase64(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String convertirImagenBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void guardarContacto() {
        String nombre = txtNombre.getText().toString();
        String numero = txtNumero.getText().toString();
        String nota = txtNota.getText().toString();
        String pais = spinnerPais.getSelectedItem().toString();

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("numero", numero);
        values.put("pais", pais);
        values.put("nota", nota);
        values.put("foto", imagenBase64);

        long id = db.insert("contactos", null, values);
        if (id > 0) {
            Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();
            limpiarCampos();
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtNumero.setText("");
        txtNota.setText("");
        spinnerPais.setSelection(0);
        imgFoto.setImageResource(R.mipmap.ic_launcher);
        imagenBase64 = "";
    }
}