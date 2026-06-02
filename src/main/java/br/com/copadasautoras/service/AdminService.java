package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.AdminDashboardDTO;
import br.com.copadasautoras.dto.CreateUsuarioRequest;
import br.com.copadasautoras.dto.UsuarioAdminResponseDTO;
import br.com.copadasautoras.entity.*;
import br.com.copadasautoras.repository.CompeticaoRepository;
import br.com.copadasautoras.repository.ConfrontoRepository;
import br.com.copadasautoras.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CompeticaoRepository competicaoRepository;
    private final ConfrontoRepository confrontoRepository;
    private final SubmissaoService submissaoService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

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
    public List<UsuarioAdminResponseDTO> listarUsuariosAdministrativos() {

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
    // 📊 DASHBOARD
    // =========================

    public AdminDashboardDTO obterDashboard() {

        Competicao competicao = obterCompeticao();

        List<Confronto> confrontos =
                confrontoRepository.findByFase(
                        competicao.getFaseAtual()
                );

        int total = confrontos.size();

        int resolvidos = (int) confrontos.stream()
                .filter(c ->
                        Boolean.TRUE.equals(
                                c.getResolvido()
                        )
                )
                .count();

        return new AdminDashboardDTO(
                competicao.getFaseAtual(),
                competicao.getStatusFase(),
                total,
                resolvidos,
                total - resolvidos
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

