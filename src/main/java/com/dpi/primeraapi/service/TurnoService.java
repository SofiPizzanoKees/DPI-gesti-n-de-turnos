package com.dpi.primeraapi.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dpi.primeraapi.model.Turno;
import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.repository.TurnoRepository;

@Service
public class TurnoService {
    
    @Autowired
    private TurnoRepository turnoRepository;
    
    public Turno guardarTurno(Turno turno) {
        return turnoRepository.save(turno);
    }
    
    // AGREGAR ESTE MÉTODO FALTANTE
    public List<Turno> obtenerTurnosPorPaciente(Usuario paciente) {
        return turnoRepository.findByPaciente(paciente);
    }
    
    public List<Turno> obtenerTurnosPorFecha(LocalDate fecha) {
        return turnoRepository.findByFecha(fecha);
    }
    
    // Método corregido - usa el objeto Usuario completo
    public List<Turno> obtenerTurnosPorMedicoYFecha(Usuario medico, LocalDate fecha) {
        return turnoRepository.findByMedicoAndFecha(medico, fecha);
    }
    
    // Método corregido - usa el objeto Usuario completo
    public boolean existeTurnoEnHorario(Usuario medico, LocalDate fecha, LocalTime hora) {
        return turnoRepository.existsByMedicoAndFechaAndHora(medico, fecha, hora);
    }
    
    // Nuevo método para crear y guardar un turno
    public Turno crearTurno(Usuario paciente, Usuario medico, 
                           com.dpi.primeraapi.model.Estudio estudio, 
                           LocalDate fecha, LocalTime hora) {
        
        // Verificar si ya existe un turno en ese horario
        if (existeTurnoEnHorario(medico, fecha, hora)) {
            throw new RuntimeException("Ya existe un turno para este médico en el horario seleccionado");
        }
        
        Turno turno = new Turno(paciente, medico, estudio, fecha, hora);
        return guardarTurno(turno);
    }
    
    // Método adicional que podrías necesitar
    public Turno obtenerTurnoPorCodigo(String codigoTurno) {
        return turnoRepository.findByCodigoTurno(codigoTurno);
    }
}