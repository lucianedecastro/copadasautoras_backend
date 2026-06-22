package br.com.copadasautoras.dto;

public record ObraPublicaDTO(

        Long id,
        String titulo,
        String categoria,

        // Só usado na FASE_32, pra destacar quem avançou dentro do grupo
        // de 4. Nas demais fases (confronto 1x1) fica null.
        Boolean avancou

) {}
