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

    /**
     * Busca o AceiteTermo vinculado a uma submissão.
     *
     * Necessário na exclusão: o AceiteTermo tem FK para a Submissao, então
     * ele precisa ser apagado antes dela. Buscar pela autora não serve —
     * ela pode ter tido outras submissões em outras edições da Copa.
     */
    Optional<AceiteTermo> findBySubmissaoId(Long submissaoId);
}