package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.CampeaPublicaDTO;
import br.com.copadasautoras.dto.ChaveamentoPublicoResponseDTO;
import br.com.copadasautoras.dto.ConfrontoPublicoDTO;
import br.com.copadasautoras.dto.GrupoPublicoDTO;
import br.com.copadasautoras.dto.ObraPublicaDTO;
import br.com.copadasautoras.entity.Competicao;
import br.com.copadasautoras.entity.Confronto;
import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.entity.GrupoCompeticao;
import br.com.copadasautoras.entity.StatusSubmissao;
import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.repository.CompeticaoRepository;
import br.com.copadasautoras.repository.ConfrontoRepository;
import br.com.copadasautoras.repository.GrupoCompeticaoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Monta os dados públicos do chaveamento — sem login, sem dado de autora
 * (exceto na campeã, já revelada). Visibilidade real é controlada pelo
 * campo Competicao.chaveamentoPublicado, alternado pelo admin.
 */
@Service
@RequiredArgsConstructor
public class PublicoService {

    private final CompeticaoRepository competicaoRepository;
    private final GrupoCompeticaoRepository grupoCompeticaoRepository;
    private final SubmissaoRepository submissaoRepository;
    private final ConfrontoRepository confrontoRepository;

    public ChaveamentoPublicoResponseDTO obterChaveamento() {

        Competicao competicao =
                competicaoRepository.findAll()
                        .stream()
                        .findFirst()
                        .orElse(null);

        boolean publicado =
                competicao != null
                        && Boolean.TRUE.equals(
                        competicao.getChaveamentoPublicado()
                );

        if (!publicado) {

            return new ChaveamentoPublicoResponseDTO(
                    false,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    null
            );
        }

        return new ChaveamentoPublicoResponseDTO(
                true,
                montarGruposFase32(),
                montarConfrontos(FaseCompeticao.OITAVAS),
                montarConfrontos(FaseCompeticao.QUARTAS),
                montarConfrontos(FaseCompeticao.SEMIFINAL),
                montarFinalistas(),
                montarCampea()
        );
    }

    private List<GrupoPublicoDTO> montarGruposFase32() {

        return grupoCompeticaoRepository
                .findByFase(FaseCompeticao.FASE_32)
                .stream()
                .sorted(
                        Comparator.comparing(
                                g -> g.getNomeGrupo().name()
                        )
                )
                .map(this::toGrupoPublico)
                .toList();
    }

    private GrupoPublicoDTO toGrupoPublico(GrupoCompeticao grupo) {

        List<ObraPublicaDTO> obras =
                submissaoRepository
                        .findByGrupoId(grupo.getId())
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

    private List<ConfrontoPublicoDTO> montarConfrontos(
            FaseCompeticao fase
    ) {

        return confrontoRepository
                .findByFase(fase)
                .stream()
                .map(c -> new ConfrontoPublicoDTO(
                        toObraPublica(c.getCasa()),
                        toObraPublica(c.getFora()),
                        c.getVencedora() != null
                                ? toObraPublica(c.getVencedora())
                                : null
                ))
                .toList();
    }

    private ObraPublicaDTO toObraPublica(Submissao submissao) {

        if (submissao == null) {
            return null;
        }

        return new ObraPublicaDTO(
                submissao.getId(),
                submissao.getTitulo(),
                submissao.getCategoria(),
                null
        );
    }

    private List<ObraPublicaDTO> montarFinalistas() {

        // Antes da revelação: as 2 obras estão CLASSIFICADA na FINAL.
        // Depois: a vencedora vira CAMPEA (faseAtual também muda pra
        // CAMPEA) e a perdedora vira ELIMINADA (mas continua com
        // faseAtual FINAL). Somando os três casos sempre dá as mesmas
        // 2 obras, antes ou depois da revelação.
        List<Submissao> finalistas = new ArrayList<>();

        finalistas.addAll(
                submissaoRepository.findByFaseAtualAndStatus(
                        FaseCompeticao.FINAL,
                        StatusSubmissao.CLASSIFICADA
                )
        );

        finalistas.addAll(
                submissaoRepository.findByFaseAtualAndStatus(
                        FaseCompeticao.FINAL,
                        StatusSubmissao.ELIMINADA
                )
        );

        finalistas.addAll(
                submissaoRepository.findByFaseAtualAndStatus(
                        FaseCompeticao.CAMPEA,
                        StatusSubmissao.CAMPEA
                )
        );

        return finalistas.stream()
                .map(this::toObraPublica)
                .toList();
    }

    private CampeaPublicaDTO montarCampea() {

        return submissaoRepository
                .findByFaseAtualAndStatus(
                        FaseCompeticao.CAMPEA,
                        StatusSubmissao.CAMPEA
                )
                .stream()
                .findFirst()
                .map(s -> new CampeaPublicaDTO(
                        s.getTitulo(),
                        s.getCategoria(),
                        s.getAutora() != null
                                ? s.getAutora().getNomeExibicao()
                                : null
                ))
                .orElse(null);
    }
}