
package br.com.copadasautoras.entity;

public enum StatusAutora {

    /**
     * Cadastro realizado, aguardando validação da equipe admin.
     */
    PENDENTE,

    /**
     * Cadastro validado e autorizado a participar da plataforma.
     */
    APROVADA,

    /**
     * Cadastro temporariamente suspenso pela administração.
     */
    SUSPENSA,

    /**
     * Perfil excluído mediante solicitação da autora
     * ou decisão administrativa.
     */
    EXCLUIDA
}
