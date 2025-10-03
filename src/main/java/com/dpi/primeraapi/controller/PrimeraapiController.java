package com.dpi.primeraapi.controller;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.repository.UsuarioRepository;

import jakarta.validation.Valid;

@Controller
public class PrimeraapiController {
    private final UsuarioRepository usuarioRepository;

    public PrimeraapiController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/dpi")
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "formulario";
    }

    @PostMapping("/dpi")
    public String procesarRegistro(
        @Valid @ModelAttribute("usuario") Usuario usuario,
        BindingResult bindingResult,
        @RequestParam String confirm_password,
        Model model
    ) {
        // Validar que las contraseñas coincidan
        if (!usuario.getPassword().equals(confirm_password)) {
            model.addAttribute("errorPassword", "Las contraseñas no coinciden");
            return cargarModeloConErrores(usuario, model);
        }

        // Validar fecha de nacimiento manualmente
        if (usuario.getFechaNacimiento() != null) {
            LocalDate hoy = LocalDate.now();
            
            // Validar que sea fecha pasada
            if (!usuario.getFechaNacimiento().isBefore(hoy)) {
                bindingResult.rejectValue("fechaNacimiento", "error.fecha", "La fecha debe ser en el pasado");
            }
            
            // Validar edad mínima (18 años)
            Period periodo = Period.between(usuario.getFechaNacimiento(), hoy);
            if (periodo.getYears() < 18) {
                bindingResult.rejectValue("fechaNacimiento", "error.edad", "Debe ser mayor de 18 años");
            }
            
            // Validar edad máxima razonable (120 años)
            if (periodo.getYears() > 120) {
                bindingResult.rejectValue("fechaNacimiento", "error.edad", "Fecha de nacimiento no válida");
            }
        }

        // Verificar si hay errores de validación de la entidad
        if (bindingResult.hasErrors()) {
            return cargarModeloConErrores(usuario, model);
        }

        // Validar que el DNI no exista
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            model.addAttribute("errorDni", "El DNI ya está registrado");
            return cargarModeloConErrores(usuario, model);
        }

        try {
            // Guardar el usuario
            usuarioRepository.save(usuario);
            return "redirect:/menu"; // Redirigir al menú después del registro exitoso
        } catch (Exception e) {
            model.addAttribute("errorGeneral", "Error al registrar el usuario: " + e.getMessage());
            return cargarModeloConErrores(usuario, model);
        }
    }

    // Método auxiliar para recargar el modelo con errores
    private String cargarModeloConErrores(Usuario usuario, Model model) {
        // Mantener los valores en el formulario para que no se pierdan
        model.addAttribute("usuario", usuario);
        return "formulario";
    }

    // Home / Menu
    @GetMapping({"/menu"})
    public String menu() {
        return "menu";
    }

    @GetMapping("/calendario")
    public String calendario() {
        return "calendario";
    }

    @GetMapping("/confirmacionturno")
    public String confirmacionTurno() {
        return "confirmacionturno";
    }

    @GetMapping("/estudio")
    public String estudio() {
        return "estudio";
    }

    @GetMapping("/formulario")
    public String formulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "formulario";
    }

    @GetMapping("/horarios")
    public String horarios() {
        return "horarios";
    }

    @GetMapping({"","/login"})
    public String login() {
        return "login"; 
    }

    
    @GetMapping("/pedirturno")
    public String pedirTurno() {
        return "pedirturno"; 
    }

    @GetMapping("/resultado")
    public String resultado() {
        return "resultado";
    }

    @GetMapping("/verturnos")
    public String verTurnos() {
        return "verturnos";
    }
}