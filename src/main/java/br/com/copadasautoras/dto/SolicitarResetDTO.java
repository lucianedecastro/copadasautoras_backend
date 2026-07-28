package br.com.copadasautoras.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicitarResetDTO(

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "E-mail inválido.")
        String email
) {
}
