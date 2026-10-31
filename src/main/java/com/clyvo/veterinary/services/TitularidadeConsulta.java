package com.clyvo.veterinary.services;

import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.repositories.ClinicaRepository;
import com.clyvo.veterinary.repositories.TutorRepository;
import com.clyvo.veterinary.repositories.VeterinarioRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Responde se a conta autenticada é o tutor, o veterinário ou a clínica responsável por uma consulta. */
@Component
public class TitularidadeConsulta {

    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;

    public TitularidadeConsulta(TutorRepository tutorRepository,
                                VeterinarioRepository veterinarioRepository,
                                ClinicaRepository clinicaRepository) {
        this.tutorRepository = tutorRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.clinicaRepository = clinicaRepository;
    }

    public boolean isTutor(Consulta consulta, UUID idConta) {
        return consulta.getPet() != null
                && tutorRepository.findByContaAcessoIdConta(idConta)
                    .map(tutor -> consulta.getPet().pertenceA(tutor))
                    .orElse(false);
    }

    public boolean isVeterinario(Consulta consulta, UUID idConta) {
        return consulta.getVeterinario() != null
                && veterinarioRepository.findByContaAcessoIdConta(idConta)
                    .map(v -> v.getIdVeterinario().equals(consulta.getVeterinario().getIdVeterinario()))
                    .orElse(false);
    }

    public boolean isClinica(Consulta consulta, UUID idConta) {
        return consulta.getClinica() != null
                && clinicaRepository.findByContaAcessoIdConta(idConta)
                    .map(c -> c.getIdClinica().equals(consulta.getClinica().getIdClinica()))
                    .orElse(false);
    }

    /** Quem atende a consulta: o veterinário responsável ou a clínica onde ela acontece. */
    public boolean isResponsavelPeloAtendimento(Consulta consulta, UUID idConta) {
        return isVeterinario(consulta, idConta) || isClinica(consulta, idConta);
    }
}
