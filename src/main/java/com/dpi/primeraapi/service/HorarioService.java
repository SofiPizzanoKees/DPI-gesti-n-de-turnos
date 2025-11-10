package com.dpi.primeraapi.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dpi.primeraapi.model.BloqueoHorarios;
import com.dpi.primeraapi.model.DisponibilidadExcepcional;
import com.dpi.primeraapi.model.DisponibilidadMedica;
import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.model.enums.DiaSemana;
import com.dpi.primeraapi.repository.BloqueoHorarioRepository;
import com.dpi.primeraapi.repository.DisponibilidadExcepcionalRepository;
import com.dpi.primeraapi.repository.DisponibilidadMedicaRepository;
import com.dpi.primeraapi.repository.TurnoRepository;

@Service
public class HorarioService {
    private final DisponibilidadMedicaRepository disponibilidadRepo;
    private final DisponibilidadExcepcionalRepository excepcionalRepo;
    private final BloqueoHorarioRepository bloqueoRepo;
    private final TurnoRepository turnoRepo;
    
    public HorarioService(DisponibilidadMedicaRepository disponibilidadRepo,
                         DisponibilidadExcepcionalRepository excepcionalRepo,
                         BloqueoHorarioRepository bloqueoRepo,
                         TurnoRepository turnoRepo) {
        this.disponibilidadRepo = disponibilidadRepo;
        this.excepcionalRepo = excepcionalRepo;
        this.bloqueoRepo = bloqueoRepo;
        this.turnoRepo = turnoRepo;
    }
    
    public List<LocalTime> obtenerHorariosDisponibles(Usuario medico, LocalDate fecha) {
        List<LocalTime> horariosDisponibles = new ArrayList<>();
        
        DisponibilidadInfo disponibilidad = obtenerDisponibilidadDelDia(medico, fecha);
        if (disponibilidad == null || !disponibilidad.trabaja) {
            return horariosDisponibles;
        }
        
        List<LocalTime> slots = generarSlotsTiempo(
            disponibilidad.horaInicio, 
            disponibilidad.horaFin, 
            disponibilidad.duracionTurno
        );
        
        for (LocalTime slot : slots) {
            if (esHorarioDisponible(medico, fecha, slot)) {
                horariosDisponibles.add(slot);
            }
        }
        
        return horariosDisponibles;
    }
    
    private DisponibilidadInfo obtenerDisponibilidadDelDia(Usuario medico, LocalDate fecha) {
        DiaSemana diaSemana = DiaSemana.valueOf(fecha.getDayOfWeek().name());
        
        // Primero verificar disponibilidad excepcional
        List<DisponibilidadExcepcional> excepcionales = excepcionalRepo.findByMedicoAndFecha(medico, fecha);
        if (!excepcionales.isEmpty()) {
            DisponibilidadExcepcional excepcional = excepcionales.get(0);
            return new DisponibilidadInfo(
                excepcional.getHoraInicio() != null,
                excepcional.getHoraInicio(),
                excepcional.getHoraFin(),
                excepcional.getDuracionTurnoMinutos() != null ? 
                    excepcional.getDuracionTurnoMinutos() : 30
            );
        }
        
        // Si no hay excepción, usar disponibilidad regular
        List<DisponibilidadMedica> disponibilidades = disponibilidadRepo
            .findByMedicoAndDiaSemanaAndActivoTrue(medico, diaSemana);
        
        if (disponibilidades.isEmpty()) {
            return null;
        }
        
        DisponibilidadMedica disp = disponibilidades.get(0);
        return new DisponibilidadInfo(true, disp.getHoraInicio(), disp.getHoraFin(), 
                                    disp.getDuracionTurnoMinutos());
    }
    
    private List<LocalTime> generarSlotsTiempo(LocalTime inicio, LocalTime fin, int duracionMinutos) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = inicio;
        
        while (current.plusMinutes(duracionMinutos).isBefore(fin) || 
               current.plusMinutes(duracionMinutos).equals(fin)) {
            slots.add(current);
            current = current.plusMinutes(duracionMinutos);
        }
        
        return slots;
    }
    
    private boolean esHorarioDisponible(Usuario medico, LocalDate fecha, LocalTime hora) {
        // Verificar si está bloqueado
        List<BloqueoHorarios> bloqueos = bloqueoRepo.findByMedicoAndFecha(medico, fecha);
        for (BloqueoHorarios bloqueo : bloqueos) {
            if (!hora.isBefore(bloqueo.getHoraInicio()) && hora.isBefore(bloqueo.getHoraFin())) {
                return false;
            }
        }
        
        // Verificar si ya hay un turno
        return !turnoRepo.existsByMedicoAndFechaAndHora(medico, fecha, hora);
    }
    
    private static class DisponibilidadInfo {
        boolean trabaja;
        LocalTime horaInicio;
        LocalTime horaFin;
        int duracionTurno;
        
        DisponibilidadInfo(boolean trabaja, LocalTime horaInicio, LocalTime horaFin, int duracionTurno) {
            this.trabaja = trabaja;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.duracionTurno = duracionTurno;
        }
    }
}