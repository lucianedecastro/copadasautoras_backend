package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.Usuario;
import br.com.copadasautoras.entity.VotoFinal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VotoFinalRepository
        extends JpaRepository<VotoFinal, Long> {

    /**
     * Garante 1 voto por jurada.
     */
    boolean existsByBanca(
            Usuario banca
    );

    /**
     * Recupera votos da final.
     */
    List<VotoFinal> findAll();

    /**
     * Busca voto da jurada.
     */
    Optional<VotoFinal> findByBanca(
            Usuario banca
    );

    /**
     * Contagem de votos por submissão.
     */
    long countBySubmissaoId(
            Long submissaoId
    );
}
