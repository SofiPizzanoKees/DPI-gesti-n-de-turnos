package com.dpi.primeraapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Value("${spring.sendgrid.api-key:fake-key-for-local-dev}")
    private String sendGridApiKey;
    
    @Value("${app.email.from:dpi.vallemedio1@gmail.com}")
    private String fromEmail;
    
    @Value("${app.email.from-name:Centro Médico DPI}")
    private String fromName;

    public void sendPasswordResetEmail(String toEmail, String resetCode) {
        logger.info("🔐 Intentando enviar código a: {}, Código: {}", toEmail, resetCode);
        
        if (!isRealSendGridKey()) {
            logger.warn("❌ SendGrid no configurado - Modo desarrollo");
            System.out.println("=== 🔐 CÓDIGO DESARROLLO ===");
            System.out.println("Para: " + toEmail);
            System.out.println("Código: " + resetCode);
            System.out.println("============================");
            return;
        }
        
        try {
            // ✅ CONFIGURAR EMAIL
            Email from = new Email(fromEmail, fromName);
            String subject = "Recuperación de Contraseña - Centro Médico DPI";
            Email to = new Email(toEmail);
            
            // CONTENIDO HTML
            String htmlContent = createPasswordResetHtml(resetCode);
            Content content = new Content("text/html", htmlContent);
            
            // CREAR Y ENVIAR EMAIL
            Mail mail = new Mail(from, subject, to, content);
            SendGrid sg = new SendGrid(sendGridApiKey);
            
            logger.info("📧 Enviando email real via SendGrid...");
            
            // ✅ ENVÍO REAL
            var request = new com.sendgrid.Request();
            request.setMethod(com.sendgrid.Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            var response = sg.api(request);
            
            logger.info("✅ SendGrid response - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() == 202) {
                logger.info("🎉 Email enviado exitosamente a: {}", toEmail);
            } else {
                logger.error("❌ Error SendGrid - Status: {}, Response: {}", response.getStatusCode(), response.getBody());
            }
            
        } catch (Exception e) {
            logger.error("💥 Error crítico enviando email: {}", e.getMessage(), e);
            System.out.println("=== 🔐 CÓDIGO FALLBACK ===");
            System.out.println("Para: " + toEmail);
            System.out.println("Código: " + resetCode);
            System.out.println("Error: " + e.getMessage());
            System.out.println("==========================");
        }
    }
    
    public void sendAppointmentConfirmation(String toEmail, String pacienteNombre, 
                                          String fecha, String hora, String medico,
                                          String estudioNombre, String estudioDescripcion) {
        
        logger.info("📅 Intentando enviar confirmación a: {}", toEmail);
        
        if (!isRealSendGridKey()) {
            logger.warn("❌ SendGrid no configurado - Modo desarrollo");
            System.out.println("=== 📅 CONFIRMACIÓN DESARROLLO ===");
            System.out.println("Para: " + toEmail);
            System.out.println("Paciente: " + pacienteNombre);
            System.out.println("Fecha: " + fecha + " Hora: " + hora);
            System.out.println("Médico: " + medico);
            System.out.println("================================");
            return;
        }
        
        try {
            // ✅ CONFIGURAR EMAIL
            Email from = new Email(fromEmail, fromName);
            String subject = "Confirmación de Turno - Centro Médico DPI";
            Email to = new Email(toEmail);
            
            // CONTENIDO HTML
            String htmlContent = createAppointmentConfirmationHtml(pacienteNombre, fecha, hora, medico, estudioNombre, estudioDescripcion);
            Content content = new Content("text/html", htmlContent);
            
            // CREAR Y ENVIAR EMAIL
            Mail mail = new Mail(from, subject, to, content);
            SendGrid sg = new SendGrid(sendGridApiKey);
            
            logger.info("📧 Enviando confirmación real via SendGrid...");
            
            // ✅ ENVÍO REAL
            var request = new com.sendgrid.Request();
            request.setMethod(com.sendgrid.Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            var response = sg.api(request);
            
            logger.info("✅ SendGrid response - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() == 202) {
                logger.info("🎉 Confirmación enviada exitosamente a: {}", toEmail);
            } else {
                logger.error("❌ Error SendGrid - Status: {}, Response: {}", response.getStatusCode(), response.getBody());
            }
            
        } catch (Exception e) {
            logger.error("💥 Error crítico enviando confirmación: {}", e.getMessage(), e);
            System.out.println("=== 📅 CONFIRMACIÓN FALLBACK ===");
            System.out.println("Para: " + toEmail);
            System.out.println("Paciente: " + pacienteNombre);
            System.out.println("Fecha: " + fecha + " Hora: " + hora);
            System.out.println("Médico: " + medico);
            System.out.println("Error: " + e.getMessage());
            System.out.println("================================");
        }
    }

    // ✅ NUEVO MÉTODO PARA CANCELACIÓN DE TURNOS
    public void sendAppointmentCancellation(String toEmail, String patientName, String fecha, 
                                          String hora, String medico, String estudio) {
        
        logger.info("❌ Intentando enviar cancelación a: {}", toEmail);
        
        if (!isRealSendGridKey()) {
            logger.warn("❌ SendGrid no configurado - Modo desarrollo");
            System.out.println("=== ❌ CANCELACIÓN DESARROLLO ===");
            System.out.println("Para: " + toEmail);
            System.out.println("Paciente: " + patientName);
            System.out.println("Fecha: " + fecha + " Hora: " + hora);
            System.out.println("Médico: " + medico);
            System.out.println("Estudio: " + estudio);
            System.out.println("=================================");
            return;
        }
        
        try {
            // ✅ CONFIGURAR EMAIL
            Email from = new Email(fromEmail, fromName);
            String subject = "Cancelación de Turno - Centro Médico DPI";
            Email to = new Email(toEmail);
            
            // CONTENIDO HTML
            String htmlContent = createAppointmentCancellationHtml(patientName, fecha, hora, medico, estudio);
            Content content = new Content("text/html", htmlContent);
            
            // CREAR Y ENVIAR EMAIL
            Mail mail = new Mail(from, subject, to, content);
            SendGrid sg = new SendGrid(sendGridApiKey);
            
            logger.info("📧 Enviando cancelación real via SendGrid...");
            
            // ✅ ENVÍO REAL
            var request = new com.sendgrid.Request();
            request.setMethod(com.sendgrid.Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            var response = sg.api(request);
            
            logger.info("✅ SendGrid response - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() == 202) {
                logger.info("🎉 Cancelación enviada exitosamente a: {}", toEmail);
            } else {
                logger.error("❌ Error SendGrid - Status: {}, Response: {}", response.getStatusCode(), response.getBody());
            }
            
        } catch (Exception e) {
            logger.error("💥 Error crítico enviando cancelación: {}", e.getMessage(), e);
            System.out.println("=== ❌ CANCELACIÓN FALLBACK ===");
            System.out.println("Para: " + toEmail);
            System.out.println("Paciente: " + patientName);
            System.out.println("Fecha: " + fecha + " Hora: " + hora);
            System.out.println("Médico: " + medico);
            System.out.println("Estudio: " + estudio);
            System.out.println("Error: " + e.getMessage());
            System.out.println("===============================");
        }
    }
    
    private boolean isRealSendGridKey() {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            return false;
        }
        
        boolean isReal = sendGridApiKey.startsWith("SG.") && 
                        sendGridApiKey.length() > 40 &&
                        !sendGridApiKey.equals("fake-key-for-local-dev");
        
        logger.debug("🔑 Verificando API Key - Real: {}, Longitud: {}", isReal, sendGridApiKey.length());
        return isReal;
    }
    
    private String createPasswordResetHtml(String resetCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; }
                    .header { background: #00c2a8; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .code { font-size: 24px; font-weight: bold; color: #00c2a8; text-align: center; margin: 20px 0; padding: 10px; background: #f0f8ff; border-radius: 5px; }
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
                        <p>📍 Tello 337, Choele Choel, Río Negro</p>
                        <p>📞 +54 2946 15-508112</p>
                        <p>Saludos,<br><strong>Equipo DPI Valle Medio</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(resetCode);
    }
    
    private String createAppointmentConfirmationHtml(String pacienteNombre, String fecha, 
                                                   String hora, String medico, 
                                                   String estudioNombre, String estudioDescripcion) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
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
    }

    private String createAppointmentCancellationHtml(String patientName, String fecha, 
                                                   String hora, String medico, String estudio) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; }
                    .header { background: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .cancellation-details { background: #f8d7da; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; }
                    .detail-row { margin: 8px 0; }
                    .detail-label { font-weight: bold; color: #721c24; }
                    .detail-value { color: #856404; }
                    .reschedule { background: #d1ecf1; padding: 15px; border-radius: 5px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Centro Médico DPI</h1>
                        <p>Cancelación de Turno</p>
                    </div>
                    
                    <h2>Hola %s,</h2>
                    <p>Tu turno ha sido <strong style="color: #dc3545;">cancelado</strong>.</p>
                    
                    <div class="cancellation-details">
                        <h3>❌ Turno Cancelado</h3>
                        <div class="detail-row">
                            <span class="detail-label">Estudio:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Médico:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Fecha:</span>
                            <span class="detail-value">%s</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Hora:</span>
                            <span class="detail-value">%s</span>
                        </div>
                    </div>
                    
                    <div class="reschedule">
                        <h3>📅 ¿Necesitas reagendar?</h3>
                        <p>Puedes pedir un nuevo turno ingresando a nuestra plataforma:</p>
                        <p><a href="https://tu-dominio.com/pedirturno" style="color: #007bff; text-decoration: none; font-weight: bold;">👉 Solicitar nuevo turno</a></p>
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
            """.formatted(patientName, estudio, medico, fecha, hora);
    }
}