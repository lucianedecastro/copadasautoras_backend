package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.entity.VotoPopular;
import br.com.copadasautoras.repository.SubmissaoRepository;
import br.com.copadasautoras.repository.VotoPopularRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VotoPopularService {

    private final SubmissaoRepository submissaoRepository;
    private final VotoPopularRepository votoPopularRepository;
    private final EmailService emailService;
    private final EmailTemplates emailTemplates;

    // URL da página de confirmação no frontend. O token vai como parâmetro.
    @Value("${app.votacao-popular.url:https://www.copadasautoras.com.br/votacao/confirmar.html}")
    private String baseConfirmacaoUrl;

    @Transactional
    public void solicitarVoto(Long submissaoId, String email) {

        Submissao submissao = submissaoRepository.findById(submissaoId)
                .orElseThrow(() -> new RuntimeException("Obra não encontrada."));

        if (!submissao.isElegivelVotoPopular()) {
            throw new RuntimeException(
                    "Esta obra não está elegível para a votação popular."
            );
        }

        if (votoPopularRepository.existsByEmailAndConfirmadoTrue(email)) {
            throw new RuntimeException(
                    "Este e-mail já confirmou um voto no Escolha do Público."
            );
        }

        String token = UUID.randomUUID().toString();

        VotoPopular voto = VotoPopular.builder()
                .submissao(submissao)
                .email(email)
                .token(token)
                .confirmado(false)
                .build();

        votoPopularRepository.save(voto);

        String link = baseConfirmacaoUrl + "?token=" + token;
        String corpo = emailTemplates.confirmarVotoPopular(submissao.getTitulo(), link);
        emailService.enviarHtml(email, "Copa das Autoras — confirme o seu voto", corpo);
    }

    @Transactional
    public void confirmarVoto(String token) {

        VotoPopular voto = votoPopularRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Link de confirmação inválido."));

        if (voto.isConfirmado()) {
            throw new RuntimeException("Este voto já foi confirmado.");
        }

        // Checagem na aplicação; a constraint única parcial no banco
        // (ver V16) é a rede de segurança final contra corrida entre
        // duas confirmações simultâneas do mesmo e-mail.
        if (votoPopularRepository.existsByEmailAndConfirmadoTrue(voto.getEmail())) {
            throw new RuntimeException(
                    "Este e-mail já confirmou um voto no Escolha do Público."
            );
        }

        voto.setConfirmado(true);
        voto.setDataConfirmacao(java.time.LocalDateTime.now());

        votoPopularRepository.save(voto);
    }
}