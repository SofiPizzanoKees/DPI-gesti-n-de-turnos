package com.dpi.primeraapi.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estudios")
public class Estudio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudio")
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    private String descripcion;
    
    @Column(nullable = false)
    private boolean activo = true;

    // Relación con usuarios (médicos que realizan este estudio)
    @OneToMany(mappedBy = "estudio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UsuarioEstudio> usuarios = new ArrayList<>();
    
    // Constructores
    public Estudio() {}
    
    public Estudio(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public List<UsuarioEstudio> getUsuarios() { return usuarios; }
    public void setUsuarios(List<UsuarioEstudio> usuarios) { this.usuarios = usuarios; }
}