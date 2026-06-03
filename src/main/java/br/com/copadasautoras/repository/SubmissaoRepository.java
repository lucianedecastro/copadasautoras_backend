package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.entity.StatusSubmissao;
import br.com.copadasautoras.entity.Submissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissaoRepository
        extends JpaRepository<Submissao, Long> {

    List<Submissao> findByFaseAtual(
            FaseCompeticao fase
    );

    List<Submissao> findByFaseAtualAndStatus(
            FaseCompeticao fase,
            StatusSubmissao status
    );

    List<Submissao> findByGrupoId(
            Long grupoId
    );

    List<Submissao> findByGrupoIdAndFaseAtual(
            Long grupoId,
            FaseCompeticao faseAtual
    );

    /**
     * Regra institucional:
     * uma autora possui apenas uma submissão por evento.
     */
    boolean existsByAutoraIdAndEventoId(
            Long autoraId,
            Long eventoId
    );

    /**
     * Busca submissão da autora
     * (usada no perfil público).
     */
    Optional<Submissao> findFirstByAutoraId(
            Long autoraId
    );
}