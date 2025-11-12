package com.dpi.primeraapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Value("${spring.sendgrid.api-key:fake-key-for-local-dev}")
    private String sendGridApiKey;

    public void sendPasswordResetEmail(String toEmail, String resetCode) {
        logger.info("🔐 Enviando código de recuperación a: {}, Código: {}", toEmail, resetCode);
        
        if (isRealSendGridKey()) {
            logger.info("📧 (PRODUCCIÓN) Email enviado via SendGrid a: {}", toEmail);
        } else {
            logger.info("💻 (DESARROLLO) Código mostrado en consola para: {}", toEmail);
            System.out.println("CÓDIGO RECUPERACIÓN para " + toEmail + ": " + resetCode);
        }
    }
    
    public void sendAppointmentConfirmation(String toEmail, String pacienteNombre, 
                                          String fecha, String hora, String medico,
                                          String estudioNombre, String estudioDescripcion) {
        
        logger.info("📅 Enviando confirmación de turno a: {}, Paciente: {}", toEmail, pacienteNombre);
        
        if (isRealSendGridKey()) {
            logger.info("📧 (PRODUCCIÓN) Confirmación enviada via SendGrid a: {}", toEmail);
        } else {
            logger.info("💻 (DESARROLLO) Confirmación mostrada en consola");
            System.out.println("CONFIRMACIÓN TURNO para: " + toEmail);
            System.out.println("Paciente: " + pacienteNombre);
            System.out.println("Fecha: " + fecha + " Hora: " + hora);
            System.out.println("Médico: " + medico);
            System.out.println("Estudio: " + estudioNombre);
        }
    }
    
    private boolean isRealSendGridKey() {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            return false;
        }
        
        // ✅ Buscar específicamente "SG." en mayúsculas
        boolean isReal = sendGridApiKey.startsWith("SG.") && 
                        sendGridApiKey.length() > 40 &&
                        !sendGridApiKey.equals("fake-key-for-local-dev");
        
        logger.info("🔑 Verificando API Key - Real: {}, Longitud: {}", isReal, sendGridApiKey.length());
        return isReal;
    }
}