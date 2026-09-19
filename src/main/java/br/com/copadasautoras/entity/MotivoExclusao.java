package br.com.copadasautoras.entity;

/**
 * Motivo pelo qual uma autora foi movida para {@link StatusAutora#EXCLUIDA}.
 *
 * É registro INTERNO — vive na auditoria e no painel administrativo. O que a
 * autora vê no painel dela é sempre uma mensagem GENÉRICA derivada deste enum,
 * nunca a justificativa livre nem o motivo específico. A separação é proposital:
 * o regulamento é público, o motivo detalhado é interno.
 */
public enum MotivoExclusao {

    /** A própria autora solicitou a exclusão do cadastro. */
    AUTOEXCLUSAO,

    /** Exclusão pela administração por inadequação às regras da competição. */
    INADEQUACAO,

    /** Exclusão administrativa por outro motivo (cadastro duplicado, teste etc.). */
    ADMINISTRATIVA
}
