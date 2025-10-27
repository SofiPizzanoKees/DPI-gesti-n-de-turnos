package com.dpi.primeraapi.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
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

import com.dpi.primeraapi.model.ObraSocial;
import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.model.UsuarioObraSocial;
import com.dpi.primeraapi.repository.ObraSocialRepository;
import com.dpi.primeraapi.repository.UsuarioObraSocialRepository;
import com.dpi.primeraapi.repository.UsuarioRepository;
import com.dpi.primeraapi.service.PasswordEncoderService;

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

    // ========== REGISTRO DE USUARIOS ==========
    
    @GetMapping("/dpi")
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        
        // Cargar obras sociales disponibles
        List<ObraSocial> obrasSociales = obraSocialRepository.findByActivoTrue();
        model.addAttribute("obrasSociales", obrasSociales);
        
        return "formulario";
    }

@PostMapping("/dpi")
    public String procesarRegistro(
        @Valid @ModelAttribute("usuario") Usuario usuario,
        BindingResult bindingResult,
        @RequestParam String confirm_password,
        @RequestParam(required = false) Long obraSocialId, // Nuevo parámetro para la obra social seleccionada
        Model model
    ) {
        // Cargar obras sociales para el caso de error
        List<ObraSocial> obrasSociales = obraSocialRepository.findByActivoTrue();
        model.addAttribute("obrasSociales", obrasSociales);

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            model.addAttribute("errorPassword", "La contraseña es obligatoria");
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        if (usuario.getPassword().length() < 6 || usuario.getPassword().length() > 16) {
            model.addAttribute("errorPassword", "La contraseña debe tener entre 6 y 16 caracteres");
            return cargarModeloConErrores(usuario, model, "formulario");
        }
        
        if (!usuario.getPassword().equals(confirm_password)) {
            model.addAttribute("errorPassword", "Las contraseñas no coinciden");
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        // Validar obra social para pacientes
        if (obraSocialId == null) {
            model.addAttribute("errorObraSocial", "Debe seleccionar una obra social");
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        if (usuario.getFechaNacimiento() != null) {
            LocalDate hoy = LocalDate.now();
            
            if (!usuario.getFechaNacimiento().isBefore(hoy)) {
                bindingResult.rejectValue("fechaNacimiento", "error.fecha", "La fecha debe ser en el pasado");
            }
            
            Period periodo = Period.between(usuario.getFechaNacimiento(), hoy);
            if (periodo.getYears() < 18) {
                bindingResult.rejectValue("fechaNacimiento", "error.edad", "Debe ser mayor de 18 años");
            }
            
            if (periodo.getYears() > 100) {
                bindingResult.rejectValue("fechaNacimiento", "error.edad", "Fecha de nacimiento no válida");
            }
        }

        if (bindingResult.hasErrors()) {
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        if (usuarioRepository.existsByDni(usuario.getDni())) {
            model.addAttribute("errorDni", "El DNI ya está registrado");
            return cargarModeloConErrores(usuario, model, "formulario");
        }

        try {
            String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
            usuario.setPassword(passwordEncriptada);
            usuario.setRol("PACIENTE"); // Forzar rol paciente para este formulario
            
            // Guardar usuario primero
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            
            // Asignar obra social
            Optional<ObraSocial> obraSocialOpt = obraSocialRepository.findById(obraSocialId);
            if (obraSocialOpt.isPresent()) {
                UsuarioObraSocial usuarioObraSocial = new UsuarioObraSocial(usuarioGuardado, obraSocialOpt.get());
                usuarioObraSocialRepository.save(usuarioObraSocial);
            }
            
            return "redirect:/menu";
        } catch (Exception e) {
            model.addAttribute("errorGeneral", "Error al registrar el usuario: " + e.getMessage());
            return cargarModeloConErrores(usuario, model, "formulario");
        }
    }

    @GetMapping("/registroAdmin")
    public String mostrarFormularioAdmin(Model model) {
        model.addAttribute("usuario", new Usuario());
        
        // Cargar obras sociales para pacientes
        List<ObraSocial> obrasSociales = obraSocialRepository.findByActivoTrue();
        model.addAttribute("obrasSociales", obrasSociales);
        
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
        System.out.println("=== INICIANDO REGISTRO ADMIN ===");
        System.out.println("Usuario recibido: " + usuario.getNombre() + " " + usuario.getApellido());
        System.out.println("Rol: " + usuario.getRol());
        System.out.println("DNI: " + usuario.getDni());
        System.out.println("Email: " + usuario.getEmail());
        System.out.println("Fecha Nacimiento: " + usuario.getFechaNacimiento());
        System.out.println("Obra Social ID: " + obraSocialId);
        System.out.println("Obras Sociales IDs: " + obrasSocialesIds);
        
        // Cargar obras sociales para el caso de error
        List<ObraSocial> obrasSociales = obraSocialRepository.findByActivoTrue();
        model.addAttribute("obrasSociales", obrasSociales);

        // 🔐 ESTABLECER AUTOMÁTICAMENTE LA CONTRASEÑA COMO EL DNI
        String dni = usuario.getDni();
        if (dni != null && !dni.isBlank()) {
            usuario.setPassword(dni);
            System.out.println("✅ Contraseña establecida automáticamente como DNI: " + dni);
        } else {
            model.addAttribute("errorGeneral", "El DNI es obligatorio para generar la contraseña");
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        }

        // Validaciones manuales de la contraseña
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            bindingResult.rejectValue("password", "error.password", "La contraseña es obligatoria");
        } else if (usuario.getPassword().length() < 6) {
            bindingResult.rejectValue("password", "error.password", "La contraseña debe tener al menos 6 caracteres");
        }

        // Validaciones de fecha de nacimiento
        if (usuario.getFechaNacimiento() != null) {
            LocalDate hoy = LocalDate.now();
            
            if (!usuario.getFechaNacimiento().isBefore(hoy)) {
                bindingResult.rejectValue("fechaNacimiento", "error.fecha", "La fecha debe ser en el pasado");
            }
            
            Period periodo = Period.between(usuario.getFechaNacimiento(), hoy);
            if (periodo.getYears() < 18) {
                bindingResult.rejectValue("fechaNacimiento", "error.edad", "Debe ser mayor de 18 años");
            }
            
            if (periodo.getYears() > 100) {
                bindingResult.rejectValue("fechaNacimiento", "error.edad", "Fecha de nacimiento no válida");
            }
        }

        String rol = usuario.getRol();
        System.out.println("🔍 Validando rol: " + rol);
        
        // 🔄 VALIDACIONES ESPECÍFICAS SOLO PARA MÉDICOS
        if ("MEDICO".equals(rol)) {
            System.out.println("🔍 Validando datos de médico...");
            System.out.println("Matrícula Nacional: " + usuario.getMatriculaNacional());
            System.out.println("Matrícula Provincial: " + usuario.getMatriculaProvincial());
            System.out.println("Especialidad: " + usuario.getEspecialidad());
            
            if (usuario.getMatriculaNacional() == null || usuario.getMatriculaNacional().isBlank()) {
                System.out.println("❌ Matrícula nacional vacía");
                bindingResult.rejectValue("matriculaNacional", "error.matricula", "La matrícula nacional es obligatoria para médicos");
            }
            if (usuario.getMatriculaProvincial() == null || usuario.getMatriculaProvincial().isBlank()) {
                System.out.println("❌ Matrícula provincial vacía");
                bindingResult.rejectValue("matriculaProvincial", "error.matricula", "La matrícula provincial es obligatoria para médicos");
            }
            if (usuario.getEspecialidad() == null || usuario.getEspecialidad().isBlank()) {
                System.out.println("❌ Especialidad vacía");
                bindingResult.rejectValue("especialidad", "error.especialidad", "La especialidad es obligatoria para médicos");
            }
            
            if (usuario.getMatriculaNacional() != null && !usuario.getMatriculaNacional().isBlank()) {
                if (!usuario.getMatriculaNacional().matches("\\d{6}")) {
                    bindingResult.rejectValue("matriculaNacional", "error.matricula.formato", 
                                            "La matrícula nacional debe tener exactamente 6 dígitos");
                } else {
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
                    if (usuarioRepository.existsByMatriculaProvincial(usuario.getMatriculaProvincial())) {
                        bindingResult.rejectValue("matriculaProvincial", "error.matricula.duplicada", 
                                                "La matrícula provincial ya está registrada");
                    }
                }
            }
        } else {
            // ⚠️ PARA OTROS ROLES: LIMPIAR CAMPOS DE MÉDICO PARA EVITAR CONFUSIONES
            System.out.println("🔄 Limpiando campos de médico para rol: " + rol);
            usuario.setMatriculaNacional(null);
            usuario.setMatriculaProvincial(null);
            usuario.setEspecialidad(null);
            usuario.setRealizaEstudios(null);
            usuario.setTipoEstudios(null);
        }

        // 🔄 VALIDACIÓN ESPECÍFICA PARA PACIENTES
        if ("PACIENTE".equals(rol)) {
            System.out.println("🔍 Validando datos de paciente...");
            if (obraSocialId == null) {
                System.out.println("❌ Obra social no seleccionada para paciente");
                model.addAttribute("errorObraSocial", "Debe seleccionar una obra social para el paciente");
                return cargarModeloConErrores(usuario, model, "registroAdmin");
            }
        }

        // Verificar si hay errores de validación
        if (bindingResult.hasErrors()) {
            System.out.println("❌ ERRORES DE VALIDACIÓN ENCONTRADOS:");
            bindingResult.getFieldErrors().forEach(error -> {
                System.out.println(" - Campo: " + error.getField() + " -> " + error.getDefaultMessage());
            });
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        } else {
            System.out.println("✅ No hay errores de validación");
        }

        // Verificar duplicados
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            System.out.println("❌ DNI duplicado: " + usuario.getDni());
            model.addAttribute("errorDni", "El DNI ya está registrado");
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        } else {
            System.out.println("✅ DNI disponible: " + usuario.getDni());
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            System.out.println("❌ Email duplicado: " + usuario.getEmail());
            model.addAttribute("errorEmail", "El email ya está registrado. Por favor use otro email.");
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        } else {
            System.out.println("✅ Email disponible: " + usuario.getEmail());
        }

        try {
            System.out.println("💾 Intentando guardar usuario...");
            
            // 🔐 ENCRIPTAR LA CONTRASEÑA
            String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
            usuario.setPassword(passwordEncriptada);
            usuario.setEstado(true);
            
            System.out.println("🔐 Contraseña encriptada: " + passwordEncriptada.substring(0, 20) + "...");
            
            // Guardar usuario
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            System.out.println("✅ Usuario guardado con ID: " + usuarioGuardado.getId());
            
            // 🔄 ASIGNACIONES ESPECÍFICAS POR ROL
            
            // Para PACIENTES: asignar obra social (una sola)
            if ("PACIENTE".equals(rol) && obraSocialId != null) {
                Optional<ObraSocial> obraSocialOpt = obraSocialRepository.findById(obraSocialId);
                if (obraSocialOpt.isPresent()) {
                    UsuarioObraSocial usuarioObraSocial = new UsuarioObraSocial(usuarioGuardado, obraSocialOpt.get());
                    usuarioObraSocialRepository.save(usuarioObraSocial);
                    System.out.println("✅ Obra social asignada al paciente: " + obraSocialOpt.get().getNombre());
                }
            }
            
            // Para MÉDICOS: asignar obras sociales (múltiples)
            if ("MEDICO".equals(rol) && obrasSocialesIds != null && !obrasSocialesIds.isEmpty()) {
                System.out.println("🏥 Asignando " + obrasSocialesIds.size() + " obras sociales al médico");
                for (Long obraSocialIdMedico : obrasSocialesIds) {
                    Optional<ObraSocial> obraSocialOpt = obraSocialRepository.findById(obraSocialIdMedico);
                    if (obraSocialOpt.isPresent()) {
                        UsuarioObraSocial usuarioObraSocial = new UsuarioObraSocial(usuarioGuardado, obraSocialOpt.get());
                        usuarioObraSocialRepository.save(usuarioObraSocial);
                        System.out.println("✅ Obra social asignada: " + obraSocialOpt.get().getNombre());
                    }
                }
            }
            
            // Para ADMIN y SECRETARIO: no requieren asignaciones adicionales
            if ("ADMIN".equals(rol) || "SECRETARIO".equals(rol)) {
                System.out.println("✅ " + rol + " registrado sin asignaciones adicionales");
            }
            
            System.out.println("🎉 Registro completado exitosamente para: " + usuario.getNombre() + " " + usuario.getApellido() + " como " + rol);
            return "redirect:/menu?registroExitoso=true";
            
        } catch (Exception e) {
            System.out.println("💥 ERROR CRÍTICO al guardar usuario: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorGeneral", "Error al registrar el usuario: " + e.getMessage());
            return cargarModeloConErrores(usuario, model, "registroAdmin");
        }
    }

    // ========== GESTIÓN DE TURNOS ==========

    @GetMapping("/estudio")
    public String estudio(Model model) {
        List<Usuario> medicos = usuarioRepository.findByRolAndEstadoTrue("MEDICO");
        model.addAttribute("medicos", medicos);
        
        List<Map<String, String>> estudios = Arrays.asList(
            Map.of("id", "1", "nombre", "Radiografía"),
            Map.of("id", "2", "nombre", "Ecografía"),
            Map.of("id", "3", "nombre", "Análisis de Sangre"),
            Map.of("id", "4", "nombre", "Tomografía")
        );
        model.addAttribute("estudios", estudios);
        
        return "estudio";
    }

    @PostMapping("/estudio/seleccion")
    public String procesarSeleccion(
        @RequestParam Long medicoId,
        @RequestParam String estudioId,
        RedirectAttributes redirectAttributes) {
        
        try {
            redirectAttributes.addAttribute("medicoId", medicoId);
            redirectAttributes.addAttribute("estudioId", estudioId);
            return "redirect:/calendario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar selección");
            return "redirect:/estudio";
        }
    }

    @GetMapping("/calendario")
    public String calendario(
        @RequestParam(required = false) Long medicoId,
        @RequestParam(required = false) String estudioId,
        Model model) {
        
        if (medicoId == null || estudioId == null) {
            model.addAttribute("error", "Faltan datos. Por favor, seleccione médico y estudio.");
            return "calendario";
        }
        
        model.addAttribute("medicoId", medicoId);
        model.addAttribute("estudioId", estudioId);
        
        Optional<Usuario> medicoOpt = usuarioRepository.findById(medicoId);
        medicoOpt.ifPresent(medico -> {
            model.addAttribute("medicoNombre", medico.getNombre() + " " + medico.getApellido());
            model.addAttribute("medicoEspecialidad", medico.getEspecialidad());
        });
        
        return "calendario";
    }

    @GetMapping("/api/fechas-disponibles")
    @ResponseBody
    public List<LocalDate> obtenerFechasDisponibles(
        @RequestParam Long medicoId,
        @RequestParam String estudioId) {
        
        List<LocalDate> fechasDisponibles = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        
        for (int i = 1; i <= 30; i++) {
            LocalDate fecha = hoy.plusDays(i);
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY && 
                fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
                fechasDisponibles.add(fecha);
            }
        }
        
        return fechasDisponibles;
    }

    @PostMapping("/calendario/seleccionar-fecha")
    public String seleccionarFecha(
        @RequestParam Long medicoId,
        @RequestParam String estudioId,
        @RequestParam String fechaSeleccionada,
        RedirectAttributes redirectAttributes) {
        
        redirectAttributes.addAttribute("medicoId", medicoId);
        redirectAttributes.addAttribute("estudioId", estudioId);
        redirectAttributes.addAttribute("fecha", fechaSeleccionada);
        return "redirect:/horarios";
    }

    @GetMapping("/horarios")
    public String horarios(
        @RequestParam(required = false) Long medicoId,
        @RequestParam(required = false) String estudioId,
        @RequestParam(required = false) String fecha,
        Model model) {
        
        if (medicoId == null || estudioId == null || fecha == null) {
            model.addAttribute("error", "Faltan datos. Por favor, complete todos los pasos.");
            return "horarios";
        }
        
        model.addAttribute("medicoId", medicoId);
        model.addAttribute("estudioId", estudioId);
        model.addAttribute("fecha", fecha);
        
        Optional<Usuario> medicoOpt = usuarioRepository.findById(medicoId);
        medicoOpt.ifPresent(medico -> {
            model.addAttribute("medicoNombre", medico.getNombre() + " " + medico.getApellido());
        });
        
        List<String> horariosDisponibles = Arrays.asList(
            "09:00", "10:00", "11:00", "12:00", 
            "13:00", "14:00", "15:00", "16:00"
        );
        model.addAttribute("horariosDisponibles", horariosDisponibles);
        
        return "horarios";
    }

    @PostMapping("/horarios/confirmar")
    public String confirmarTurno(
        @RequestParam Long medicoId,
        @RequestParam String estudioId,
        @RequestParam String fecha,
        @RequestParam String hora,
        RedirectAttributes redirectAttributes) {
        
        try {
            String codigoTurno = "T" + System.currentTimeMillis();
            
            Optional<Usuario> medicoOpt = usuarioRepository.findById(medicoId);
            if (medicoOpt.isPresent()) {
                Usuario medico = medicoOpt.get();
                redirectAttributes.addFlashAttribute("medicoNombre", medico.getNombre() + " " + medico.getApellido());
                redirectAttributes.addFlashAttribute("especialidad", medico.getEspecialidad());
            }
            
            redirectAttributes.addFlashAttribute("codigoTurno", codigoTurno);
            redirectAttributes.addFlashAttribute("fecha", fecha);
            redirectAttributes.addFlashAttribute("hora", hora);
            redirectAttributes.addFlashAttribute("estudioId", estudioId);
            
            return "redirect:/confirmacionturno";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al confirmar el turno: " + e.getMessage());
            return "redirect:/horarios";
        }
    }

    @GetMapping("/confirmacionturno")
    public String confirmacionTurno(Model model) {
        if (!model.containsAttribute("codigoTurno")) {
            return "redirect:/estudio";
        }
        return "confirmacionturno";
    }

    // ========== RECUPERACIÓN DE CONTRASEÑA ==========

    @GetMapping("/cambiarContrasena")
    public String cambiarContrasena(Model model) {
        return "cambiarContrasena";
    }

    @PostMapping("/cambiarContrasena")
    public String procesarCambioContrasena(
        @RequestParam String dni,
        @RequestParam String nuevaPassword,
        @RequestParam String confirmarPassword,
        RedirectAttributes redirectAttributes) {
        
        try {
            // Validar que las contraseñas coincidan
            if (!nuevaPassword.equals(confirmarPassword)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                return "redirect:/cambiarContrasena";
            }
            
            // Validar longitud de contraseña
            if (nuevaPassword.length() < 6 || nuevaPassword.length() > 16) {
                redirectAttributes.addFlashAttribute("error", "La contraseña debe tener entre 6 y 16 caracteres");
                return "redirect:/cambiarContrasena";
            }
            
            // Buscar usuario por DNI o email
            Optional<Usuario> usuarioOpt = usuarioRepository.findByDni(dni);
            if (!usuarioOpt.isPresent()) {
                usuarioOpt = usuarioRepository.findByEmail(dni);
            }
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                
                // 🔐 ENCRIPTAR LA NUEVA CONTRASEÑA
                String passwordEncriptada = passwordEncoder.encode(nuevaPassword);
                usuario.setPassword(passwordEncriptada);
                
                usuarioRepository.save(usuario);
                
                redirectAttributes.addFlashAttribute("mensaje", "Contraseña cambiada exitosamente");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "No se encontró usuario con ese DNI o email");
                return "redirect:/cambiarContrasena";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar contraseña: " + e.getMessage());
            return "redirect:/cambiarContrasena";
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private String cargarModeloConErrores(Usuario usuario, Model model, String vista) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", java.util.List.of("ADMIN", "MEDICO", "SECRETARIO", "PACIENTE"));
        
        // Siempre cargar obras sociales
        List<ObraSocial> obrasSociales = obraSocialRepository.findByActivoTrue();
        model.addAttribute("obrasSociales", obrasSociales);
        
        return vista;
    }

    @GetMapping({"/menu"})
    public String menu() {
        return "menu";
    }
    
    @GetMapping("/miperfil")
    public String miperfil() {
        return "miperfil";
    }

    @GetMapping("/recuperar")
    public String recuperar() {
        return "recuperar";
    }
    
    @GetMapping("/recuperarCodigo")
    public String recuperarCodigo() {
        return "recuperarCodigo";
    }
    
    @GetMapping("/formulario")
    public String formulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        
        // Cargar obras sociales disponibles
        List<ObraSocial> obrasSociales = obraSocialRepository.findByActivoTrue();
        model.addAttribute("obrasSociales", obrasSociales);
        
        return "formulario";
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
        Optional<Usuario> usuarioOpt = usuarioRepository.findByDni(dni);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            boolean passwordValido = passwordEncoder.matches(password, usuario.getPassword());
            
            if (passwordValido) {
                if (!usuario.isEstado()) {
                    model.addAttribute("error", "Tu cuenta está inactiva.");
                    return "login";
                }
                model.addAttribute("usuario", usuario);
                return "redirect:/menu";
            }
        }
        
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
}