package com.dpi.primeraapi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dpi.primeraapi.model.Estudio;
import com.dpi.primeraapi.repository.EstudioRepository;

@Service
public class EstudioService {
    
    @Autowired
    private EstudioRepository estudioRepository;
    
    public List<Estudio> obtenerEstudiosActivos() {
        return estudioRepository.findByActivoTrue();
    }
    
    public Estudio obtenerPorId(Long id) {
        return estudioRepository.findById(id).orElse(null);
    }

}