package com.dpi.primeraapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dpi.primeraapi.model.Estudio;

public interface EstudioRepository extends JpaRepository<Estudio, Long> {
    List<Estudio> findByActivoTrue();
}