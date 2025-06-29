package com.example.pm2examen0653;

public class Contacto
{
    public int id;
    public String nombre;
    public String numero;
    public String pais;
    public String nota;
    public String foto;

    public Contacto(int id, String nombre, String numero, String pais, String nota, String foto) {
        this.id = id;
        this.nombre = nombre;
        this.numero = numero;
        this.pais = pais;
        this.nota = nota;
        this.foto = foto;
    }

    @Override
    public String toString() {
        return nombre + " - " + numero + " - "+ pais + " - " + nota;
    }
}
