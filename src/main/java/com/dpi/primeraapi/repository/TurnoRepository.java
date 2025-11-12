package com.dpi.primeraapi.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dpi.primeraapi.model.Turno;
import com.dpi.primeraapi.model.Usuario;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    List<Turno> findByMedicoAndFecha(Usuario medico, LocalDate fecha);
    List<Turno> findByMedicoAndFechaAndEstado(Usuario medico, LocalDate fecha, String estado);
    List<Turno> findByPaciente(Usuario paciente);
    boolean existsByMedicoAndFechaAndHora(Usuario medico, LocalDate fecha, LocalTime hora);
    Turno findByCodigoTurno(String codigoTurno);
    List<Turno> findByFecha(LocalDate fecha);
    List<Turno> findByMedico(Usuario medico);
}