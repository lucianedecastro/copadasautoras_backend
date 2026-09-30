package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.VotoPopular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VotoPopularRepository
        extends JpaRepository<VotoPopular, Long> {

    /**
     * Checa se o e-mail já tem voto confirmado
     * (regra de um voto por pessoa na competição).
     */
    boolean existsByEmailAndConfirmadoTrue(
            String email
    );

    /**
     * Busca o registro de voto pendente pelo token
     * enviado no e-mail de confirmação.
     */
    Optional<VotoPopular> findByToken(
            String token
    );

    /**
     * Contagem de votos confirmados por obra —
     * alimenta o resultado do "Escolha do Público".
     */
    long countBySubmissaoIdAndConfirmadoTrue(
            Long submissaoId
    );

    // =========================
    // 📊 PAINEL ADMIN — placar do Escolha do Público
    // =========================

    /** Total de votos confirmados (um por e-mail). */
    long countByConfirmadoTrue();

    /** Total de votos ainda aguardando confirmação por e-mail. */
    long countByConfirmadoFalse();

    /**
     * Votos confirmados agrupados por obra.
     * Cada linha: [submissaoId (Long), total (Long)].
     * Obras sem voto não aparecem — o service completa com zero.
     */
    @Query("""
            select v.submissao.id, count(v)
            from VotoPopular v
            where v.confirmado = true
            group by v.submissao.id
            """)
    List<Object[]> contarConfirmadosPorSubmissao();

    /**
     * Votos pendentes agrupados por obra.
     * Cada linha: [submissaoId (Long), total (Long)].
     */
    @Query("""
            select v.submissao.id, count(v)
            from VotoPopular v
            where v.confirmado = false
            group by v.submissao.id
            """)
    List<Object[]> contarPendentesPorSubmissao();
}
