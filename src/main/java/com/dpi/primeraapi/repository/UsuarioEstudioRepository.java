package com.dpi.primeraapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dpi.primeraapi.model.Estudio;
import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.model.UsuarioEstudio;

@Repository
public interface UsuarioEstudioRepository extends JpaRepository<UsuarioEstudio, Long> {
    
    List<UsuarioEstudio> findByEstudio(Estudio estudio);
    List<UsuarioEstudio> findByUsuario(Usuario usuario);

    boolean existsByUsuarioAndEstudio(Usuario usuario, Estudio estudio);
    Optional<UsuarioEstudio> findByUsuarioAndEstudio(Usuario usuario, Estudio estudio);
}