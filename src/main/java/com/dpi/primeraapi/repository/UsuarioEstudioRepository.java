package com.dpi.primeraapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dpi.primeraapi.model.Estudio;
import com.dpi.primeraapi.model.UsuarioEstudio;

@Repository
public interface UsuarioEstudioRepository extends JpaRepository<UsuarioEstudio, Long> {
    
    List<UsuarioEstudio> findByEstudio(Estudio estudio);
}