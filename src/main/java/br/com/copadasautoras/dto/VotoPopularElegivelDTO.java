package br.com.copadasautoras.dto;

public record VotoPopularElegivelDTO(
        Long id,
        String titulo,
        String categoria,
        String nomeAutora,
        String trechoLiberado
) {
}
