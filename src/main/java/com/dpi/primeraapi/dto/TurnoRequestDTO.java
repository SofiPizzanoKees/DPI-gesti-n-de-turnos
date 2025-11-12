package com.dpi.primeraapi.dto;  // Ajusta el nombre del paquete según tu proyecto

public class TurnoRequestDTO {
    private Long pacienteId;
    private Long estudioId;
    private Long medicoId;
    
    // Constructor vacío (OBLIGATORIO para Spring)
    public TurnoRequestDTO() {
    }
    
    // Constructor con parámetros
    public TurnoRequestDTO(Long pacienteId, Long estudioId, Long medicoId) {
        this.pacienteId = pacienteId;
        this.estudioId = estudioId;
        this.medicoId = medicoId;
    }
    
    // Getters y Setters (OBLIGATORIOS)
    public Long getPacienteId() {
        return pacienteId;
    }
    
    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }
    
    public Long getEstudioId() {
        return estudioId;
    }
    
    public void setEstudioId(Long estudioId) {
        this.estudioId = estudioId;
    }
    
    public Long getMedicoId() {
        return medicoId;
    }
    
    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }
    
    // Método toString para debugging (opcional)
    @Override
    public String toString() {
        return "TurnoRequestDTO{" +
                "pacienteId=" + pacienteId +
                ", estudioId=" + estudioId +
                ", medicoId=" + medicoId +
                '}';
    }
}