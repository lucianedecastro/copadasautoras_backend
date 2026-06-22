package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.ConfrontoResultadoDTO;
import br.com.copadasautoras.entity.Confronto;
import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.entity.GrupoCompeticao;
import br.com.copadasautoras.entity.NomeGrupo;
import br.com.copadasautoras.entity.Role;
import br.com.copadasautoras.entity.StatusSubmissao;
import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.entity.Usuario;
import br.com.copadasautoras.repository.ConfrontoRepository;
import br.com.copadasautoras.repository.GrupoCompeticaoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import br.com.copadasautoras.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Responsável por gerar os confrontos de cada fase a partir das OITAVAS
 * (sorteio de pares de obras + sorteio de 1 jurada por par) e por sortear
 * as 3 juradas que avaliam a FINAL.
 *
 * A FASE_32 não passa por aqui: seus grupos (de 4 obras cada) são criados
 * pelo CompeticaoService.iniciar(). A FINAL também não forma pares — as 3
 * juradas sorteadas veem as 2 finalistas e votam (ver BancaService.votarFinal).
 */
@Service
@RequiredArgsConstructor
public class ConfrontoService {

    private final ConfrontoRepository confrontoRepository;
    private final SubmissaoRepository submissaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoCompeticaoRepository grupoCompeticaoRepository;

    // Quantidade de obras classificadas esperada ao entrar em cada fase —
    // usado só pra avisar o admin se algo estiver incompleto antes do sorteio
    // (ex: alguma jurada da fase anterior ainda não classificou).
    private static final Map<FaseCompeticao, Integer> SOBREVIVENTES_ESPERADOS =
            Map.of(
                    FaseCompeticao.OITAVAS, 16,
                    FaseCompeticao.QUARTAS, 8,
                    FaseCompeticao.SEMIFINAL, 4
            );

    @Transactional
    public List<Confronto> gerarConfrontos(
            FaseCompeticao fase
    ) {

        if (fase == FaseCompeticao.FASE_32
                || fase == FaseCompeticao.FINAL) {

            throw new RuntimeException(
                    "A fase "
                            + fase
                            + " não utiliza sorteio de confrontos."
            );
        }

        if (confrontoRepository.existsByFase(fase)) {

            throw new RuntimeException(
                    "Os confrontos da fase "
                            + fase
                            + " já foram gerados."
            );
        }

        List<Submissao> sobreviventes =
                submissaoRepository.findByFaseAtualAndStatus(
                        fase,
                        StatusSubmissao.CLASSIFICADA
                );

        if (sobreviventes.isEmpty()) {

            throw new RuntimeException(
                    "Nenhuma obra classificada encontrada para a fase "
                            + fase
                            + "."
            );
        }

        Integer esperado =
                SOBREVIVENTES_ESPERADOS.get(fase);

        if (esperado != null
                && sobreviventes.size() != esperado) {

            throw new RuntimeException(
                    "A fase "
                            + fase
                            + " espera "
                            + esperado
                            + " obra(s) classificada(s), mas foram encontradas "
                            + sobreviventes.size()
                            + "."
            );
        }

        if (sobreviventes.size() % 2 != 0) {

            throw new RuntimeException(
                    "Número ímpar de obras classificadas ("
                            + sobreviventes.size()
                            + ") — não é possível formar confrontos."
            );
        }

        int totalConfrontos =
                sobreviventes.size() / 2;

        List<Usuario> bancas =
                usuarioRepository.findByRole(
                        Role.BANCA
                );

        if (bancas.size() < totalConfrontos) {

            throw new RuntimeException(
                    "É necessário ao menos "
                            + totalConfrontos
                            + " juradas cadastradas para sortear os confrontos desta fase."
            );
        }

        Collections.shuffle(sobreviventes);
        Collections.shuffle(bancas);

        NomeGrupo[] nomes =
                NomeGrupo.values();

        List<Confronto> novosConfrontos =
                new ArrayList<>();

        for (int i = 0; i < totalConfrontos; i++) {

            Submissao casa =
                    sobreviventes.get(i * 2);

            Submissao fora =
                    sobreviventes.get(i * 2 + 1);

            Usuario banca =
                    bancas.get(i);

            GrupoCompeticao grupo =
                    GrupoCompeticao.builder()
                            .nomeGrupo(
                                    nomes[i % nomes.length]
                            )
                            .fase(fase)
                            .banca(banca)
                            .build();

            grupo =
                    grupoCompeticaoRepository.save(
                            grupo
                    );

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

            novosConfrontos.add(
                    confrontoRepository.save(
                            confronto
                    )
            );
        }

        return novosConfrontos;
    }

    @Transactional
    public List<GrupoCompeticao> sortearJuradasFinal() {

        if (grupoCompeticaoRepository.existsByFase(
                FaseCompeticao.FINAL
        )) {

            throw new RuntimeException(
                    "As juradas da final já foram sorteadas."
            );
        }

        List<Submissao> finalistas =
                submissaoRepository.findByFaseAtualAndStatus(
                        FaseCompeticao.FINAL,
                        StatusSubmissao.CLASSIFICADA
                );

        if (finalistas.size() != 2) {

            throw new RuntimeException(
                    "A FINAL deve possuir exatamente 2 obras classificadas para sortear as juradas."
            );
        }

        List<Usuario> bancas =
                usuarioRepository.findByRole(
                        Role.BANCA
                );

        if (bancas.size() < 3) {

            throw new RuntimeException(
                    "É necessário ao menos 3 juradas cadastradas para sortear a final."
            );
        }

        Collections.shuffle(bancas);

        List<Usuario> sorteadas =
                bancas.subList(0, 3);

        NomeGrupo[] nomes =
                NomeGrupo.values();

        List<GrupoCompeticao> grupos =
                new ArrayList<>();

        for (int i = 0; i < 3; i++) {

            GrupoCompeticao grupo =
                    GrupoCompeticao.builder()
                            .nomeGrupo(nomes[i])
                            .fase(FaseCompeticao.FINAL)
                            .banca(sorteadas.get(i))
                            .build();

            grupos.add(
                    grupoCompeticaoRepository.save(
                            grupo
                    )
            );
        }

        return grupos;
    }

    /**
     * Permite o admin corrigir/registrar manualmente o resultado de um
     * confronto (ex: empate técnico resolvido manualmente, correção de
     * erro). Na operação normal, o resultado já é sincronizado
     * automaticamente pelo BancaService.classificar().
     */
    @Transactional
    public Confronto registrarResultadoManual(
            ConfrontoResultadoDTO request
    ) {

        Confronto confronto =
                confrontoRepository.findAll()
                        .stream()
                        .filter(c ->
                                pertenceAoPar(
                                        c,
                                        request.idCasa(),
                                        request.idFora()
                                )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Nenhum confronto encontrado para essas obras."
                                )
                        );

        if (!request.idVencedor().equals(request.idCasa())
                && !request.idVencedor().equals(request.idFora())) {

            throw new RuntimeException(
                    "A vencedora precisa ser uma das duas obras do confronto."
            );
        }

        Submissao vencedora =
                submissaoRepository.findById(
                                request.idVencedor()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Obra vencedora não encontrada."
                                )
                        );

        confronto.setVencedora(vencedora);
        confronto.setResolvido(true);

        return confrontoRepository.save(confronto);
    }

    public List<Confronto> listarPorFase(
            FaseCompeticao fase
    ) {

        return confrontoRepository.findByFase(fase);
    }

    private boolean pertenceAoPar(
            Confronto confronto,
            Long idCasa,
            Long idFora
    ) {

        Long casaId =
                confronto.getCasa()
                        .getId();

        Long foraId =
                confronto.getFora()
                        .getId();

        return (casaId.equals(idCasa) && foraId.equals(idFora))
                || (casaId.equals(idFora) && foraId.equals(idCasa));
    }
}
