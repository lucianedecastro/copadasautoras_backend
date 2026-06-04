package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventoRepository
        extends JpaRepository<Evento, Long> {

    /**
     * Retorna o evento atualmente ativo.
     */
    Optional<Evento> findByAtivoTrue();
}