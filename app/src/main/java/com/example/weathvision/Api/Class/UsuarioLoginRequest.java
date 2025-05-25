package com.example.weathvision.Api.Class;

import com.google.gson.annotations.SerializedName;

public class UsuarioLoginRequest {
    @SerializedName("nombre_usuario")
    private String nombre_usuario;

    @SerializedName("contraseña")
    private String contraseña;

    public UsuarioLoginRequest(String nombre_usuario, String contraseña) {
        this.nombre_usuario = nombre_usuario;
        this.contraseña = contraseña;
    }

    // Getters y setters (opcional si usas Lombok)
    public String getNombre_usuario() {
        return nombre_usuario;
    }

    public void setNombre_usuario(String nombre_usuario) {
        this.nombre_usuario = nombre_usuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }
}