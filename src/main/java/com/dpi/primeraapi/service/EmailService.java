package com.dpi.primeraapi.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.from-name}")
    private String fromName;

    public void sendPasswordResetEmail(String toEmail, String resetCode) {
        try {
            Email from = new Email(fromEmail, fromName);
            String subject = "Recuperación de Contraseña - Centro Médico DPI";
            Email to = new Email(toEmail);
            
            // Plantilla HTML del email
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                        .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; }
                        .header { background: #00c2a8; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                        .code { font-size: 24px; font-weight: bold; color: #00c2a8; text-align: center; margin: 20px 0; }
                        .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Centro Médico DPI</h1>
                        </div>
                        <h2>Recuperación de Contraseña</h2>
                        <p>Hemos recibido una solicitud para restablecer tu contraseña.</p>
                        <p>Utiliza el siguiente código para continuar:</p>
                        <div class="code">%s</div>
                        <p>Si no solicitaste este cambio, puedes ignorar este mensaje.</p>
                        <div class="footer">
                            <p>Saludos,<br>Equipo DPI Valle Medio</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(resetCode);
            
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            logger.info("Email enviado a: {}, Status: {}, Body: {}", 
                       toEmail, response.getStatusCode(), response.getBody());
            
        } catch (IOException e) {
            logger.error("Error enviando email a {}: {}", toEmail, e.getMessage());
            // En desarrollo, puedes mostrar el código en consola
            System.out.println("=== CÓDIGO DE RECUPERACIÓN (Desarrollo) ===");
            System.out.println("Para: " + toEmail);
            System.out.println("Código: " + resetCode);
            System.out.println("===========================================");
        }
    }
    
public void sendAppointmentConfirmation(String toEmail, String pacienteNombre, 
                                      String fecha, String hora, String medico,
                                      String estudioNombre, String estudioDescripcion) {
    
    // Verificar si SendGrid está configurado
    if (sendGridApiKey == null || sendGridApiKey.isEmpty() || sendGridApiKey.equals("dummy_key")) {
        // MODO DESARROLLO: Mostrar en consola
        System.out.println("=== 📅 CONFIRMACIÓN TURNO (Desarrollo) ===");
        System.out.println("Para: " + toEmail);
        System.out.println("Paciente: " + pacienteNombre);
        System.out.println("Fecha: " + fecha);
        System.out.println("Hora: " + hora);
        System.out.println("Médico: " + medico);
        System.out.println("Estudio: " + estudioNombre);
        System.out.println("Descripción: " + (estudioDescripcion != null ? estudioDescripcion : "No disponible"));
        System.out.println("===========================================");
        return;
    }
    
    try {
        Email from = new Email(fromEmail, fromName);
        String subject = "Confirmación de Turno - Centro Médico DPI";
        Email to = new Email(toEmail);
        
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; }
                    .header { background: #00c2a8; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .appointment-details { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .study-info { background: #e8f7f5; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; }
                    .detail-row { margin: 8px 0; }
                    .detail-label { font-weight: bold; color: #333; }
                    .detail-value { color: #555; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Centro Médico DPI</h1>
                        <p>Confirmación de Turno</p>
                    </div>
                    
                    <h2>¡Hola %s!</h2>
                    <p>Tu turno ha sido confirmado exitosamente.</p>
                    
                    <div class="appointment-details">
                        <h3>📅 Detalles del Turno</h3>
                        <div class="detail-row">
                            <span class="detail-label">Fecha:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Hora:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Médico:</span>
                            <span class="detail-value">%s</span>
                        </div>
                    </div>
                    
                    <div class="study-info">
                        <h3>🔬 Información del Estudio</h3>
                        <div class="detail-row">
                            <span class="detail-label">Estudio:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Descripción:</span>
                            <span class="detail-value">%s</span>
                        </div>
                    </div>
                    
                    <div style="background: #fff3cd; padding: 15px; border-radius: 5px; margin: 15px 0;">
                        <h3>📋 Preparación y Recomendaciones</h3>
                        <ul>
                            <li>📍 <strong>Llegue 15 minutos antes</strong> de la hora programada</li>
                            <li>📄 Traer <strong>DNI y credencial de obra social</strong></li>
                            <li>📋 Traer <strong>estudios médicos previos</strong> si los tiene</li>
                            <li>⏰ En caso de no poder asistir, <strong>cancele con 24h de anticipación</strong></li>
                        </ul>
                    </div>
                    
                    <div class="footer">
                        <p>📍 <strong>Dirección:</strong> Tello 337, Choele Choel, Río Negro</p>
                        <p>📞 <strong>Teléfono:</strong> +54 2946 15-508112</p>
                        <p>✉️ <strong>Email:</strong> info@centroimagen.com</p>
                        <br>
                        <p>Saludos cordiales,<br><strong>Equipo DPI Valle Medio</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                pacienteNombre,
                fecha, 
                hora, 
                medico,
                estudioNombre,
                estudioDescripcion != null ? estudioDescripcion : "No se proporcionó descripción"
            );
        
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        
        Response response = sg.api(request);
        
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            logger.info("✅ Email de confirmación enviado a: {}, Status: {}", toEmail, response.getStatusCode());
        } else {
            logger.warn("⚠️  Email de confirmación NO enviado. Status: {}, Error: {}", response.getStatusCode(), response.getBody());
            // Fallback a modo desarrollo
            System.out.println("=== 📅 CONFIRMACIÓN TURNO (Fallback) ===");
            System.out.println("Para: " + toEmail);
            System.out.println("Paciente: " + pacienteNombre);
            System.out.println("Fecha: " + fecha);
            System.out.println("Hora: " + hora);
            System.out.println("Médico: " + medico);
            System.out.println("Estudio: " + estudioNombre);
            System.out.println("Descripción: " + (estudioDescripcion != null ? estudioDescripcion : "No disponible"));
            System.out.println("Error SendGrid: " + response.getBody());
            System.out.println("===========================================");
        }
        
    } catch (Exception e) {
        logger.error("❌ Error enviando confirmación de turno a {}: {}", toEmail, e.getMessage());
        // Fallback a modo desarrollo
        System.out.println("=== 📅 CONFIRMACIÓN TURNO (Error) ===");
        System.out.println("Para: " + toEmail);
        System.out.println("Paciente: " + pacienteNombre);
        System.out.println("Fecha: " + fecha);
        System.out.println("Hora: " + hora);
        System.out.println("Médico: " + medico);
        System.out.println("Estudio: " + estudioNombre);
        System.out.println("Descripción: " + (estudioDescripcion != null ? estudioDescripcion : "No disponible"));
        System.out.println("Error: " + e.getMessage());
        System.out.println("======================================");
    }
}
}