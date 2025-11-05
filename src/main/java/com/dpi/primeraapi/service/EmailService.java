package com.dpi.primeraapi.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class EmailService {
    
    @Value("${SENDGRID_API_KEY}")
    private String sendGridApiKey;
    
    @Value("${SENDGRID_FROM_EMAIL}")
    private String fromEmail;
    
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            Email from = new Email(fromEmail);
            Email to = new Email(toEmail);
            String subject = "Recuperación de Contraseña - Sistema de Turnos DPI";
            
            String resetUrl = "https://dpi-gesti-n-de-turnos.onrender.com/reset-password?token=" + resetToken;
            String contentText = "Hola,\n\nPara restablecer tu contraseña, haz clic en el siguiente enlace:\n" + resetUrl + 
                               "\n\nSi no solicitaste este cambio, ignora este email.\n\nSaludos,\nEquipo DPI";
            
            Content content = new Content("text/plain", contentText);
            Mail mail = new Mail(from, subject, to, content);
            
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            // ✅ DEBUGGING DETALLADO - ESTO ES LO NUEVO
            System.out.println("=== DEBUG SENDGRID ===");
            System.out.println("From Email: " + fromEmail);
            System.out.println("To Email: " + toEmail);
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("Response Headers: " + response.getHeaders());
            System.out.println("=== FIN DEBUG ===");
            
            if (response.getStatusCode() == 202) {
                System.out.println("✅ Email aceptado por SendGrid");
            } else {
                System.out.println("❌ Error de SendGrid: " + response.getStatusCode());
                // Lanza una excepción más específica para debugging
                throw new RuntimeException("SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
            }
            
        } catch (IOException ex) {
            System.err.println("Error enviando email: " + ex.getMessage());
            throw new RuntimeException("Error enviando email de recuperación: " + ex.getMessage());
        }
    }
}