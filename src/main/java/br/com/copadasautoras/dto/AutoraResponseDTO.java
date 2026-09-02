package br.com.copadasautoras.dto;

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
        boolean perfilCompleto
) {

    /**
     * Construtor de compatibilidade.
     *
     * Mantém funcionando qualquer ponto do código que ainda cria o DTO
     * sem o campo perfilCompleto (ele assume 'false' nesses casos). O
     * AutoraService usa o construtor completo, com o valor real.
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
                false
        );
    }
}
