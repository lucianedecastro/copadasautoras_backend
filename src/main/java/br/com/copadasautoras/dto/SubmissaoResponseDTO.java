package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.entity.StatusSubmissao;
import br.com.copadasautoras.entity.TipoExibicao;

public record SubmissaoResponseDTO(

        Long id,
        String titulo,
        String categoria,
        TipoExibicao tipoExibicao,
        StatusSubmissao status,
        FaseCompeticao faseAtual,
        Long autoraId,
        Long eventoId,
        String arquivoPublicoUrl,
        String arquivoCompletoUrl

) {

    public static SubmissaoResponseDTO fromEntity(
            br.com.copadasautoras.entity.Submissao submissao
    ) {
        return new SubmissaoResponseDTO(
                submissao.getId(),
                submissao.getTitulo(),
                submissao.getCategoria(),
                submissao.getTipoExibicao(),
                submissao.getStatus(),
                submissao.getFaseAtual(),
                submissao.getAutora().getId(),
                submissao.getEvento().getId(),
                submissao.getArquivoPublicoUrl(),
                submissao.getArquivoCompletoUrl()
        );
    }
}