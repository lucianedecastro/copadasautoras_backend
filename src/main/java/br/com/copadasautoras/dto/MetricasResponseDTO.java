package br.com.copadasautoras.dto;

import java.util.List;

/**
 * Payload da caixa de dados do painel admin.
 *
 * Alimenta os números grandes (autoras e obras), os detalhamentos
 * por status e o mini gráfico de obras inscritas por semana.
 *
 * Estado atual (snapshot): as contagens por status refletem a
 * situação de agora, não um recorte de período. O recorte por
 * período existe apenas no relatório ({@link RelatorioMetricasDTO}).
 *
 * Só números — nenhum nome de autora, nenhum título de obra.
 */
public record MetricasResponseDTO(

        long totalAutoras,
        long autorasPendentes,
        long autorasAprovadas,
        long autorasSuspensas,
        long autorasExcluidas,

        long totalObras,
        long obrasEmCuradoria,
        long obrasNaoSelecionadas,
        long obrasEmCompeticao,
        long obrasClassificadas,
        long obrasEliminadas,
        long obrasCampea,

        List<SeriePontoDTO> obrasPorSemana,

        String geradoEm

) {}
