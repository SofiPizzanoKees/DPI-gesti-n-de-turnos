package com.dpi.primeraapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

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
        return "log in"; // matches the existing filename `log in.html`
    }

    // Template file has a dot in its name: pedir.turno.html
    // Consider renaming to pedir-turno.html and return "pedir-turno" instead.
    @GetMapping("/pedir-turno")
    public String pedirTurno() {
        return "pedir.turno"; // matches existing filename `pedir.turno.html`
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
