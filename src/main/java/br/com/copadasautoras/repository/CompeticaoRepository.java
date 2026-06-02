package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.Competicao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompeticaoRepository extends JpaRepository<Competicao, Long> {
}
