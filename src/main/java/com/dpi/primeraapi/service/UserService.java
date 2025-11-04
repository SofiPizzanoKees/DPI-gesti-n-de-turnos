package com.dpi.primeraapi.service;

import com.dpi.primeraapi.entity.PasswordResetToken;
import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.repository.PasswordResetTokenRepository;
import com.dpi.primeraapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private PasswordEncoderService passwordEncoderService;
    
    // Buscar usuario por email
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }
    
    // Generar token de recuperación
    public String generatePasswordResetToken(Usuario usuario) {
        try {
            String token = UUID.randomUUID().toString();
            
            // Crear y guardar el token
            PasswordResetToken resetToken = new PasswordResetToken(token, usuario);
            passwordResetTokenRepository.save(resetToken);
            
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Error generando token de recuperación");
        }
    }
    
    // Resetear contraseña
    public boolean resetPassword(String token, String newPassword) {
        try {
            // Buscar token
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
            
            if (resetToken == null || resetToken.isExpired()) {
                return false;
            }
            
            // Obtener usuario y actualizar contraseña
            Usuario usuario = resetToken.getUsuario();
            String encryptedPassword = passwordEncoderService.encode(newPassword);
            usuario.setPassword(encryptedPassword);
            usuarioRepository.save(usuario);
            
            // Eliminar token usado
            passwordResetTokenRepository.delete(resetToken);
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error resetando contraseña");
        }
    }
}