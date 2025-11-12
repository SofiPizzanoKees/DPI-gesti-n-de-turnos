package com.dpi.primeraapi.controller;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dpi.primeraapi.dto.TurnoRequestDTO;
import com.dpi.primeraapi.model.DisponibilidadMedica;
import com.dpi.primeraapi.model.Estudio;
import com.dpi.primeraapi.model.Turno;
import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.model.UsuarioEstudio;
import com.dpi.primeraapi.model.UsuarioObraSocial;
import com.dpi.primeraapi.model.enums.DiaSemana;
import com.dpi.primeraapi.repository.BloqueoHorarioRepository;
import com.dpi.primeraapi.repository.DisponibilidadExcepcionalRepository;
import com.dpi.primeraapi.repository.DisponibilidadMedicaRepository;
import com.dpi.primeraapi.repository.EstudioRepository;
import com.dpi.primeraapi.repository.ObraSocialRepository;
import com.dpi.primeraapi.repository.UsuarioEstudioRepository;
import com.dpi.primeraapi.repository.UsuarioObraSocialRepository;
import com.dpi.primeraapi.repository.UsuarioRepository;
import com.dpi.primeraapi.service.EmailService;
import com.dpi.primeraapi.service.PasswordEncoderService;
import com.dpi.primeraapi.service.TurnoService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class PrimeraapiController {
    private final UsuarioRepository usuarioRepository;
    private final ObraSocialRepository obraSocialRepository;
    private final UsuarioObraSocialRepository usuarioObraSocialRepository;
    private final PasswordEncoderService passwordEncoder;
    private final TurnoService turnoService;
 // NUEVAS DEPENDENCIAS AGREGADAS
    private final DisponibilidadMedicaRepository disponibilidadMedicaRepository;
    private final EstudioRepository estudioRepository;
    private final UsuarioEstudioRepository usuarioEstudioRepository;
    private final EmailService emailService;
    @Autowired
    private DisponibilidadMedicaRepository disponibilidadRepository;
    @Autowired
    private Environment env;
// CONSTRUCTOR ACTUALIZADO CON LAS NUEVAS DEPENDENCIAS
    public PrimeraapiController(UsuarioRepository usuarioRepository, 
                                ObraSocialRepository obraSocialRepository,
                                UsuarioObraSocialRepository usuarioObraSocialRepository,
                                PasswordEncoderService passwordEncoder,
                                DisponibilidadMedicaRepository disponibilidadMedicaRepository,        // NUEVO
                                DisponibilidadExcepcionalRepository disponibilidadExcepcionalRepository, // NUEVO  
                                BloqueoHorarioRepository bloqueoHorarioRepository,
                                EstudioRepository estudioRepository,                    // NUEVO
                                UsuarioEstudioRepository usuarioEstudioRepository,
                                TurnoService turnoService,
                                EmailService emailService) {    // NUEVO
        this.usuarioRepository = usuarioRepository;
        this.obraSocialRepository = obraSocialRepository;
        this.usuarioObraSocialRepository = usuarioObraSocialRepository;
        this.passwordEncoder = passwordEncoder;
        this.disponibilidadMedicaRepository = disponibilidadMedicaRepository;      // NUEVO
        this.estudioRepository = estudioRepository;
        this.usuarioEstudioRepository = usuarioEstudioRepository;
        this.turnoService = turnoService;
        this.emailService = emailService;
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
     @GetMapping("/formularioSecretaria")
    public String formularioSecretaria() {
        return "formularioSecretaria";
    }

      @GetMapping("/menuMedico")
    public String menuMedico() {
        return "menuMedico";
    }
    
    @GetMapping("/menuSecretaria")
    public String menuSecretaria() {
        return "menuSecretaria";
    }
     @GetMapping("/especialistas")
    public String especialistas() {
        return "especialistas";
    }
@GetMapping("/turnoMedico")
public String turnoMedico(HttpSession session, Model model) {
    try {
        // Obtener el médico logueado de la sesión
        Usuario medicoLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        
        if (medicoLogueado == null) {
            return "redirect:/login";
        }
        
        // Verificar que sea un médico
        if (!"MEDICO".equals(medicoLogueado.getRol())) {
            return "redirect:/menu";
        }
        
        // Obtener los turnos del médico
        List<Turno> turnos = turnoService.obtenerTurnosPorMedico(medicoLogueado);
        
        // Filtrar solo turnos futuros o del día actual
        LocalDate hoy = LocalDate.now();
        List<Turno> turnosFuturos = turnos.stream()
            .filter(turno -> !turno.getFecha().isBefore(hoy))
            .sorted((t1, t2) -> {
                int fechaCompare = t1.getFecha().compareTo(t2.getFecha());
                if (fechaCompare != 0) return fechaCompare;
                return t1.getHora().compareTo(t2.getHora());
            })
            .collect(Collectors.toList());
        
        model.addAttribute("turnos", turnosFuturos);
        model.addAttribute("medico", medicoLogueado);
        
        System.out.println("=== TURNOS DEL MÉDICO ===");
        System.out.println("Médico: " + medicoLogueado.getNombre() + " " + medicoLogueado.getApellido());
        System.out.println("Cantidad de turnos: " + turnosFuturos.size());
        
    } catch (Exception e) {
        System.out.println("Error cargando turnos del médico: " + e.getMessage());
        model.addAttribute("error", "Error al cargar los turnos: " + e.getMessage());
    }
    
    return "turnoMedico";
}
    @GetMapping("/quienessomos")
    public String quienessomos() {
        return "quienessomos";
    }
// ========== PERFIL MÉDICO - GESTIÓN DE ESTUDIOS ==========

@GetMapping("/perfilMedico")
public String mostrarPerfilMedico(HttpSession session, Model model) {
    try {
        // Obtener el médico logueado
        Usuario medicoLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        
        if (medicoLogueado == null || !"MEDICO".equals(medicoLogueado.getRol())) {
            return "redirect:/login";
        }
        
        // Obtener los estudios actuales del médico
        List<UsuarioEstudio> estudiosMedico = usuarioEstudioRepository.findByUsuario(medicoLogueado);
        List<String> nombresEstudios = estudiosMedico.stream()
                .map(ue -> ue.getEstudio().getNombre())
                .collect(Collectors.toList());
        
        // Obtener todos los estudios disponibles
        List<Estudio> todosLosEstudios = estudioRepository.findByActivoTrue();
        
        // ✅ FILTRAR: Obtener solo los estudios que NO tiene asignados
        List<Estudio> estudiosDisponibles = todosLosEstudios.stream()
                .filter(estudio -> estudiosMedico.stream()
                        .noneMatch(ue -> ue.getEstudio().getId().equals(estudio.getId())))
                .collect(Collectors.toList());
        
        // Pasar datos al modelo
        model.addAttribute("medico", medicoLogueado);
        model.addAttribute("estudiosActuales", nombresEstudios);
        model.addAttribute("todosLosEstudios", todosLosEstudios);
        model.addAttribute("estudiosDisponibles", estudiosDisponibles); // ✅ NUEVO
        
        System.out.println("=== PERFIL MÉDICO ===");
        System.out.println("Médico: " + medicoLogueado.getNombre() + " " + medicoLogueado.getApellido());
        System.out.println("Estudios actuales: " + nombresEstudios.size());
        System.out.println("Estudios disponibles para agregar: " + estudiosDisponibles.size());
        System.out.println("Todos los estudios: " + todosLosEstudios.size());
        
    } catch (Exception e) {
        System.out.println("Error cargando perfil médico: " + e.getMessage());
        model.addAttribute("error", "Error al cargar el perfil: " + e.getMessage());
    }
    
    return "perfilMedico";
}

@PostMapping("/perfilMedico/agregarEstudio")
public String agregarEstudioMedico(
    @RequestParam Long estudioId,
    HttpSession session,
    RedirectAttributes redirectAttributes) {
    
    try {
        // Obtener el médico logueado
        Usuario medicoLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        
        if (medicoLogueado == null || !"MEDICO".equals(medicoLogueado.getRol())) {
            return "redirect:/login";
        }
        
        // Buscar el estudio
        Optional<Estudio> estudioOpt = estudioRepository.findById(estudioId);
        if (!estudioOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Estudio no encontrado");
            return "redirect:/perfilMedico";
        }
        
        Estudio estudio = estudioOpt.get();
        
        // Verificar si ya tiene este estudio
        boolean yaTieneEstudio = usuarioEstudioRepository.existsByUsuarioAndEstudio(medicoLogueado, estudio);
        if (yaTieneEstudio) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes asignado este estudio: " + estudio.getNombre());
            return "redirect:/perfilMedico";
        }
        
        // Crear la relación UsuarioEstudio
        UsuarioEstudio usuarioEstudio = new UsuarioEstudio(medicoLogueado, estudio);
        usuarioEstudioRepository.save(usuarioEstudio);
        
        redirectAttributes.addFlashAttribute("success", "Estudio agregado correctamente: " + estudio.getNombre());
        
        System.out.println("✅ Estudio agregado al médico: " + estudio.getNombre());
        
    } catch (Exception e) {
        System.out.println("Error agregando estudio al médico: " + e.getMessage());
        redirectAttributes.addFlashAttribute("error", "Error al agregar el estudio: " + e.getMessage());
    }
    
    return "redirect:/perfilMedico";
}

@PostMapping("/perfilMedico/eliminarEstudio")
public String eliminarEstudioMedico(
    @RequestParam Long estudioId,
    HttpSession session,
    RedirectAttributes redirectAttributes) {
    
    try {
        // Obtener el médico logueado
        Usuario medicoLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        
        if (medicoLogueado == null || !"MEDICO".equals(medicoLogueado.getRol())) {
            return "redirect:/login";
        }
        
        // Buscar el estudio
        Optional<Estudio> estudioOpt = estudioRepository.findById(estudioId);
        if (!estudioOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Estudio no encontrado");
            return "redirect:/perfilMedico";
        }
        
        Estudio estudio = estudioOpt.get();
        
        // Buscar y eliminar la relación UsuarioEstudio
        Optional<UsuarioEstudio> usuarioEstudioOpt = usuarioEstudioRepository.findByUsuarioAndEstudio(medicoLogueado, estudio);
        
        if (usuarioEstudioOpt.isPresent()) {
            usuarioEstudioRepository.delete(usuarioEstudioOpt.get());
            redirectAttributes.addFlashAttribute("success", "Estudio eliminado correctamente: " + estudio.getNombre());
            System.out.println("✅ Estudio eliminado del médico: " + estudio.getNombre());
        } else {
            redirectAttributes.addFlashAttribute("error", "No tienes asignado este estudio");
        }
        
    } catch (Exception e) {
        System.out.println("Error eliminando estudio del médico: " + e.getMessage());
        redirectAttributes.addFlashAttribute("error", "Error al eliminar el estudio: " + e.getMessage());
    }
    
    return "redirect:/perfilMedico";
}
    
@GetMapping("/miperfil")
public String miperfil(HttpSession session, Model model) {
    try {
        // Obtener el usuario logueado de la sesión
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        
        if (usuarioLogueado == null) {
            return "redirect:/login";
        }
        
        // Obtener la obra social del usuario
        String obraSocial = "No asignada";
        List<UsuarioObraSocial> obrasSociales = usuarioObraSocialRepository.findByUsuario(usuarioLogueado);
        if (!obrasSociales.isEmpty()) {
            obraSocial = obrasSociales.get(0).getObraSocial().getNombre();
        }
        
        // Pasar los datos al modelo
        model.addAttribute("usuario", usuarioLogueado);
        model.addAttribute("obraSocial", obraSocial);
        model.addAttribute("rolUsuario", usuarioLogueado.getRol());
        
        System.out.println("=== CARGANDO PERFIL ===");
        System.out.println("Usuario: " + usuarioLogueado.getNombre() + " " + usuarioLogueado.getApellido());
        System.out.println("DNI: " + usuarioLogueado.getDni());
        System.out.println("Email: " + usuarioLogueado.getEmail());
        System.out.println("Obra Social: " + obraSocial);
        
    } catch (Exception e) {
        System.out.println("Error cargando perfil: " + e.getMessage());
        model.addAttribute("error", "Error al cargar el perfil: " + e.getMessage());
    }
    
    return "miperfil";
}

    @GetMapping("/recuperar")
    public String recuperar(Model model) {
        return "recuperar";
    }

@GetMapping("/pedirturno")
public String pedirTurno(HttpSession session) {
    // Obtener el usuario logueado
    Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
    
    if (usuarioLogueado == null) {
        return "redirect:/login";
    }
    
    String rol = usuarioLogueado.getRol();
    
    switch (rol) {
        case "SECRETARIO":
            return "redirect:/elegirPaciente";
        case "PACIENTE":
            // Solo mostrar el menú de paciente, NO iniciar el proceso automáticamente
            return "pedirturno"; // ← Esto renderiza el template pedirturno.html
        case "ADMIN":
        case "MEDICO":
            // Redirigir a su menú principal
            return "redirect:/menu" + rol;
        default:
            return "redirect:/menu";
    }
}
    @GetMapping("/inicio")
    public String inicio() {
        return "inicio"; 
    }

    @GetMapping("/resultado")
    public String resultado() {
        return "resultado";
    }

@GetMapping("/verturnos")
public String verTurnos(HttpSession session, Model model) {
    try {
        // Obtener el usuario logueado de la sesión
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        
        if (usuarioLogueado == null) {
            return "redirect:/login";
        }
        
        // Obtener los turnos del paciente
        List<Turno> turnos = turnoService.obtenerTurnosPorPaciente(usuarioLogueado);
        
        // Ordenar los turnos por fecha y hora (más recientes primero)
        turnos.sort((t1, t2) -> {
            int fechaCompare = t2.getFecha().compareTo(t1.getFecha());
            if (fechaCompare != 0) return fechaCompare;
            return t2.getHora().compareTo(t1.getHora());
        });
        
        model.addAttribute("turnos", turnos);
        model.addAttribute("paciente", usuarioLogueado);
        model.addAttribute("rolUsuario", usuarioLogueado.getRol()); // ← AGREGAR ESTA LÍNEA
        
        System.out.println("=== TURNOS ENCONTRADOS ===");
        System.out.println("Paciente: " + usuarioLogueado.getNombre() + " " + usuarioLogueado.getApellido());
        System.out.println("Cantidad de turnos: " + turnos.size());
        
    } catch (Exception e) {
        System.out.println("Error cargando turnos: " + e.getMessage());
        model.addAttribute("error", "Error al cargar los turnos: " + e.getMessage());
    }
    
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
    Model model,
    HttpSession session  // ← AGREGAR ESTE PARÁMETRO
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
        
        // ✅ GUARDAR USUARIO EN SESIÓN Y REDIRIGIR AL MENÚ CORRECTO
        session.setAttribute("usuarioLogueado", usuarioGuardado);
        return "redirect:/pedirturno";  
        
    } catch (Exception e) {
        model.addAttribute("errorGeneral", "Error al registrar el usuario: " + e.getMessage());
        return cargarModeloConErrores(usuario, model, "formulario");
    }
}

    @GetMapping("/registroAdmin")
    public String mostrarFormularioAdmin(Model model) {
        model.addAttribute("usuario", new Usuario());
        cargarObrasSociales(model);
        // NUEVO: Cargar lista de estudios activos
        try {
            List<Estudio> estudios = estudioRepository.findByActivoTrue();
            model.addAttribute("estudios", estudios);
            System.out.println("Estudios cargados para registro: " + estudios.size());
        } catch (Exception e) {
            System.out.println("Error cargando estudios: " + e.getMessage());
            model.addAttribute("estudios", new ArrayList<>());
        }
        return "registroAdmin";
    }

@PostMapping("/registroAdmin")
public String procesarRegistroAdmin(
    @Valid @ModelAttribute("usuario") Usuario usuario,
    BindingResult bindingResult,
    @RequestParam(required = false) Long obraSocialId,
    @RequestParam(required = false) List<Long> obrasSocialesIds,
    @RequestParam(required = false) List<Long> estudiosIds,  // NUEVO PARÁMETRO
    Model model,
    HttpSession session
) {
    cargarObrasSociales(model);

    try {
        List<Estudio> estudios = estudioRepository.findByActivoTrue();
        model.addAttribute("estudios", estudios);
    } catch (Exception e) {
        model.addAttribute("estudios", new ArrayList<>());
    }

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
        } else if ("MEDICO".equals(rol)) {
            // Asignar obras sociales
            if (obrasSocialesIds != null) {
                asignarObrasSocialesMedico(usuarioGuardado, obrasSocialesIds);
            }
            
            // NUEVO: Asignar estudios
            if (estudiosIds != null && !estudiosIds.isEmpty()) {
                asignarEstudiosMedico(usuarioGuardado, estudiosIds);
            }
        }
        
        return "redirect:/menuAdmin?registroExitoso=true&usuarioCreado=" + usuarioGuardado.getNombre();
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
            
            // Redirigir según el rol
            String rol = usuario.getRol();
            switch (rol) {
                case "ADMIN":
                    return "redirect:/menuAdmin";
                case "SECRETARIO":
                    return "redirect:/menuSecretaria";
                case "MEDICO":
                    return "redirect:/menuMedico";
                case "PACIENTE":
                    return "redirect:/pedirturno"; // ← Va al menú del paciente
                default:
                    return "redirect:/menu";
            }
        }
    }
    
    model.addAttribute("error", "DNI o contraseña incorrectos");
    return "login";
}
@GetMapping("/iniciarTurno")
public String iniciarTurno(HttpSession session) {
    // Obtener el usuario logueado
    Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
    
    if (usuarioLogueado == null) {
        return "redirect:/login";
    }
    
    // Solo pacientes pueden iniciar el proceso
    if ("PACIENTE".equals(usuarioLogueado.getRol())) {
        // Guardar automáticamente al paciente en sesión
        session.setAttribute("turnoPaciente", usuarioLogueado);
        return "redirect:/estudio";
    }
    
    return "redirect:/pedirturno";
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
    
    try {
        // Generar código real de 6 dígitos
        String codigoRecuperacion = generarCodigoRecuperacion();
        
        // Enviar email con SendGrid
        emailService.sendPasswordResetEmail(email, codigoRecuperacion);
        
        // Guardar en sesión
        session.setAttribute("codigoRecuperacion", codigoRecuperacion);
        session.setAttribute("emailRecuperacion", email);
        
        redirectAttributes.addFlashAttribute("success", "Se ha enviado un código de recuperación a su email");
        
    } catch (Exception e) {
        System.out.println("Error enviando email: " + e.getMessage());
        // Fallback: usar código temporal y mostrar en consola
        String codigoTemporal = "000000";
        session.setAttribute("codigoRecuperacion", codigoTemporal);
        session.setAttribute("emailRecuperacion", email);
        
        System.out.println("=== CÓDIGO DE RECUPERACIÓN (Fallback) ===");
        System.out.println("Para: " + email);
        System.out.println("Código: " + codigoTemporal);
        System.out.println("=========================================");
        
        redirectAttributes.addFlashAttribute("info", "Servicio de email temporalmente no disponible. Use el código: 000000");
    }
    
    return "redirect:/recuperarCodigo?email=" + email;
}

// Método para generar código de 6 dígitos
private String generarCodigoRecuperacion() {
    return String.valueOf((int) ((Math.random() * 900000) + 100000));
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
        
        // NUEVO: Cargar estudios también
        try {
            List<Estudio> estudios = estudioRepository.findByActivoTrue();
            model.addAttribute("estudios", estudios);
        } catch (Exception e) {
            model.addAttribute("estudios", new ArrayList<>());
        }
        
        return vista;
    }
    private void asignarEstudiosMedico(Usuario medico, List<Long> estudiosIds) {
        try {
            System.out.println("Asignando estudios al médico: " + estudiosIds.size() + " estudios");
            
            for (Long estudioId : estudiosIds) {
                Optional<Estudio> estudioOpt = estudioRepository.findById(estudioId);
                if (estudioOpt.isPresent()) {
                    Estudio estudio = estudioOpt.get();
                    
                    // Crear la relación UsuarioEstudio
                    UsuarioEstudio usuarioEstudio = new UsuarioEstudio(medico, estudio);
                    usuarioEstudioRepository.save(usuarioEstudio);
                    
                    System.out.println("Estudio asignado: " + estudio.getNombre());
                }
            }
            
            System.out.println("Total de estudios asignados: " + estudiosIds.size());
        } catch (Exception e) {
            System.out.println("Error asignando estudios: " + e.getMessage());
            throw e;
        }
    }
    // ========== NUEVO: DISPONIBILIDAD MÉDICA ==========
    
    @GetMapping("/horarioMedico")
    public String mostrarHorarioMedico(Model model) {
        try {
            // Cargar lista de médicos para el dropdown
            List<Usuario> medicos = usuarioRepository.findByRolAndEstado("MEDICO", true);
            model.addAttribute("medicos", medicos);
            System.out.println("Médicos encontrados: " + medicos.size());
        } catch (Exception e) {
            System.out.println("Error cargando médicos: " + e.getMessage());
            model.addAttribute("medicos", new ArrayList<>());
        }
        return "horarioMedico";
    }

    @PostMapping("/guardarHorarios")
    public String guardarHorariosMedico(
        @RequestParam Long medicoId,
        @RequestParam Map<String, String> allParams,
        RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("=== INICIANDO GUARDADO DE HORARIOS ===");
            System.out.println("Medico ID: " + medicoId);
            System.out.println("Parámetros recibidos: " + allParams);
            
            Optional<Usuario> medicoOpt = usuarioRepository.findById(medicoId);
            if (!medicoOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Médico no encontrado");
                return "redirect:/horarioMedico";
            }
            
            Usuario medico = medicoOpt.get();
            System.out.println("Médico: " + medico.getNombre() + " " + medico.getApellido());
            
            // Procesar cada día de la semana
            String[] dias = {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado"};
            int horariosGuardados = 0;
            
            for (String dia : dias) {
                String checkboxValue = allParams.get(dia);
                if ("on".equals(checkboxValue)) { // Si el checkbox está marcado
                    String horaInicioStr = allParams.get(dia + "-desde");
                    String horaFinStr = allParams.get(dia + "-hasta");
                    
                    System.out.println("Procesando " + dia + ": " + horaInicioStr + " - " + horaFinStr);
                    
                    if (horaInicioStr != null && horaFinStr != null && 
                        !horaInicioStr.isEmpty() && !horaFinStr.isEmpty()) {
                        
                        LocalTime horaInicio = LocalTime.parse(horaInicioStr);
                        LocalTime horaFin = LocalTime.parse(horaFinStr);
                        
                        // Validar que la hora de inicio sea antes que la de fin
                        if (horaInicio.isBefore(horaFin)) {
                            // Convertir el día a enum
                            DiaSemana diaSemana = convertirDiaStringAEnum(dia);
                            
                            // Verificar si ya existe una disponibilidad para este día
                            List<DisponibilidadMedica> existentes = disponibilidadMedicaRepository
                                .findByMedicoAndDiaSemanaAndActivoTrue(medico, diaSemana);
                            
                            // Si existe, actualizar; si no, crear nueva
                            DisponibilidadMedica disponibilidad;
                            if (!existentes.isEmpty()) {
                                disponibilidad = existentes.get(0); // Tomar el primero
                                System.out.println("Actualizando disponibilidad existente para " + dia);
                            } else {
                                disponibilidad = new DisponibilidadMedica();
                                System.out.println("Creando nueva disponibilidad para " + dia);
                            }
                            
                            disponibilidad.setMedico(medico);
                            disponibilidad.setDiaSemana(diaSemana);
                            disponibilidad.setHoraInicio(horaInicio);
                            disponibilidad.setHoraFin(horaFin);
                            disponibilidad.setDuracionTurnoMinutos(20); // 20 minutos por turno
                            disponibilidad.setActivo(true);
                            
                            disponibilidadMedicaRepository.save(disponibilidad);
                            horariosGuardados++;
                            System.out.println("Horario guardado para " + dia);
                        } else {
                            System.out.println("Horario inválido para " + dia + ": " + horaInicio + " - " + horaFin);
                        }
                    }
                }
            }
            
            System.out.println("Total de horarios guardados: " + horariosGuardados);
            redirectAttributes.addFlashAttribute("success", 
                "Horarios guardados exitosamente. " + horariosGuardados + " días configurados.");
            
        } catch (Exception e) {
            System.out.println("ERROR al guardar horarios: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar horarios: " + e.getMessage());
        }
        
        return "redirect:/horarioMedico";
    }

    // Método auxiliar para convertir string a enum
    private DiaSemana convertirDiaStringAEnum(String dia) {
        switch (dia.toLowerCase()) {
            case "lunes": return DiaSemana.LUNES;
            case "martes": return DiaSemana.MARTES;
            case "miercoles": return DiaSemana.MIERCOLES;
            case "jueves": return DiaSemana.JUEVES;
            case "viernes": return DiaSemana.VIERNES;
            case "sabado": return DiaSemana.SABADO;
            default: return DiaSemana.LUNES;
        }
    }
    // ========== SELECCIÓN DE PACIENTE PARA TURNO ==========

    @GetMapping("/elegirPaciente")
    public String mostrarElegirPaciente(Model model) {
        try {
            // Cargar lista de pacientes activos
            List<Usuario> pacientes = usuarioRepository.findByRolAndEstado("PACIENTE", true);
            model.addAttribute("pacientes", pacientes);
            System.out.println("Pacientes encontrados: " + pacientes.size());
        } catch (Exception e) {
            System.out.println("Error cargando pacientes: " + e.getMessage());
            model.addAttribute("pacientes", new ArrayList<>());
        }
        return "elegirPaciente";
    }

    @PostMapping("/seleccionarPaciente")
    public String seleccionarPaciente(
        @RequestParam Long pacienteId,
        HttpSession session,
        RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("Seleccionando paciente ID: " + pacienteId);
            
            Optional<Usuario> pacienteOpt = usuarioRepository.findById(pacienteId);
            if (!pacienteOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Paciente no encontrado");
                return "redirect:/elegirPaciente";
            }
            
            Usuario paciente = pacienteOpt.get();
            
            // Guardar el paciente seleccionado en la sesión
            session.setAttribute("pacienteSeleccionado", paciente);
            session.setAttribute("pacienteId", paciente.getId());
            
            System.out.println("Paciente seleccionado: " + paciente.getNombre() + " " + paciente.getApellido());
            
            // Redirigir a la página de estudios
            return "redirect:/estudio";
            
        } catch (Exception e) {
            System.out.println("Error seleccionando paciente: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al seleccionar el paciente: " + e.getMessage());
            return "redirect:/elegirPaciente";
        }
    }

@GetMapping("/estudio")
public String mostrarEstudio(HttpSession session, Model model) {
    try {
        // Para SECRETARIOS: Recuperar el paciente de la sesión
        // Para PACIENTES: Usar el usuario logueado automáticamente
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        Usuario paciente = (Usuario) session.getAttribute("turnoPaciente");
        
        // Si es PACIENTE y no hay paciente seleccionado en sesión, usar el usuario logueado
        if (usuarioLogueado != null && "PACIENTE".equals(usuarioLogueado.getRol()) && paciente == null) {
            paciente = usuarioLogueado;
            session.setAttribute("turnoPaciente", paciente);
        }
        
        if (paciente == null) {
            System.out.println("No hay paciente seleccionado en sesión");
            // Si es SECRETARIO, volver a elegir paciente
            if (usuarioLogueado != null && "SECRETARIO".equals(usuarioLogueado.getRol())) {
                return "redirect:/elegirPaciente";
            }
            return "redirect:/pedirturno";
        }
        
        // Cargar datos para la página de estudio
        model.addAttribute("paciente", paciente);
        
        // Cargar lista de estudios disponibles
        List<Estudio> estudios = estudioRepository.findByActivoTrue();
        model.addAttribute("estudios", estudios);
        
        // Cargar lista de médicos disponibles
        List<Usuario> medicos = usuarioRepository.findByRolAndEstado("MEDICO", true);
        model.addAttribute("medicos", medicos);
        
        // Pasar el rol del usuario para mostrar/ocultar elementos en el template
        model.addAttribute("rolUsuario", usuarioLogueado != null ? usuarioLogueado.getRol() : "");
        
        System.out.println("Cargando estudio para paciente: " + paciente.getNombre() + " " + paciente.getApellido());
        System.out.println("Rol del usuario: " + (usuarioLogueado != null ? usuarioLogueado.getRol() : "No logueado"));
        System.out.println("Estudios disponibles: " + estudios.size());
        System.out.println("Médicos disponibles: " + medicos.size());
        
    } catch (Exception e) {
        System.out.println("Error cargando página de estudio: " + e.getMessage());
        model.addAttribute("error", "Error al cargar la página: " + e.getMessage());
    }
    
    return "estudio";
}
@PostMapping("/estudio/continuar")
public String procesarEstudio(@ModelAttribute TurnoRequestDTO turnoRequest, 
                            HttpSession session, 
                            Model model) {
    try {
        System.out.println("Procesando turno - Estudio: " + turnoRequest.getEstudioId() + 
                        ", Médico: " + turnoRequest.getMedicoId());
        
        // Validar que todos los datos estén presentes
        if (turnoRequest.getEstudioId() == null || 
            turnoRequest.getMedicoId() == null) {
            model.addAttribute("error", "Todos los campos son obligatorios");
            return "redirect:/estudio";
        }
        
        // Obtener el paciente de la sesión
        Usuario paciente = (Usuario) session.getAttribute("turnoPaciente");
        if (paciente == null) {
            model.addAttribute("error", "No se encontró el paciente");
            return "redirect:/estudio";
        }
        
        // Buscar las entidades completas
        Estudio estudio = estudioRepository.findById(turnoRequest.getEstudioId())
                .orElseThrow(() -> new RuntimeException("Estudio no encontrado"));
        
        Usuario medico = usuarioRepository.findById(turnoRequest.getMedicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
        
        // Guardar en sesión para usar en calendario
        session.setAttribute("turnoPaciente", paciente);
        session.setAttribute("turnoEstudio", estudio);
        session.setAttribute("turnoMedico", medico);
        
        // Log para debugging
        System.out.println("Datos guardados en sesión:");
        System.out.println("Paciente: " + paciente.getNombre() + " " + paciente.getApellido());
        System.out.println("Estudio: " + estudio.getNombre());
        System.out.println("Médico: " + medico.getNombre() + " " + medico.getApellido());
        
        return "redirect:/calendario";
        
    } catch (Exception e) {
        System.out.println("Error procesando estudio: " + e.getMessage());
        model.addAttribute("error", "Error al procesar la solicitud: " + e.getMessage());
        return "redirect:/estudio";
    }
}

@GetMapping("/calendario")
public String mostrarCalendario(HttpSession session, Model model) {
    try {
        // Recuperar datos de la sesión
        Usuario paciente = (Usuario) session.getAttribute("turnoPaciente");
        Estudio estudio = (Estudio) session.getAttribute("turnoEstudio");
        Usuario medico = (Usuario) session.getAttribute("turnoMedico");
        
        // Validar que todos los datos estén presentes
        if (paciente == null || estudio == null || medico == null) {
            System.out.println("Faltan datos para mostrar el calendario");
            return "redirect:/estudio";
        }
        
        // Obtener la disponibilidad del médico
        List<DisponibilidadMedica> disponibilidad = disponibilidadRepository.findByMedicoAndActivoTrue(medico);
        
        // Pasar datos al modelo
        model.addAttribute("paciente", paciente);
        model.addAttribute("estudio", estudio);
        model.addAttribute("medico", medico);
        
        // SOLUCIÓN SIMPLE: Usar directamente los números de los días
        List<Integer> diasDisponiblesNumeros = new ArrayList<>();
        
        for (DisponibilidadMedica disp : disponibilidad) {
            switch (disp.getDiaSemana()) {
                case LUNES: 
                    diasDisponiblesNumeros.add(1); // 1 = Lunes en JavaScript
                    System.out.println("✅ Agregado LUNES -> 1");
                    break;
                case MARTES: 
                    diasDisponiblesNumeros.add(2); 
                    break;
                case MIERCOLES: 
                    diasDisponiblesNumeros.add(3); 
                    break;
                case JUEVES: 
                    diasDisponiblesNumeros.add(4); 
                    break;
                case VIERNES: 
                    diasDisponiblesNumeros.add(5); 
                    break;
                case SABADO: 
                    diasDisponiblesNumeros.add(6); 
                    break;
                case DOMINGO: 
                    diasDisponiblesNumeros.add(0); // 0 = Domingo en JavaScript
                    break;
            }
        }
        
        // Remover duplicados
        diasDisponiblesNumeros = diasDisponiblesNumeros.stream()
                .distinct()
                .collect(Collectors.toList());
        
        model.addAttribute("diasDisponibles", diasDisponiblesNumeros);
        
        // Depuración
        System.out.println("Días disponibles para " + medico.getNombre() + ": " + diasDisponiblesNumeros);
        
    } catch (Exception e) {
        System.out.println("Error cargando calendario: " + e.getMessage());
        model.addAttribute("error", "Error al cargar el calendario: " + e.getMessage());
    }
    
    return "calendario";
}
@PostMapping("/calendario/continuar")
public String procesarCalendario(@RequestParam String fechaSeleccionada,
                                HttpSession session,
                                Model model) {
    try {
        // Recuperar datos de la sesión
        Usuario paciente = (Usuario) session.getAttribute("turnoPaciente");
        Estudio estudio = (Estudio) session.getAttribute("turnoEstudio");
        Usuario medico = (Usuario) session.getAttribute("turnoMedico");
        
        if (paciente == null || estudio == null || medico == null) {
            return "redirect:/estudio";
        }
        
        // Guardar la fecha seleccionada en la sesión
        session.setAttribute("fechaSeleccionada", fechaSeleccionada);
        
        System.out.println("Fecha seleccionada: " + fechaSeleccionada);
        System.out.println("Redirigiendo a horarios...");
        
        return "redirect:/horarios";
        
    } catch (Exception e) {
        System.out.println("Error procesando calendario: " + e.getMessage());
        model.addAttribute("error", "Error al procesar la fecha: " + e.getMessage());
        return "redirect:/calendario";
    }
}
@GetMapping("/horarios")
public String mostrarHorarios(HttpSession session, Model model) {
    try {
        // Recuperar todos los datos de la sesión
        Usuario paciente = (Usuario) session.getAttribute("turnoPaciente");
        Estudio estudio = (Estudio) session.getAttribute("turnoEstudio");
        Usuario medico = (Usuario) session.getAttribute("turnoMedico");
        String fechaSeleccionada = (String) session.getAttribute("fechaSeleccionada");
        
        if (paciente == null || estudio == null || medico == null || fechaSeleccionada == null) {
            return "redirect:/estudio";
        }
        
        // Convertir la fecha seleccionada a LocalDate
        LocalDate fecha = LocalDate.parse(fechaSeleccionada);
        
        // ✅ USAR EL SERVICIO PARA OBTENER SOLO HORARIOS DISPONIBLES
        List<String> horariosDisponibles = turnoService.obtenerHorariosDisponibles(medico, fecha);
        
        // Obtener el día de la semana para mostrar
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        DiaSemana diaSemanaEnum = convertirDayOfWeekADiaSemana(diaSemana);
        
        // Pasar todos los datos al modelo
        model.addAttribute("paciente", paciente);
        model.addAttribute("estudio", estudio);
        model.addAttribute("medico", medico);
        model.addAttribute("fechaSeleccionada", fechaSeleccionada);
        model.addAttribute("horariosDisponibles", horariosDisponibles);
        model.addAttribute("diaSemana", diaSemanaEnum.toString());
        
        System.out.println("=== DATOS PARA HORARIOS ===");
        System.out.println("Paciente: " + paciente.getNombre() + " " + paciente.getApellido());
        System.out.println("Estudio: " + estudio.getNombre());
        System.out.println("Médico: " + medico.getNombre() + " " + medico.getApellido());
        System.out.println("Fecha: " + fechaSeleccionada);
        System.out.println("Día de la semana: " + diaSemanaEnum);
        System.out.println("Horarios disponibles: " + horariosDisponibles.size());
        System.out.println("Horarios: " + horariosDisponibles);
        
    } catch (Exception e) {
        System.out.println("Error cargando horarios: " + e.getMessage());
        model.addAttribute("error", "Error al cargar los horarios: " + e.getMessage());
        return "redirect:/calendario";
    }
    
    return "horarios";
}

// Método para convertir DayOfWeek a DiaSemana
private DiaSemana convertirDayOfWeekADiaSemana(DayOfWeek dayOfWeek) {
    switch (dayOfWeek) {
        case MONDAY: return DiaSemana.LUNES;
        case TUESDAY: return DiaSemana.MARTES;
        case WEDNESDAY: return DiaSemana.MIERCOLES;
        case THURSDAY: return DiaSemana.JUEVES;
        case FRIDAY: return DiaSemana.VIERNES;
        case SATURDAY: return DiaSemana.SABADO;
        case SUNDAY: return DiaSemana.DOMINGO;
        default: return DiaSemana.LUNES;
    }
}

// Método para generar horarios disponibles
private List<String> generarHorariosDisponibles(LocalTime horaInicio, LocalTime horaFin, Integer duracionTurno) {
    List<String> horarios = new ArrayList<>();
    LocalTime horaActual = horaInicio;
    
    while (horaActual.plusMinutes(duracionTurno).isBefore(horaFin) || 
           horaActual.plusMinutes(duracionTurno).equals(horaFin)) {
        horarios.add(horaActual.toString());
        horaActual = horaActual.plusMinutes(duracionTurno);
    }
    
    return horarios;
}
@PostMapping("/horarios/confirmar")
public String confirmarHorario(@RequestParam String fechaSeleccionada,
                              @RequestParam String horarioSeleccionado,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
    try {
        // Recuperar datos de la sesión
        Usuario paciente = (Usuario) session.getAttribute("turnoPaciente");
        Estudio estudio = (Estudio) session.getAttribute("turnoEstudio");
        Usuario medico = (Usuario) session.getAttribute("turnoMedico");
        
        if (paciente == null || estudio == null || medico == null) {
            redirectAttributes.addFlashAttribute("error", "Faltan datos del turno. Por favor, comience nuevamente.");
            return "redirect:/estudio";
        }
        
        // Convertir fecha y hora
        LocalDate fecha = LocalDate.parse(fechaSeleccionada);
        LocalTime hora = LocalTime.parse(horarioSeleccionado);
        
        System.out.println("=== CREANDO TURNO ===");
        System.out.println("Paciente: " + paciente.getNombre() + " " + paciente.getApellido());
        System.out.println("Estudio: " + estudio.getNombre());
        System.out.println("Descripción: " + estudio.getDescripcion());
        System.out.println("Médico: " + medico.getNombre() + " " + medico.getApellido());
        System.out.println("Fecha: " + fecha);
        System.out.println("Horario: " + hora);
        
        // Crear y guardar el turno en la base de datos
        Turno turnoGuardado = turnoService.crearTurno(paciente, medico, estudio, fecha, hora);
        
        System.out.println("✅ Turno guardado exitosamente. ID: " + turnoGuardado.getIdTurno());
        System.out.println("Código de turno: " + turnoGuardado.getCodigoTurno());
        
        // ENVIAR EMAIL DE CONFIRMACIÓN CON ESTUDIO Y DESCRIPCIÓN
        try {
            emailService.sendAppointmentConfirmation(
                paciente.getEmail(),
                paciente.getNombre() + " " + paciente.getApellido(),
                fecha.toString(),
                hora.toString(),
                medico.getNombre() + " " + medico.getApellido(),
                estudio.getNombre(),
                estudio.getDescripcion()  // ← NUEVO PARÁMETRO
            );
            System.out.println("✅ Email de confirmación enviado a: " + paciente.getEmail());
        } catch (Exception e) {
            System.out.println("⚠️  Email no enviado, pero turno creado. Error: " + e.getMessage());
            // No falla el proceso si el email no se envía
        }
        
        // Guardar el turno creado en sesión para mostrar en confirmación
        session.setAttribute("turnoCreado", turnoGuardado);
        session.setAttribute("horarioSeleccionado", horarioSeleccionado);
        session.setAttribute("fechaSeleccionada", fechaSeleccionada);
        
        return "redirect:/confirmacionturno";
        
    } catch (Exception e) {
        System.out.println("❌ Error confirmando horario: " + e.getMessage());
        redirectAttributes.addFlashAttribute("error", "Error al confirmar el turno: " + e.getMessage());
        return "redirect:/horarios";
    }
}
@GetMapping("/confirmacionturno")
public String mostrarConfirmacionTurno(HttpSession session, Model model) {
    try {
        // Recuperar el turno creado de la sesión
        Turno turno = (Turno) session.getAttribute("turnoCreado");
        
        if (turno == null) {
            return "redirect:/estudio";
        }
        
        // Pasar los datos al modelo para mostrar en la página de confirmación
        model.addAttribute("turno", turno);
        model.addAttribute("paciente", turno.getPaciente());
        model.addAttribute("estudio", turno.getEstudio());
        model.addAttribute("medico", turno.getMedico());
        model.addAttribute("fecha", turno.getFecha());
        model.addAttribute("horario", turno.getHora());
        model.addAttribute("codigoTurno", turno.getCodigoTurno());
        
        System.out.println("=== MOSTRANDO CONFIRMACIÓN ===");
        System.out.println("Turno confirmado - Código: " + turno.getCodigoTurno());
        System.out.println("Paciente: " + turno.getPaciente().getNombre() + " " + turno.getPaciente().getApellido());
        System.out.println("Estudio: " + turno.getEstudio().getNombre());
        System.out.println("Médico: " + turno.getMedico().getNombre() + " " + turno.getMedico().getApellido());
        System.out.println("Fecha: " + turno.getFecha());
        System.out.println("Horario: " + turno.getHora());
        
        // Limpiar la sesión después de mostrar la confirmación
        session.removeAttribute("turnoCreado");
        session.removeAttribute("turnoPaciente");
        session.removeAttribute("turnoEstudio");
        session.removeAttribute("turnoMedico");
        session.removeAttribute("fechaSeleccionada");
        session.removeAttribute("horarioSeleccionado");
        
        return "confirmacionturno";
        
    } catch (Exception e) {
        System.out.println("Error cargando confirmación: " + e.getMessage());
        model.addAttribute("error", "Error al cargar la confirmación: " + e.getMessage());
        return "redirect:/horarios";
    }
}
    // ========== MÉTODO DEBUG PARA VER MÉDICOS ==========
    
    @GetMapping("/debug/medicos")
    @ResponseBody
    public String debugMedicos() {
        try {
            List<Usuario> medicos = usuarioRepository.findByRolAndEstado("MEDICO", true);
            StringBuilder result = new StringBuilder();
            result.append("Médicos encontrados: ").append(medicos.size()).append("<br>");
            for (Usuario medico : medicos) {
                result.append("ID: ").append(medico.getId())
                      .append(" - ").append(medico.getApellido())
                      .append(", ").append(medico.getNombre())
                      .append(" - ").append(medico.getEspecialidad())
                      .append("<br>");
            }
            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    @GetMapping("/debug/email-config")
    @ResponseBody
    public String debugEmailConfig() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== CONFIGURACIÓN EMAIL ===<br>");
            
            // Obtener perfiles activos
            String[] activeProfiles = env.getActiveProfiles();
            result.append("Perfil activo: ").append(activeProfiles.length > 0 ? activeProfiles[0] : "default").append("<br>");
            
            // Obtener propiedades
            String apiKey = env.getProperty("spring.sendgrid.api-key");
            result.append("API Key configurada: ").append(apiKey != null).append("<br>");
            result.append("Longitud API Key: ").append(apiKey != null ? apiKey.length() : 0).append("<br>");
            result.append("From Email: ").append(env.getProperty("app.email.from")).append("<br>");
            result.append("From Name: ").append(env.getProperty("app.email.from-name")).append("<br>");
            
            // Verificar si es una clave real
            boolean isRealKey = apiKey != null && 
                            apiKey.startsWith("SG.") && 
                            apiKey.length() > 40 &&
                            !apiKey.equals("fake-key-for-local-dev");
            result.append("Es clave real: ").append(isRealKey).append("<br>");
            
            // Mostrar primeros y últimos caracteres de la API key (sin mostrar completa por seguridad)
            if (apiKey != null && apiKey.length() > 10) {
                String maskedKey = apiKey.substring(0, 5) + "..." + apiKey.substring(apiKey.length() - 5);
                result.append("API Key (masked): ").append(maskedKey).append("<br>");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    @GetMapping("/test-email")
    @ResponseBody
    public String testEmail() {
        try {
            // Test email de recuperación
            emailService.sendPasswordResetEmail("test@example.com", "123456");
            
            // Test email de confirmación de turno
            emailService.sendAppointmentConfirmation(
                "test@example.com", 
                "Paciente Test", 
                "2024-01-15", 
                "10:00", 
                "Dr. Test", 
                "Ecografía Test", 
                "Estudio de prueba"
            );
            
            return "✅ Emails de prueba enviados. Revisa los logs en Render para ver el resultado.";
        } catch (Exception e) {
            return "❌ Error enviando emails de prueba: " + e.getMessage();
        }
    }
// ========== GESTIÓN DE ESTUDIOS ==========

    @GetMapping("/editarEstudio")
    public String mostrarGestionEstudios(Model model) {
        try {
            // Cargar lista de estudios activos
            List<Estudio> estudios = estudioRepository.findByActivoTrue();
            model.addAttribute("estudios", estudios);
            System.out.println("Estudios encontrados: " + estudios.size());
        } catch (Exception e) {
            System.out.println("Error cargando estudios: " + e.getMessage());
            model.addAttribute("estudios", new ArrayList<>());
        }
        return "editarEstudio";
    }

    @PostMapping("/agregarEstudio")
    public String agregarEstudio(
        @RequestParam String nombre,
        @RequestParam String descripcion,
        RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("Agregando estudio: " + nombre);
            
            // Validar que el nombre no esté vacío
            if (nombre == null || nombre.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El nombre del estudio es obligatorio");
                return "redirect:/editarEstudio";
            }
            
            // Verificar si ya existe un estudio con el mismo nombre
            if (estudioRepository.existsByNombreAndActivoTrue(nombre)) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un estudio con ese nombre");
                return "redirect:/editarEstudio";
            }
            
            // Crear y guardar el nuevo estudio
            Estudio estudio = new Estudio();
            estudio.setNombre(nombre.trim());
            estudio.setDescripcion(descripcion != null ? descripcion.trim() : "");
            estudio.setActivo(true);
            
            estudioRepository.save(estudio);
            
            redirectAttributes.addFlashAttribute("success", "Estudio agregado correctamente: " + nombre);
            
        } catch (Exception e) {
            System.out.println("Error agregando estudio: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al agregar el estudio: " + e.getMessage());
        }
        
        return "redirect:/editarEstudio";
    }

    @PostMapping("/eliminarEstudio")
    public String eliminarEstudio(
        @RequestParam Long estudioId,
        RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("Eliminando estudio ID: " + estudioId);
            
            Optional<Estudio> estudioOpt = estudioRepository.findById(estudioId);
            if (!estudioOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Estudio no encontrado");
                return "redirect:/editarEstudio";
            }
            
            Estudio estudio = estudioOpt.get();
            
            // Verificar si el estudio está siendo usado por algún médico
            List<UsuarioEstudio> usuariosConEstudio = usuarioEstudioRepository.findByEstudio(estudio);
            if (!usuariosConEstudio.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "No se puede eliminar el estudio '" + estudio.getNombre() + 
                    "' porque está asignado a " + usuariosConEstudio.size() + " médico(s)");
                return "redirect:/editarEstudio";
            }
            
            // Marcar como inactivo en lugar de eliminar (borrado lógico)
            estudio.setActivo(false);
            estudioRepository.save(estudio);
            
            redirectAttributes.addFlashAttribute("success", "Estudio eliminado correctamente: " + estudio.getNombre());
            
        } catch (Exception e) {
            System.out.println("Error eliminando estudio: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el estudio: " + e.getMessage());
        }
        
        return "redirect:/editarEstudio";
    }
    // ========== MÉTODO PARA CREAR MÉDICO DE PRUEBA ==========
    
    @GetMapping("/crear-medico-test")
    @ResponseBody
    public String crearMedicoTest() {
        try {
            Usuario medico = new Usuario();
            medico.setDni("40000000");
            medico.setNombre("Laura");
            medico.setApellido("Gómez");
            medico.setEmail("laura@clinica.com");
            medico.setPassword(passwordEncoder.encode("medico123"));
            medico.setFechaNacimiento(LocalDate.of(1985, 5, 15));
            medico.setRol("MEDICO");
            medico.setEstado(true);
            medico.setEspecialidad("Cardiología");
            medico.setMatriculaNacional("789012");
            medico.setMatriculaProvincial("210987");
            
            usuarioRepository.save(medico);
            return "Médico de prueba creado: Laura Gómez";
        } catch (Exception e) {
            return "Error creando médico: " + e.getMessage();
        }
    }
}