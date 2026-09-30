package br.com.copadasautoras.dto;

/**
 * Linha do relatório de localização: quantas autoras por cidade (e UF).
 *
 * Atenção: em cidade pequena, "1 autora" pode identificar quem é. Pro
 * material de patrocínio, prefira o recorte por estado/região.
 */
public record LocalizacaoPorCidadeDTO(

        String estado,

        String cidade,

        Long autoras
) {
}
