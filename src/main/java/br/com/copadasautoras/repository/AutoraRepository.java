package br.com.copadasautoras.repository;

import br.com.copadasautoras.dto.LocalizacaoPorCidadeDTO;
import br.com.copadasautoras.dto.LocalizacaoPorEstadoDTO;
import br.com.copadasautoras.entity.Autora;
import br.com.copadasautoras.entity.StatusAutora;
import br.com.copadasautoras.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AutoraRepository extends JpaRepository<Autora, Long> {

    /**
     * Busca autora pelo email institucional/login.
     * Email agora pertence a Usuario.
     */
    Optional<Autora> findByUsuarioEmail(String email);

    /**
     * Busca autora vinculada ao usuário autenticado.
     */
    Optional<Autora> findByUsuario(Usuario usuario);

    /**
     * Busca autora pelo id do usuário autenticado.
     */
    Optional<Autora> findByUsuarioId(Long usuarioId);

    /**
     * Lista autoras por status institucional.
     */
    List<Autora> findByStatusAutora(StatusAutora statusAutora);

    /**
     * Conta autoras por status institucional.
     * Usado pelas métricas do painel (só contagem, sem dados).
     */
    long countByStatusAutora(StatusAutora statusAutora);

    /**
     * Verifica se email já existe.
     * Email agora pertence a Usuario.
     */
    boolean existsByUsuarioEmail(String email);

    /**
     * Verifica se nome de exibição já existe.
     * (opcional para regra futura)
     */
    boolean existsByNomeExibicao(String nomeExibicao);

    // =====================================================
    // RELATÓRIO DE LOCALIZAÇÃO (agregado — nunca individual)
    // =====================================================

    /**
     * Conta autoras dos status informados.
     */
    long countByStatusAutoraIn(Collection<StatusAutora> status);

    /**
     * Conta autoras dos status informados que já informaram localização.
     */
    long countByStatusAutoraInAndEstadoIsNotNull(Collection<StatusAutora> status);

    /**
     * Autoras por UF, das mais para as menos numerosas.
     */
    @Query("""
            select new br.com.copadasautoras.dto.LocalizacaoPorEstadoDTO(a.estado, count(a))
            from Autora a
            where a.statusAutora in :status
              and a.estado is not null
            group by a.estado
            order by count(a) desc, a.estado
            """)
    List<LocalizacaoPorEstadoDTO> contarPorEstado(@Param("status") Collection<StatusAutora> status);

    /**
     * Autoras por cidade (com a UF), das mais para as menos numerosas.
     */
    @Query("""
            select new br.com.copadasautoras.dto.LocalizacaoPorCidadeDTO(a.estado, a.cidade, count(a))
            from Autora a
            where a.statusAutora in :status
              and a.estado is not null
              and a.cidade is not null
            group by a.estado, a.cidade
            order by count(a) desc, a.estado, a.cidade
            """)
    List<LocalizacaoPorCidadeDTO> contarPorCidade(@Param("status") Collection<StatusAutora> status);
}
