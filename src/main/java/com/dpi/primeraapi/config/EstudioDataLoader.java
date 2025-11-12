package com.dpi.primeraapi.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.dpi.primeraapi.model.Estudio;
import com.dpi.primeraapi.repository.EstudioRepository;

@Component
public class EstudioDataLoader implements CommandLineRunner {

    @Autowired
    private EstudioRepository estudioRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== INICIANDO CARGA DE ESTUDIOS POR DEFECTO ===");
        
        // Verificar si ya existen estudios activos
        List<Estudio> estudiosActivos = estudioRepository.findByActivoTrue();
        
        if (estudiosActivos.isEmpty()) {
            System.out.println("No hay estudios activos. Cargando estudios por defecto...");
            cargarEstudiosPorDefecto();
        } else {
            System.out.println("Ya existen " + estudiosActivos.size() + " estudios activos. No se cargan estudios por defecto.");
            // Opcional: Mostrar qué estudios existen
            estudiosActivos.forEach(estudio -> 
                System.out.println(" - " + estudio.getNombre())
            );
        }
    }

    private void cargarEstudiosPorDefecto() {
        try {
            List<Estudio> estudios = Arrays.asList(
                new Estudio("Ecografía Prenatal", 
                    "Monitoreo del crecimiento y desarrollo del feto. " +
                    "Preparación: Primer trimestre - vejiga llena. Segundo y tercer trimestre - sin preparación especial."),
                
                new Estudio("Ecografía 5D", 
                    "Imágenes avanzadas del feto con mayor definición y realismo. " +
                    "Preparación: No requiere preparación especial. Evitar cremas en el abdomen el día del estudio."),
                
                new Estudio("Ecografía Renal", 
                    "Evaluación de riñones, uréteres y vejiga para detectar infecciones, quistes o cálculos. " +
                    "Preparación: Vejiga llena - beber agua 1 hora antes y no orinar hasta finalizar."),
                
                new Estudio("Ecografía Doppler", 
                    "Evaluación del flujo sanguíneo en arterias y venas. " +
                    "Preparación: Depende del área - extremidades sin preparación, abdomen requiere ayuno de 6-8 horas."),
                
                new Estudio("Ecografía Prostática", 
                    "Evaluación de la glándula prostática para detectar agrandamiento o tumores. " +
                    "Preparación: Transabdominal - vejiga llena. Transrectal - recto vacío y vejiga con poca orina."),
                
                new Estudio("Ecografía de Tiroides", 
                    "Evaluación de la glándula tiroides para detectar nódulos, quistes o bocio. " +
                    "Preparación: No requiere ninguna preparación especial."),
                
                new Estudio("Ecografía Abdominal", 
                    "Evaluación de hígado, vesícula, páncreas y bazo. " +
                    "Preparación: Ayuno de 6-8 horas para reducir gases intestinales."),
                
                new Estudio("Ecografía Ginecológica", 
                    "Evaluación de útero y ovarios. " +
                    "Preparación: Vejiga llena para mejor visualización."),
                
                new Estudio("Ecografía Mamaria", 
                    "Evaluación de mamas para detectar quistes, nódulos o anomalías. " +
                    "Preparación: No requiere preparación especial."),
                
                new Estudio("Ecografía de Partes Blandas", 
                    "Evaluación de músculos, tendones y tejidos subcutáneos. " +
                    "Preparación: No requiere preparación especial.")
            );

            // Guardar todos los estudios
            estudioRepository.saveAll(estudios);
            
            System.out.println("✅ " + estudios.size() + " estudios cargados por defecto en la base de datos");
            
        } catch (Exception e) {
            System.out.println("❌ ERROR cargando estudios: " + e.getMessage());
            e.printStackTrace();
        }
    }
}