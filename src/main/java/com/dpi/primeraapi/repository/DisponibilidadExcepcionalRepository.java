package com.dpi.primeraapi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dpi.primeraapi.model.DisponibilidadExcepcional;
import com.dpi.primeraapi.model.Usuario;

public interface DisponibilidadExcepcionalRepository extends JpaRepository<DisponibilidadExcepcional, Long> {
    List<DisponibilidadExcepcional> findByMedicoAndFecha(Usuario medico, LocalDate fecha);
    List<DisponibilidadExcepcional> findByMedicoAndFechaBetween(Usuario medico, LocalDate inicio, LocalDate fin);
}