package com.example.weathvision.Api.Class;


import com.google.gson.annotations.SerializedName;

public class UsuarioResponse {
    @SerializedName("id_usuario")
    private int idUsuario;

    @SerializedName("nombre_usuario")
    private String nombreUsuario;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("correo")
    private String correo;

    @SerializedName("contraseña_hash")
    private String contraseñaHash;

    @SerializedName("categoria_trabajo")
    private String categoriaTrabajo;

    @SerializedName("creado_en")
    private String creadoEn;

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getContraseñaHash() {
        return contraseñaHash;
    }

    public String getCategoriaTrabajo() {
        return categoriaTrabajo;
    }

    public String getCreadoEn() {
        return creadoEn;
    }
}