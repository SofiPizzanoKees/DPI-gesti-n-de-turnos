package com.dpi.primeraapi.controller;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional; 

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.repository.UsuarioRepository;
import com.dpi.primeraapi.service.PasswordEncoderService;

import jakarta.validation.Valid;

@Controller
public class PrimeraapiController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoderService passwordEncoder;

    // Inyectar ambos servicios en el constructor
    public PrimeraapiController(UsuarioRepository usuarioRepository, 
                               PasswordEncoderService passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        if (usuario.getPassword().length() < 6 || usuario.getPassword().length() > 16) {
            model.addAttribute("errorPassword", "La contraseña debe tener entre 6 y 16 caracteres");
            return cargarModeloConErrores(usuario, model, "formulario");
        }
        // Validar que las contraseñas coincidan
        if (!usuario.getPassword().equals(confirm_password)) {
            model.addAttribute("errorPassword", "Las contraseñas no coinciden");
            return cargarModeloConErrores(usuario, model, "formulario");
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
            
            // Validar edad máxima razonable (100 años)
            if (periodo.getYears() > 100) {
                bindingResult.rejectValue("fechaNacimiento", "error.edad", "Fecha de nacimiento no válida");
            }
        }

        // Verificar si hay errores de validación de la entidad
        if (bindingResult.hasErrors()) {
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        // Validar que el DNI no exista
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            model.addAttribute("errorDni", "El DNI ya está registrado");
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        try {
            // 🔐 ENCRIPTAR LA CONTRASEÑA ANTES DE GUARDAR
            String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
            usuario.setPassword(passwordEncriptada);
            
            // Guardar el usuario con la contraseña encriptada
            usuarioRepository.save(usuario);
            return "redirect:/menu"; // Redirigir al menú después del registro exitoso
        } catch (Exception e) {
            model.addAttribute("errorGeneral", "Error al registrar el usuario: " + e.getMessage());
            return cargarModeloConErrores(usuario, model, "formulario");
        }
    }

    //Lo mismo para registroAdmin
    @GetMapping("/registroAdmin")
    public String mostrarFormularioAdmin(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registroAdmin";
    }

    @PostMapping("/registroAdmin")
    public String procesarRegistroAdmin(
        @Valid @ModelAttribute("usuario") Usuario usuario,
        BindingResult bindingResult,
        Model model
    ) {

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            usuario.setPassword("admin123");
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
            
            // Validar edad máxima razonable (100 años)
            if (periodo.getYears() > 100) {
                bindingResult.rejectValue("fechaNacimiento", "error.edad", "Fecha de nacimiento no válida");
            }
        }

        // Validar campos específicos según el rol
        String rol = usuario.getRol();
        if ("MEDICO".equals(rol)) {
            // Validar campos obligatorios para médico
            if (usuario.getMatriculaNacional() == null || usuario.getMatriculaNacional().isBlank()) {
                bindingResult.rejectValue("matriculaNacional", "error.matricula", "La matrícula nacional es obligatoria para médicos");
            }
            if (usuario.getMatriculaProvincial() == null || usuario.getMatriculaProvincial().isBlank()) {
                bindingResult.rejectValue("matriculaProvincial", "error.matricula", "La matrícula provincial es obligatoria para médicos");
            }
            if (usuario.getEspecialidad() == null || usuario.getEspecialidad().isBlank()) {
                bindingResult.rejectValue("especialidad", "error.especialidad", "La especialidad es obligatoria para médicos");
            }
            
            // ✅ NUEVA VALIDACIÓN: Formato de matrículas (6 dígitos exactos)
            if (usuario.getMatriculaNacional() != null && !usuario.getMatriculaNacional().isBlank()) {
                if (!usuario.getMatriculaNacional().matches("\\d{6}")) {
                    bindingResult.rejectValue("matriculaNacional", "error.matricula.formato", 
                                            "La matrícula nacional debe tener exactamente 6 dígitos");
                } else {
                    // ✅ Validar que la matrícula nacional no esté duplicada
                    if (usuarioRepository.existsByMatriculaNacional(usuario.getMatriculaNacional())) {
                        bindingResult.rejectValue("matriculaNacional", "error.matricula.duplicada", 
                                                "La matrícula nacional ya está registrada");
                    }
                }
            }
            
            if (usuario.getMatriculaProvincial() != null && !usuario.getMatriculaProvincial().isBlank()) {
                if (!usuario.getMatriculaProvincial().matches("\\d{6}")) {
                    bindingResult.rejectValue("matriculaProvincial", "error.matricula.formato", 
                                            "La matrícula provincial debe tener exactamente 6 dígitos");
                } else {
                    // ✅ Validar que la matrícula provincial no esté duplicada
                    if (usuarioRepository.existsByMatriculaProvincial(usuario.getMatriculaProvincial())) {
                        bindingResult.rejectValue("matriculaProvincial", "error.matricula.duplicada", 
                                                "La matrícula provincial ya está registrada");
                    }
                }
            }
            
        } else if ("PACIENTE".equals(rol)) {
            // Validar campos obligatorios para paciente
            if (usuario.getObraSocial() == null || usuario.getObraSocial().isBlank()) {
                bindingResult.rejectValue("obraSocial", "error.obraSocial", "La obra social es obligatoria para pacientes");
            }
        }

        // Verificar si hay errores de validación de la entidad
        if (bindingResult.hasErrors()) {
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        }

        // Validar que el DNI no exista
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            model.addAttribute("errorDni", "El DNI ya está registrado");
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        }

        // Validar que el email no exista (si es necesario)
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            model.addAttribute("errorEmail", "El email ya está registrado");
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        }

        try {
            // 🔐 ENCRIPTAR LA CONTRASEÑA ANTES DE GUARDAR
            String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
            usuario.setPassword(passwordEncriptada);
            
            // Asegurar que el estado esté activo
            usuario.setEstado(true);
            
            // Guardar el usuario con la contraseña encriptada
            usuarioRepository.save(usuario);
            return "redirect:/menu?registroExitoso=true"; // Redirigir al menú después del registro exitoso
        } catch (Exception e) {
            model.addAttribute("errorGeneral", "Error al registrar el usuario: " + e.getMessage());
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        }
    }

    // Método auxiliar para recargar el modelo con errores
    private String cargarModeloConErrores(Usuario usuario, Model model, String vista) {
        // Mantener los valores en el formulario para que no se pierdan
        model.addAttribute("usuario", usuario);
        
        model.addAttribute("roles", java.util.List.of("ADMIN", "MEDICO", "SECRETARIO", "PACIENTE"));
        
        return vista;
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

     @GetMapping("/recuperar")
    public String recuperar() {
        return "recuperar";
    }
      @GetMapping("/cambiarContraseña")
    public String cambiarContraseña() {
        return "cambiarContraseña";
    }
      @GetMapping("/recuperarCodigo")
    public String recuperarCodigo() {
        return "recuperarCodigo";
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

    @PostMapping({"","/login"})
    public String procesarLogin(
            @RequestParam String dni,
            @RequestParam String password,
            Model model
    ) {
        // Buscar usuario por DNI
        Optional<Usuario> usuarioOpt = usuarioRepository.findByDni(dni);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get(); // ← OBTENER EL USUARIO DEL OPTIONAL
            
            // 🔐 VERIFICAR CONTRASEÑA ENCRIPTADA
            boolean passwordValido = passwordEncoder.matches(password, usuario.getPassword());
            
            if (passwordValido) {
                // Si el usuario está inactivo, no permitir ingreso
                if (!usuario.isEstado()) {
                    model.addAttribute("error", "Tu cuenta está inactiva.");
                    return "login";
                }

                // Si todo está bien, redirigir al menú
                model.addAttribute("usuario", usuario);
                return "redirect:/menu";
            }
        }
        
        // Si no encuentra usuario o la contraseña no coincide
        model.addAttribute("error", "DNI o contraseña incorrectos");
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