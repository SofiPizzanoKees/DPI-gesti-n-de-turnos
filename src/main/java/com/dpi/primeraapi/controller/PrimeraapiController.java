package com.dpi.primeraapi.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dpi.primeraapi.model.Registro;
import com.dpi.primeraapi.repository.RegistroRepository;

@Controller
public class PrimeraapiController {
    private final RegistroRepository registroRepository;

    public PrimeraapiController(RegistroRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    @GetMapping("/dpi")
    public String dpi(){
        return "formulario";
    }
    @PostMapping("/dpi")
    public String procesarLogin(
        @RequestParam("dni") String dni,
        @RequestParam("nombre") String nombre,
        @RequestParam("apellido") String apellido,
        @RequestParam("telefono") String telefono,
        @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
        @RequestParam("email") String email,
        @RequestParam("obrasocial") String obrasocial,
        Model model
    ) {
        // Aca tengo todo ya en variables
        registroRepository.guardarUsuario(dni, nombre, apellido, telefono);
        System.out.println("Nombre: " + nombre);
        System.out.println("Apellido: " + apellido);
        System.out.println("Fecha: " + fecha);
        List<Registro> usuarios = registroRepository.obtenerUsuarios();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("dni", dni);
        model.addAttribute("nombre", nombre);
        model.addAttribute("apellido", apellido);
        model.addAttribute("telefono", telefono);
        model.addAttribute("fecha", fecha);
        model.addAttribute("email", email);
        model.addAttribute("obrasocial", obrasocial);
        // Lógica de validación o guardado
        // Por ejemplo, redirigir a otra página
        return "resultado"; // templates/resultado.html
    }
}
