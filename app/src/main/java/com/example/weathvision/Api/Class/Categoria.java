package com.example.weathvision.Api.Class;

import com.google.gson.annotations.SerializedName;

public class Categoria {
    @SerializedName("id_categoria")
    private int id_categoria;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("imagen")
    private String imagen;



    public Categoria(int id_categoria, String imagen, String nombre) {
        this.id_categoria = id_categoria;
        this.imagen = imagen;
        this.nombre = nombre;
    }

    public int getId_categoria() {
        return id_categoria;
    }

    public void setId_categoria(int id_categoria) {
        this.id_categoria = id_categoria;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
