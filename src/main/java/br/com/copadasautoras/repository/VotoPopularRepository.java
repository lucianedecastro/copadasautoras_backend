package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.VotoPopular;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
