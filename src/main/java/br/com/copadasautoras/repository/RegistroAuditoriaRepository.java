package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.RegistroAuditoria;
import br.com.copadasautoras.entity.TipoEntidadeAuditoria;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
