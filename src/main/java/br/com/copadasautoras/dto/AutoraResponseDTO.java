package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.StatusAutora;

public record AutoraResponseDTO(

        Long id,

        String nome,

        String nomeExibicao,

        String email,

        String biografia,

        String site,

        String redesSociais,

        StatusAutora statusAutora
) {
}


