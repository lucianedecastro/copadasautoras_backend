package br.com.copadasautoras.dto;

public record ConfrontoPublicoDTO(

        ObraPublicaDTO casa,
        ObraPublicaDTO fora,
        ObraPublicaDTO vencedora

) {}
