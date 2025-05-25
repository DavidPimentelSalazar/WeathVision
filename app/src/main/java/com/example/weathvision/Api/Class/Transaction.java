
package com.example.weathvision.Api.Class;


import com.example.weathvision.R;
import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("id_transaccion")
    private int idTransaccion;

    @SerializedName("id_usuario")
    private int idUsuario;

    private String tipo;
    private double monto;
    private String categoria;
    private String descripcion;
    private String fecha;
    private int color; // ID del recurso de color

    // Constructor para la API
    public Transaction(int idTransaccion, int idUsuario, String tipo, double monto, String categoria, String descripcion, String fecha) {
        this.idTransaccion = idTransaccion;
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.monto = monto;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.fecha = fecha;
        // Asignar color basado en el tipo
        this.color = tipo.equals("gasto") ? R.color.red : R.color.green;
    }

    // Constructor para agregar transacciones localmente
    public Transaction(String descripcion, String monto, int color) {
        this.descripcion = descripcion;
        this.monto = Double.parseDouble(monto.replace("€", ""));
        this.color = color;
    }

    public Transaction() {

    }

    // Getters
    public int getIdTransaccion() {
        return idTransaccion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getTipo() {
        return tipo;
    }

    public double getMonto() {
        return monto;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public int getColor() {
        return color;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setIdTransaccion(int idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}