package com.dpi.primeraapi.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.dpi.primeraapi.model.ObraSocial;
import com.dpi.primeraapi.repository.ObraSocialRepository;

@Component
public class ObraSocialDataLoader implements CommandLineRunner {

    private final ObraSocialRepository obraSocialRepository;

    public ObraSocialDataLoader(ObraSocialRepository obraSocialRepository) {
        this.obraSocialRepository = obraSocialRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Cargar obras sociales iniciales si no existen
        if (obraSocialRepository.count() == 0) {
            obraSocialRepository.save(new ObraSocial("IPROSS"));
            obraSocialRepository.save(new ObraSocial("PAMI"));
            obraSocialRepository.save(new ObraSocial("OSDE"));
            obraSocialRepository.save(new ObraSocial("SWISS MEDICAL"));
            obraSocialRepository.save(new ObraSocial("GALENO"));
            obraSocialRepository.save(new ObraSocial("OTROS"));
        }
    }
}