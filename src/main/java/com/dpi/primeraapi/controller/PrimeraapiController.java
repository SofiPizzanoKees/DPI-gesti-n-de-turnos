package com.dpi.primeraapi.controller;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.model.UsuarioObraSocial;
import com.dpi.primeraapi.repository.ObraSocialRepository;
import com.dpi.primeraapi.repository.UsuarioObraSocialRepository;
import com.dpi.primeraapi.repository.UsuarioRepository;
import com.dpi.primeraapi.service.PasswordEncoderService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class PrimeraapiController {
    private final UsuarioRepository usuarioRepository;
    private final ObraSocialRepository obraSocialRepository;
    private final UsuarioObraSocialRepository usuarioObraSocialRepository;
    private final PasswordEncoderService passwordEncoder;

    public PrimeraapiController(UsuarioRepository usuarioRepository, 
                               ObraSocialRepository obraSocialRepository,
                               UsuarioObraSocialRepository usuarioObraSocialRepository,
                               PasswordEncoderService passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.obraSocialRepository = obraSocialRepository;
        this.usuarioObraSocialRepository = usuarioObraSocialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ========== PÁGINAS BÁSICAS ==========
    
    @GetMapping({"", "/", "/login"})
    public String login() {
        return "login"; 
    }

    @GetMapping("/menu")
    public String menu() {
        return "menu";
    }
    @GetMapping("/menuAdmin")
    public String menuAdmin() {
        return "menuAdmin";
    }
     @GetMapping("/editarEstudio")
    public String editarEstudio() {
        return "editarEstudio";
    }
      @GetMapping("/menuMedico")
    public String menuMedico() {
        return "menuMedico";
    }
     @GetMapping("/especialistas")
    public String especialistas() {
        return "especialistas";
    }
    @GetMapping("/turnoMedico")
    public String turnoMedico() {
        return "turnoMedico";
    }
    @GetMapping("/horarioMedico")
    public String horarioMedico() {
        return "horarioMedico";
    }
    @GetMapping("/quienessomos")
    public String quienessomos() {
        return "quienessomos";
    }
     @GetMapping("/perfilMedico")
    public String perfilMedico() {
        return "perfilMedico";
    }
    
    @GetMapping("/miperfil")
    public String miperfil() {
        return "miperfil";
    }

    @GetMapping("/recuperar")
    public String recuperar(Model model) {
        return "recuperar";
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

    // ========== REGISTRO DE USUARIOS ==========
    
    @GetMapping("/dpi")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        cargarObrasSociales(model);
        return "formulario";
    }

    @PostMapping("/dpi")
    public String procesarRegistroPaciente(
        @Valid @ModelAttribute("usuario") Usuario usuario,
        BindingResult bindingResult,
        @RequestParam String confirm_password,
        @RequestParam Long obraSocialId,
        Model model
    ) {
        cargarObrasSociales(model);

        // Validaciones de contraseña
        if (!validarContrasena(usuario.getPassword(), confirm_password, model)) {
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        // Validar obra social
        if (obraSocialId == null) {
            model.addAttribute("errorObraSocial", "Debe seleccionar una obra social");
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        // Validar fecha
        validarFechaNacimiento(usuario.getFechaNacimiento(), bindingResult);

        if (bindingResult.hasErrors()) {
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        if (usuarioRepository.existsByDni(usuario.getDni())) {
            model.addAttribute("errorDni", "El DNI ya está registrado");
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        try {
            // Guardar usuario
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setRol("PACIENTE");
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            
            // Asignar obra social
            asignarObraSocial(usuarioGuardado, obraSocialId);
            
            return "redirect:/menu";
        } catch (Exception e) {
            model.addAttribute("errorGeneral", "Error al registrar el usuario: " + e.getMessage());
            return cargarModeloConErrores(usuario, model, "formulario");
        }
    }

    @GetMapping("/registroAdmin")
    public String mostrarFormularioAdmin(Model model) {
        model.addAttribute("usuario", new Usuario());
        cargarObrasSociales(model);
        return "registroAdmin";
    }

    @PostMapping("/registroAdmin")
    public String procesarRegistroAdmin(
        @Valid @ModelAttribute("usuario") Usuario usuario,
        BindingResult bindingResult,
        @RequestParam(required = false) Long obraSocialId,
        @RequestParam(required = false) List<Long> obrasSocialesIds,
        Model model
    ) {
        cargarObrasSociales(model);

        // Establecer contraseña automática como DNI
        if (usuario.getDni() != null && !usuario.getDni().isBlank()) {
            usuario.setPassword(usuario.getDni());
        } else {
            model.addAttribute("errorGeneral", "El DNI es obligatorio");
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        }

        // Validaciones básicas
        validarFechaNacimiento(usuario.getFechaNacimiento(), bindingResult);
        
        // Validaciones específicas por rol
        String rol = usuario.getRol();
        if ("MEDICO".equals(rol)) {
            validarDatosMedico(usuario, bindingResult);
        } else if ("PACIENTE".equals(rol)) {
            if (obraSocialId == null) {
                model.addAttribute("errorObraSocial", "Debe seleccionar una obra social para el paciente");
                return cargarModeloConErrores(usuario, model, "registroAdmin");
            }
        } else {
            // Limpiar campos de médico para otros roles
            limpiarCamposMedico(usuario);
        }

        if (bindingResult.hasErrors()) {
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        }

        // Verificar duplicados
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            model.addAttribute("errorDni", "El DNI ya está registrado");
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            model.addAttribute("errorEmail", "El email ya está registrado");
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        }

        try {
            // Guardar usuario
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setEstado(true);
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            
            // Asignaciones por rol
            if ("PACIENTE".equals(rol) && obraSocialId != null) {
                asignarObraSocial(usuarioGuardado, obraSocialId);
            } else if ("MEDICO".equals(rol) && obrasSocialesIds != null) {
                asignarObrasSocialesMedico(usuarioGuardado, obrasSocialesIds);
            }
            
            return "redirect:/menu?registroExitoso=true";
        } catch (Exception e) {
            model.addAttribute("errorGeneral", "Error al registrar el usuario: " + e.getMessage());
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        }
    }

    // ========== LOGIN ==========

    @PostMapping("/login")
    public String procesarLogin(
        @RequestParam String dni,
        @RequestParam String password,
        HttpSession session,
        Model model
    ) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByDni(dni);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            boolean passwordValido = passwordEncoder.matches(password, usuario.getPassword());
            
            if (passwordValido) {
                if (!usuario.isEstado()) {
                    model.addAttribute("error", "Tu cuenta está inactiva.");
                    return "login";
                }
                // Guardar usuario en sesión
                session.setAttribute("usuarioLogueado", usuario);
                return "redirect:/menu";
            }
        }
        
        model.addAttribute("error", "DNI o contraseña incorrectos");
        return "login";
    }

    // ========== RECUPERACIÓN DE CONTRASEÑA ==========

    @PostMapping("/recuperarCodigo")
    public String procesarRecuperacion(@RequestParam String email, 
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        
        // Validar formato de email básico
        if (email == null || email.isBlank() || !email.contains("@")) {
            redirectAttributes.addFlashAttribute("error", "Por favor, ingrese un email válido");
            return "redirect:/recuperar";
        }
        
        // Verificar si el email existe en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (!usuarioOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró una cuenta asociada a ese email. Por favor, verifique el email ingresado.");
            return "redirect:/recuperar";
        }
        
        // Aquí normalmente enviarías un código real por email
        // Por ahora, guardamos el código "0000" en sesión
        session.setAttribute("codigoRecuperacion", "0000");
        session.setAttribute("emailRecuperacion", email);
        
        // Redirigir a la página de verificación de código
        return "redirect:/recuperarCodigo?email=" + email;
    }
    @GetMapping("/recuperarCodigo")
    public String mostrarRecuperarCodigo(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "recuperarCodigo";
    }

    @PostMapping("/verificarCodigo")
    public String verificarCodigo(@RequestParam String email,
                                @RequestParam String codigo,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        String codigoGuardado = (String) session.getAttribute("codigoRecuperacion");
        String emailGuardado = (String) session.getAttribute("emailRecuperacion");
        
        if (emailGuardado == null || !emailGuardado.equals(email) || 
            codigoGuardado == null || !codigoGuardado.equals(codigo)) {
            redirectAttributes.addFlashAttribute("error", "Código incorrecto");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/recuperarCodigo?email=" + email;
        }
        
        return "redirect:/cambiarContrasena";
    }

    @GetMapping("/cambiarContrasena")
    public String cambiarContrasena(HttpSession session) {
        if (session.getAttribute("emailRecuperacion") == null) {
            return "redirect:/recuperar";
        }
        return "cambiarContrasena";
    }

    @PostMapping("/cambiarContrasena")
    public String procesarCambioContrasena(
        @RequestParam String nuevaPassword,
        @RequestParam String confirmarPassword,
        HttpSession session,
        RedirectAttributes redirectAttributes) {
        
        try {
            String email = (String) session.getAttribute("emailRecuperacion");
            if (email == null) {
                return "redirect:/recuperar";
            }
            
            // Validaciones
            if (!nuevaPassword.equals(confirmarPassword)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                return "redirect:/cambiarContrasena";
            }
            
            if (nuevaPassword.length() < 6 || nuevaPassword.length() > 16) {
                redirectAttributes.addFlashAttribute("error", "La contraseña debe tener entre 6 y 16 caracteres");
                return "redirect:/cambiarContrasena";
            }
            
            // Actualizar contraseña
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                usuario.setPassword(passwordEncoder.encode(nuevaPassword));
                usuarioRepository.save(usuario);
                
                session.removeAttribute("emailRecuperacion");
                session.removeAttribute("codigoRecuperacion");
                
                redirectAttributes.addFlashAttribute("mensaje", "Contraseña cambiada exitosamente");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "No se encontró usuario");
                return "redirect:/cambiarContrasena";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar contraseña");
            return "redirect:/cambiarContrasena";
        }
    }

    // ========== MÉTODOS AUXILIARES PRIVADOS ==========

    private void cargarObrasSociales(Model model) {
        model.addAttribute("obrasSociales", obraSocialRepository.findByActivoTrue());
    }

    private boolean validarContrasena(String password, String confirmPassword, Model model) {
        if (password == null || password.isBlank()) {
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            return false;
        }
        if (password.length() < 6 || password.length() > 16) {
            model.addAttribute("errorPassword", "La contraseña debe tener entre 6 y 16 caracteres");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorPassword", "Las contraseñas no coinciden");
            return false;
        }
        return true;
    }

    private void validarFechaNacimiento(LocalDate fechaNacimiento, BindingResult bindingResult) {
        if (fechaNacimiento != null) {
            LocalDate hoy = LocalDate.now();
            
            if (!fechaNacimiento.isBefore(hoy)) {
                bindingResult.rejectValue("fechaNacimiento", "error.fecha", "La fecha debe ser en el pasado");
            }
            
            Period periodo = Period.between(fechaNacimiento, hoy);
            if (periodo.getYears() < 18) {
                bindingResult.rejectValue("fechaNacimiento", "error.edad", "Debe ser mayor de 18 años");
            }
        }
    }

    private void validarDatosMedico(Usuario usuario, BindingResult bindingResult) {
        if (usuario.getMatriculaNacional() == null || usuario.getMatriculaNacional().isBlank()) {
            bindingResult.rejectValue("matriculaNacional", "error.matricula", "La matrícula nacional es obligatoria");
        }
        if (usuario.getMatriculaProvincial() == null || usuario.getMatriculaProvincial().isBlank()) {
            bindingResult.rejectValue("matriculaProvincial", "error.matricula", "La matrícula provincial es obligatoria");
        }
        if (usuario.getEspecialidad() == null || usuario.getEspecialidad().isBlank()) {
            bindingResult.rejectValue("especialidad", "error.especialidad", "La especialidad es obligatoria");
        }
        
        // Validar duplicados de matrícula
        if (usuario.getMatriculaNacional() != null && 
            usuarioRepository.existsByMatriculaNacional(usuario.getMatriculaNacional())) {
            bindingResult.rejectValue("matriculaNacional", "error.matricula.duplicada", 
                                    "La matrícula nacional ya está registrada");
        }
        
        if (usuario.getMatriculaProvincial() != null && 
            usuarioRepository.existsByMatriculaProvincial(usuario.getMatriculaProvincial())) {
            bindingResult.rejectValue("matriculaProvincial", "error.matricula.duplicada", 
                                    "La matrícula provincial ya está registrada");
        }
    }

    private void limpiarCamposMedico(Usuario usuario) {
        usuario.setMatriculaNacional(null);
        usuario.setMatriculaProvincial(null);
        usuario.setEspecialidad(null);
        usuario.setRealizaEstudios(null);
        usuario.setTipoEstudios(null);
    }

    private void asignarObraSocial(Usuario usuario, Long obraSocialId) {
        obraSocialRepository.findById(obraSocialId).ifPresent(obraSocial -> {
            UsuarioObraSocial usuarioObraSocial = new UsuarioObraSocial(usuario, obraSocial);
            usuarioObraSocialRepository.save(usuarioObraSocial);
        });
    }

    private void asignarObrasSocialesMedico(Usuario medico, List<Long> obrasSocialesIds) {
        for (Long obraSocialId : obrasSocialesIds) {
            asignarObraSocial(medico, obraSocialId);
        }
    }

    private String cargarModeloConErrores(Usuario usuario, Model model, String vista) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Arrays.asList("ADMIN", "MEDICO", "SECRETARIO", "PACIENTE"));
        cargarObrasSociales(model);
        return vista;
    }

    // ========== MÉTODO PARA CREAR MÉDICO DE PRUEBA ==========
    
    @GetMapping("/crear-medico-prueba")
    @ResponseBody
    public String crearMedicoPrueba() {
        try {
            if (!usuarioRepository.existsByDni("30000000")) {
                Usuario medico = new Usuario();
                medico.setDni("30000000");
                medico.setNombre("Carlos");
                medico.setApellido("López");
                medico.setEmail("carlos@clinica.com");
                medico.setPassword(passwordEncoder.encode("medico123"));
                medico.setFechaNacimiento(LocalDate.of(1980, 1, 1));
                medico.setRol("MEDICO");
                medico.setEstado(true);
                medico.setEspecialidad("Radiología");
                medico.setMatriculaNacional("123456");
                medico.setMatriculaProvincial("654321");
                
                usuarioRepository.save(medico);
                return "Médico de prueba creado: Carlos López - DNI: 30000000";
            }
            return "El médico de prueba ya existe";
        } catch (Exception e) {
            return "Error creando médico: " + e.getMessage();
        }
    }
    @RestController
    public class HealthController {
        
        @Autowired
        private DataSource dataSource;
        
        @GetMapping("/health")
        public String healthCheck() {
            try (Connection conn = dataSource.getConnection()) {
                return "Conexión a BD exitosa!";
            } catch (Exception e) {
                return "Error conectando a BD: " + e.getMessage();
            }
        }
    }
}