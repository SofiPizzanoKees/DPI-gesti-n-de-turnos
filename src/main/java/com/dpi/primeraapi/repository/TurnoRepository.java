package com.dpi.primeraapi.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dpi.primeraapi.model.Turno;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    List<Turno> findByFecha(LocalDate fecha);
    List<Turno> findByMedicoIdAndFecha(Long medicoId, LocalDate fecha);
    boolean existsByMedicoIdAndFechaAndHora(Long medicoId, LocalDate fecha, LocalTime hora);
}