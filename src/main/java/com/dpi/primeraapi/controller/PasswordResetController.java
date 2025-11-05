package com.dpi.primeraapi.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.repository.UsuarioRepository;
import com.dpi.primeraapi.service.EmailService;
import com.dpi.primeraapi.service.PasswordEncoderService;
import com.dpi.primeraapi.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoderService passwordEncoderService;
    
    @PostMapping("/create-test-user")
    public String createTestUser() {
        try {
            // Verificar si ya existe
            if (usuarioRepository.findByEmail("test@ejemplo.com").isPresent()) {
                return "✅ Usuario de prueba ya existe: test@ejemplo.com / password123";
            }
            
            Usuario usuario = new Usuario();
            usuario.setDni("12345678");
            usuario.setNombre("Usuario");
            usuario.setApellido("Prueba");
            usuario.setEmail("test@ejemplo.com");
            usuario.setPassword(passwordEncoderService.encode("password123"));
            usuario.setRol("PACIENTE");
            usuario.setTelefono("1122334455");
            
            usuarioRepository.save(usuario);
            return "✅ Usuario de prueba creado: test@ejemplo.com / password123";
        } catch (Exception e) {
            return "❌ Error creando usuario: " + e.getMessage();
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            // Buscar usuario por email
            Usuario usuario = userService.findByEmail(email);
            if (usuario == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Si el email existe, recibirás un enlace de recuperación");
                return ResponseEntity.ok(response);
            }
            
            // Generar token
            String token = userService.generatePasswordResetToken(usuario);
            
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