package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.Role;

public record UsuarioAdminResponseDTO(

        Long id,
        String nome,
        String email,
        Role role

) {}

