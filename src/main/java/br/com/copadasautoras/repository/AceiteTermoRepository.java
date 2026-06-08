package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.AceiteTermo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AceiteTermoRepository extends JpaRepository<AceiteTermo, Long> {

    /**
     * Busca o AceiteTermo pelo e-mail do Usuário vinculado à Autora.
     * Cadeia: AceiteTermo.autora → Autora.usuario → Usuario.email
     * Confirmado via TermoService: aceite.getAutora().getUsuario().getEmail()
     */
    Optional<AceiteTermo> findByAutora_Usuario_Email(String email);
}