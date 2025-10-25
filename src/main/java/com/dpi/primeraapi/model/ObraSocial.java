package com.dpi.primeraapi.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "obras_sociales")
public class ObraSocial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_obrasocial")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    // Relación con usuarios
    @OneToMany(mappedBy = "obraSocial", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UsuarioObraSocial> usuarios = new ArrayList<>();

    // Constructores
    public ObraSocial() {}

    public ObraSocial(String nombre) {
        this.nombre = nombre;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public List<UsuarioObraSocial> getUsuarios() { return usuarios; }
    public void setUsuarios(List<UsuarioObraSocial> usuarios) { this.usuarios = usuarios; }

    @Override
    public String toString() {
        return "ObraSocial{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                '}';
    }
}