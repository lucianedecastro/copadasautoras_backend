package br.com.copadasautoras.dto;

/**
 * Corpo opcional de POST /admin/votacao-popular/encerrar.
 * obraId só é necessário quando há empate no topo: é a obra que o
 * admin escolheu como vencedora.
 */
public record VotoPopularEncerrarRequestDTO(Long obraId) {}
