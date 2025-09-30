package com.dpi.primeraapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dpi.primeraapi.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Buscar usuario por DNI
    Optional<Usuario> findByDni(String dni);
    
    // Verificar si existe un usuario con ese DNI
    boolean existsByDni(String dni);
    
    // Estos métodos vienen incluidos con JpaRepository:
    // - save()
    // - findAll() 
    // - findById()
    // - delete()
    // - etc.
}