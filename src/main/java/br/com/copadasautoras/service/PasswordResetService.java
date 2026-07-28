package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.PasswordResetToken;
import br.com.copadasautoras.entity.Usuario;
import br.com.copadasautoras.repository.PasswordResetTokenRepository;
import br.com.copadasautoras.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final EmailTemplates emailTemplates;
    private final PasswordEncoder passwordEncoder;

    // URL da página de redefinição no frontend. O token vai como parâmetro.
    @Value("${app.reset-senha.url:https://copadasautoras.com.br/redefinir-senha}")
    private String baseResetUrl;

    @Transactional
    public void solicitarReset(String email) {

        // Se o e-mail não existe, encerra em silêncio — a resposta ao usuário
        // é sempre a mesma, pra não revelar quais e-mails têm conta.
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) {
            return;
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken prt = PasswordResetToken.builder()
                .token(token)
                .usuario(usuario)
                .expiraEm(LocalDateTime.now().plusHours(1))
                .usado(false)
                .build();

        tokenRepository.save(prt);

        String link = baseResetUrl + "?token=" + token;
        String corpo = emailTemplates.resetSenha(usuario.getNome(), link);
        emailService.enviarHtml(email, "Copa das Autoras — redefinição de senha", corpo);
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {

        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Link inválido."));

        if (Boolean.TRUE.equals(prt.getUsado())) {
            throw new RuntimeException("Este link já foi utilizado. Solicite um novo.");
        }

        if (prt.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Este link expirou. Solicite um novo.");
        }

        Usuario usuario = prt.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        prt.setUsado(true);
        tokenRepository.save(prt);
    }
}
