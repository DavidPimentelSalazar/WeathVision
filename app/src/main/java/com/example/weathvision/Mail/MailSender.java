package com.example.weathvision.Mail;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class MailSender {
    private String correoUsuario = "wealthvisionoficial@gmail.com"; // Tu correo de envío
    private String password = "enil jbgh wuag mphe"; // Contraseña o App Password (Gmail)

    public void enviarCorreo(String destinatario, String asunto, String mensaje) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(correoUsuario, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(correoUsuario));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);
            message.setContent(mensaje, "text/html; charset=utf-8"); // Changed to setContent for HTML
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // Optionally, log the error or notify the user
        }
    }
}