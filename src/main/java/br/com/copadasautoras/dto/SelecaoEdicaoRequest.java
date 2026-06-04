package br.com.copadasautoras.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record SelecaoEdicaoRequest(

        @Size(
                min = 1,
                max = 32,
                message = "A seleção da edição deve conter exatamente 32 obras."
        )
        List<Long> submissaoIds

) {}
