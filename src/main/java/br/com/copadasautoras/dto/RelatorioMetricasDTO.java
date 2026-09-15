package br.com.copadasautoras.dto;

import java.util.List;

/**
 * Dados consolidados do relatório de inscrições, já recortados
 * pelo período informado. Base para a geração de PDF e Excel.
 *
 * Precisão do recorte:
 * - As OBRAS são filtradas pela data de submissão ({@code dataSubmissao}).
 *   {@code obrasNoPeriodo} e o detalhamento por status referem-se às
 *   obras inscritas dentro do período, classificadas pelo status ATUAL.
 * - As AUTORAS são sempre o total acumulado até a geração. O cadastro
 *   de autora não registra data de inscrição, portanto não é filtrável
 *   por período. Isso fica explícito no rodapé do relatório.
 *
 * Só números — nenhum dado identificável.
 */
public record RelatorioMetricasDTO(

        String periodoInicio,
        String periodoFim,
        boolean periodoDefinido,

        long totalAutoras,
        long autorasPendentes,
        long autorasAprovadas,
        long autorasSuspensas,
        long autorasExcluidas,

        long obrasNoPeriodo,
        long obrasEmCuradoria,
        long obrasNaoSelecionadas,
        long obrasEmCompeticao,
        long obrasClassificadas,
        long obrasEliminadas,
        long obrasCampea,

        List<SeriePontoDTO> obrasPorSemana,

        String geradoEm

) {}
