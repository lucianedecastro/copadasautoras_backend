package br.com.copadasautoras.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SubmissaoEmailListener {

    private final EmailService emailService;
    private final EmailTemplates emailTemplates;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoRegistrarSubmissao(SubmissaoRegistradaEvent evento) {
        try {
            String corpo = emailTemplates.termoDeAceite(evento.nomeAutora());
            emailService.enviarHtmlComAnexo(
                    evento.destinatario(),
                    "Copa das Autoras — inscrição recebida",
                    corpo,
                    evento.termoPdf(),
                    "termo-de-aceite.pdf"
            );
        } catch (Exception e) {
            System.err.println("Falha ao enviar e-mail do termo para "
                    + evento.destinatario() + ": " + e.getMessage());
        }
    }
}
