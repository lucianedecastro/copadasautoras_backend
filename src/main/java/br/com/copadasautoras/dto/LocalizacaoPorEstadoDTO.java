package br.com.copadasautoras.dto;

/**
 * Linha do relatório de localização: quantas autoras por UF.
 * Só número agregado — nenhuma autora individual.
 */
public record LocalizacaoPorEstadoDTO(

        String estado,

        Long autoras
) {
}
