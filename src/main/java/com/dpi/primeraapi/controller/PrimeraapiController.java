package com.dpi.primeraapi.controller;


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dpi.primeraapi.model.DisponibilidadMedica;
import com.dpi.primeraapi.model.Estudio;
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
import com.dpi.primeraapi.service.PasswordEncoderService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class PrimeraapiController {
    private final UsuarioRepository usuarioRepository;
    private final ObraSocialRepository obraSocialRepository;
    private final UsuarioObraSocialRepository usuarioObraSocialRepository;
    private final PasswordEncoderService passwordEncoder;
 // NUEVAS DEPENDENCIAS AGREGADAS
    private final DisponibilidadMedicaRepository disponibilidadMedicaRepository;
    private final EstudioRepository estudioRepository;
    private final UsuarioEstudioRepository usuarioEstudioRepository;
// CONSTRUCTOR ACTUALIZADO CON LAS NUEVAS DEPENDENCIAS
    public PrimeraapiController(UsuarioRepository usuarioRepository, 
                                ObraSocialRepository obraSocialRepository,
                                UsuarioObraSocialRepository usuarioObraSocialRepository,
                                PasswordEncoderService passwordEncoder,
                                DisponibilidadMedicaRepository disponibilidadMedicaRepository,        // NUEVO
                                DisponibilidadExcepcionalRepository disponibilidadExcepcionalRepository, // NUEVO  
                                BloqueoHorarioRepository bloqueoHorarioRepository,
                                EstudioRepository estudioRepository,                    // NUEVO
                                UsuarioEstudioRepository usuarioEstudioRepository) {    // NUEVO
        this.usuarioRepository = usuarioRepository;
        this.obraSocialRepository = obraSocialRepository;
        this.usuarioObraSocialRepository = usuarioObraSocialRepository;
        this.passwordEncoder = passwordEncoder;
        this.disponibilidadMedicaRepository = disponibilidadMedicaRepository;      // NUEVO
        this.estudioRepository = estudioRepository;
        this.usuarioEstudioRepository = usuarioEstudioRepository;
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
    Model model
) {
    cargarObrasSociales(model);
    
    // NUEVO: Cargar estudios para el formulario en caso de error
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