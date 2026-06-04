package br.com.copadasautoras.dto;

public record SelecaoEdicaoResponseDTO(

        int totalSubmetidas,

        int totalSelecionadas,

        int totalNaoSelecionadas,

        String mensagem

) {
}
