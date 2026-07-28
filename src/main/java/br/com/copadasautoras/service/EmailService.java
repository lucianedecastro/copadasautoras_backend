package br.com.copadasautoras.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Precisa ser a conta autenticada (ou alias verificado); o Gmail recusa outro "de".
    @Value("${spring.mail.username}")
    private String remetente;

    public void enviarHtml(String destinatario, String assunto, String corpoHtml) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mensagem, false, StandardCharsets.UTF_8.name());

            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);

            mailSender.send(mensagem);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao enviar e-mail para " + destinatario, e);
        }
    }

    public void enviarHtmlComAnexo(String destinatario, String assunto,
                                   String corpoHtml, byte[] anexo, String nomeAnexo) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mensagem, true, StandardCharsets.UTF_8.name());

            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);
            helper.addAttachment(nomeAnexo, new ByteArrayResource(anexo));

            mailSender.send(mensagem);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao enviar e-mail com anexo para " + destinatario, e);
        }
    }
}