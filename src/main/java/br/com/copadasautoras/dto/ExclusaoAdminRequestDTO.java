package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.MotivoExclusao;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo da exclusão administrativa de uma autora.
 *
 * O motivo é obrigatório: é ele que define a mensagem GENÉRICA que a
 * autora verá no painel. A justificativa é livre e INTERNA — vai para a
 * auditoria e o registro da autora, e nunca aparece pra ela. Se vier em
 * branco, o serviço grava um texto padrão.
 */
public record ExclusaoAdminRequestDTO(

        @NotNull(message = "O motivo da exclusão é obrigatório.")
        MotivoExclusao motivo,

        String justificativa
) {}
