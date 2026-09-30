package br.com.copadasautoras.dto;

/** Situação do Escolha do Público para o painel da autora. */
public record VotoPopularMinhaDTO(
        boolean votacaoEncerrada,
        boolean seloEscolhaPublico
) {}
