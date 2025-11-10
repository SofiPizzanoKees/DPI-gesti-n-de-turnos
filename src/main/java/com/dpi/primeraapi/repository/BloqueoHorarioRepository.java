package com.dpi.primeraapi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dpi.primeraapi.model.BloqueoHorarios;
import com.dpi.primeraapi.model.Usuario;

public interface BloqueoHorarioRepository extends JpaRepository<BloqueoHorarios, Long> {
    List<BloqueoHorarios> findByMedicoAndFecha(Usuario medico, LocalDate fecha);
    List<BloqueoHorarios> findByMedicoAndFechaBetween(Usuario medico, LocalDate inicio, LocalDate fin);
}