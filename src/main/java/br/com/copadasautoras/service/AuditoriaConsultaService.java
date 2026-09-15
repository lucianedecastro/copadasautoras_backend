package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.AuditoriaPageDTO;
import br.com.copadasautoras.dto.RegistroAuditoriaDTO;
import br.com.copadasautoras.entity.AcaoAuditoria;
import br.com.copadasautoras.entity.OrigemAuditoria;
import br.com.copadasautoras.entity.RegistroAuditoria;
import br.com.copadasautoras.entity.TipoEntidadeAuditoria;
import br.com.copadasautoras.repository.AutoraRepository;
import br.com.copadasautoras.repository.RegistroAuditoriaRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Leitura do log de auditoria para a tela do painel.
 *
 * Só leitura — separado do AuditoriaService (que escreve), pra não
 * misturar responsabilidades nem tocar no que já está no ar.
 *
 * Resolve o nome ATUAL do alvo (nome de exibição da autora / título da
 * obra) a partir do id guardado no registro, em lote, e marca como
 * "removida" quando o id não existe mais.
 */
@Service
@RequiredArgsConstructor
public class AuditoriaConsultaService {

    private final RegistroAuditoriaRepository registroAuditoriaRepository;
    private final AutoraRepository autoraRepository;
    private final SubmissaoRepository submissaoRepository;

    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final int TAMANHO_PADRAO = 25;
    private static final int TAMANHO_MAX = 100;

    @Transactional(readOnly = true)
    public AuditoriaPageDTO consultar(
            OrigemAuditoria origem,
            TipoEntidadeAuditoria entidade,
            Long entidadeId,
            AcaoAuditoria acao,
            LocalDate inicio,
            LocalDate fim,
            int pagina,
            int tamanho
    ) {

        if (tamanho <= 0 || tamanho > TAMANHO_MAX) {
            tamanho = TAMANHO_PADRAO;
        }
        if (pagina < 0) {
            pagina = 0;
        }

        LocalDateTime ini = (inicio != null) ? inicio.atStartOfDay() : null;
        LocalDateTime fimDt = (fim != null) ? fim.atTime(LocalTime.MAX) : null;

        String origemStr = (origem != null) ? origem.name() : null;
        String entidadeStr = (entidade != null) ? entidade.name() : null;
        String acaoStr = (acao != null) ? acao.name() : null;

        long total = registroAuditoriaRepository.contar(
                origemStr, entidadeStr, entidadeId, acaoStr, ini, fimDt);

        int deslocamento = pagina * tamanho;

        List<RegistroAuditoria> registros = registroAuditoriaRepository.buscar(
                origemStr, entidadeStr, entidadeId, acaoStr, ini, fimDt,
                tamanho, deslocamento);

        Map<Long, String> nomesAutora = resolverAutoras(registros);
        Map<Long, String> titulosObra = resolverObras(registros);

        List<RegistroAuditoriaDTO> itens = registros.stream()
                .map(r -> paraDTO(r, nomesAutora, titulosObra))
                .toList();

        boolean temMais = (long) (pagina + 1) * tamanho < total;

        return new AuditoriaPageDTO(itens, pagina, tamanho, total, temMais);
    }

    // =========================
    // RESOLUÇÃO DE NOMES (em lote)
    // =========================

    private Map<Long, String> resolverAutoras(List<RegistroAuditoria> registros) {

        Set<Long> ids = new HashSet<>();
        for (RegistroAuditoria r : registros) {
            if (r.getEntidade() == TipoEntidadeAuditoria.AUTORA
                    && r.getEntidadeId() != null) {
                ids.add(r.getEntidadeId());
            }
        }

        Map<Long, String> nomes = new HashMap<>();
        if (!ids.isEmpty()) {
            autoraRepository.findAllById(ids).forEach(a ->
                    nomes.put(a.getId(), a.getNomeExibicao()));
        }
        return nomes;
    }

    private Map<Long, String> resolverObras(List<RegistroAuditoria> registros) {

        Set<Long> ids = new HashSet<>();
        for (RegistroAuditoria r : registros) {
            if (r.getEntidade() == TipoEntidadeAuditoria.SUBMISSAO
                    && r.getEntidadeId() != null) {
                ids.add(r.getEntidadeId());
            }
        }

        Map<Long, String> titulos = new HashMap<>();
        if (!ids.isEmpty()) {
            submissaoRepository.findAllById(ids).forEach(s ->
                    titulos.put(s.getId(), s.getTitulo()));
        }
        return titulos;
    }

    private RegistroAuditoriaDTO paraDTO(
            RegistroAuditoria r,
            Map<Long, String> nomesAutora,
            Map<Long, String> titulosObra
    ) {

        String alvoNome = null;
        boolean removido = false;

        if (r.getEntidadeId() != null && r.getEntidade() != null) {

            if (r.getEntidade() == TipoEntidadeAuditoria.AUTORA) {
                removido = !nomesAutora.containsKey(r.getEntidadeId());
                alvoNome = nomesAutora.get(r.getEntidadeId());

            } else if (r.getEntidade() == TipoEntidadeAuditoria.SUBMISSAO) {
                removido = !titulosObra.containsKey(r.getEntidadeId());
                alvoNome = titulosObra.get(r.getEntidadeId());
            }
        }

        return new RegistroAuditoriaDTO(
                r.getId(),
                r.getDataHora() != null ? r.getDataHora().format(DATA_HORA) : null,
                r.getOrigem() != null ? r.getOrigem().name() : null,
                r.getAtorNome(),
                r.getEntidade() != null ? r.getEntidade().name() : null,
                r.getEntidadeId(),
                alvoNome,
                removido,
                r.getAcao() != null ? r.getAcao().name() : null,
                r.getValorAnterior(),
                r.getValorNovo(),
                r.getJustificativa()
        );
    }
}
