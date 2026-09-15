package br.com.copadasautoras.dto;

/**
 * Uma linha do log já pronta para a tela: além dos campos crus do
 * registro, traz o nome atual do alvo (autora/obra) resolvido pelo id,
 * com {@code alvoRemovido = true} quando a autora/obra não existe mais.
 */
public record RegistroAuditoriaDTO(

        Long id,
        String dataHora,        // "dd/MM/yyyy HH:mm"
        String origem,          // ADMIN | BANCA | AUTORA | SISTEMA
        String atorNome,
        String entidade,        // AUTORA | SUBMISSAO
        Long entidadeId,
        String alvoNome,        // nome atual da autora / título da obra (ou null)
        boolean alvoRemovido,   // true se o id não existe mais
        String acao,
        String valorAnterior,
        String valorNovo,
        String justificativa

) {}
