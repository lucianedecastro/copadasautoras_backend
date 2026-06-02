package br.com.copadasautoras.dto;

public record AutoraPublicResponseDTO(

        Long id,

        String nomeExibicao,

        String biografia,

        String site,

        String redesSociais,

        String tituloObra,

        String categoria
) {
}

