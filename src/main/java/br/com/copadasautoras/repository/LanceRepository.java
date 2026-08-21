package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.CategoriaLance;
import br.com.copadasautoras.entity.Lance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LanceRepository
        extends JpaRepository<Lance, Long> {

    boolean existsBySlug(String slug);

    Optional<Lance> findBySlug(String slug);

    /**
     * Timeline pública.
     *
     * Um lance é visível quando está PUBLICADO ou quando
     * está AGENDADO e a hora marcada já passou — sem
     * necessidade de job em segundo plano.
     *
     * Filtros opcionais:
     *  - categoria nula  → todas as categorias;
     *  - apenasGolaco    → true limita aos destaques.
     */
    @Query("""
            SELECT l FROM Lance l
            WHERE (
                    l.status = br.com.copadasautoras.entity.StatusLance.PUBLICADO
                 OR (l.status = br.com.copadasautoras.entity.StatusLance.AGENDADO
                     AND l.publicarEm IS NOT NULL
                     AND l.publicarEm <= :agora)
                  )
              AND (:categoria IS NULL OR l.categoria = :categoria)
              AND (:apenasGolaco = false OR l.golaco = true)
            ORDER BY l.dataAcontecimento DESC, l.id DESC
            """)
    List<Lance> buscarVisiveis(
            @Param("agora") LocalDateTime agora,
            @Param("categoria") CategoriaLance categoria,
            @Param("apenasGolaco") boolean apenasGolaco
    );

    /**
     * Detalhe público por slug — respeita a mesma regra
     * de visibilidade da timeline.
     */
    @Query("""
            SELECT l FROM Lance l
            WHERE l.slug = :slug
              AND (
                    l.status = br.com.copadasautoras.entity.StatusLance.PUBLICADO
                 OR (l.status = br.com.copadasautoras.entity.StatusLance.AGENDADO
                     AND l.publicarEm IS NOT NULL
                     AND l.publicarEm <= :agora)
                  )
            """)
    Optional<Lance> buscarVisivelPorSlug(
            @Param("slug") String slug,
            @Param("agora") LocalDateTime agora
    );

    /**
     * Listagem do admin — todos os status, com filtro
     * opcional por categoria e por destaque. Alimenta o
     * painel e a exportação (relatório).
     */
    @Query("""
            SELECT l FROM Lance l
            WHERE (:categoria IS NULL OR l.categoria = :categoria)
              AND (:apenasGolaco = false OR l.golaco = true)
            ORDER BY l.dataAcontecimento DESC, l.id DESC
            """)
    List<Lance> buscarParaAdmin(
            @Param("categoria") CategoriaLance categoria,
            @Param("apenasGolaco") boolean apenasGolaco
    );
}
