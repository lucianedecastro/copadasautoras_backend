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

) {}