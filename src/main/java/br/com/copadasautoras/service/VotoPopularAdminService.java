package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.SeloEscolhaPublicoDTO;
import br.com.copadasautoras.dto.VotoPopularEncerramentoDTO;
import br.com.copadasautoras.dto.VotoPopularResultadoDTO;
import br.com.copadasautoras.entity.AcaoAuditoria;
import br.com.copadasautoras.entity.Competicao;
import br.com.copadasautoras.entity.OrigemAuditoria;
import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.entity.TipoEntidadeAuditoria;
import br.com.copadasautoras.repository.CompeticaoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import br.com.copadasautoras.repository.VotoPopularRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Placar, encerramento e reabertura do "Escolha do Público" (admin).
 *
 * Participam da votação somente semifinalistas elegíveis que autorizaram
 * o voto popular (e, portanto, têm trecho liberado).
 */
@Service
@RequiredArgsConstructor
public class VotoPopularAdminService {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Mais votos primeiro; empate desempata por título.
    private static final Comparator<VotoPopularResultadoDTO.Obra> PLACAR =
            (a, b) -> {
                int porVotos = Long.compare(
                        b.votosConfirmados(), a.votosConfirmados());
                if (porVotos != 0) return porVotos;
                String ta = a.titulo() == null ? "" : a.titulo();
                String tb = b.titulo() == null ? "" : b.titulo();
                return ta.compareToIgnoreCase(tb);
            };

    private final SubmissaoRepository submissaoRepository;
    private final VotoPopularRepository votoPopularRepository;
    private final CompeticaoRepository competicaoRepository;
    private final AuditoriaService auditoriaService;

    // ------------------------------------------------------------------
    // Placar
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public VotoPopularResultadoDTO resultado() {

        Competicao competicao = competicaoAtual();
        List<VotoPopularResultadoDTO.Obra> obras = montarObras();

        int foraDaVotacao =
                submissaoRepository.findByElegivelVotoPopularTrue().size()
                        - obras.size();

        return new VotoPopularResultadoDTO(
                obras.size(),
                votoPopularRepository.countByConfirmadoTrue(),
                votoPopularRepository.countByConfirmadoFalse(),
                ZonedDateTime.now(SAO_PAULO).format(FORMATO),
                competicao.isVotacaoPopularEncerrada(),
                competicao.getVotacaoPopularEncerradaEm() != null
                        ? competicao.getVotacaoPopularEncerradaEm().format(FORMATO)
                        : null,
                Math.max(foraDaVotacao, 0),
                obras
        );
    }

    // ------------------------------------------------------------------
    // Encerrar
    // ------------------------------------------------------------------

    /**
     * Fecha a votação e aplica o selo na obra mais votada.
     * Com empate no topo e sem obraId, não muda nada e devolve as
     * empatadas para o admin escolher.
     */
    @Transactional
    public VotoPopularEncerramentoDTO encerrar(Long obraIdEscolhida) {

        Competicao competicao = competicaoAtual();

        if (competicao.isVotacaoPopularEncerrada()) {
            throw new RuntimeException(
                    "A votação do Escolha do Público já foi encerrada."
            );
        }

        List<VotoPopularResultadoDTO.Obra> obras = montarObras();

        if (obras.isEmpty()) {
            throw new RuntimeException(
                    "Não há obras participando da votação do Escolha do Público."
            );
        }

        long maximo = obras.get(0).votosConfirmados();

        if (maximo == 0) {
            throw new RuntimeException(
                    "Ainda não há votos confirmados: não é possível encerrar."
            );
        }

        List<VotoPopularResultadoDTO.Obra> topo = obras.stream()
                .filter(o -> o.votosConfirmados() == maximo)
                .toList();

        boolean empate = topo.size() > 1;

        if (empate && obraIdEscolhida == null) {
            return new VotoPopularEncerramentoDTO(false, true, topo, null);
        }

        Long vencedoraId;
        String justificativa;

        if (empate) {
            boolean valida = topo.stream()
                    .anyMatch(o -> o.id().equals(obraIdEscolhida));
            if (!valida) {
                throw new RuntimeException(
                        "A obra escolhida não está entre as empatadas no topo."
                );
            }
            vencedoraId = obraIdEscolhida;
            justificativa = "Empate em " + maximo
                    + " voto(s) confirmados; vencedora escolhida pela administração.";
        } else {
            vencedoraId = topo.get(0).id();
            justificativa = "Obra mais votada, com " + maximo
                    + " voto(s) confirmados.";
        }

        Submissao vencedora = submissaoRepository.findById(vencedoraId)
                .orElseThrow(() -> new RuntimeException("Obra não encontrada."));

        vencedora.setSeloEscolhaPublico(true);
        submissaoRepository.save(vencedora);

        competicao.setVotacaoPopularEncerrada(true);
        competicao.setVotacaoPopularEncerradaEm(
                LocalDateTime.now(SAO_PAULO)
        );
        competicaoRepository.save(competicao);

        auditoriaService.registrar(
                OrigemAuditoria.ADMIN,
                TipoEntidadeAuditoria.SUBMISSAO,
                vencedora.getId(),
                AcaoAuditoria.VOTACAO_POPULAR_ENCERRADA,
                "ABERTA",
                "ENCERRADA",
                justificativa
        );

        auditoriaService.registrar(
                OrigemAuditoria.ADMIN,
                TipoEntidadeAuditoria.SUBMISSAO,
                vencedora.getId(),
                AcaoAuditoria.SELO_ESCOLHA_PUBLICO,
                "SEM_SELO",
                "COM_SELO",
                justificativa
        );

        return new VotoPopularEncerramentoDTO(
                true,
                false,
                List.of(),
                new SeloEscolhaPublicoDTO(
                        vencedora.getId(),
                        vencedora.getTitulo(),
                        vencedora.getAutora() != null
                                ? vencedora.getAutora().getNomeExibicao()
                                : null
                )
        );
    }

    // ------------------------------------------------------------------
    // Reabrir (opcional)
    // ------------------------------------------------------------------

    /** Reabre a votação e retira o selo de quem o recebeu. */
    @Transactional
    public void reabrir() {

        Competicao competicao = competicaoAtual();

        if (!competicao.isVotacaoPopularEncerrada()) {
            throw new RuntimeException(
                    "A votação do Escolha do Público não está encerrada."
            );
        }

        for (Submissao s : submissaoRepository.findBySeloEscolhaPublicoTrue()) {
            s.setSeloEscolhaPublico(false);
            submissaoRepository.save(s);

            auditoriaService.registrar(
                    OrigemAuditoria.ADMIN,
                    TipoEntidadeAuditoria.SUBMISSAO,
                    s.getId(),
                    AcaoAuditoria.VOTACAO_POPULAR_REABERTA,
                    "ENCERRADA",
                    "ABERTA",
                    "Votação reaberta pela administração; selo retirado."
            );
        }

        competicao.setVotacaoPopularEncerrada(false);
        competicao.setVotacaoPopularEncerradaEm(null);
        competicaoRepository.save(competicao);
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private Competicao competicaoAtual() {
        return competicaoRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Competição não encontrada."));
    }

    private List<VotoPopularResultadoDTO.Obra> montarObras() {

        List<Submissao> participantes = submissaoRepository
                .findByElegivelVotoPopularTrueAndAutorizaVotoPopularTrue();

        Map<Long, Long> confirmados =
                paraMapa(votoPopularRepository.contarConfirmadosPorSubmissao());
        Map<Long, Long> pendentes =
                paraMapa(votoPopularRepository.contarPendentesPorSubmissao());

        long baseVotos = participantes.stream()
                .mapToLong(s -> confirmados.getOrDefault(s.getId(), 0L))
                .sum();

        long maximo = participantes.stream()
                .mapToLong(s -> confirmados.getOrDefault(s.getId(), 0L))
                .max()
                .orElse(0L);

        long noTopo = participantes.stream()
                .filter(s -> confirmados.getOrDefault(s.getId(), 0L) == maximo)
                .count();

        return participantes.stream()
                .map(s -> {
                    long votos = confirmados.getOrDefault(s.getId(), 0L);
                    long pend = pendentes.getOrDefault(s.getId(), 0L);
                    double pct = baseVotos == 0
                            ? 0.0
                            : Math.round(votos * 1000.0 / baseVotos) / 10.0;
                    String trecho = s.getTrechoLiberado();
                    return new VotoPopularResultadoDTO.Obra(
                            s.getId(),
                            s.getTitulo(),
                            s.getCategoria(),
                            s.getAutora() != null
                                    ? s.getAutora().getNomeExibicao()
                                    : null,
                            trecho != null && !trecho.isBlank(),
                            votos,
                            pend,
                            pct,
                            s.isSeloEscolhaPublico(),
                            maximo > 0 && votos == maximo && noTopo > 1
                    );
                })
                .sorted(PLACAR)
                .toList();
    }

    private Map<Long, Long> paraMapa(List<Object[]> linhas) {
        Map<Long, Long> mapa = new HashMap<>();
        for (Object[] l : linhas) {
            mapa.put(((Number) l[0]).longValue(), ((Number) l[1]).longValue());
        }
        return mapa;
    }
}
