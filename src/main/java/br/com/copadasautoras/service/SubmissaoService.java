package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.*;
import br.com.copadasautoras.entity.*;
import br.com.copadasautoras.repository.*;
import br.com.copadasautoras.storage.CloudinaryStorageService;
import br.com.copadasautoras.termo.TermoService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final UsuarioRepository usuarioRepository;
    private final GrupoCompeticaoRepository grupoCompeticaoRepository;
    private final CloudinaryStorageService storageService;
    private final TermoService termoService;
    private final ApplicationEventPublisher eventPublisher;

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

            // Agenda o e-mail do termo para DEPOIS do commit da transação.
            // Só chega aqui se o PDF foi gerado — o pdfBytes vai anexo.
            eventPublisher.publishEvent(new SubmissaoRegistradaEvent(
                    email,
                    autora.getNomeExibicao(),
                    pdfBytes
            ));

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
    // JANELA DE EDIÇÃO
    // =========================

    /**
     * Recupera a submissão da autora autenticada e verifica se ela ainda
     * pode ser alterada.
     *
     * A janela de edição é a MESMA janela de criação:
     *   - status == SUBMETIDA  → a curadoria ainda não decidiu sobre a obra
     *   - evento ativo         → o prazo de inscrições não encerrou
     *
     * Depois que a obra entra em competição, trocar o arquivo corromperia
     * a avaliação cega (Art. 17) e excluí-la quebraria o chaveamento.
     * O portão é o mesmo dos dois lados: só se desfaz enquanto ainda
     * daria para fazer.
     */
    private Submissao buscarSubmissaoEditavel() {

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

        Submissao submissao = submissaoRepository
                .findFirstByAutoraId(autora.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Você ainda não possui uma obra inscrita."
                        )
                );

        if (submissao.getStatus() != StatusSubmissao.SUBMETIDA) {
            throw new RuntimeException(
                    "Sua obra já passou pela curadoria e está em competição. "
                            + "Não é mais possível alterá-la ou excluí-la. "
                            + "Fale com a organização."
            );
        }

        if (!Boolean.TRUE.equals(submissao.getEvento().getAtivo())) {
            throw new RuntimeException(
                    "O prazo de inscrições encerrou. "
                            + "Não é mais possível alterar ou excluir sua obra."
            );
        }

        return submissao;
    }

    // =========================
    // EDITAR DADOS DA SUBMISSÃO
    // =========================

    /**
     * Corrige os dados da obra já submetida: título, categoria, descrição
     * e tipo de exibição.
     *
     * Os cinco aceites do Art. 19 NÃO são pedidos de novo. Eles foram
     * declarados no envio e continuam valendo — corrigir um título não é
     * declarar novamente a autoria.
     *
     * Mas o termo de aceite imprime Título, Categoria e Modalidade de
     * exibição. Se a autora corrige o título e o termo continua exibindo o
     * antigo, o documento jurídico passa a mentir. Por isso ele é regerado
     * aqui, com o mesmo AceiteTermo (mesmos aceites, mesma versão).
     */
    @Transactional
    public SubmissaoResponseDTO editarMinhaSubmissao(
            SubmissaoUpdateDTO dto
    ) {

        Submissao submissao = buscarSubmissaoEditavel();

        submissao.setTitulo(dto.titulo());
        submissao.setCategoria(dto.categoria());
        submissao.setDescricao(dto.descricao());
        submissao.setTipoExibicao(dto.tipoExibicao());

        submissao = submissaoRepository.save(submissao);

        regerarTermo(submissao);

        return mapToResponse(submissao);
    }

    /**
     * Regera o PDF do termo com os dados atualizados da obra.
     *
     * Falha aqui não derruba a edição: o registro de aceite continua no
     * banco, íntegro, e ele é a fonte de verdade — o PDF é uma
     * materialização dele. Um PDF desatualizado é um problema a corrigir;
     * uma edição que não salva é um problema para a autora agora.
     * Mesmo critério do criar(), que já trata a geração do termo como
     * best-effort.
     */
    private void regerarTermo(Submissao submissao) {

        aceiteTermoRepository
                .findBySubmissaoId(submissao.getId())
                .ifPresent(aceite -> {

                    String urlAntiga = aceite.getTermoPdfUrl();

                    try {
                        byte[] pdfBytes = termoService.gerarTermoPdf(
                                aceite,
                                submissao
                        );

                        String urlNova = storageService.uploadTermoPdf(
                                pdfBytes,
                                submissao.getId()
                        );

                        aceite.setTermoPdfUrl(urlNova);
                        aceiteTermoRepository.save(aceite);

                        // Só apaga o antigo depois que o novo está salvo.
                        apagarDoStorage(urlAntiga, "termo anterior da submissão "
                                + submissao.getId());

                    } catch (Exception e) {
                        System.err.println(
                                "Aviso: falha ao regerar o termo da submissão "
                                        + submissao.getId()
                                        + ": "
                                        + e.getMessage()
                        );
                    }
                });
    }

    // =========================
    // SUBSTITUIR ARQUIVOS (Art. 18 §3º)
    // =========================

    /**
     * Troca o arquivo completo e/ou o arquivo público da submissão,
     * preservando o id e a dataSubmissao originais.
     *
     * Preserva de propósito: recriar a submissão daria um id novo e uma
     * data de envio nova — o que poderia jogar a autora para depois do
     * prazo e alterar a ordem de inscrição.
     *
     * A ordem das operações importa. O upload novo vem PRIMEIRO: se ele
     * falhar, nada mudou. Só depois de o banco estar consistente é que
     * apagamos o arquivo antigo — e uma falha ali vira log, não erro,
     * porque o pior resultado possível é um arquivo órfão no Cloudinary,
     * nunca uma submissão sem arquivo.
     */
    @Transactional
    public SubmissaoResponseDTO substituirArquivos(
            MultipartFile arquivoCompleto,
            MultipartFile arquivoPublico
    ) {

        boolean trocaCompleto =
                arquivoCompleto != null && !arquivoCompleto.isEmpty();

        boolean trocaPublico =
                arquivoPublico != null && !arquivoPublico.isEmpty();

        if (!trocaCompleto && !trocaPublico) {
            throw new RuntimeException(
                    "Envie ao menos um arquivo para substituir."
            );
        }

        Submissao submissao = buscarSubmissaoEditavel();
        Long autoraId = submissao.getAutora().getId();

        String urlAntigaCompleta = submissao.getArquivoCompletoUrl();
        String urlAntigaPublica  = submissao.getArquivoPublicoUrl();

        // 1. Sobe o novo. Se falhar aqui, nada foi alterado.
        if (trocaCompleto) {
            submissao.setArquivoCompletoUrl(
                    storageService.uploadObra(arquivoCompleto, autoraId)
            );
        }

        if (trocaPublico) {
            submissao.setArquivoPublicoUrl(
                    storageService.uploadObraPublica(arquivoPublico, autoraId)
            );
        }

        // 2. Banco consistente.
        submissao = submissaoRepository.save(submissao);

        // 3. Só então apaga o antigo. Falha aqui não desfaz a troca.
        if (trocaCompleto) {
            apagarDoStorage(urlAntigaCompleta, "arquivo completo anterior");
        }

        if (trocaPublico) {
            apagarDoStorage(urlAntigaPublica, "arquivo público anterior");
        }

        return mapToResponse(submissao);
    }

    // =========================
    // EXCLUIR MINHA SUBMISSÃO
    // =========================

    /**
     * Exclui a submissão da autora autenticada, junto com o AceiteTermo
     * e todos os arquivos no Cloudinary.
     *
     * O termo é apagado junto: se a obra não existe, o aceite dela não
     * tem o que comprovar. Não sobra rastro — e é isso mesmo que se
     * espera, inclusive do ponto de vista de proteção de dados.
     */
    @Transactional
    public void excluirMinhaSubmissao() {

        Submissao submissao = buscarSubmissaoEditavel();
        Long submissaoId = submissao.getId();

        // Guarda extra: se por algum caminho a obra já estiver num grupo,
        // não é seguro apagá-la, mesmo que o status ainda diga SUBMETIDA.
        if (submissao.getGrupo() != null) {
            throw new RuntimeException(
                    "Sua obra já foi sorteada em um grupo da competição. "
                            + "Fale com a organização."
            );
        }

        // Guarda as URLs antes de apagar as linhas do banco.
        List<String> arquivosParaApagar = new ArrayList<>();
        arquivosParaApagar.add(submissao.getArquivoCompletoUrl());
        arquivosParaApagar.add(submissao.getArquivoPublicoUrl());

        aceiteTermoRepository
                .findBySubmissaoId(submissaoId)
                .ifPresent(aceite -> {
                    arquivosParaApagar.add(aceite.getTermoPdfUrl());
                    aceiteTermoRepository.delete(aceite);
                });

        submissaoRepository.delete(submissao);

        // O Cloudinary não participa da transação. Se o banco desse
        // rollback depois de apagarmos os arquivos, a linha continuaria
        // existindo apontando para arquivos inexistentes — o pior cenário.
        // Por isso: banco primeiro, arquivos por último, falha vira log.
        arquivosParaApagar.forEach(url ->
                apagarDoStorage(url, "arquivo da submissão " + submissaoId)
        );
    }

    /**
     * Apaga do Cloudinary sem derrubar a transação.
     *
     * Um arquivo órfão é um incômodo de faxina. Uma submissão sem arquivo,
     * ou uma exclusão que falha pela metade, é um problema de verdade.
     */
    private void apagarDoStorage(String url, String descricao) {

        if (url == null || url.isBlank()) {
            return;
        }

        try {
            storageService.deletar(url);
        } catch (Exception e) {
            System.err.println(
                    "Aviso: falha ao apagar do Cloudinary ("
                            + descricao
                            + "): "
                            + e.getMessage()
            );
        }
    }

    // =========================
    // EXCLUIR SUBMISSÃO (ADMIN)
    // =========================

    /**
     * Exclui uma submissão pelo ID — uso administrativo.
     *
     * Espelha a exclusão da autora (remove AceiteTermo + arquivos no
     * Cloudinary + a obra), mas identificada por ID e sem exigir que
     * seja a obra da pessoa autenticada. Reaproveita apagarDoStorage.
     *
     * Trava de integridade: só exclui obra FORA da competição
     * (SUBMETIDA ou NAO_SELECIONADA) e sem grupo. Excluir uma obra em
     * disputa quebraria o chaveamento.
     */
    @Transactional
    public void excluirSubmissaoAdmin(Long submissaoId) {

        Submissao submissao = submissaoRepository.findById(submissaoId)
                .orElseThrow(() ->
                        new RuntimeException("Submissão não encontrada."));

        StatusSubmissao status = submissao.getStatus();
        boolean foraDaCompeticao =
                status == StatusSubmissao.SUBMETIDA
                        || status == StatusSubmissao.NAO_SELECIONADA;

        if (!foraDaCompeticao || submissao.getGrupo() != null) {
            throw new RuntimeException(
                    "Só é possível excluir obras fora da competição "
                            + "(submetidas ou não selecionadas). Esta obra "
                            + "está em disputa — excluí-la quebraria o chaveamento."
            );
        }

        List<String> arquivosParaApagar = new ArrayList<>();
        arquivosParaApagar.add(submissao.getArquivoCompletoUrl());
        arquivosParaApagar.add(submissao.getArquivoPublicoUrl());

        aceiteTermoRepository
                .findBySubmissaoId(submissaoId)
                .ifPresent(aceite -> {
                    arquivosParaApagar.add(aceite.getTermoPdfUrl());
                    aceiteTermoRepository.delete(aceite);
                });

        submissaoRepository.delete(submissao);

        arquivosParaApagar.forEach(url ->
                apagarDoStorage(url, "arquivo da submissão " + submissaoId)
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

        if (fase == FaseCompeticao.FINAL) {
            throw new RuntimeException(
                    "A FINAL não utiliza confrontos — use o sorteio das juradas da final."
            );
        }

        Competicao competicao = obterCompeticao();

        if (!competicao.getFaseAtual().equals(fase)) {
            throw new RuntimeException(
                    "Fase solicitada é diferente da fase atual da competição"
            );
        }

        // O fluxo real do admin (AdminService.avancarFaseManual) já deixa
        // a fase em EM_ANDAMENTO ao avançar — é nesse estado que os
        // confrontos devem ser gerados.
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

        // Grupos de uma geração anterior desta mesma fase (ex: de uma
        // regeneração). Serão removidos só depois que as obras forem
        // reatribuídas aos grupos novos, pra não violar FK.
        List<GrupoCompeticao> gruposAntigos =
                grupoCompeticaoRepository.findByFase(fase);

        List<Submissao> lista =
                submissaoRepository.findByFaseAtualAndStatus(
                        fase,
                        StatusSubmissao.CLASSIFICADA
                );

        if (lista.isEmpty()) {
            throw new RuntimeException(
                    "Nenhuma submissão classificada encontrada para esta fase"
            );
        }

        if (lista.size() % 2 != 0) {
            throw new RuntimeException(
                    "Número ímpar de obras classificadas ("
                            + lista.size()
                            + ") — não é possível formar confrontos."
            );
        }

        int totalConfrontos = lista.size() / 2;

        List<Usuario> bancas =
                usuarioRepository.findByRole(Role.BANCA);

        if (bancas.size() < totalConfrontos) {
            throw new RuntimeException(
                    "É necessário ao menos "
                            + totalConfrontos
                            + " juradas cadastradas para sortear os confrontos desta fase."
            );
        }

        Collections.shuffle(lista);
        Collections.shuffle(bancas);

        // Nomenclatura de confronto (mata-mata), separada das letras
        // A-H usadas pelos grupos de 4 da FASE_32.
        NomeGrupo[] nomes = {
                NomeGrupo.CONFRONTO_1, NomeGrupo.CONFRONTO_2,
                NomeGrupo.CONFRONTO_3, NomeGrupo.CONFRONTO_4,
                NomeGrupo.CONFRONTO_5, NomeGrupo.CONFRONTO_6,
                NomeGrupo.CONFRONTO_7, NomeGrupo.CONFRONTO_8
        };

        for (int i = 0; i < totalConfrontos; i++) {

            Submissao casa = lista.get(i * 2);
            Submissao fora = lista.get(i * 2 + 1);

            Usuario banca = bancas.get(i);

            GrupoCompeticao grupo =
                    GrupoCompeticao.builder()
                            .nomeGrupo(nomes[i % nomes.length])
                            .fase(fase)
                            .banca(banca)
                            .build();

            grupo = grupoCompeticaoRepository.save(grupo);

            casa.setGrupo(grupo);
            fora.setGrupo(grupo);

            submissaoRepository.save(casa);
            submissaoRepository.save(fora);

            Confronto confronto =
                    Confronto.builder()
                            .fase(fase)
                            .casa(casa)
                            .fora(fora)
                            .resolvido(false)
                            .build();

            confrontoRepository.save(confronto);
        }

        // Agora sim — nenhuma obra aponta mais pros grupos antigos
        // (todas foram reatribuídas aos grupos novos acima).
        if (!gruposAntigos.isEmpty()) {
            grupoCompeticaoRepository.deleteAll(gruposAntigos);
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

        if (fase == FaseCompeticao.FASE_32) {
            return obterFaseGrupos(fase);
        }

        if (fase == FaseCompeticao.FINAL) {
            return obterFaseFinal();
        }

        return obterFaseConfrontos(fase);
    }

    private FaseResponseDTO obterFaseConfrontos(
            FaseCompeticao fase
    ) {

        List<Confronto> confrontos =
                confrontoRepository.findByFase(fase);

        List<ConfrontoResponseDTO> confrontoDTOs =
                confrontos.stream()
                        .map(this::mapConfronto)
                        .toList();

        // Classificadas/eliminadas derivadas direto do resultado de cada
        // confronto — não do faseAtual da obra, que já avança pra próxima
        // fase assim que ela é classificada (e por isso some do
        // findByFaseAtual da fase atual, deixando essa lista sempre vazia).
        List<SubmissaoResponseDTO> classificadas =
                confrontos.stream()
                        .map(Confronto::getVencedora)
                        .filter(Objects::nonNull)
                        .map(this::mapToResponse)
                        .toList();

        List<SubmissaoResponseDTO> eliminadas =
                confrontos.stream()
                        .filter(c -> c.getVencedora() != null)
                        .map(c ->
                                c.getVencedora().getId().equals(c.getCasa().getId())
                                        ? c.getFora()
                                        : c.getCasa()
                        )
                        .filter(Objects::nonNull)
                        .map(this::mapToResponse)
                        .toList();

        return new FaseResponseDTO(
                fase,
                confrontoDTOs.size(),
                confrontoDTOs,
                classificadas,
                eliminadas,
                List.of()
        );
    }

    private FaseResponseDTO obterFaseGrupos(
            FaseCompeticao fase
    ) {

        // FASE_32 não usa Confronto (são grupos de 4 obras, não 1x1) —
        // monta os grupos completos pro admin ver quem avançou de cada.
        List<GrupoCompeticao> gruposEntities =
                grupoCompeticaoRepository.findByFase(fase);

        List<GrupoPublicoDTO> grupos =
                gruposEntities.stream()
                        .sorted(Comparator.comparing(g -> g.getNomeGrupo().name()))
                        .map(this::mapGrupo)
                        .toList();

        List<SubmissaoResponseDTO> classificadas = new ArrayList<>();
        List<SubmissaoResponseDTO> eliminadas = new ArrayList<>();

        for (GrupoCompeticao grupo : gruposEntities) {

            for (Submissao s : submissaoRepository.findByGrupoId(grupo.getId())) {

                if (s.getStatus() == StatusSubmissao.CLASSIFICADA
                        || s.getStatus() == StatusSubmissao.CAMPEA) {

                    classificadas.add(mapToResponse(s));

                } else if (s.getStatus() == StatusSubmissao.ELIMINADA) {

                    eliminadas.add(mapToResponse(s));
                }
            }
        }

        return new FaseResponseDTO(
                fase,
                0,
                List.of(),
                classificadas,
                eliminadas,
                grupos
        );
    }

    private FaseResponseDTO obterFaseFinal() {

        // FINAL não usa Confronto nem GrupoCompeticao pareado — são 3
        // juradas sorteadas vendo as mesmas 2 obras e votando. Mesmo
        // truque: junta CLASSIFICADA (ainda em disputa) + ELIMINADA
        // (perdeu) + CAMPEA (ganhou), que juntas são sempre as 2
        // finalistas, antes ou depois da revelação.
        List<Submissao> finalistas = new ArrayList<>();

        finalistas.addAll(
                submissaoRepository.findByFaseAtualAndStatus(
                        FaseCompeticao.FINAL, StatusSubmissao.CLASSIFICADA
                )
        );
        finalistas.addAll(
                submissaoRepository.findByFaseAtualAndStatus(
                        FaseCompeticao.FINAL, StatusSubmissao.ELIMINADA
                )
        );
        finalistas.addAll(
                submissaoRepository.findByFaseAtualAndStatus(
                        FaseCompeticao.CAMPEA, StatusSubmissao.CAMPEA
                )
        );

        List<SubmissaoResponseDTO> classificadas =
                finalistas.stream()
                        .filter(s ->
                                s.getStatus() == StatusSubmissao.CLASSIFICADA
                                        || s.getStatus() == StatusSubmissao.CAMPEA
                        )
                        .map(this::mapToResponse)
                        .toList();

        List<SubmissaoResponseDTO> eliminadas =
                finalistas.stream()
                        .filter(s -> s.getStatus() == StatusSubmissao.ELIMINADA)
                        .map(this::mapToResponse)
                        .toList();

        // Sintetiza 1 "confronto" (id -1, não existe no banco) só pra
        // reaproveitar a mesma tabela casa/fora/vencedora que o admin
        // já usa pras outras fases.
        List<ConfrontoResponseDTO> confrontos = List.of();

        if (finalistas.size() == 2) {

            Submissao casa = finalistas.get(0);
            Submissao fora = finalistas.get(1);

            Submissao vencedora =
                    finalistas.stream()
                            .filter(s -> s.getStatus() == StatusSubmissao.CAMPEA)
                            .findFirst()
                            .orElse(null);

            confrontos = List.of(new ConfrontoResponseDTO(
                    -1L,
                    mapToResponse(casa),
                    mapToResponse(fora),
                    vencedora != null ? mapToResponse(vencedora) : null,
                    vencedora != null
            ));
        }

        return new FaseResponseDTO(
                FaseCompeticao.FINAL,
                confrontos.size(),
                confrontos,
                classificadas,
                eliminadas,
                List.of()
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

    private GrupoPublicoDTO mapGrupo(
            GrupoCompeticao grupo
    ) {

        List<ObraPublicaDTO> obras =
                submissaoRepository.findByGrupoId(grupo.getId())
                        .stream()
                        .map(s -> new ObraPublicaDTO(
                                s.getId(),
                                s.getTitulo(),
                                s.getCategoria(),
                                s.getStatus() == StatusSubmissao.CLASSIFICADA
                                        || s.getStatus() == StatusSubmissao.CAMPEA
                        ))
                        .toList();

        return new GrupoPublicoDTO(
                grupo.getNomeGrupo().name(),
                obras
        );
    }
}