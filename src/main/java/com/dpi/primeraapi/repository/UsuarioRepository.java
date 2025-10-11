package com.dpi.primeraapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dpi.primeraapi.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Verificar si existe un usuario con el DNI
    boolean existsByDni(String dni);
    
    // Verificar si existe un usuario con el email
    boolean existsByEmail(String email);
    
    // Buscar usuario por DNI
    Optional<Usuario> findByDni(String dni);
    
    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);
}