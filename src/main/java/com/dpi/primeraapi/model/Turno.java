package com.dpi.primeraapi.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "turnos")
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Long idTurno;
    
    @Column(nullable = false)
    private LocalDate fecha;
    
    @Column(nullable = false)
    private LocalTime hora;
    
    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private Usuario paciente;
    
    @ManyToOne
    @JoinColumn(name = "id_med", nullable = false)
    private Usuario medico;
    
    @ManyToOne
    @JoinColumn(name = "id_estudio", nullable = false)
    private Estudio estudio;
    
    @Column(nullable = false)
    private String estado = "PENDIENTE"; // PENDIENTE, CONFIRMADO, CANCELADO, COMPLETADO
    
    // Constructores
    public Turno() {}
    
    public Turno(Usuario paciente, Usuario medico, Estudio estudio, LocalDate fecha, LocalTime hora) {
        this.paciente = paciente;
        this.medico = medico;
        this.estudio = estudio;
        this.fecha = fecha;
        this.hora = hora;
    }
    
    // Getters y Setters
    public Long getIdTurno() { 
        return idTurno; 
    }
    
    public void setIdTurno(Long idTurno) { 
        this.idTurno = idTurno; 
    }
    
    public LocalDate getFecha() { 
        return fecha; 
    }
    
    public void setFecha(LocalDate fecha) { 
        this.fecha = fecha; 
    }
    
    public LocalTime getHora() { 
        return hora; 
    }
    
    public void setHora(LocalTime hora) { 
        this.hora = hora; 
    }
    
    public Usuario getPaciente() { 
        return paciente; 
    }
    
    public void setPaciente(Usuario paciente) { 
        this.paciente = paciente; 
    }
    
    public Usuario getMedico() { 
        return medico; 
    }
    
    public void setMedico(Usuario medico) { 
        this.medico = medico; 
    }
    
    public Estudio getEstudio() { 
        return estudio; 
    }
    
    public void setEstudio(Estudio estudio) { 
        this.estudio = estudio; 
    }
    
    public String getEstado() { 
        return estado; 
    }
    
    public void setEstado(String estado) { 
        this.estado = estado; 
    }
}