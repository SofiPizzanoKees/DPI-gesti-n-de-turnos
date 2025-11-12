package com.dpi.primeraapi.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.repository.UsuarioRepository;
import com.dpi.primeraapi.service.PasswordEncoderService;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoderService passwordEncoder;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoderService passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        crearAdminPorDefecto();
    }

    private void crearAdminPorDefecto() {
        String adminDni = "00000000";
        
        // Verificar si el admin ya existe
        if (!usuarioRepository.existsByDni(adminDni)) {
            try {
                Usuario admin = new Usuario();
                admin.setDni(adminDni);
                admin.setNombre("Administrador");
                admin.setApellido("Sistema");
                admin.setTelefono("0000000000");
                admin.setEmail("admin@clinica.com");
                admin.setFechaNacimiento(java.time.LocalDate.of(1990, 1, 1));
                admin.setPassword(passwordEncoder.encode("admin123")); // Contraseña por defecto
                admin.setRol("ADMIN");
                admin.setEstado(true);
                
                // Limpiar campos de médico (no aplicables para admin)
                admin.setMatriculaNacional(null);
                admin.setMatriculaProvincial(null);
                admin.setEspecialidad(null);
                admin.setRealizaEstudios(null);
                admin.setTipoEstudios(null);
                
                usuarioRepository.save(admin);
                
                System.out.println("✅ Usuario ADMIN creado automáticamente");
                System.out.println("📋 Datos de acceso:");
                System.out.println("   DNI: " + adminDni);
                System.out.println("   Contraseña: admin123");
                System.out.println("   Rol: ADMIN");
                
            } catch (Exception e) {
                System.err.println("❌ Error creando usuario admin: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ℹ️  Usuario ADMIN ya existe en el sistema");
        }
    }
}