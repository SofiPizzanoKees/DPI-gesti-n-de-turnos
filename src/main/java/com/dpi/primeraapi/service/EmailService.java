package com.dpi.primeraapi.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    public void sendPasswordResetEmail(String toEmail, String resetCode) {
        // Versión simplificada para desarrollo - solo imprime en consola
        System.out.println("=== EMAIL SIMULADO ===");
        System.out.println("Para: " + toEmail);
        System.out.println("Código de recuperación: " + resetCode);
        System.out.println("=== FIN EMAIL ===");
        
        // En desarrollo, no necesitamos enviar emails reales
        // Esto evita los errores de SendGrid
    }
    
    public void sendWelcomeEmail(String toEmail, String nombre) {
        System.out.println("=== EMAIL BIENVENIDA ===");
        System.out.println("Para: " + toEmail);
        System.out.println("Bienvenido: " + nombre);
        System.out.println("=== FIN EMAIL ===");
    }
}