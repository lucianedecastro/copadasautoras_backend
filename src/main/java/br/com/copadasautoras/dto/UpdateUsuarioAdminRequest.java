package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUsuarioAdminRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        // Opcional:
        // só atualiza se vier preenchida
        @Size(min = 6,
                message = "A senha deve ter pelo menos 6 caracteres")
        String senha,

        @NotNull(message = "Perfil é obrigatório")
        Role role

) {}
