package com.dpi.primeraapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario_estudio")
public class UsuarioEstudio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_estudio", nullable = false)
    private Estudio estudio;

    // Constructores
    public UsuarioEstudio() {}

    public UsuarioEstudio(Usuario usuario, Estudio estudio) {
        this.usuario = usuario;
        this.estudio = estudio;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Estudio getEstudio() { return estudio; }
    public void setEstudio(Estudio estudio) { this.estudio = estudio; }
}