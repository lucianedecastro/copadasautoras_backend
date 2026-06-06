package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.*;
import br.com.copadasautoras.entity.*;
import br.com.copadasautoras.repository.*;
import br.com.copadasautoras.storage.CloudinaryStorageService;
import br.com.copadasautoras.termo.TermoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SubmissaoService {

    private final SubmissaoRepository submissaoRepository;
    private final AutoraRepository autoraRepository;
    private final EventoRepository eventoRepository;
    private final AceiteTermoRepository aceiteTermoRepository;
    private final ConfrontoRepository confrontoRepository;
    private final CompeticaoRepository competicaoRepository;
    private final CloudinaryStorageService storageService;
    private final TermoService termoService;

    // =========================
    // CRIAR SUBMISSÃO
    // =========================
    @Transactional
    public SubmissaoResponseDTO criar(
            SubmissaoRequestDTO dto,
            MultipartFile arquivoCompleto,
            MultipartFile arquivoPublico
    ) {

        // =====================================================
        // AUTORA AUTENTICADA VIA JWT
        // =====================================================

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Autora autora = autoraRepository
                .findByUsuarioEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Autora autenticada não encontrada."
                        )
                );

        // =====================================================
        // GOVERNANÇA DA AUTORA
        // =====================================================

        if (autora.getStatusAutora()
                != StatusAutora.APROVADA) {

            throw new RuntimeException(
                    "Seu perfil ainda não foi aprovado para realizar submissões."
            );
        }

        // =====================================================
        // EVENTO
        // =====================================================

        Evento evento = eventoRepository
                .findById(dto.eventoId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Evento não encontrado."
                        )
                );

        if (!Boolean.TRUE.equals(
                evento.getAtivo()
        )) {

            throw new RuntimeException(
                    "O evento não está disponível para submissões."
            );
        }

        // =====================================================
        // REGRA:
        // UMA AUTORA = UMA SUBMISSÃO POR EVENTO
        // =====================================================

        boolean jaExisteSubmissao =
                submissaoRepository
                        .existsByAutoraIdAndEventoId(
                                autora.getId(),
                                evento.getId()
                        );

        if (jaExisteSubmissao) {

            throw new RuntimeException(
                    "Você já possui uma obra inscrita neste evento."
            );
        }

        // =====================================================
        // UPLOAD DA OBRA
        // =====================================================

        String urlCompleta =
                storageService.uploadObra(
                        arquivoCompleto,
                        autora.getId()
                );

        String urlPublica =
                arquivoPublico != null
                        ? storageService.uploadObraPublica(
                        arquivoPublico,
                        autora.getId()
                )
                        : null;

        // =====================================================
        // SALVA SUBMISSÃO
        // =====================================================

        Submissao submissao =
                Submissao.builder()
                        .titulo(dto.titulo())
                        .categoria(dto.categoria())
                        .descricao(dto.descricao())
                        .tipoExibicao(
                                dto.tipoExibicao()
                        )
                        .arquivoCompletoUrl(
                                urlCompleta
                        )
                        .arquivoPublicoUrl(
                                urlPublica
                        )
                        .autora(autora)
                        .evento(evento)
                        .build();

        submissao =
                submissaoRepository
                        .save(submissao);

        // =====================================================
        // REGISTRO DE ACEITE
        // =====================================================

        AceiteTermo aceite =
                AceiteTermo.builder()
                        .autora(autora)
                        .submissao(submissao)
                        .aceiteAutoria(
                                dto.aceiteAutoria()
                        )
                        .aceiteExibicao(
                                dto.aceiteExibicao()
                        )
                        .aceiteBanca(
                                dto.aceiteBanca()
                        )
                        .aceiteTitularidade(
                                dto.aceiteTitularidade()
                        )
                        .aceiteTermoCompleto(
                                dto.aceiteTermoCompleto()
                        )
                        .versaoTermo("1.0")
                        .build();

        aceite =
                aceiteTermoRepository
                        .save(aceite);

        // =====================================================
        // PDF DO TERMO
        // =====================================================

        try {

            byte[] pdfBytes =
                    termoService.gerarTermoPdf(
                            aceite,
                            submissao
                    );

            String urlTermo =
                    storageService.uploadTermoPdf(
                            pdfBytes,
                            submissao.getId()
                    );

            aceite.setTermoPdfUrl(
                    urlTermo
            );

            aceiteTermoRepository
                    .save(aceite);

        } catch (Exception e) {

            System.err.println(
                    "Aviso: falha ao gerar PDF do termo da submissão "
                            + submissao.getId()
                            + ": "
                            + e.getMessage()
            );
        }

        return mapToResponse(
                submissao
        );
    }

    // =========================
// CONSULTAR MINHA SUBMISSÃO
// =========================
    @Transactional(readOnly = true)
    public SubmissaoResponseDTO buscarMinhaSubmissao() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Autora autora = autoraRepository
                .findByUsuarioEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Autora autenticada não encontrada."
                        )
                );

        Submissao submissao =
                submissaoRepository
                        .findFirstByAutoraId(
                                autora.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Autora ainda não possui submissão."
                                )
                        );

        return mapToResponse(
                submissao
        );
    }

    // =========================
    // GERAR CONFRONTOS
    // =========================
    @Transactional
    public void gerarConfrontos(FaseCompeticao fase) {

        if (fase == FaseCompeticao.FASE_32) {
            throw new RuntimeException(
                    "A FASE_32 utiliza grupos e não confrontos."
            );
        }

        Competicao competicao = obterCompeticao();

        if (!competicao.getFaseAtual().equals(fase)) {
            throw new RuntimeException(
                    "Fase solicitada é diferente da fase atual da competição"
            );
        }

        if (competicao.getStatusFase()
                != StatusFase.EM_ANDAMENTO) {

            throw new RuntimeException(
                    "A fase não está em andamento"
            );
        }

        if (confrontoRepository.existsByFase(fase)) {
            throw new RuntimeException(
                    "Confrontos desta fase já foram gerados"
            );
        }

        List<Submissao> lista =
                submissaoRepository.findByFaseAtual(fase);

        if (lista.isEmpty()) {
            throw new RuntimeException(
                    "Nenhuma submissão encontrada para esta fase"
            );
        }

        Collections.shuffle(lista);

        for (int i = 0; i < lista.size(); i += 2) {

            Submissao casa = lista.get(i);

            Submissao fora =
                    (i + 1 < lista.size())
                            ? lista.get(i + 1)
                            : null;

            Confronto confronto =
                    Confronto.builder()
                            .fase(fase)
                            .casa(casa)
                            .fora(fora)
                            .resolvido(false)
                            .build();

            confrontoRepository.save(confronto);
        }
    }

    // =========================
    // RESOLVER CONFRONTO
    // =========================
    @Transactional
    public void resolverConfronto(
            Long confrontoId,
            Long vencedorId
    ) {

        Confronto confronto =
                confrontoRepository.findById(
                                confrontoId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Confronto não encontrado"
                                )
                        );

        if (Boolean.TRUE.equals(
                confronto.getResolvido()
        )) {

            throw new RuntimeException(
                    "Confronto já resolvido"
            );
        }

        Competicao competicao =
                obterCompeticao();

        if (competicao.getStatusFase()
                != StatusFase.EM_ANDAMENTO) {

            throw new RuntimeException(
                    "A fase não está em andamento"
            );
        }

        if (!competicao.getFaseAtual()
                .equals(confronto.getFase())) {

            throw new RuntimeException(
                    "Confronto não pertence à fase atual"
            );
        }

        Submissao casa =
                confronto.getCasa();

        Submissao fora =
                confronto.getFora();

        Submissao vencedora;
        Submissao perdedora;

        if (casa.getId()
                .equals(vencedorId)) {

            vencedora = casa;
            perdedora = fora;

        } else if (fora != null
                && fora.getId()
                .equals(vencedorId)) {

            vencedora = fora;
            perdedora = casa;

        } else {

            throw new RuntimeException(
                    "Vencedor inválido: id não pertence a nenhum dos participantes"
            );
        }

        if (perdedora != null) {

            perdedora.setStatus(
                    StatusSubmissao.ELIMINADA
            );

            submissaoRepository.save(
                    perdedora
            );
        }

        FaseCompeticao proximaFase =
                vencedora.getFaseAtual()
                        .proxima();

        vencedora.setFaseAtual(
                proximaFase
        );

        /**
         * FINAL permanece sigilosa.
         * Não revelamos CAMPEA automaticamente.
         */
        vencedora.setStatus(
                StatusSubmissao.CLASSIFICADA
        );

        submissaoRepository.save(
                vencedora
        );

        confronto.setVencedora(
                vencedora
        );

        confronto.setResolvido(
                true
        );

        confrontoRepository.save(
                confronto
        );

        verificarEncerramentoDaFase(
                competicao
        );
    }

    // =========================
    // ENCERRAR FASE
    // =========================
    private void verificarEncerramentoDaFase(
            Competicao competicao
    ) {

        boolean existePendente =
                confrontoRepository
                        .findByFase(
                                competicao.getFaseAtual()
                        )
                        .stream()
                        .anyMatch(c ->
                                !Boolean.TRUE.equals(
                                        c.getResolvido()
                                )
                        );

        if (!existePendente) {

            /**
             * Admin controla avanço.
             * Não avançamos automaticamente.
             */
            competicao.setStatusFase(
                    StatusFase.ENCERRADA
            );

            competicaoRepository.save(
                    competicao
            );
        }
    }

    // =========================
    // CONSULTAR FASE
    // =========================
    public FaseResponseDTO obterFase(
            FaseCompeticao fase
    ) {

        List<Confronto> confrontos =
                confrontoRepository.findByFase(
                        fase
                );

        List<ConfrontoResponseDTO> confrontoDTOs =
                confrontos.stream()
                        .map(this::mapConfronto)
                        .toList();

        List<Submissao> lista =
                submissaoRepository.findByFaseAtual(
                        fase
                );

        List<SubmissaoResponseDTO> classificadas =
                lista.stream()
                        .filter(s ->
                                s.getStatus()
                                        == StatusSubmissao.CLASSIFICADA
                                        || s.getStatus()
                                        == StatusSubmissao.CAMPEA
                        )
                        .map(this::mapToResponse)
                        .toList();

        List<SubmissaoResponseDTO> eliminadas =
                lista.stream()
                        .filter(s ->
                                s.getStatus()
                                        == StatusSubmissao.ELIMINADA
                        )
                        .map(this::mapToResponse)
                        .toList();

        return new FaseResponseDTO(
                fase,
                lista.size(),
                confrontoDTOs,
                classificadas,
                eliminadas
        );
    }

    // =========================
    // HELPERS
    // =========================
    private Competicao obterCompeticao() {

        return competicaoRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Competição não iniciada"
                        )
                );
    }

    private ConfrontoResponseDTO mapConfronto(
            Confronto c
    ) {

        return new ConfrontoResponseDTO(
                c.getId(),
                mapToResponse(
                        c.getCasa()
                ),
                c.getFora() != null
                        ? mapToResponse(
                        c.getFora()
                )
                        : null,
                c.getVencedora() != null
                        ? mapToResponse(
                        c.getVencedora()
                )
                        : null,
                c.getResolvido()
        );
    }

    private SubmissaoResponseDTO mapToResponse(
            Submissao submissao
    ) {

        String baseUrl =
                "/submissoes/"
                        + submissao.getId();

        return new SubmissaoResponseDTO(
                submissao.getId(),
                submissao.getTitulo(),
                submissao.getCategoria(),
                submissao.getTipoExibicao(),
                submissao.getStatus(),
                submissao.getFaseAtual(),
                submissao.getAutora().getId(),
                submissao.getEvento().getId(),
                submissao.getArquivoPublicoUrl() != null
                        ? baseUrl
                        + "/arquivo-publico"
                        : null,
                baseUrl
                        + "/arquivo-completo"
        );
    }
}
