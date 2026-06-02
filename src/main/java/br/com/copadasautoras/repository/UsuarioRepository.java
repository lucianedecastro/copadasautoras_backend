package br.com.copadasautoras.repository;

import br.com.copadasautoras.entity.Role;
import br.com.copadasautoras.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(
            String email
    );

    List<Usuario> findByRole(
            Role role
    );
}
