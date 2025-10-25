package com.dpi.primeraapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dpi.primeraapi.model.UsuarioObraSocial;

public interface UsuarioObraSocialRepository extends JpaRepository<UsuarioObraSocial, Long> {
    List<UsuarioObraSocial> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioIdAndObraSocialId(Long usuarioId, Long obraSocialId);
}