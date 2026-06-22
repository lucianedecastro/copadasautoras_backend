package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.entity.GrupoCompeticao;
import br.com.copadasautoras.entity.NomeGrupo;
import br.com.copadasautoras.entity.Role;
import br.com.copadasautoras.entity.StatusSubmissao;
import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.entity.Usuario;
import br.com.copadasautoras.repository.GrupoCompeticaoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import br.com.copadasautoras.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Responsável só pelo sorteio das 3 juradas da FINAL — a geração dos
 * confrontos das demais fases (OITAVAS/QUARTAS/SEMIFINAL) já vive em
 * SubmissaoService.gerarConfrontos(), exposta via
 * SubmissaoController#gerarConfrontos.
 */
@Service
@RequiredArgsConstructor
public class ConfrontoService {

    private final SubmissaoRepository submissaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoCompeticaoRepository grupoCompeticaoRepository;

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
}