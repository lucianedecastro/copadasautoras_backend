package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.TipoExibicao;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmissaoRequestDTO(

        @NotBlank(message = "Título é obrigatório")
        String titulo,

        String categoria,

        String descricao,

        @NotNull(message = "Tipo de exibição é obrigatório")
        TipoExibicao tipoExibicao,

        @NotNull(message = "Evento é obrigatório")
        Long eventoId,

        /*
         * Os cinco aceites abaixo materializam o Art. 19 do regulamento —
         * a declaração de autoria, titularidade, ineditismo e concordância
         * com os termos. São a base jurídica da submissão e do Art. 14
         * (desclassificação).
         *
         * Estavam com @NotNull, que só rejeita null: um "false" passava na
         * validação e a obra entrava na Copa com todos os aceites negados.
         * O front sempre manda true (exige os checkboxes), mas a API é
         * pública — bastava um curl.
         *
         * @AssertTrue exige true. Rejeita false E null, então cobre tudo
         * o que o @NotNull cobria, e mais.
         */

        @AssertTrue(message = "É necessário declarar a autoria da obra")
        Boolean aceiteAutoria,

        @AssertTrue(message = "É necessário autorizar a exibição da obra")
        Boolean aceiteExibicao,

        @AssertTrue(message = "É necessário aceitar a avaliação da banca")
        Boolean aceiteBanca,

        @AssertTrue(message = "É necessário declarar a titularidade dos direitos da obra")
        Boolean aceiteTitularidade,

        @AssertTrue(message = "É necessário aceitar integralmente o regulamento")
        Boolean aceiteTermoCompleto

) {}