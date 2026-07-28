package br.com.copadasautoras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaDTO(

        @NotBlank(message = "Token ausente.")
        String token,

        @NotBlank(message = "Informe a nova senha.")
        @Size(min = 8, message = "A senha deve ter ao menos 8 caracteres.")
        String novaSenha
) {
}
