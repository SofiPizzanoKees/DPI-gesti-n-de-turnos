package com.dpi.primeraapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dpi.primeraapi.model.ObraSocial;

public interface ObraSocialRepository extends JpaRepository<ObraSocial, Long> {
    List<ObraSocial> findByActivoTrue();
    List<ObraSocial> findByNombreContainingIgnoreCase(String nombre);
}