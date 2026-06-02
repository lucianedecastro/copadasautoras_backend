package br.com.copadasautoras.dto;

import jakarta.validation.constraints.Size;

public record AutoraUpdateRequestDTO(

        @Size(
                min = 2,
                max = 255,
                message = "O nome de exibição deve possuir entre 2 e 255 caracteres"
        )
        String nomeExibicao,

        @Size(
                max = 2000,
                message = "A biografia deve ter no máximo 2000 caracteres"
        )
        String biografia,

        @Size(
                max = 500,
                message = "O site deve ter no máximo 500 caracteres"
        )
        String site,

        @Size(
                max = 255,
                message = "A rede social deve ter no máximo 255 caracteres"
        )
        String redesSociais
) {
}
