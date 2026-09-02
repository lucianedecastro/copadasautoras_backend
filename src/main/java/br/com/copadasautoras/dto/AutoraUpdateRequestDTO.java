package br.com.copadasautoras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AutoraUpdateRequestDTO(

        @NotBlank(
                message = "O nome de exibição é obrigatório"
        )
        @Size(
                min = 2,
                max = 255,
                message = "O nome de exibição deve possuir entre 2 e 255 caracteres"
        )
        String nomeExibicao,

        @NotBlank(
                message = "A biografia é obrigatória"
        )
        @Size(
                max = 2000,
                message = "A biografia deve ter no máximo 2000 caracteres"
        )
        String biografia,

        // Site é o único campo opcional do perfil — só valida o tamanho
        // quando a autora tiver um. Sem @NotBlank de propósito.
        @Size(
                max = 500,
                message = "O site deve ter no máximo 500 caracteres"
        )
        String site,

        @NotBlank(
                message = "O link de rede social é obrigatório"
        )
        @Size(
                max = 255,
                message = "A rede social deve ter no máximo 255 caracteres"
        )
        String redesSociais
) {
}
