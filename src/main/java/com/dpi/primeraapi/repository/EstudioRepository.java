package com.dpi.primeraapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dpi.primeraapi.model.Estudio;

@Repository
public interface EstudioRepository extends JpaRepository<Estudio, Long> {
    
    List<Estudio> findByActivoTrue();
    
    Optional<Estudio> findByNombreAndActivoTrue(String nombre);
    
    boolean existsByNombreAndActivoTrue(String nombre);
    
    @Query("SELECT e FROM Estudio e WHERE e.activo = true ORDER BY e.nombre")
    List<Estudio> findAllActiveOrdered();
    long count();
}