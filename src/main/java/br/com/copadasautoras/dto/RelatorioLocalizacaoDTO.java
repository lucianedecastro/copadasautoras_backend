package br.com.copadasautoras.dto;

import java.util.List;

/**
 * Relatório agregado de localização das autoras ativas (aprovadas e
 * em análise). Uso administrativo.
 *
 * totalAutoras e comLocalizacao dão a COBERTURA do dado — é o número que
 * diz se o mapa já tem corpo pra ser mostrado.
 */
public record RelatorioLocalizacaoDTO(

        long totalAutoras,

        long comLocalizacao,

        List<LocalizacaoPorEstadoDTO> porEstado,

        List<LocalizacaoPorCidadeDTO> porCidade
) {
}
