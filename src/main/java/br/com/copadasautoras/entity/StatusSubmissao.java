package br.com.copadasautoras.entity;

public enum StatusSubmissao {

    /**
     * Obra enviada pela autora
     * e aguardando decisão da edição.
     */
    SUBMETIDA,

    /**
     * Obra analisada, mas não escolhida
     * para compor as 32 competidoras.
     */
    NAO_SELECIONADA,

    /**
     * Obra selecionada para a edição vigente.
     */
    EM_COMPETICAO,

    CLASSIFICADA,

    ELIMINADA,

    CAMPEA
}