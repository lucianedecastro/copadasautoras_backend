package br.com.copadasautoras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SolicitacaoExclusaoDTO(

        @NotBlank(
                message = "A justificativa para exclusão é obrigatória"
        )
        @Size(
                min = 10,
                max = 2000,
                message = "A justificativa deve possuir entre 10 e 2000 caracteres"
        )
        String justificativa
) {
}


