package com.dpi.primeraapi.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dpi.primeraapi.model.DisponibilidadMedica;
import com.dpi.primeraapi.model.Estudio;
import com.dpi.primeraapi.model.Turno;
import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.model.enums.DiaSemana;
import com.dpi.primeraapi.repository.DisponibilidadMedicaRepository;
import com.dpi.primeraapi.repository.TurnoRepository;

@Service
public class TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private DisponibilidadMedicaRepository disponibilidadRepository;

    @Transactional
    public Turno crearTurno(Usuario paciente, Usuario medico, Estudio estudio, LocalDate fecha, LocalTime hora) {
        // ✅ VERIFICAR DISPONIBILIDAD ANTES DE CREAR EL TURNO
        if (!estaHorarioDisponible(medico, fecha, hora)) {
            throw new RuntimeException("El horario seleccionado ya no está disponible. Por favor, elija otro horario.");
        }

        // Crear el turno
        Turno turno = new Turno(paciente, medico, estudio, fecha, hora);
        
        // Guardar el turno
        Turno turnoGuardado = turnoRepository.save(turno);
        
        return turnoGuardado;
    }

    /**
     * Verifica si un horario está disponible para un médico en una fecha específica
     */
    public boolean estaHorarioDisponible(Usuario medico, LocalDate fecha, LocalTime hora) {
        // 1. Verificar que el médico tenga disponibilidad en ese día de la semana
        DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        DiaSemana diaSemana = convertirDayOfWeekADiaSemana(dayOfWeek);
        
        List<DisponibilidadMedica> disponibilidades = disponibilidadRepository
                .findByMedicoAndDiaSemanaAndActivoTrue(medico, diaSemana);
        
        if (disponibilidades.isEmpty()) {
            return false; // El médico no trabaja ese día
        }

        // 2. Verificar que el horario esté dentro del rango de disponibilidad
        DisponibilidadMedica disponibilidad = disponibilidades.get(0);
        if (hora.isBefore(disponibilidad.getHoraInicio()) || 
            hora.isAfter(disponibilidad.getHoraFin().minusMinutes(disponibilidad.getDuracionTurnoMinutos()))) {
            return false; // Horario fuera del rango disponible
        }

        // 3. Verificar que no haya otro turno en el mismo horario
        List<Turno> turnosExistentes = turnoRepository.findByMedicoAndFechaAndHora(medico, fecha, hora);
        
        return turnosExistentes.isEmpty(); // True si no hay turnos en ese horario
    }

    /**
     * Obtiene los horarios disponibles para un médico en una fecha específica
     */
    public List<String> obtenerHorariosDisponibles(Usuario medico, LocalDate fecha) {
        // Obtener la disponibilidad del médico para ese día
        DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        DiaSemana diaSemana = convertirDayOfWeekADiaSemana(dayOfWeek);
        
        List<DisponibilidadMedica> disponibilidades = disponibilidadRepository
                .findByMedicoAndDiaSemanaAndActivoTrue(medico, diaSemana);
        
        if (disponibilidades.isEmpty()) {
            return List.of(); // No hay disponibilidad ese día
        }

        DisponibilidadMedica disponibilidad = disponibilidades.get(0);
        
        // Generar todos los horarios posibles
        List<String> todosLosHorarios = generarHorariosDisponibles(
            disponibilidad.getHoraInicio(), 
            disponibilidad.getHoraFin(), 
            disponibilidad.getDuracionTurnoMinutos()
        );

        // Filtrar los horarios que ya están ocupados
        return todosLosHorarios.stream()
                .filter(horario -> {
                    LocalTime hora = LocalTime.parse(horario);
                    List<Turno> turnosExistentes = turnoRepository.findByMedicoAndFechaAndHora(medico, fecha, hora);
                    return turnosExistentes.isEmpty(); // Mantener solo los horarios libres
                })
                .collect(Collectors.toList());
    }

    /**
     * Convierte DayOfWeek a DiaSemana
     */
    private DiaSemana convertirDayOfWeekADiaSemana(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return DiaSemana.LUNES;
            case TUESDAY: return DiaSemana.MARTES;
            case WEDNESDAY: return DiaSemana.MIERCOLES;
            case THURSDAY: return DiaSemana.JUEVES;
            case FRIDAY: return DiaSemana.VIERNES;
            case SATURDAY: return DiaSemana.SABADO;
            case SUNDAY: return DiaSemana.DOMINGO;
            default: return DiaSemana.LUNES;
        }
    }

    /**
     * Genera horarios disponibles basados en la disponibilidad del médico
     */
    private List<String> generarHorariosDisponibles(LocalTime horaInicio, LocalTime horaFin, Integer duracionTurno) {
        List<String> horarios = new ArrayList<>();
        LocalTime horaActual = horaInicio;
        
        while (horaActual.plusMinutes(duracionTurno).isBefore(horaFin) || 
               horaActual.plusMinutes(duracionTurno).equals(horaFin)) {
            horarios.add(horaActual.toString());
            horaActual = horaActual.plusMinutes(duracionTurno);
        }
        
        return horarios;
    }

    
    // Método adicional que podrías necesitar
    public Turno obtenerTurnoPorCodigo(String codigoTurno) {
        return turnoRepository.findByCodigoTurno(codigoTurno);
    }
    public List<Turno> obtenerTurnosPorMedico(Usuario medico) {
    return turnoRepository.findByMedico(medico);
}
    public List<Turno> obtenerTurnosFuturosPorEstudio(Estudio estudio) {
    LocalDate hoy = LocalDate.now();
    return turnoRepository.findByEstudioAndFechaGreaterThanEqualAndEstadoNot(
        estudio, hoy, "CANCELADO");
    }
    public Turno guardarTurno(Turno turno) {
        return turnoRepository.save(turno);
    }
    public List<Turno> obtenerTurnosPorPaciente(Usuario paciente) {
    return turnoRepository.findByPacienteAndEstadoNot(paciente, "CANCELADO");
}
/**
 * Obtiene turnos futuros por médico y día de la semana
 */
public List<Turno> obtenerTurnosFuturosPorMedicoYDia(Usuario medico, DiaSemana diaSemana) {
    LocalDate hoy = LocalDate.now();
    return turnoRepository.findByMedico(medico).stream()
        .filter(turno -> !turno.getFecha().isBefore(hoy) && 
                       obtenerDiaSemana(turno.getFecha()).equals(diaSemana) &&
                       !"CANCELADO".equals(turno.getEstado()))
        .collect(Collectors.toList());
}

private DiaSemana obtenerDiaSemana(LocalDate fecha) {
    DayOfWeek dayOfWeek = fecha.getDayOfWeek();
    switch (dayOfWeek) {
        case MONDAY: return DiaSemana.LUNES;
        case TUESDAY: return DiaSemana.MARTES;
        case WEDNESDAY: return DiaSemana.MIERCOLES;
        case THURSDAY: return DiaSemana.JUEVES;
        case FRIDAY: return DiaSemana.VIERNES;
        case SATURDAY: return DiaSemana.SABADO;
        case SUNDAY: return DiaSemana.DOMINGO;
        default: return DiaSemana.LUNES;
    }
}
}