package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.AdminDashboardDTO;
import br.com.copadasautoras.dto.CreateUsuarioRequest;
import br.com.copadasautoras.dto.UpdateUsuarioAdminRequest;
import br.com.copadasautoras.dto.UsuarioAdminResponseDTO;
import br.com.copadasautoras.entity.*;
import br.com.copadasautoras.repository.CompeticaoRepository;
import br.com.copadasautoras.repository.ConfrontoRepository;
import br.com.copadasautoras.repository.GrupoCompeticaoRepository;
import br.com.copadasautoras.repository.UsuarioRepository;
import br.com.copadasautoras.repository.VotoFinalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.copadasautoras.dto.SelecaoEdicaoRequest;
import br.com.copadasautoras.dto.SelecaoEdicaoResponseDTO;
import br.com.copadasautoras.repository.EventoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import br.com.copadasautoras.dto.SubmissaoResponseDTO;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CompeticaoRepository competicaoRepository;
    private final ConfrontoRepository confrontoRepository;
    private final SubmissaoService submissaoService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventoRepository eventoRepository;
    private final SubmissaoRepository submissaoRepository;
    private final GrupoCompeticaoRepository grupoCompeticaoRepository;
    private final VotoFinalRepository votoFinalRepository;

    // =========================
    // 👤 GERENCIAMENTO DE USUÁRIOS
    // =========================

    @Transactional
    public void criarUsuario(CreateUsuarioRequest request) {

        boolean usuarioExiste = usuarioRepository
                .findByEmail(request.email())
                .isPresent();

        if (usuarioExiste) {
            throw new RuntimeException(
                    "Já existe um usuário com este email."
            );
        }

        // Segurança:
        // AUTORA deve usar cadastro público
        if (request.role() == Role.AUTORA) {
            throw new RuntimeException(
                    "AUTORA deve utilizar o endpoint público de cadastro."
            );
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(
                        passwordEncoder.encode(
                                request.senha()
                        )
                )
                .role(request.role())
                .build();

        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponseDTO>
    listarUsuariosAdministrativos() {

        return usuarioRepository.findAll()
                .stream()
                .filter(usuario ->
                        usuario.getRole() == Role.ADMIN
                                || usuario.getRole() == Role.BANCA
                )
                .map(usuario ->
                        new UsuarioAdminResponseDTO(
                                usuario.getId(),
                                usuario.getNome(),
                                usuario.getEmail(),
                                usuario.getRole()
                        )
                )
                .toList();
    }

    // =========================
    // 🔍 BUSCAR USUÁRIO POR ID
    // =========================

    @Transactional(readOnly = true)
    public UsuarioAdminResponseDTO
    buscarUsuarioAdministrativo(Long id) {

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuário não encontrado"
                        )
                );

        if (usuario.getRole() != Role.ADMIN
                && usuario.getRole() != Role.BANCA) {

            throw new RuntimeException(
                    "Usuário não é administrativo"
            );
        }

        return new UsuarioAdminResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole()
        );
    }

    // =========================
    // ✏️ ATUALIZAR USUÁRIO
    // =========================

    @Transactional
    public void atualizarUsuarioAdministrativo(
            Long id,
            UpdateUsuarioAdminRequest request
    ) {

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuário não encontrado"
                        )
                );

        if (usuario.getRole() != Role.ADMIN
                && usuario.getRole() != Role.BANCA) {

            throw new RuntimeException(
                    "Usuário não é administrativo"
            );
        }

        boolean emailEmUso = usuarioRepository
                .findByEmail(request.email())
                .filter(u -> !u.getId().equals(id))
                .isPresent();

        if (emailEmUso) {
            throw new RuntimeException(
                    "Já existe um usuário com este email."
            );
        }

        // Segurança:
        // AUTORA nunca pode ser editada aqui
        if (request.role() == Role.AUTORA) {
            throw new RuntimeException(
                    "AUTORA deve utilizar o fluxo público."
            );
        }

        usuario.setNome(
                request.nome()
        );

        usuario.setEmail(
                request.email()
        );

        usuario.setRole(
                request.role()
        );

        // senha opcional
        if (request.senha() != null
                && !request.senha().isBlank()) {

            usuario.setSenha(
                    passwordEncoder.encode(
                            request.senha()
                    )
            );
        }

        usuarioRepository.save(usuario);
    }

    // =========================
    // 🗑️ DELETAR USUÁRIO
    // =========================

    @Transactional
    public void deletarUsuarioAdministrativo(
            Long id,
            Authentication authentication
    ) {

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuário não encontrado"
                        )
                );

        if (usuario.getRole() != Role.ADMIN
                && usuario.getRole() != Role.BANCA) {

            throw new RuntimeException(
                    "Usuário não é administrativo"
            );
        }

        // impede auto exclusão
        if (usuario.getEmail()
                .equals(authentication.getName())) {

            throw new RuntimeException(
                    "Você não pode excluir seu próprio usuário"
            );
        }

        // impede apagar último admin
        if (usuario.getRole() == Role.ADMIN) {

            long totalAdmins =
                    usuarioRepository.findAll()
                            .stream()
                            .filter(u ->
                                    u.getRole()
                                            == Role.ADMIN
                            )
                            .count();

            if (totalAdmins <= 1) {
                throw new RuntimeException(
                        "Não é possível excluir o último ADMIN do sistema"
                );
            }
        }

        usuarioRepository.delete(usuario);
    }

    // =========================
    // 📊 DASHBOARD
    // =========================

    public AdminDashboardDTO obterDashboard() {

        Competicao competicao = obterCompeticao();

        FaseCompeticao fase = competicao.getFaseAtual();

        int total;
        int resolvidos;

        if (fase == FaseCompeticao.FASE_32) {

            // FASE_32 usa grupos de 4 obras, não confrontos 1x1 —
            // cada grupo "concluído" significa que a jurada já decidiu
            // (nenhuma obra do grupo segue EM_COMPETICAO).
            List<GrupoCompeticao> grupos =
                    grupoCompeticaoRepository.findByFase(fase);

            total = grupos.size();

            resolvidos = (int) grupos.stream()
                    .filter(this::grupoJaDecidido)
                    .count();

        } else if (fase == FaseCompeticao.FINAL) {

            // FINAL não usa confronto nem grupo pareado — são 3 juradas
            // sorteadas, cada uma vota uma vez.
            total = grupoCompeticaoRepository
                    .findByFase(fase)
                    .size();

            resolvidos = (int) votoFinalRepository.count();

        } else {

            // OITAVAS, QUARTAS, SEMIFINAL — modelo de confronto 1x1.
            List<Confronto> confrontos =
                    confrontoRepository.findByFase(fase);

            total = confrontos.size();

            resolvidos = (int) confrontos.stream()
                    .filter(c ->
                            Boolean.TRUE.equals(
                                    c.getResolvido()
                            )
                    )
                    .count();
        }

        return new AdminDashboardDTO(
                fase,
                competicao.getStatusFase(),
                total,
                resolvidos,
                total - resolvidos
        );
    }

    private boolean grupoJaDecidido(GrupoCompeticao grupo) {

        return submissaoRepository
                .findByGrupoId(grupo.getId())
                .stream()
                .noneMatch(s ->
                        s.getStatus() == StatusSubmissao.EM_COMPETICAO
                );
    }

    // =========================
    // 🔓 REABRIR FASE
    // =========================

    @Transactional
    public void reabrirFase() {

        Competicao competicao = obterCompeticao();

        if (competicao.getStatusFase()
                != StatusFase.ENCERRADA) {

            throw new RuntimeException(
                    "Só é possível reabrir uma fase encerrada"
            );
        }

        competicao.setStatusFase(
                StatusFase.EM_ANDAMENTO
        );

        competicaoRepository.save(
                competicao
        );
    }

    // =========================
    // ⏭️ FORÇAR AVANÇO MANUAL
    // =========================

    @Transactional
    public void avancarFaseManual() {

        Competicao competicao =
                obterCompeticao();

        if (competicao.getFaseAtual()
                .isUltima()) {

            throw new RuntimeException(
                    "A competição já chegou à fase final"
            );
        }

        competicao.setFaseAtual(
                competicao.getFaseAtual()
                        .proxima()
        );

        competicao.setStatusFase(
                StatusFase.EM_ANDAMENTO
        );

        competicaoRepository.save(
                competicao
        );
    }

    // =========================
    // 🔁 REGERAR CONFRONTOS
    // =========================

    @Transactional
    public void regenerarConfrontos() {

        Competicao competicao =
                obterCompeticao();

        List<Confronto> confrontos =
                confrontoRepository.findByFase(
                        competicao.getFaseAtual()
                );

        boolean existeResolvido =
                confrontos.stream()
                        .anyMatch(c ->
                                Boolean.TRUE.equals(
                                        c.getResolvido()
                                )
                        );

        if (existeResolvido) {
            throw new RuntimeException(
                    "Não é possível regenerar confrontos que já foram iniciados"
            );
        }

        confrontoRepository.deleteAll(
                confrontos
        );

        submissaoService.gerarConfrontos(
                competicao.getFaseAtual()
        );
    }

    // =========================
    // 🔒 PAUSAR FASE
    // =========================

    @Transactional
    public void pausarFase() {

        Competicao competicao =
                obterCompeticao();

        if (competicao.getStatusFase()
                != StatusFase.EM_ANDAMENTO) {

            throw new RuntimeException(
                    "A fase não está em andamento"
            );
        }

        competicao.setStatusFase(
                StatusFase.NAO_INICIADA
        );

        competicaoRepository.save(
                competicao
        );
    }

    // =========================
    // 🏆 PUBLICAR CHAVEAMENTO
    // =========================

    @Transactional
    public void publicarChaveamento() {

        Competicao competicao =
                obterCompeticao();

        competicao.setChaveamentoPublicado(
                true
        );

        competicaoRepository.save(
                competicao
        );
    }

    @Transactional
    public void despublicarChaveamento() {

        Competicao competicao =
                obterCompeticao();

        competicao.setChaveamentoPublicado(
                false
        );

        competicaoRepository.save(
                competicao
        );
    }

    // =========================
// 🏆 SELEÇÃO EDITORIAL
// =========================

    @Transactional(readOnly = true)
    public List<SubmissaoResponseDTO> listarSubmissoesPendentes() {

        Evento eventoAtivo = eventoRepository.findByAtivoTrue()
                .orElseThrow(() ->
                        new RuntimeException("Nenhum evento ativo encontrado."));

        return submissaoRepository
                .findByEventoIdAndStatus(
                        eventoAtivo.getId(),
                        StatusSubmissao.SUBMETIDA
                )
                .stream()
                .map(SubmissaoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public SelecaoEdicaoResponseDTO selecionarObrasDaEdicao(
            SelecaoEdicaoRequest request
    ) {

        Evento evento = eventoRepository
                .findByAtivoTrue()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Nenhum evento ativo encontrado."
                        )
                );

        List<Submissao> submetidas =
                submissaoRepository
                        .findByEventoIdAndStatus(
                                evento.getId(),
                                StatusSubmissao.SUBMETIDA
                        );

        if (submetidas.isEmpty()) {
            throw new RuntimeException(
                    "Não existem submissões pendentes para seleção."
            );
        }

        Set<Long> selecionadas =
                new HashSet<>(request.submissaoIds());

        LocalDateTime agora =
                LocalDateTime.now();

        int totalSelecionadas = 0;
        int totalNaoSelecionadas = 0;

        for (Submissao submissao : submetidas) {

            if (selecionadas.contains(
                    submissao.getId()
            )) {

                submissao.setStatus(
                        StatusSubmissao.EM_COMPETICAO
                );

                submissao.setFaseAtual(
                        FaseCompeticao.FASE_32
                );

                submissao.setDataDecisaoEditorial(
                        agora
                );

                submissao.setJustificativaNaoSelecao(
                        null
                );

                totalSelecionadas++;

            } else {

                submissao.setStatus(
                        StatusSubmissao.NAO_SELECIONADA
                );

                submissao.setDataDecisaoEditorial(
                        agora
                );

                submissao.setJustificativaNaoSelecao(
                        "Obra analisada pela curadoria editorial, mas não selecionada para compor a edição vigente da Copa de Literatura de Futebol Feminino."
                );

                totalNaoSelecionadas++;
            }
        }

        submissaoRepository.saveAll(
                submetidas
        );

        return new SelecaoEdicaoResponseDTO(
                submetidas.size(),
                totalSelecionadas,
                totalNaoSelecionadas,
                "Seleção editorial concluída com sucesso."
        );
    }

    // =========================
    // EXCLUIR SUBMISSÃO
    // =========================

    /**
     * Exclui uma submissão pelo ID. Uso administrativo.
     *
     * Delega ao SubmissaoService, que remove termo de aceite e arquivos
     * junto e aplica a trava de integridade (só obra fora da competição).
     */
    @Transactional
    public void excluirSubmissao(Long submissaoId) {
        submissaoService.excluirSubmissaoAdmin(submissaoId);
    }

    // =========================
    // HELPER
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
}