package com.example.weathvision.Api.Class;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Metas implements Serializable {



    @SerializedName("id_usuario")
    private int idUsuario;

    private String titulo;

    @SerializedName("monto_objetivo")
    private double montoObjetivo;

    @SerializedName("monto_actual")
    private double monto_actual;

    @SerializedName("fecha_limite")
    private String fechaLimite;


    public Metas (){

    }

    @Override
    public String toString() {
        return "Metas{" +
                "fechaLimite='" + fechaLimite + '\'' +

                ", idUsuario=" + idUsuario +
                ", titulo='" + titulo + '\'' +
                ", montoObjetivo=" + montoObjetivo +
                ", monto_actual=" + monto_actual +
                '}';
    }

    public Metas( int idUsuario, double monto_actual, double montoObjetivo, String titulo, String fechaLimite) {

        this.idUsuario = idUsuario;
        this.monto_actual = monto_actual;
        this.montoObjetivo = montoObjetivo;
        this.titulo = titulo;
        this.fechaLimite = fechaLimite;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(String fechaLimite) {
        this.fechaLimite = fechaLimite;
    }


    public double getMontoObjetivo() {
        return montoObjetivo;
    }

    public void setMontoObjetivo(double montoObjetivo) {
        this.montoObjetivo = montoObjetivo;
    }

    public double getMonto_actual() {
        return monto_actual;
    }

    public void setMonto_actual(double monto_actual) {
        this.monto_actual = monto_actual;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
