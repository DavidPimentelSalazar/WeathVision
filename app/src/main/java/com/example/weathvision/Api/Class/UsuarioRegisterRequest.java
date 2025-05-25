package com.example.weathvision.Api.Class;

import com.google.gson.annotations.SerializedName;

public class UsuarioRegisterRequest {
    @SerializedName("nombre_usuario")
    private String nombreUsuario;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("correo")
    private String correo;

    @SerializedName("contraseña")
    private String contraseña;

    @SerializedName("categoria_trabajo")
    private String categoriaTrabajo;

    @SerializedName("creado_en")
    private String creadoEn;

    public UsuarioRegisterRequest(String nombreUsuario, String nombre, String correo, String contraseña, String categoriaTrabajo, String creadoEn) {
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.contraseña = contraseña;
        this.categoriaTrabajo = categoriaTrabajo;
        this.creadoEn = creadoEn;
    }
}