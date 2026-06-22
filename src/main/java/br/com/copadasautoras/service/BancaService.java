package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.BancaClassificacaoRequestDTO;
import br.com.copadasautoras.dto.BancaObraResponseDTO;
import br.com.copadasautoras.dto.VotoFinalRequestDTO;
import br.com.copadasautoras.entity.Competicao;
import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.entity.GrupoCompeticao;
import br.com.copadasautoras.entity.Role;
import br.com.copadasautoras.entity.StatusSubmissao;
import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.entity.Usuario;
import br.com.copadasautoras.entity.VotoFinal;
import br.com.copadasautoras.repository.CompeticaoRepository;
import br.com.copadasautoras.repository.GrupoCompeticaoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import br.com.copadasautoras.repository.UsuarioRepository;
import br.com.copadasautoras.repository.VotoFinalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BancaService {

    private final GrupoCompeticaoRepository grupoCompeticaoRepository;
    private final SubmissaoRepository submissaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompeticaoRepository competicaoRepository;
    private final VotoFinalRepository votoFinalRepository;

    public List<BancaObraResponseDTO> minhasObras() {

        Usuario usuarioLogado =
                getUsuarioLogado();

        validarBanca(usuarioLogado);

        Competicao competicao =
                obterCompeticao();

        // FINAL = todas as juradas veem as 2 finalistas
        if (competicao.getFaseAtual()
                == FaseCompeticao.FINAL) {

            return submissaoRepository
                    .findByFaseAtualAndStatus(
                            FaseCompeticao.FINAL,
                            StatusSubmissao.CLASSIFICADA
                    )
                    .stream()
                    .map(this::mapToDTO)
                    .toList();
        }

        GrupoCompeticao grupo =
                obterGrupoDaBanca(
                        usuarioLogado
                );

        return submissaoRepository
                .findByGrupoIdAndFaseAtual(
                        grupo.getId(),
                        competicao.getFaseAtual()
                )
                .stream()
                // obras eliminadas em fases anteriores podem permanecer
                // com o mesmo faseAtual (esse campo só é atualizado
                // quando a obra avança) — por isso o status precisa
                // ser checado aqui, não só a fase.
                .filter(s -> s.getStatus() != StatusSubmissao.ELIMINADA)
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public void classificar(
            BancaClassificacaoRequestDTO request
    ) {

        Usuario usuarioLogado =
                getUsuarioLogado();

        validarBanca(usuarioLogado);

        GrupoCompeticao grupo =
                obterGrupoDaBanca(
                        usuarioLogado
                );

        Competicao competicao =
                obterCompeticao();

        FaseCompeticao faseAtual =
                competicao.getFaseAtual();

        // Filtra apenas as obras que estão de fato na fase atual da
        // competição e que ainda estão ativas — obras eliminadas podem
        // permanecer com o mesmo faseAtual (esse campo só é atualizado
        // quando a obra avança), então o status precisa ser checado
        // também, senão uma obra já eliminada volta a aparecer como
        // classificável.
        List<Submissao> obrasGrupo =
                submissaoRepository.findByGrupoIdAndFaseAtual(
                                grupo.getId(),
                                faseAtual
                        )
                        .stream()
                        .filter(s -> s.getStatus() != StatusSubmissao.ELIMINADA)
                        .toList();

        if (obrasGrupo.isEmpty()) {

            throw new RuntimeException(
                    "Nenhuma obra encontrada para esta jurada nesta fase"
            );
        }

        int quantidadePermitida =
                faseAtual == FaseCompeticao.FASE_32
                        ? 2
                        : 1;

        if (request.classificadas() == null
                || request.classificadas()
                .size()
                != quantidadePermitida) {

            throw new RuntimeException(
                    "A fase "
                            + faseAtual
                            + " exige exatamente "
                            + quantidadePermitida
                            + " classificação(ões)."
            );
        }

        Set<Long> idsGrupo =
                obrasGrupo.stream()
                        .map(Submissao::getId)
                        .collect(Collectors.toSet());

        boolean possuiIdInvalido =
                request.classificadas()
                        .stream()
                        .anyMatch(id ->
                                !idsGrupo.contains(id)
                        );

        if (possuiIdInvalido) {

            throw new RuntimeException(
                    "A jurada só pode classificar obras do próprio grupo."
            );
        }

        FaseCompeticao proximaFase =
                faseAtual.proxima();

        for (Submissao submissao
                : obrasGrupo) {

            if (request.classificadas()
                    .contains(
                            submissao.getId()
                    )) {

                submissao.setStatus(
                        StatusSubmissao.CLASSIFICADA
                );

                submissao.setFaseAtual(
                        proximaFase
                );

            } else {

                submissao.setStatus(
                        StatusSubmissao.ELIMINADA
                );
            }

            submissaoRepository.save(
                    submissao
            );
        }
    }

    @Transactional
    public void votarFinal(
            VotoFinalRequestDTO request
    ) {

        Usuario usuarioLogado =
                getUsuarioLogado();

        validarBanca(usuarioLogado);

        Competicao competicao =
                obterCompeticao();

        if (competicao.getFaseAtual()
                != FaseCompeticao.FINAL) {

            throw new RuntimeException(
                    "A votação só é permitida durante a FINAL."
            );
        }

        if (votoFinalRepository
                .existsByBanca(
                        usuarioLogado
                )) {

            throw new RuntimeException(
                    "Esta jurada já registrou voto."
            );
        }

        Submissao submissao =
                submissaoRepository
                        .findById(
                                request.submissaoId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Submissão não encontrada."
                                )
                        );

        if (submissao.getFaseAtual()
                != FaseCompeticao.FINAL
                || submissao.getStatus()
                != StatusSubmissao.CLASSIFICADA) {

            throw new RuntimeException(
                    "Somente obras finalistas podem receber votos."
            );
        }

        VotoFinal voto =
                VotoFinal.builder()
                        .banca(
                                usuarioLogado
                        )
                        .submissao(
                                submissao
                        )
                        .build();

        votoFinalRepository.save(
                voto
        );
    }

    private Competicao obterCompeticao() {

        return competicaoRepository
                .findById(1L)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Competição não encontrada"
                        )
                );
    }

    private void validarBanca(
            Usuario usuario
    ) {

        if (usuario.getRole()
                != Role.BANCA) {

            throw new RuntimeException(
                    "Apenas juradas podem acessar este recurso"
            );
        }
    }

    private GrupoCompeticao obterGrupoDaBanca(
            Usuario usuario
    ) {

        return grupoCompeticaoRepository
                .findByBancaId(
                        usuario.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Nenhum grupo encontrado para esta jurada"
                        )
                );
    }

    private BancaObraResponseDTO mapToDTO(
            Submissao submissao
    ) {

        String baseUrl =
                "/submissoes/"
                        + submissao.getId();

        return new BancaObraResponseDTO(
                submissao.getId(),
                submissao.getTitulo(),
                submissao.getTipoExibicao(),
                submissao.getArquivoPublicoUrl() != null
                        ? baseUrl + "/arquivo-publico"
                        : null
        );
    }

    private Usuario getUsuarioLogado() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email =
                authentication.getName();

        return usuarioRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuário logado não encontrado"
                        )
                );
    }
}