package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.*;
import br.com.copadasautoras.dto.ConfrontoResponseDTO;
import br.com.copadasautoras.dto.FaseDetalheResponseDTO;
import br.com.copadasautoras.dto.SubmissaoResponseDTO;
import br.com.copadasautoras.repository.CompeticaoRepository;
import br.com.copadasautoras.repository.ConfrontoRepository;
import br.com.copadasautoras.repository.GrupoCompeticaoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import br.com.copadasautoras.repository.UsuarioRepository;
import br.com.copadasautoras.repository.VotoFinalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.copadasautoras.repository.EventoRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompeticaoService {

    private final CompeticaoRepository repository;
    private final SubmissaoRepository submissaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoCompeticaoRepository grupoCompeticaoRepository;
    private final VotoFinalRepository votoFinalRepository;
    private final EventoRepository eventoRepository;
    private final ConfrontoRepository confrontoRepository;

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

        Evento evento =
                eventoRepository.findByAtivoTrue()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Nenhum evento ativo encontrado."
                                )
                        );

        List<Submissao> submissoes =
                submissaoRepository
                        .findByEventoIdAndStatus(
                                evento.getId(),
                                StatusSubmissao.EM_COMPETICAO
                        );

        if (submissoes.size() != 32) {

            throw new RuntimeException(
                    "A competição exige exatamente 32 obras EM_COMPETICAO."
            );
        }

        List<Usuario> bancas =
                usuarioRepository.findByRole(
                        Role.BANCA
                );

        if (bancas.size() < 8) {

            throw new RuntimeException(
                    "É necessário possuir ao menos 8 juradas cadastradas"
            );
        }

        if (grupoCompeticaoRepository.existsByFase(
                FaseCompeticao.FASE_32
        )) {

            throw new RuntimeException(
                    "Os grupos da FASE_32 já foram gerados"
            );
        }

        Collections.shuffle(submissoes);
        Collections.shuffle(bancas);

        NomeGrupo[] grupos =
                NomeGrupo.values();

        int indexSubmissao = 0;

        for (int i = 0; i < grupos.length; i++) {

            Usuario banca =
                    bancas.get(i);

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

            for (int j = 0; j < 4; j++) {

                Submissao submissao =
                        submissoes.get(
                                indexSubmissao
                        );

                submissao.setGrupo(
                        grupo
                );

                submissao.setFaseAtual(
                        FaseCompeticao.FASE_32
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

    @Transactional
    public Submissao revelarCampea() {

        Competicao competicao =
                obter();

        if (competicao.getStatusFase()
                != StatusFase.ENCERRADA) {

            throw new RuntimeException(
                    "A competição precisa estar encerrada."
            );
        }

        if (competicao.getFaseAtual()
                != FaseCompeticao.FINAL) {

            throw new RuntimeException(
                    "A revelação só pode ocorrer após a FINAL."
            );
        }

        List<Submissao> finalistas =
                submissaoRepository
                        .findByFaseAtualAndStatus(
                                FaseCompeticao.FINAL,
                                StatusSubmissao.CLASSIFICADA
                        );

        if (finalistas.size() != 2) {

            throw new RuntimeException(
                    "A FINAL deve possuir exatamente 2 obras."
            );
        }

        Submissao campea =
                finalistas.stream()
                        .max(
                                Comparator.comparingLong(
                                        s -> votoFinalRepository
                                                .countBySubmissaoId(
                                                        s.getId()
                                                )
                                )
                        )
                        .orElseThrow();

        for (Submissao submissao
                : finalistas) {

            if (submissao.getId()
                    .equals(campea.getId())) {

                submissao.setStatus(
                        StatusSubmissao.CAMPEA
                );

                submissao.setFaseAtual(
                        FaseCompeticao.CAMPEA
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

        competicao.setFaseAtual(
                FaseCompeticao.CAMPEA
        );

        repository.save(
                competicao
        );

        return campea;
    }

    public FaseDetalheResponseDTO obterFase(
            FaseCompeticao fase
    ) {

        List<Confronto> confrontos =
                confrontoRepository.findByFase(
                        fase
                );

        List<Submissao> classificadas =
                submissaoRepository.findByFaseAtualAndStatus(
                        fase,
                        StatusSubmissao.CLASSIFICADA
                );

        List<Submissao> eliminadas =
                submissaoRepository.findByFaseAtualAndStatus(
                        fase,
                        StatusSubmissao.ELIMINADA
                );

        return new FaseDetalheResponseDTO(
                fase,
                confrontos.size(),
                confrontos.stream()
                        .map(this::toConfrontoResponseDTO)
                        .toList(),
                classificadas.stream()
                        .map(SubmissaoResponseDTO::fromEntity)
                        .toList(),
                eliminadas.stream()
                        .map(SubmissaoResponseDTO::fromEntity)
                        .toList()
        );
    }

    private ConfrontoResponseDTO toConfrontoResponseDTO(
            Confronto confronto
    ) {

        return new ConfrontoResponseDTO(
                confronto.getId(),
                SubmissaoResponseDTO.fromEntity(
                        confronto.getCasa()
                ),
                SubmissaoResponseDTO.fromEntity(
                        confronto.getFora()
                ),
                confronto.getVencedora() != null
                        ? SubmissaoResponseDTO.fromEntity(
                        confronto.getVencedora()
                )
                        : null,
                confronto.getResolvido()
        );
    }
}