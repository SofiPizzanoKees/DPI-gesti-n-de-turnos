package com.dpi.primeraapi.repository;

import java.util.List;
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
    
    // ✅ NUEVO: Verificar si existe un usuario con la matrícula nacional
    boolean existsByMatriculaNacional(String matriculaNacional);
    
    // ✅ NUEVO: Verificar si existe un usuario con la matrícula provincial
    boolean existsByMatriculaProvincial(String matriculaProvincial);
    
    // Buscar usuario por DNI
    Optional<Usuario> findByDni(String dni);
    
    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);

    // Buscar usuarios por rol y estado activo
    List<Usuario> findByRolAndEstadoTrue(String rol);
    List<Usuario> findByRolAndEstado(String rol, boolean estado);
    // Buscar usuarios por rol, especialidad y estado activo
    List<Usuario> findByRolAndEspecialidadAndEstadoTrue(String rol, String especialidad);
}