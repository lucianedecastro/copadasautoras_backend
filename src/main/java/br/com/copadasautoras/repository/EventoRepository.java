package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {
}
