package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.entity.StatusSubmissao;
import br.com.copadasautoras.entity.Submissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    /**
     * Busca submissões por status.
     */
    List<Submissao> findByStatus(
            StatusSubmissao status
    );

    /**
     * Busca submissões de um evento.
     */
    List<Submissao> findByEventoId(
            Long eventoId
    );

    /**
     * Busca submissões de um evento
     * filtrando por status.
     */
    List<Submissao> findByEventoIdAndStatus(
            Long eventoId,
            StatusSubmissao status
    );

    // =========================
    // MÉTRICAS (só contagem / datas — sem dados identificáveis)
    // =========================

    /**
     * Conta obras pelo status atual.
     */
    long countByStatus(
            StatusSubmissao status
    );

    /**
     * Conta obras inscritas dentro do período
     * (pela data de submissão).
     */
    long countByDataSubmissaoBetween(
            LocalDateTime inicio,
            LocalDateTime fim
    );

    /**
     * Conta obras inscritas no período (pela data de submissão)
     * que hoje estão em determinado status.
     */
    long countByStatusAndDataSubmissaoBetween(
            StatusSubmissao status,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    /**
     * Datas de submissão de todas as obras.
     * Alimenta a série "obras por semana" do painel.
     */
    @Query("select s.dataSubmissao from Submissao s "
            + "where s.dataSubmissao is not null")
    List<LocalDateTime> findDatasSubmissao();

    /**
     * Datas de submissão dentro do período.
     * Alimenta a série "obras por semana" do relatório.
     */
    @Query("select s.dataSubmissao from Submissao s "
            + "where s.dataSubmissao between :inicio and :fim")
    List<LocalDateTime> findDatasSubmissaoBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
