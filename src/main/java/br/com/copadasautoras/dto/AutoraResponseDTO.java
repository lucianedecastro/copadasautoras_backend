package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.MotivoExclusao;
import br.com.copadasautoras.entity.StatusAutora;

public record AutoraResponseDTO(

        Long id,

        String nome,

        String nomeExibicao,

        String email,

        String biografia,

        String site,

        String redesSociais,

        StatusAutora statusAutora,

        /**
         * Sinaliza se o perfil está completo (nome completo, nome de
         * exibição, biografia e rede social preenchidos). Calculado a
         * partir de Autora.isPerfilCompleto() — o front usa isso pra
         * separar quem está pronta pra conferência de quem ainda não
         * preencheu.
         */
        boolean perfilCompleto,

        /**
         * Motivo da exclusão — apenas a CATEGORIA (INADEQUACAO,
         * AUTOEXCLUSAO, ADMINISTRATIVA), nunca a justificativa livre.
         * Só vem preenchido quando statusAutora == EXCLUIDA; o painel da
         * autora usa isso pra escolher a mensagem genérica certa. NULL nas
         * autoras já excluídas antes da V15.
         */
        MotivoExclusao motivoExclusao
) {

    /**
     * Construtor de compatibilidade (8 campos).
     *
     * Mantém funcionando qualquer ponto do código que ainda cria o DTO
     * sem perfilCompleto (assume 'false') e sem motivoExclusao (null).
     */
    public AutoraResponseDTO(
            Long id,
            String nome,
            String nomeExibicao,
            String email,
            String biografia,
            String site,
            String redesSociais,
            StatusAutora statusAutora
    ) {
        this(
                id,
                nome,
                nomeExibicao,
                email,
                biografia,
                site,
                redesSociais,
                statusAutora,
                false,
                null
        );
    }

    /**
     * Construtor de compatibilidade (9 campos).
     *
     * Mantém funcionando quem cria o DTO com perfilCompleto mas ainda
     * sem motivoExclusao (assume null).
     */
    public AutoraResponseDTO(
            Long id,
            String nome,
            String nomeExibicao,
            String email,
            String biografia,
            String site,
            String redesSociais,
            StatusAutora statusAutora,
            boolean perfilCompleto
    ) {
        this(
                id,
                nome,
                nomeExibicao,
                email,
                biografia,
                site,
                redesSociais,
                statusAutora,
                perfilCompleto,
                null
        );
    }
}
