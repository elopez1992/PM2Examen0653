package com.example.pm2examen0653;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    Button btnCapturar, btnGuardar, btncontactosalvado;
    Spinner spinnerPais;
    EditText txtNombre, txtNumero, txtNota;
    String imagenBase64 = "";
    int idEditar = -1; // ← variable global para saber si estamos editando

    String[] paises = {"Honduras +504", "El Salvador +503", "Guatemala +502", "Costa Rica +506", "Nicaragua +505"};
    final int REQUEST_CAMARA = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgFoto = findViewById(R.id.imgFoto);
        btnCapturar = findViewById(R.id.btnCapturarFoto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btncontactosalvado = findViewById(R.id.btncontactossalvados);
        spinnerPais = findViewById(R.id.spinnerPais);
        txtNombre = findViewById(R.id.txtNombre);
        txtNumero = findViewById(R.id.txtNumero);
        txtNota = findViewById(R.id.txtNota);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPais.setAdapter(adapter);

        // ← Detectar si se recibió un ID para editar
        idEditar = getIntent().getIntExtra("id", -1);
        if (idEditar != -1) {
            cargarDatosParaEditar(idEditar);
            btnGuardar.setText("Actualizar Contacto");
        }

        btnCapturar.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMARA);
            } else {
                abrirCamara();
            }
        });

        // ← Guardar o actualizar dependiendo del estado
        btnGuardar.setOnClickListener(v -> {
            if (idEditar == -1) {
                guardarContacto();
            } else {
                actualizarContactoExistente(idEditar);
            }
        });

        btncontactosalvado.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ListaContactosActivity.class);
            startActivity(intent);
        });
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMARA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CAMARA && resultCode == RESULT_OK && data != null) {
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
        String nombre = txtNombre.getText().toString().trim();
        String numero = txtNumero.getText().toString().trim();
        String nota = txtNota.getText().toString().trim();
        String pais = spinnerPais.getSelectedItem().toString();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Debe escribir un nombre.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numero.isEmpty()) {
            Toast.makeText(this, "Debe escribir un teléfono.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nota.isEmpty()) {
            Toast.makeText(this, "Debe escribir una nota.", Toast.LENGTH_SHORT).show();
            return;
        }

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("numero", numero);
        values.put("pais", pais);
        values.put("nota", nota);
        values.put("foto", imagenBase64);

        long id = db.insert("contactos", null, values);
        db.close();

        if (id > 0) {
            Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();
            limpiarCampos();
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarContactoExistente(int id) {
        String nombre = txtNombre.getText().toString();
        String numero = txtNumero.getText().toString();
        String pais = spinnerPais.getSelectedItem().toString();
        String nota = txtNota.getText().toString();

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("numero", numero);
        values.put("pais", pais);
        values.put("nota", nota);
        values.put("foto", imagenBase64);

        int filas = db.update("contactos", values, "id=?", new String[]{String.valueOf(id)});
        db.close();

        if (filas > 0) {
            Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarDatosParaEditar(int id) {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM contactos WHERE id=?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            txtNombre.setText(cursor.getString(1));
            txtNumero.setText(cursor.getString(2));
            txtNota.setText(cursor.getString(4));
            String pais = cursor.getString(3);
            String foto = cursor.getString(5);

            imagenBase64 = foto;

            if (foto != null && !foto.isEmpty()) {
                byte[] bytes = Base64.decode(foto, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imgFoto.setImageBitmap(bitmap);
            }

            for (int i = 0; i < paises.length; i++) {
                if (paises[i].equals(pais)) {
                    spinnerPais.setSelection(i);
                    break;
                }
            }
        }

        cursor.close();
        db.close();
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtNumero.setText("");
        txtNota.setText("");
        spinnerPais.setSelection(0);
        imgFoto.setImageResource(R.mipmap.add_photo_png);
        imagenBase64 = "";
    }
}