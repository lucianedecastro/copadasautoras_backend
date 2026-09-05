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
import java.util.Objects;

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
     *
     * A obrigatoriedade dos campos (nome de exibição, biografia e rede
     * social) é garantida pelas validações do AutoraUpdateRequestDTO —
     * uma requisição incompleta é barrada com 400 antes de chegar aqui.
     */
    public AutoraResponseDTO atualizarMeuPerfil(
            AutoraUpdateRequestDTO request
    ) {

        Autora autora = obterAutoraAutenticada();

        // Guarda o link social ANTES de sobrescrever, pra detectar troca.
        String redeSocialAnterior = autora.getRedesSociais();

        autora.setNomeExibicao(request.nomeExibicao());
        autora.setBiografia(request.biografia());
        autora.setSite(request.site());
        autora.setRedesSociais(request.redesSociais());

        // =====================================================
        // RE-ANÁLISE AO TROCAR A REDE SOCIAL
        //
        // A aprovação manual valida um perfil de rede social
        // específico. Se uma autora JÁ APROVADA troca esse link,
        // o vínculo conferido deixa de valer: ela volta pra
        // PENDENTE pra nova conferência antes de reautorizar.
        //
        // Trocar apenas biografia ou site NÃO reabre a análise.
        // (Se não quiser esse comportamento, remova este bloco.)
        // =====================================================
        boolean trocouRedeSocial =
                !Objects.equals(
                        normalizar(redeSocialAnterior),
                        normalizar(request.redesSociais())
                );

        if (autora.getStatusAutora() == StatusAutora.APROVADA
                && trocouRedeSocial) {

            autora.setStatusAutora(StatusAutora.PENDENTE);
        }

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
     * Busca a autora completa por ID — uso administrativo.
     *
     * Diferente do perfil público, devolve status e o sinal de
     * perfilCompleto, que o painel do admin usa pra habilitar (ou não)
     * a aprovação e pra mostrar se a autora já está pronta pra conferência.
     */
    @Transactional(readOnly = true)
    public AutoraResponseDTO buscarPorIdAdmin(
            Long autoraId
    ) {

        return toResponseDTO(
                buscarAutoraPorId(autoraId)
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
     *
     * Trava institucional: não é possível aprovar uma autora com perfil
     * incompleto. Sem biografia e link de rede social não há o que a
     * administração conferir antes de autorizar submissões.
     */
    public AutoraResponseDTO aprovarAutora(
            Long autoraId
    ) {

        Autora autora = buscarAutoraPorId(
                autoraId
        );

        if (!autora.isPerfilCompleto()) {

            throw new RuntimeException(
                    "Não é possível aprovar: o perfil da autora está incompleto "
                            + "(nome completo, biografia e rede social são obrigatórios)."
            );
        }

        autora.setStatusAutora(
                StatusAutora.APROVADA
        );

        autoraRepository.save(autora);

        return toResponseDTO(autora);
    }

    /**
     * Suspende autora.
     * Uso administrativo.
     *
     * Vale a partir de qualquer status ativo (pendente, aprovada) — útil
     * para bloquear um cadastro que não deve seguir na competição.
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

    /**
     * Exclui a autora (soft delete).
     * Uso administrativo.
     *
     * Marca o status como EXCLUIDA sem remover fisicamente do banco —
     * mesmo princípio da autoexclusão. É reversível (a autora pode ser
     * reativada depois). Preserva a justificativa existente, se houver.
     */
    public AutoraResponseDTO excluirAutora(
            Long autoraId
    ) {

        Autora autora = buscarAutoraPorId(
                autoraId
        );

        autora.setStatusAutora(
                StatusAutora.EXCLUIDA
        );

        if (autora.getJustificativaExclusao() == null
                || autora.getJustificativaExclusao().isBlank()) {

            autora.setJustificativaExclusao(
                    "Excluída pela administração."
            );
        }

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

    /**
     * Normaliza um valor de texto para comparação (trim, null-safe).
     */
    private static String normalizar(String valor) {

        return valor == null ? null : valor.trim();
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
                autora.getStatusAutora(),
                autora.isPerfilCompleto()
        );
    }
}
