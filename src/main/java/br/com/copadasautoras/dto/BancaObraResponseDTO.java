package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.TipoExibicao;

public record BancaObraResponseDTO(

        Long submissaoId,
        String titulo,
        TipoExibicao tipoExibicao,
        String arquivoPublicoUrl

) {
}
