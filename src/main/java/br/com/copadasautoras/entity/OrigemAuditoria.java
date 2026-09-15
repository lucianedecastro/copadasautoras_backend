package br.com.copadasautoras.entity;

/**
 * De onde partiu a decisão registrada no log.
 *
 * Nem toda mudança é de admin: a banca julga, a autora age sobre o
 * próprio perfil, e o sistema aplica regras automáticas (como a
 * reanálise ao trocar a rede social).
 */
public enum OrigemAuditoria {
    ADMIN,
    BANCA,
    AUTORA,
    SISTEMA
}
