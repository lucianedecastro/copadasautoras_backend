package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.AcaoAuditoria;
import br.com.copadasautoras.entity.OrigemAuditoria;
import br.com.copadasautoras.entity.RegistroAuditoria;
import br.com.copadasautoras.entity.TipoEntidadeAuditoria;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistroAuditoriaRepository
        extends JpaRepository<RegistroAuditoria, Long> {

    /**
     * Histórico completo de uma autora/obra específica, mais recente
     * primeiro. É o "por que essa obra não passou?" respondido.
     */
    List<RegistroAuditoria> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(
            TipoEntidadeAuditoria entidade,
            Long entidadeId
    );

    /**
     * Feed geral do log, mais recente primeiro (paginado).
     */
    List<RegistroAuditoria> findAllByOrderByDataHoraDesc(Pageable pageable);

    // =========================
    // CONSULTA FILTRADA (tela de auditoria)
    // =========================

    /**
     * Busca filtrada e paginada. Cada filtro é opcional: quando o
     * parâmetro vem nulo, aquele critério é ignorado.
     */
    @Query("""
            select r from RegistroAuditoria r
            where (:origem is null or r.origem = :origem)
              and (:entidade is null or r.entidade = :entidade)
              and (:entidadeId is null or r.entidadeId = :entidadeId)
              and (:acao is null or r.acao = :acao)
              and (:inicio is null or r.dataHora >= :inicio)
              and (:fim is null or r.dataHora <= :fim)
            order by r.dataHora desc
            """)
    List<RegistroAuditoria> buscar(
            @Param("origem") OrigemAuditoria origem,
            @Param("entidade") TipoEntidadeAuditoria entidade,
            @Param("entidadeId") Long entidadeId,
            @Param("acao") AcaoAuditoria acao,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Pageable pageable
    );

    /**
     * Total de registros que casam com os mesmos filtros — para saber
     * se ainda há mais páginas.
     */
    @Query("""
            select count(r) from RegistroAuditoria r
            where (:origem is null or r.origem = :origem)
              and (:entidade is null or r.entidade = :entidade)
              and (:entidadeId is null or r.entidadeId = :entidadeId)
              and (:acao is null or r.acao = :acao)
              and (:inicio is null or r.dataHora >= :inicio)
              and (:fim is null or r.dataHora <= :fim)
            """)
    long contar(
            @Param("origem") OrigemAuditoria origem,
            @Param("entidade") TipoEntidadeAuditoria entidade,
            @Param("entidadeId") Long entidadeId,
            @Param("acao") AcaoAuditoria acao,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
