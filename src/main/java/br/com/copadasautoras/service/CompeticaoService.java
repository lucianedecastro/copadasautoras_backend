package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.*;
import br.com.copadasautoras.repository.CompeticaoRepository;
import br.com.copadasautoras.repository.GrupoCompeticaoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import br.com.copadasautoras.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompeticaoService {

    private final CompeticaoRepository repository;
    private final SubmissaoRepository submissaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoCompeticaoRepository grupoCompeticaoRepository;

    public Competicao obter() {

        return repository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Competição não inicializada"
                        )
                );
    }

    @Transactional
    public Competicao iniciar() {

        Competicao competicao =
                repository.findAll()
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (competicao == null) {
            competicao =
                    Competicao.builder()
                            .build();
        }

        if (competicao.getStatusFase()
                == StatusFase.EM_ANDAMENTO) {

            throw new RuntimeException(
                    "A competição já está em andamento"
            );
        }

        // ==================================
        // VALIDAÇÃO FASE 32
        // ==================================

        List<Submissao> submissoes =
                submissaoRepository.findByFaseAtual(
                        FaseCompeticao.FASE_32
                );

        if (submissoes.size() != 32) {
            throw new RuntimeException(
                    "A FASE_32 exige exatamente 32 submissões"
            );
        }

        // ==================================
        // VALIDAÇÃO JURADAS
        // ==================================

        List<Usuario> bancas =
                usuarioRepository.findByRole(
                        Role.BANCA
                );

        if (bancas.size() < 8) {
            throw new RuntimeException(
                    "É necessário possuir ao menos 8 juradas cadastradas"
            );
        }

        // ==================================
        // EVITAR DUPLICAÇÃO DE GRUPOS
        // ==================================

        if (grupoCompeticaoRepository.existsByFase(
                FaseCompeticao.FASE_32
        )) {

            throw new RuntimeException(
                    "Os grupos da FASE_32 já foram gerados"
            );
        }

        // ==================================
        // EMBARALHAR OBRAS E BANCAS
        // ==================================

        Collections.shuffle(submissoes);
        Collections.shuffle(bancas);

        NomeGrupo[] grupos =
                NomeGrupo.values();

        // ==================================
        // GERAR GRUPOS
        // ==================================

        int indexSubmissao = 0;

        for (int i = 0; i < grupos.length; i++) {

            Usuario banca = bancas.get(i);

            GrupoCompeticao grupo =
                    GrupoCompeticao.builder()
                            .nomeGrupo(
                                    grupos[i]
                            )
                            .fase(
                                    FaseCompeticao.FASE_32
                            )
                            .banca(
                                    banca
                            )
                            .build();

            grupo =
                    grupoCompeticaoRepository
                            .save(grupo);

            // ==========================
            // DISTRIBUIR 4 OBRAS
            // ==========================

            for (int j = 0; j < 4; j++) {

                Submissao submissao =
                        submissoes.get(
                                indexSubmissao
                        );

                submissao.setGrupo(
                        grupo
                );

                submissaoRepository.save(
                        submissao
                );

                indexSubmissao++;
            }
        }

        competicao.setStatusFase(
                StatusFase.EM_ANDAMENTO
        );

        return repository.save(
                competicao
        );
    }

    @Transactional
    public Competicao encerrarFase() {

        Competicao competicao =
                obter();

        if (competicao.getStatusFase()
                == StatusFase.ENCERRADA) {

            throw new RuntimeException(
                    "A fase já está encerrada"
            );
        }

        competicao.setStatusFase(
                StatusFase.ENCERRADA
        );

        return repository.save(
                competicao
        );
    }

    @Transactional
    public Competicao avancarFase() {

        Competicao competicao =
                obter();

        if (competicao.getStatusFase()
                != StatusFase.ENCERRADA) {

            throw new RuntimeException(
                    "A fase precisa estar encerrada para avançar"
            );
        }

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
                StatusFase.NAO_INICIADA
        );

        return repository.save(
                competicao
        );
    }
}
