package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.*;
import br.com.copadasautoras.entity.Autora;
import br.com.copadasautoras.entity.StatusAutora;
import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.entity.Usuario;
import br.com.copadasautoras.repository.AutoraRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import br.com.copadasautoras.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AutoraService {

    private final AutoraRepository autoraRepository;
    private final UsuarioRepository usuarioRepository;
    private final SubmissaoRepository submissaoRepository;

    /**
     * Busca o perfil privado da autora autenticada.
     */
    @Transactional(readOnly = true)
    public AutoraResponseDTO buscarMeuPerfil() {

        Autora autora = obterAutoraAutenticada();

        return toResponseDTO(autora);
    }

    /**
     * Atualiza perfil editável da autora.
     * Campos protegidos:
     * - nome
     * - email
     * - status
     */
    public AutoraResponseDTO atualizarMeuPerfil(
            AutoraUpdateRequestDTO request
    ) {

        Autora autora = obterAutoraAutenticada();

        autora.setNomeExibicao(request.nomeExibicao());
        autora.setBiografia(request.biografia());
        autora.setSite(request.site());
        autora.setRedesSociais(request.redesSociais());

        autoraRepository.save(autora);

        return toResponseDTO(autora);
    }

    /**
     * Busca perfil público da autora.
     */
    @Transactional(readOnly = true)
    public AutoraPublicResponseDTO buscarPerfilPublico(
            Long autoraId
    ) {

        Autora autora = autoraRepository.findById(autoraId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Autora não encontrada."
                        )
                );

        Submissao submissao = submissaoRepository
                .findFirstByAutoraId(autoraId)
                .orElse(null);

        String tituloObra =
                submissao != null
                        ? submissao.getTitulo()
                        : null;

        String categoria =
                submissao != null
                        ? submissao.getCategoria()
                        : null;

        return new AutoraPublicResponseDTO(
                autora.getId(),
                autora.getNomeExibicao(),
                autora.getBiografia(),
                autora.getSite(),
                autora.getRedesSociais(),
                tituloObra,
                categoria
        );
    }

    /**
     * Solicita exclusão institucional do perfil.
     * Não remove fisicamente do banco.
     */
    public void solicitarExclusao(
            SolicitacaoExclusaoDTO request
    ) {

        Autora autora = obterAutoraAutenticada();

        autora.setStatusAutora(
                StatusAutora.EXCLUIDA
        );

        autora.setJustificativaExclusao(
                request.justificativa()
        );

        autoraRepository.save(autora);
    }

    /**
     * Lista autoras por status.
     * Uso administrativo.
     */
    @Transactional(readOnly = true)
    public List<AutoraResponseDTO> listarPorStatus(
            StatusAutora status
    ) {

        return autoraRepository
                .findByStatusAutora(status)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Aprova autora.
     * Uso administrativo.
     */
    public AutoraResponseDTO aprovarAutora(
            Long autoraId
    ) {

        Autora autora = buscarAutoraPorId(
                autoraId
        );

        autora.setStatusAutora(
                StatusAutora.APROVADA
        );

        autoraRepository.save(autora);

        return toResponseDTO(autora);
    }

    /**
     * Suspende autora.
     * Uso administrativo.
     */
    public AutoraResponseDTO suspenderAutora(
            Long autoraId
    ) {

        Autora autora = buscarAutoraPorId(
                autoraId
        );

        autora.setStatusAutora(
                StatusAutora.SUSPENSA
        );

        autoraRepository.save(autora);

        return toResponseDTO(autora);
    }

    // =====================================================
    // MÉTODOS PRIVADOS
    // =====================================================

    private Autora obterAutoraAutenticada() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Usuário autenticado não encontrado."
                        )
                );

        return autoraRepository
                .findByUsuario(usuario)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Perfil da autora não encontrado."
                        )
                );
    }

    private Autora buscarAutoraPorId(
            Long autoraId
    ) {

        return autoraRepository
                .findById(autoraId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Autora não encontrada."
                        )
                );
    }

    private AutoraResponseDTO toResponseDTO(
            Autora autora
    ) {

        return new AutoraResponseDTO(
                autora.getId(),
                autora.getNome(),
                autora.getNomeExibicao(),
                autora.getUsuario().getEmail(),
                autora.getBiografia(),
                autora.getSite(),
                autora.getRedesSociais(),
                autora.getStatusAutora()
        );
    }
}

