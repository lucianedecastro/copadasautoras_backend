package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.AcaoAuditoria;
import br.com.copadasautoras.entity.OrigemAuditoria;
import br.com.copadasautoras.entity.RegistroAuditoria;
import br.com.copadasautoras.entity.StatusAutora;
import br.com.copadasautoras.entity.StatusSubmissao;
import br.com.copadasautoras.entity.TipoEntidadeAuditoria;
import br.com.copadasautoras.entity.Usuario;
import br.com.copadasautoras.repository.RegistroAuditoriaRepository;
import br.com.copadasautoras.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Ponto único de escrita do log de auditoria.
 *
 * Descobre QUEM agiu sozinho, pelo SecurityContext (o mesmo email
 * autenticado que os demais services já usam) — por isso não é
 * preciso passar o usuário nas chamadas.
 *
 * Sem @Transactional próprio de propósito: o registrar() participa da
 * transação de quem chama. Se a mudança de negócio der rollback, o log
 * some junto — ao contrário do e-mail (AFTER_COMMIT), perder um registro
 * de auditoria não é aceitável, e registrar um que não aconteceu, menos
 * ainda.
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final RegistroAuditoriaRepository registroAuditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Registro genérico.
     */
    public void registrar(
            OrigemAuditoria origem,
            TipoEntidadeAuditoria entidade,
            Long entidadeId,
            AcaoAuditoria acao,
            String valorAnterior,
            String valorNovo,
            String justificativa
    ) {

        Usuario ator = atorAtual();

        RegistroAuditoria registro = RegistroAuditoria.builder()
                .origem(origem)
                .atorId(ator != null ? ator.getId() : null)
                .atorNome(ator != null ? ator.getNome() : "sistema")
                .entidade(entidade)
                .entidadeId(entidadeId)
                .acao(acao)
                .valorAnterior(valorAnterior)
                .valorNovo(valorNovo)
                .justificativa(justificativa)
                .build();

        registroAuditoriaRepository.save(registro);
    }

    /**
     * Atalho para decisões sobre autora.
     */
    public void autora(
            OrigemAuditoria origem,
            Long autoraId,
            AcaoAuditoria acao,
            StatusAutora anterior,
            StatusAutora novo,
            String justificativa
    ) {
        registrar(
                origem,
                TipoEntidadeAuditoria.AUTORA,
                autoraId,
                acao,
                nome(anterior),
                nome(novo),
                justificativa
        );
    }

    /**
     * Atalho para decisões sobre obra.
     */
    public void submissao(
            OrigemAuditoria origem,
            Long submissaoId,
            AcaoAuditoria acao,
            StatusSubmissao anterior,
            StatusSubmissao novo,
            String justificativa
    ) {
        registrar(
                origem,
                TipoEntidadeAuditoria.SUBMISSAO,
                submissaoId,
                acao,
                nome(anterior),
                nome(novo),
                justificativa
        );
    }

    private static String nome(Enum<?> valor) {
        return valor != null ? valor.name() : null;
    }

    /**
     * Usuário autenticado na requisição atual, ou null se não houver
     * (ação disparada fora de um contexto autenticado).
     */
    private Usuario atorAtual() {
        try {
            Authentication auth = SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }

            String email = auth.getName();
            if (email == null || email.isBlank()
                    || "anonymousUser".equals(email)) {
                return null;
            }

            return usuarioRepository.findByEmail(email).orElse(null);

        } catch (Exception e) {
            return null;
        }
    }
}
