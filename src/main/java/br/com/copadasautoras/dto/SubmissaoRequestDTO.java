package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.TipoExibicao;
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

        @NotNull(message = "Aceite de autoria é obrigatório")
        Boolean aceiteAutoria,

        @NotNull(message = "Aceite de exibição é obrigatório")
        Boolean aceiteExibicao,

        @NotNull(message = "Aceite da banca é obrigatório")
        Boolean aceiteBanca,

        @NotNull(message = "Aceite de titularidade é obrigatório")
        Boolean aceiteTitularidade,

        @NotNull(message = "Aceite integral do termo é obrigatório")
        Boolean aceiteTermoCompleto

) {}