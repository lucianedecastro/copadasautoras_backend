package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.entity.GrupoCompeticao;
import br.com.copadasautoras.entity.NomeGrupo;
import br.com.copadasautoras.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrupoCompeticaoRepository
        extends JpaRepository<GrupoCompeticao, Long> {

    List<GrupoCompeticao> findByFase(
            FaseCompeticao fase
    );

    Optional<GrupoCompeticao> findByFaseAndNomeGrupo(
            FaseCompeticao fase,
            NomeGrupo nomeGrupo
    );

    Optional<GrupoCompeticao> findByBancaAndFase(
            Usuario banca,
            FaseCompeticao fase
    );

    boolean existsByFase(
            FaseCompeticao fase
    );
}
