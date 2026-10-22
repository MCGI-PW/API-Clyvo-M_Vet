package com.clyvo.veterinary.dto;

import com.clyvo.veterinary.models.Clinica;
import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.models.Pet;
import com.clyvo.veterinary.models.Veterinario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entrada de {@code POST /api/consultas}. Mantém o formato JSON já enviado pelo app mobile
 * ({@code "pet": {"idPet": "..."}}, {@code "veterinario": {"idVeterinario": "..."}}), mas não aceita
 * id nem status: a consulta sempre nasce {@code AGENDADO}, definida pelo servidor.
 * A clínica é opcional; sem ela, o servidor usa o vínculo ativo do veterinário.
 */
public record ConsultaRequest(
        @NotNull(message = "Informe o pet.") @Valid PetRef pet,
        @NotNull(message = "Informe o veterinário.") @Valid VeterinarioRef veterinario,
        @Valid ClinicaRef clinica,
        @NotNull(message = "Informe a data e a hora da consulta.") LocalDateTime dataHora,
        @Pattern(regexp = "PRESENCIAL|TELEMEDICINA", message = "A modalidade deve ser PRESENCIAL ou TELEMEDICINA.")
        String modalidade) {

    public record PetRef(@NotNull(message = "Informe o id do pet.") UUID idPet) { }

    public record VeterinarioRef(@NotNull(message = "Informe o id do veterinário.") UUID idVeterinario) { }

    public record ClinicaRef(@NotNull(message = "Informe o id da clínica.") UUID idClinica) { }

    public Consulta toEntity() {
        Pet petEntity = new Pet();
        petEntity.setIdPet(pet.idPet());

        Veterinario vetEntity = new Veterinario();
        vetEntity.setIdVeterinario(veterinario.idVeterinario());

        Consulta consulta = new Consulta();
        consulta.setPet(petEntity);
        consulta.setVeterinario(vetEntity);
        consulta.setDataHora(dataHora);
        consulta.setModalidade(modalidade);
        if (clinica != null) {
            Clinica clinicaEntity = new Clinica();
            clinicaEntity.setIdClinica(clinica.idClinica());
            consulta.setClinica(clinicaEntity);
        }
        return consulta;
    }
}
