package com.example.weathvision.Api.Class;

public class CorreoRequest {
    private String destinatario;
    private String asunto;
    private String mensaje;

    public CorreoRequest(String destinatario, String asunto, String mensaje) {
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.mensaje = mensaje;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getMensaje() {
        return mensaje;
    }
}