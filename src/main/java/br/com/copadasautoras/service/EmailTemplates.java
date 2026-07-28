package br.com.copadasautoras.service;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {

    public String termoDeAceite(String nomeAutora) {
        return BASE.replace("{{titulo}}", "Inscrição recebida")
                .replace("{{conteudo}}", """
                        <p style="margin:0 0 16px;">Olá, <strong>%s</strong>,</p>
                        <p style="margin:0 0 16px;">Recebemos a sua inscrição na
                        <strong>Copa das Autoras</strong>. Em anexo está o seu
                        termo de aceite, com os dados e as condições de participação.</p>
                        <p style="margin:0 0 16px;">Guarde este documento com você.
                        Qualquer dúvida, é só responder a este e-mail.</p>
                        <p style="margin:0;">Boa escrita — e bom jogo.</p>
                        """.formatted(escapar(nomeAutora)));
    }

    public String resetSenha(String nomeAutora, String linkReset) {
        return BASE.replace("{{titulo}}", "Redefinição de senha")
                .replace("{{conteudo}}", """
                        <p style="margin:0 0 16px;">Olá, <strong>%s</strong>,</p>
                        <p style="margin:0 0 16px;">Recebemos um pedido para redefinir
                        a senha da sua conta na <strong>Copa das Autoras</strong>.
                        Clique no botão abaixo para criar uma nova senha:</p>
                        <p style="margin:0 0 24px;">
                          <a href="%s"
                             style="display:inline-block; background-color:#7A1F35;
                                    color:#ffffff; text-decoration:none;
                                    padding:12px 28px; font-family:Georgia,serif;
                                    font-size:15px;">Redefinir minha senha</a>
                        </p>
                        <p style="margin:0 0 16px; font-size:14px; color:#555;">
                        Este link expira em 1 hora e só pode ser usado uma vez.
                        Se o botão não funcionar, copie e cole este endereço no
                        navegador:<br>%s</p>
                        <p style="margin:0; font-size:14px; color:#555;">Se você não
                        pediu esta redefinição, pode ignorar este e-mail com
                        segurança — a sua senha continua a mesma.</p>
                        """.formatted(escapar(nomeAutora), linkReset, linkReset));
    }

    private String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static final String BASE = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <body style="margin:0; padding:0; background-color:#F5F3EE;">
              <table role="presentation" width="100%" cellpadding="0" cellspacing="0"
                     style="background-color:#F5F3EE;">
                <tr>
                  <td align="center" style="padding:32px 16px;">
                    <table role="presentation" width="600" cellpadding="0" cellspacing="0"
                           style="max-width:600px; width:100%; background-color:#ffffff;">
                      <tr>
                        <td style="padding:28px 32px; border-bottom:3px solid #7A1F35;">
                          <span style="font-family:Georgia,serif; font-size:22px;
                                       color:#7A1F35; letter-spacing:0.5px;">
                            Copa das Autoras
                          </span>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:32px; font-family:Georgia,serif;
                                   font-size:16px; line-height:1.6; color:#2b2b2b;">
                          <h1 style="margin:0 0 20px; font-size:20px; color:#7A1F35;
                                     font-weight:normal;">{{titulo}}</h1>
                          {{conteudo}}
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:20px 32px; background-color:#F5F3EE;
                                   border-top:1px solid #e5e0d6;">
                          <p style="margin:0; font-family:Georgia,serif; font-style:italic;
                                    font-size:14px; color:#7A1F35;">
                            Escrever também é entrar em campo.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;
}