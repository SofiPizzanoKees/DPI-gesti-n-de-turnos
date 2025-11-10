package com.dpi.primeraapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dpi.primeraapi.model.DisponibilidadMedica;
import com.dpi.primeraapi.model.Usuario;
import com.dpi.primeraapi.model.enums.DiaSemana;

public interface DisponibilidadMedicaRepository extends JpaRepository<DisponibilidadMedica, Long> {
    List<DisponibilidadMedica> findByMedicoAndActivoTrue(Usuario medico);
    List<DisponibilidadMedica> findByMedicoAndDiaSemanaAndActivoTrue(Usuario medico, DiaSemana diaSemana);
    boolean existsByMedicoAndDiaSemanaAndActivoTrue(Usuario medico, DiaSemana diaSemana);
}