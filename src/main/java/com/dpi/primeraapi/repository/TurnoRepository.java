package com.dpi.primeraapi.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dpi.primeraapi.model.Turno;
import com.dpi.primeraapi.model.Usuario;
@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    List<Turno> findByMedicoAndFecha(Usuario medico, LocalDate fecha);
    List<Turno> findByMedicoAndFechaAndEstado(Usuario medico, LocalDate fecha, String estado);
    List<Turno> findByPaciente(Usuario paciente);
    boolean existsByMedicoAndFechaAndHora(Usuario medico, LocalDate fecha, LocalTime hora);
    Turno findByCodigoTurno(String codigoTurno);
    List<Turno> findByFecha(LocalDate fecha);
    List<Turno> findByMedico(Usuario medico);
       // ✅ NUEVO MÉTODO: Buscar turnos por médico, fecha y hora exacta
    @Query("SELECT t FROM Turno t WHERE t.medico = :medico AND t.fecha = :fecha AND t.hora = :hora")
    List<Turno> findByMedicoAndFechaAndHora(
        @Param("medico") Usuario medico, 
        @Param("fecha") LocalDate fecha, 
        @Param("hora") LocalTime hora
    );
    
}