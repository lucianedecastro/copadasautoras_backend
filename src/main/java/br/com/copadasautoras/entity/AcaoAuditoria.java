package br.com.copadasautoras.entity;

/**
 * A decisão registrada. Vocabulário controlado — cada valor novo
 * corresponde a um ponto instrumentado no código.
 *
 * A entidade afetada ({@link TipoEntidadeAuditoria}) desambigua as
 * ações compartilhadas: EXCLUIDA vale para autora e para obra, e a
 * origem ({@link OrigemAuditoria}) diz se foi a própria autora ou a
 * administração.
 */
public enum AcaoAuditoria {

    // Autora
    APROVADA,
    SUSPENSA,
    EXCLUIDA,
    REANALISE,

    // Obra — curadoria editorial
    SELECIONADA,
    NAO_SELECIONADA,

    // Obra — banca
    CLASSIFICADA,
    ELIMINADA,
    VOTO_FINAL
}
