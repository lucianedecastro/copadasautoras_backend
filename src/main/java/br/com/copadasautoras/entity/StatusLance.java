package br.com.copadasautoras.entity;

public enum StatusLance {

    /**
     * Em preparação. Não aparece na timeline pública.
     */
    RASCUNHO,

    /**
     * Pronto, mas com publicação marcada para o futuro.
     * Torna-se visível automaticamente quando
     * publicarEm é alcançado (verificado na consulta).
     */
    AGENDADO,

    /**
     * Visível na timeline pública.
     */
    PUBLICADO
}
