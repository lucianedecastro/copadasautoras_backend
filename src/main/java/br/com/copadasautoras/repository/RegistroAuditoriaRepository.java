package br.com.copadasautoras.repository;

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
    //
    // SQL nativo com CAST explícito em cada parâmetro. Sem os casts, o
    // Postgres não consegue determinar o tipo de um parâmetro nulo na
    // checagem "(:param is null or ...)" e devolve 42P18
    // ("could not determine data type of parameter"). O cast dá o tipo
    // ao banco mesmo quando o valor vem nulo (filtro desligado).
    //
    // Os enums entram como texto (a coluna é varchar via @Enumerated
    // STRING), então o serviço passa origem/entidade/acao já como name().

    /**
     * Busca filtrada e paginada. Cada filtro é opcional: nulo = ignorado.
     */
    @Query(value = """
            select * from registro_auditoria r
            where (cast(:origem as varchar) is null or r.origem = cast(:origem as varchar))
              and (cast(:entidade as varchar) is null or r.entidade = cast(:entidade as varchar))
              and (cast(:entidadeId as bigint) is null or r.entidade_id = cast(:entidadeId as bigint))
              and (cast(:acao as varchar) is null or r.acao = cast(:acao as varchar))
              and (cast(:inicio as timestamp) is null or r.data_hora >= cast(:inicio as timestamp))
              and (cast(:fim as timestamp) is null or r.data_hora <= cast(:fim as timestamp))
            order by r.data_hora desc
            limit :limite offset :deslocamento
            """, nativeQuery = true)
    List<RegistroAuditoria> buscar(
            @Param("origem") String origem,
            @Param("entidade") String entidade,
            @Param("entidadeId") Long entidadeId,
            @Param("acao") String acao,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("limite") int limite,
            @Param("deslocamento") int deslocamento
    );

    /**
     * Total de registros que casam com os mesmos filtros — para saber
     * se ainda há mais páginas.
     */
    @Query(value = """
            select count(*) from registro_auditoria r
            where (cast(:origem as varchar) is null or r.origem = cast(:origem as varchar))
              and (cast(:entidade as varchar) is null or r.entidade = cast(:entidade as varchar))
              and (cast(:entidadeId as bigint) is null or r.entidade_id = cast(:entidadeId as bigint))
              and (cast(:acao as varchar) is null or r.acao = cast(:acao as varchar))
              and (cast(:inicio as timestamp) is null or r.data_hora >= cast(:inicio as timestamp))
              and (cast(:fim as timestamp) is null or r.data_hora <= cast(:fim as timestamp))
            """, nativeQuery = true)
    long contar(
            @Param("origem") String origem,
            @Param("entidade") String entidade,
            @Param("entidadeId") Long entidadeId,
            @Param("acao") String acao,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
