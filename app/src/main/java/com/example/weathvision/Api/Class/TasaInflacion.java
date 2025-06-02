package com.example.weathvision.Api.Class;

import com.google.gson.annotations.SerializedName;

public class TasaInflacion {
    @SerializedName("año")

    private int ano;
    @SerializedName("tasa_inflacion")
    private float tasaInflacion;
    @SerializedName("tipo_moneda")

    private String tipoMoneda;

    // Constructor vacío requerido por Retrofit
    public TasaInflacion() {}

    // Constructor con parámetros (opcional, pero útil)
    public TasaInflacion(int ano, float tasaInflacion, String tipoMoneda) {
        this.ano = ano;
        this.tasaInflacion = tasaInflacion;
        this.tipoMoneda = tipoMoneda;
    }

    // Getters y setters
    public int ano() {
        return ano;
    }

    public void ano(int ano) {
        this.ano = ano;
    }

    public float getTasaInflacion() {
        return tasaInflacion;
    }

    public void setTasaInflacion(float tasaInflacion) {
        this.tasaInflacion = tasaInflacion;
    }

    public String getTipoMoneda() {
        return tipoMoneda;
    }

    public void setTipoMoneda(String tipoMoneda) {
        this.tipoMoneda = tipoMoneda;
    }
}