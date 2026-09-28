package br.com.copadasautoras.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VotoPopularRequestDTO(

        @NotNull(message = "A obra é obrigatória")
        Long submissaoId,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email
) {
}
