package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.MetricasResponseDTO;
import br.com.copadasautoras.dto.RelatorioMetricasDTO;
import br.com.copadasautoras.dto.SeriePontoDTO;
import br.com.copadasautoras.entity.StatusAutora;
import br.com.copadasautoras.entity.StatusSubmissao;
import br.com.copadasautoras.repository.AutoraRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Números do painel admin.
 *
 * Só leitura e contagem — não toca em Competição (por isso a caixa
 * de dados aparece mesmo antes de a competição ser iniciada) e não
 * altera nenhum dado. Nenhum nome de autora ou título de obra sai
 * daqui: apenas totais e a série de obras inscritas por semana.
 *
 * A geração de PDF/Excel é delegada ao {@link MetricasExportService}.
 */
@Service
@RequiredArgsConstructor
public class MetricasService {

    private final AutoraRepository autoraRepository;
    private final SubmissaoRepository submissaoRepository;
    private final MetricasExportService metricasExportService;

    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter DIA_MES =
            DateTimeFormatter.ofPattern("dd/MM");

    // Piso de data usado quando o período não tem início definido.
    private static final LocalDateTime INICIO_PADRAO =
            LocalDateTime.of(2000, 1, 1, 0, 0);

    // =========================
    // PAINEL (snapshot atual)
    // =========================

    @Transactional(readOnly = true)
    public MetricasResponseDTO obterMetricas() {

        List<SeriePontoDTO> serie =
                agruparPorSemana(submissaoRepository.findDatasSubmissao());

        return new MetricasResponseDTO(
                autoraRepository.count(),
                autoraRepository.countByStatusAutora(StatusAutora.PENDENTE),
                autoraRepository.countByStatusAutora(StatusAutora.APROVADA),
                autoraRepository.countByStatusAutora(StatusAutora.SUSPENSA),
                autoraRepository.countByStatusAutora(StatusAutora.EXCLUIDA),

                submissaoRepository.count(),
                submissaoRepository.countByStatus(StatusSubmissao.SUBMETIDA),
                submissaoRepository.countByStatus(StatusSubmissao.NAO_SELECIONADA),
                submissaoRepository.countByStatus(StatusSubmissao.EM_COMPETICAO),
                submissaoRepository.countByStatus(StatusSubmissao.CLASSIFICADA),
                submissaoRepository.countByStatus(StatusSubmissao.ELIMINADA),
                submissaoRepository.countByStatus(StatusSubmissao.CAMPEA),

                serie,
                LocalDateTime.now().format(DATA_BR)
        );
    }

    // =========================
    // RELATÓRIO (recorte por período)
    // =========================

    @Transactional(readOnly = true)
    public RelatorioMetricasDTO montarRelatorio(LocalDate inicio, LocalDate fim) {

        boolean definido = (inicio != null) || (fim != null);

        LocalDateTime ini = (inicio != null)
                ? inicio.atStartOfDay()
                : INICIO_PADRAO;

        LocalDateTime fimDt = (fim != null)
                ? fim.atTime(LocalTime.MAX)
                : LocalDateTime.now();

        List<SeriePontoDTO> serie = agruparPorSemana(
                submissaoRepository.findDatasSubmissaoBetween(ini, fimDt));

        return new RelatorioMetricasDTO(
                (inicio != null) ? inicio.format(DATA_BR) : "início",
                (fim != null) ? fim.format(DATA_BR) : "hoje",
                definido,

                autoraRepository.count(),
                autoraRepository.countByStatusAutora(StatusAutora.PENDENTE),
                autoraRepository.countByStatusAutora(StatusAutora.APROVADA),
                autoraRepository.countByStatusAutora(StatusAutora.SUSPENSA),
                autoraRepository.countByStatusAutora(StatusAutora.EXCLUIDA),

                submissaoRepository.countByDataSubmissaoBetween(ini, fimDt),
                submissaoRepository.countByStatusAndDataSubmissaoBetween(
                        StatusSubmissao.SUBMETIDA, ini, fimDt),
                submissaoRepository.countByStatusAndDataSubmissaoBetween(
                        StatusSubmissao.NAO_SELECIONADA, ini, fimDt),
                submissaoRepository.countByStatusAndDataSubmissaoBetween(
                        StatusSubmissao.EM_COMPETICAO, ini, fimDt),
                submissaoRepository.countByStatusAndDataSubmissaoBetween(
                        StatusSubmissao.CLASSIFICADA, ini, fimDt),
                submissaoRepository.countByStatusAndDataSubmissaoBetween(
                        StatusSubmissao.ELIMINADA, ini, fimDt),
                submissaoRepository.countByStatusAndDataSubmissaoBetween(
                        StatusSubmissao.CAMPEA, ini, fimDt),

                serie,
                LocalDateTime.now().format(DATA_BR)
        );
    }

    public byte[] gerarExcel(LocalDate inicio, LocalDate fim) {
        return metricasExportService.gerarExcel(montarRelatorio(inicio, fim));
    }

    public byte[] gerarPdf(LocalDate inicio, LocalDate fim) {
        return metricasExportService.gerarPdf(montarRelatorio(inicio, fim));
    }

    // =========================
    // HELPERS
    // =========================

    /**
     * Agrupa as datas de submissão por semana (segunda-feira ISO) e
     * devolve uma série contínua entre a primeira e a última semana —
     * semanas sem inscrição entram com total 0, para o gráfico não
     * "colar" períodos distantes.
     */
    private List<SeriePontoDTO> agruparPorSemana(List<LocalDateTime> datas) {

        if (datas == null || datas.isEmpty()) {
            return List.of();
        }

        TreeMap<LocalDate, Long> contagem = new TreeMap<>();

        for (LocalDateTime dt : datas) {
            if (dt == null) {
                continue;
            }
            LocalDate segunda = dt.toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            contagem.merge(segunda, 1L, Long::sum);
        }

        if (contagem.isEmpty()) {
            return List.of();
        }

        LocalDate primeira = contagem.firstKey();
        LocalDate ultima = contagem.lastKey();

        List<SeriePontoDTO> serie = new ArrayList<>();

        for (LocalDate semana = primeira;
             !semana.isAfter(ultima);
             semana = semana.plusWeeks(1)) {

            long total = contagem.getOrDefault(semana, 0L);

            serie.add(new SeriePontoDTO(
                    semana.format(DIA_MES),
                    semana.toString(),
                    total
            ));
        }

        return serie;
    }
}
