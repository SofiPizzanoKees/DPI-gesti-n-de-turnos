package com.dpi.primeraapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.repository.UsuarioRepository;

import jakarta.validation.Valid;

@Controller
public class PrimeraapiController {
    private final UsuarioRepository UsuarioRepository;

    public PrimeraapiController(UsuarioRepository UsuarioRepository) {
        this.UsuarioRepository = UsuarioRepository;
    }

    @GetMapping("/dpi")
    public String dpi(){
        return "formulario";
    }
    @PostMapping("/dpi")
    public String procesarLogin(
        @Valid Usuario usuario,
        BindingResult bindingResult,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
        return "formulario"; // Volver al formulario si hay errores
    }
    UsuarioRepository.save(usuario); // Guardar en DB

    model.addAttribute("usuario", usuario); 
    model.addAttribute("usuarios", UsuarioRepository.findAll());

    return "resultado";
    }

         // Home / Menu
    @GetMapping({"/", "/menu"})
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
    public String formulario() {
        return "formulario";
    }

    @GetMapping("/horarios")
    public String horarios() {
        return "horarios";
    }

    // Note: template file name contains a space: "log in.html".
    // It's recommended to rename it to "login.html" and update this method to return "login".
    @GetMapping("/login")
    public String login() {
        return "login"; // matches the existing filename `log in.html`
    }

    // Template file has a dot in its name: pedir.turno.html
    // Consider renaming to pedir-turno.html and return "pedir-turno" instead.
    @GetMapping("/pedir-turno")
    public String pedirTurno() {
        return "pedirturno"; // matches existing filename `pedir.turno.html`
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
