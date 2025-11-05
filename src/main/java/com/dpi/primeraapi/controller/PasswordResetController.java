package com.dpi.primeraapi.controller;

import com.dpi.primeraapi.model.Usuario;  // ← AGREGAR este import
import com.dpi.primeraapi.service.EmailService;
import com.dpi.primeraapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            // Buscar usuario por email
            Usuario usuario = userService.findByEmail(email);  // ← CAMBIADO: Object user → Usuario usuario
            if (usuario == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Si el email existe, recibirás un enlace de recuperación");
                return ResponseEntity.ok(response);
            }
            
            // Generar token
            String token = userService.generatePasswordResetToken(usuario);  // ← CAMBIADO: user → usuario
            
            // Enviar email
            emailService.sendPasswordResetEmail(email, token);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Si el email existe, recibirás un enlace de recuperación");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error procesando la solicitud");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, 
                                         @RequestParam String newPassword) {
        try {
            boolean result = userService.resetPassword(token, newPassword);
            
            if (result) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Contraseña actualizada exitosamente");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Token inválido o expirado");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error actualizando la contraseña");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}