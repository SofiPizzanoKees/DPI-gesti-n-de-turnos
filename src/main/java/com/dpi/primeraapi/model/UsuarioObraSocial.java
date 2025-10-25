package com.dpi.primeraapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario_obrasocial")
public class UsuarioObraSocial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_obrasocial", nullable = false)
    private ObraSocial obraSocial;

    // Constructores
    public UsuarioObraSocial() {}

    public UsuarioObraSocial(Usuario usuario, ObraSocial obraSocial) {
        this.usuario = usuario;
        this.obraSocial = obraSocial;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public ObraSocial getObraSocial() { return obraSocial; }
    public void setObraSocial(ObraSocial obraSocial) { this.obraSocial = obraSocial; }
}