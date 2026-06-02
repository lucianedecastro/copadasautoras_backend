package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.Confronto;
import br.com.copadasautoras.entity.FaseCompeticao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfrontoRepository extends JpaRepository<Confronto, Long> {

    List<Confronto> findByFase(FaseCompeticao fase);

    boolean existsByFase(FaseCompeticao fase);
}
