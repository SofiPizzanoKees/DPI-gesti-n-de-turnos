package com.dpi.primeraapi.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long id;

    @Column(name = "dni", unique = true, nullable = false, length = 8)
    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 dígitos")
    private String dni;
    
    @Column(name = "nombre", nullable = false, length = 30)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 30, message = "El nombre debe tener entre 2 y 30 caracteres")
    @Pattern(regexp = "^[A-Za-zÁáÉéÍíÓóÚúÑñ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 30)
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 30, message = "El apellido debe tener entre 2 y 30 caracteres")
    @Pattern(regexp = "^[A-Za-zÁáÉéÍíÓóÚúÑñ\\s]+$", message = "El apellido solo puede contener letras y espacios")
    private String apellido;

    @Column(name = "telefono", length = 10)
    @Size(min = 10, max = 10, message = "El teléfono debe tener exactamente 10 dígitos")
    @Pattern(regexp = "\\d{10}", message = "El teléfono debe contener solo números")
    private String telefono;

    @Column(name = "email")
    @Email(message = "Debe ser un email válido")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    private String email;

    @Column(name = "fecha_nacimiento")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    @Column(name = "password", nullable = false, length = 255)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Column(name = "rol", nullable = false)
    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "^(PACIENTE|ADMIN|MEDICO|SECRETARIO)$", message = "El rol debe ser PACIENTE, ADMIN, MEDICO o SECRETARIO")
    private String rol = "PACIENTE";

    @Column(name = "estado", nullable = false)
    private boolean estado = true;

    @Column(name = "matricula_nacional")
    private String matriculaNacional;
    
    @Column(name = "matricula_provincial")
    private String matriculaProvincial;
    
    @Column(name = "especialidad")
    private String especialidad;
    
    @Column(name = "realiza_estudios")
    private String realizaEstudios;
    
    @Column(name = "tipo_estudios")
    private String tipoEstudios;

    // Relación con obras sociales
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UsuarioObraSocial> obrasSociales = new ArrayList<>();

    // Relación con estudios que realiza (para médicos)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UsuarioEstudio> estudiosRealizados = new ArrayList<>();

    // Constructores
    public Usuario() {}

    public Usuario(String dni, String nombre, String apellido, String telefono, 
                   String email, LocalDate fechaNacimiento, String password) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.password = password;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public String getMatriculaNacional() { return matriculaNacional; }
    public void setMatriculaNacional(String matriculaNacional) { this.matriculaNacional = matriculaNacional; }

    public String getMatriculaProvincial() { return matriculaProvincial; }
    public void setMatriculaProvincial(String matriculaProvincial) { this.matriculaProvincial = matriculaProvincial; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getRealizaEstudios() { return realizaEstudios; }
    public void setRealizaEstudios(String realizaEstudios) { this.realizaEstudios = realizaEstudios; }

    public String getTipoEstudios() { return tipoEstudios; }
    public void setTipoEstudios(String tipoEstudios) { this.tipoEstudios = tipoEstudios; }

    public List<UsuarioObraSocial> getObrasSociales() { return obrasSociales; }
    public void setObrasSociales(List<UsuarioObraSocial> obrasSociales) { this.obrasSociales = obrasSociales; }

    public List<UsuarioEstudio> getEstudiosRealizados() { return estudiosRealizados; }
    public void setEstudiosRealizados(List<UsuarioEstudio> estudiosRealizados) { this.estudiosRealizados = estudiosRealizados; }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", dni='" + dni + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", rol='" + rol + '\'' +
                ", estado=" + estado +
                '}';
    }
}