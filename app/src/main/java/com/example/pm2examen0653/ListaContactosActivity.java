package com.example.pm2examen0653;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pm2examen0653.Configuracion.DBHelper;

import java.util.ArrayList;

public class ListaContactosActivity extends AppCompatActivity {
    ListView listViewContactos;
    ArrayList<Contacto> listaContactos = new ArrayList<>();
    ArrayAdapter<Contacto> adapter;
    String numeroPendienteLlamar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);

        listViewContactos = findViewById(R.id.listViewContactos);

        cargarContactos();

        listViewContactos.setOnItemClickListener((parent, view, position, id) -> {
            Contacto contacto = listaContactos.get(position);
            mostrarOpciones(contacto);
        });
    }

    private void cargarContactos() {
        listaContactos.clear();
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM contactos", null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String nombre = cursor.getString(1);
            String numero = cursor.getString(2);
            String pais = cursor.getString(3);
            String nota = cursor.getString(4);
            String foto = cursor.getString(5);

            listaContactos.add(new Contacto(id, nombre, numero, pais, nota, foto));
        }

        cursor.close();
        db.close();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaContactos);
        listViewContactos.setAdapter(adapter);
    }
     //Activar las opciones al momento de seleccionar contacto con el AlertDialog.Builder
    private void mostrarOpciones(Contacto contacto) {
        String[] opciones = {"Ver Imagen", "Llamar", "Compartir", "Actualizar", "Eliminar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones para " + contacto.nombre);
        builder.setItems(opciones, (dialog, which) -> {
            switch (which) {
                case 0:
                    mostrarImagen(contacto);
                    break;
                case 1:
                    llamarContacto(contacto.numero);
                    break;
                case 2:
                    compartirContacto(contacto);
                    break;
                case 3:
                    actualizarContacto(contacto);
                    break;
                case 4:
                    eliminarContacto(contacto);
                    break;
            }
        });
        builder.show();
    }

    //Metodo para mostrar imagen
    private void mostrarImagen(Contacto contacto) {
        if (contacto.foto != null && !contacto.foto.isEmpty()) {
            byte[] bytes = Base64.decode(contacto.foto, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    600 // puedes ajustar este valor
            ));

            new AlertDialog.Builder(this)
                    .setTitle("Foto de " + contacto.nombre)
                    .setView(imageView)
                    .setPositiveButton("Cerrar", null)
                    .show();
        } else {
            Toast.makeText(this, "Este contacto no tiene foto", Toast.LENGTH_SHORT).show();
        }
    }
      //Metodo de realizar llamada.
    private void llamarContacto(String numero) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            numeroPendienteLlamar = numero;

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, 123);

            Toast.makeText(this, "Se necesita permiso para llamar", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + numero));
            startActivity(intent);
        }
    }
    //Solicitud de permiso para realizar llamada.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el usuario concedió el permiso, intenta llamar si hay número pendiente
                if (numeroPendienteLlamar != null) {
                    llamarContacto(numeroPendienteLlamar);
                    numeroPendienteLlamar = null; // Limpiar
                }
            } else {
                Toast.makeText(this, "Permiso denegado para realizar llamadas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Metodo Compartir contactos
    private void compartirContacto(Contacto contacto) {
        String texto = "Nombre: " + contacto.nombre + "\nTel: " + contacto.numero +
                "\nPaís: " + contacto.pais + "\nNota: " + contacto.nota;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, texto);
        startActivity(Intent.createChooser(intent, "Compartir Contacto"));
    }

    private void actualizarContacto(Contacto contacto) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("id", contacto.id);
        startActivity(intent);
    }

    private void eliminarContacto(Contacto contacto) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar contacto")
                .setMessage("¿Estás seguro de eliminar a " + contacto.nombre + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    DBHelper dbHelper = new DBHelper(this);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete("contactos", "id=?", new String[]{String.valueOf(contacto.id)});
                    db.close();
                    cargarContactos();
                    Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}