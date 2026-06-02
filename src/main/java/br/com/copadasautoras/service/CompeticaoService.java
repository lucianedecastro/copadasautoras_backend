package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.*;
import br.com.copadasautoras.repository.CompeticaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompeticaoService {

    private final CompeticaoRepository repository;

    public Competicao obter() {
        return repository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Competição não inicializada"));
    }

    @Transactional
    public Competicao iniciar() {
        Competicao competicao = repository.findAll().stream().findFirst().orElse(null);

        if (competicao == null) {
            competicao = Competicao.builder().build();
        }

        if (competicao.getStatusFase() == StatusFase.EM_ANDAMENTO) {
            throw new RuntimeException("A competição já está em andamento");
        }

        competicao.setStatusFase(StatusFase.EM_ANDAMENTO);
        return repository.save(competicao);
    }

    @Transactional
    public Competicao encerrarFase() {

        Competicao competicao = obter();

        if (competicao.getStatusFase() == StatusFase.ENCERRADA) {
            throw new RuntimeException("A fase já está encerrada");
        }

        competicao.setStatusFase(StatusFase.ENCERRADA);
        return repository.save(competicao);
    }

    @Transactional
    public Competicao avancarFase() {

        Competicao competicao = obter();

        if (competicao.getStatusFase() != StatusFase.ENCERRADA) {
            throw new RuntimeException("A fase precisa estar encerrada para avançar");
        }

        if (competicao.getFaseAtual().isUltima()) {
            throw new RuntimeException("A competição já chegou à fase final");
        }

        competicao.setFaseAtual(competicao.getFaseAtual().proxima());
        competicao.setStatusFase(StatusFase.NAO_INICIADA);
        return repository.save(competicao);
    }
}