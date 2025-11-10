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
    
    @Column(name = "hora_fin")
    private LocalTime horaFin; // ✅ NUEVO: Para saber cuándo termina
    
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
    private String estado = "PENDIENTE";
    
    @Column(name = "codigo_turno", unique = true, length = 20)
    private String codigoTurno; // ✅ NUEVO: Para identificar turno fácilmente
    
    // Constructores
    public Turno() {}
    
    public Turno(Usuario paciente, Usuario medico, Estudio estudio, LocalDate fecha, LocalTime hora) {
        this.paciente = paciente;
        this.medico = medico;
        this.estudio = estudio;
        this.fecha = fecha;
        this.hora = hora;
        this.horaFin = hora.plusMinutes(30); // Default 30 min
        this.codigoTurno = generarCodigoTurno();
    }
    
    // Método privado para generar código de turno único y descriptivo
    private String generarCodigoTurno() {
        String fechaCodigo = String.format("%02d%02d%02d", 
            fecha.getYear() % 100, // Últimos 2 dígitos del año
            fecha.getMonthValue(), 
            fecha.getDayOfMonth()
        );
        
        String randomCodigo = String.format("%04d", 
            (int)(Math.random() * 10000) // Número aleatorio de 4 dígitos
        );
        
        return "T" + fechaCodigo + randomCodigo;
        // Ejemplo: "T2412151234" para 15/12/2024
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
        // Calcular horaFin automáticamente si no está definida
        if (this.horaFin == null) {
            this.horaFin = hora.plusMinutes(30);
        }
    }
    
    public LocalTime getHoraFin() { 
        return horaFin; 
    }
    
    public void setHoraFin(LocalTime horaFin) { 
        this.horaFin = horaFin; 
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
    
    public String getCodigoTurno() { 
        return codigoTurno; 
    }
    
    public void setCodigoTurno(String codigoTurno) { 
        this.codigoTurno = codigoTurno; 
    }
    
    @Override
    public String toString() {
        return "Turno{" +
                "idTurno=" + idTurno +
                ", fecha=" + fecha +
                ", hora=" + hora +
                ", horaFin=" + horaFin +
                ", paciente=" + (paciente != null ? paciente.getNombre() + " " + paciente.getApellido() : "null") +
                ", medico=" + (medico != null ? medico.getNombre() + " " + medico.getApellido() : "null") +
                ", estudio=" + (estudio != null ? estudio.getNombre() : "null") +
                ", estado='" + estado + '\'' +
                ", codigoTurno='" + codigoTurno + '\'' +
                '}';
    }
}