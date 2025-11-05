package com.dpi.primeraapi.entity;

import java.util.Date;  // ← Cambiar por Usuario

import com.dpi.primeraapi.model.Usuario;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String token;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private Usuario usuario;  // ← Cambiado de User a Usuario
    
    private Date expiryDate;
    
    // Constructores
    public PasswordResetToken() {}
    
    public PasswordResetToken(String token, Usuario usuario) {  // ← Cambiado aquí
        this.token = token;
        this.usuario = usuario;
        this.expiryDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 24 horas
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public Usuario getUsuario() { return usuario; }  // ← Cambiado aquí
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }  // ← Cambiado aquí
    
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    
    public boolean isExpired() {
        return new Date().after(this.expiryDate);
    }
}