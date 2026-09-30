package br.com.copadasautoras.dto;

/** Obra que recebeu o selo Escolha do Público. */
public record SeloEscolhaPublicoDTO(
        Long obraId,
        String titulo,
        String nomeAutora
) {}
