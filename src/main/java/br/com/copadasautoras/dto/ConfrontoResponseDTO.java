package br.com.copadasautoras.dto;

public record ConfrontoResponseDTO(

        Long id,
        SubmissaoResponseDTO casa,
        SubmissaoResponseDTO fora,
        SubmissaoResponseDTO vencedora,
        Boolean resolvido

) {}
