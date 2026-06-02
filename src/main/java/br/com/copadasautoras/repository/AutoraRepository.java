package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.Autora;
import br.com.copadasautoras.entity.StatusAutora;
import br.com.copadasautoras.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutoraRepository extends JpaRepository<Autora, Long> {

    /**
     * Busca autora pelo email institucional/login.
     * Email agora pertence a Usuario.
     */
    Optional<Autora> findByUsuarioEmail(String email);

    /**
     * Busca autora vinculada ao usuário autenticado.
     */
    Optional<Autora> findByUsuario(Usuario usuario);

    /**
     * Busca autora pelo id do usuário autenticado.
     */
    Optional<Autora> findByUsuarioId(Long usuarioId);

    /**
     * Lista autoras por status institucional.
     */
    List<Autora> findByStatusAutora(StatusAutora statusAutora);

    /**
     * Verifica se email já existe.
     * Email agora pertence a Usuario.
     */
    boolean existsByUsuarioEmail(String email);

    /**
     * Verifica se nome de exibição já existe.
     * (opcional para regra futura)
     */
    boolean existsByNomeExibicao(String nomeExibicao);
}

