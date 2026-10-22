package com.clyvo.veterinary.dto;

import com.clyvo.veterinary.models.Pet;
import com.clyvo.veterinary.models.Raca;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada de {@code POST /api/pets}. Mantém o mesmo formato JSON já enviado pelo app mobile
 * ({@code "raca": {"idRaca": "..."}}), mas aceita apenas os campos que o tutor pode informar:
 * id, tutor e status são sempre definidos pelo servidor.
 */
public record PetRequest(
        @NotBlank(message = "O nome do pet é obrigatório.")
        @Size(max = 255, message = "O nome do pet deve ter no máximo 255 caracteres.")
        String nome,

        @PastOrPresent(message = "A data de nascimento não pode ser no futuro.")
        LocalDate dataNascimento,

        @Size(max = 20, message = "O sexo deve ter no máximo 20 caracteres.")
        String sexo,

        @Valid
        RacaRef raca) {

    public record RacaRef(@NotNull(message = "Informe o id da raça.") UUID idRaca) { }

    public Pet toEntity() {
        Pet pet = new Pet();
        pet.setNome(nome);
        pet.setDataNascimento(dataNascimento);
        pet.setSexo(sexo);
        if (raca != null) {
            Raca entity = new Raca();
            entity.setIdRaca(raca.idRaca());
            pet.setRaca(entity);
        }
        return pet;
    }
}
