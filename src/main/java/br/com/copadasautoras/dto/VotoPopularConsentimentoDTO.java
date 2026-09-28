package br.com.copadasautoras.dto;

import jakarta.validation.constraints.NotNull;

public record VotoPopularConsentimentoDTO(

        @NotNull(message = "Informe se autoriza a votação popular")
        Boolean autorizaVotoPopular,

        String trechoLiberado

) {}