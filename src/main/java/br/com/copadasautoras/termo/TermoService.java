package br.com.copadasautoras.termo;

import br.com.copadasautoras.entity.AceiteTermo;
import br.com.copadasautoras.entity.Submissao;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class TermoService {

    // Paleta de cores — verde e branco, remetendo ao futebol feminino
    private static final DeviceRgb VERDE_ESCURO  = new DeviceRgb(27,  94,  32);
    private static final DeviceRgb VERDE_MEDIO   = new DeviceRgb(46, 125,  50);
    private static final DeviceRgb VERDE_CLARO   = new DeviceRgb(232, 245, 233);
    private static final DeviceRgb CINZA_TEXTO   = new DeviceRgb(55,  55,  55);
    private static final DeviceRgb CINZA_CLARO   = new DeviceRgb(245, 245, 245);

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    /**
     * Gera o PDF do termo de aceite e retorna os bytes prontos para salvar/enviar.
     */
    public byte[] gerarTermoPdf(AceiteTermo aceite, Submissao submissao) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter writer   = new PdfWriter(baos);
            PdfDocument pdf    = new PdfDocument(writer);
            Document documento = new Document(pdf, PageSize.A4);
            documento.setMargins(50, 50, 50, 50);

            PdfFont fonteBold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fonteNormal  = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fonteItalico = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            // ── CABEÇALHO ──────────────────────────────────────────────────
            adicionarCabecalho(documento, fonteBold, fonteNormal);

            // ── DADOS DA SUBMISSÃO ─────────────────────────────────────────
            adicionarDadosSubmissao(documento, fonteBold, fonteNormal, aceite, submissao);

            // ── CORPO DO TERMO ─────────────────────────────────────────────
            adicionarCorpoTermo(documento, fonteBold, fonteNormal, fonteItalico);

            // ── REGISTRO DE ACEITE ─────────────────────────────────────────
            adicionarRegistroAceite(documento, fonteBold, fonteNormal, aceite);

            // ── RODAPÉ ─────────────────────────────────────────────────────
            adicionarRodape(documento, fonteItalico, aceite);

            documento.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do termo: " + e.getMessage(), e);
        }
    }

    // ── CABEÇALHO ──────────────────────────────────────────────────────────
    private void adicionarCabecalho(Document doc, PdfFont bold, PdfFont normal) {

        // Faixa verde escura de topo
        Table faixa = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .setBackgroundColor(VERDE_ESCURO)
                .setBorder(Border.NO_BORDER);

        Cell celulaTitulo = new Cell()
                .add(new Paragraph("⚽  COPA DE LITERATURA DE FUTEBOL FEMININO")
                        .setFont(bold)
                        .setFontSize(14)
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Termo de Submissão e Licença de Exibição")
                        .setFont(normal)
                        .setFontSize(10)
                        .setFontColor(new DeviceRgb(200, 230, 201))
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER)
                .setPadding(16);

        faixa.addCell(celulaTitulo);
        doc.add(faixa);
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    // ── DADOS DA SUBMISSÃO ─────────────────────────────────────────────────
    private void adicionarDadosSubmissao(Document doc, PdfFont bold, PdfFont normal,
                                         AceiteTermo aceite, Submissao submissao) {

        Table tabela = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setBackgroundColor(VERDE_CLARO)
                .setBorder(new SolidBorder(VERDE_MEDIO, 1f))
                .setMarginBottom(16);

        // Cabeçalho da tabela
        Cell cabecalho = new Cell(1, 2)
                .add(new Paragraph("DADOS DA SUBMISSÃO")
                        .setFont(bold)
                        .setFontSize(9)
                        .setFontColor(VERDE_ESCURO)
                        .setTextAlignment(TextAlignment.LEFT))
                .setBackgroundColor(VERDE_CLARO)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(VERDE_MEDIO, 0.5f))
                .setPadding(8);
        tabela.addCell(cabecalho);

        adicionarLinha(tabela, bold, normal, "Autora",
                aceite.getAutora().getNome());
        adicionarLinha(tabela, bold, normal, "E-mail",
                aceite.getAutora().getUsuario().getEmail());
        adicionarLinha(tabela, bold, normal, "Título da obra",
                submissao.getTitulo());
        adicionarLinha(tabela, bold, normal, "Categoria",
                submissao.getCategoria() != null ? submissao.getCategoria() : "—");
        adicionarLinha(tabela, bold, normal, "Modalidade de exibição",
                submissao.getTipoExibicao().name().equals("COMPLETO")
                        ? "Obra completa" : "Trecho da obra");
        adicionarLinha(tabela, bold, normal, "Versão do termo",
                aceite.getVersaoTermo());

        doc.add(tabela);
    }

    private void adicionarLinha(Table tabela, PdfFont bold, PdfFont normal,
                                String rotulo, String valor) {
        tabela.addCell(new Cell()
                .add(new Paragraph(rotulo)
                        .setFont(bold)
                        .setFontSize(9)
                        .setFontColor(CINZA_TEXTO))
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(220, 220, 220), 0.3f))
                .setBackgroundColor(VERDE_CLARO)
                .setPaddingLeft(8).setPaddingTop(5).setPaddingBottom(5));

        tabela.addCell(new Cell()
                .add(new Paragraph(valor)
                        .setFont(normal)
                        .setFontSize(9)
                        .setFontColor(CINZA_TEXTO))
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(220, 220, 220), 0.3f))
                .setBackgroundColor(VERDE_CLARO)
                .setPaddingLeft(8).setPaddingTop(5).setPaddingBottom(5));
    }

    // ── CORPO DO TERMO ─────────────────────────────────────────────────────
    private void adicionarCorpoTermo(Document doc, PdfFont bold, PdfFont normal, PdfFont italico) {

        // Título da seção
        doc.add(new Paragraph("TERMOS E CONDIÇÕES")
                .setFont(bold)
                .setFontSize(10)
                .setFontColor(VERDE_ESCURO)
                .setMarginBottom(8));

        String[][] secoes = {
                {"1. APRESENTAÇÃO",
                        "A Copa de Literatura de Futebol Feminino é uma iniciativa cultural voltada ao fomento da produção literária sobre futebol feminino por mulheres, com o objetivo de promover visibilidade, circulação de obras e valorização de novas autoras. Ao submeter uma obra, a autora declara que leu, compreendeu e concorda integralmente com os termos abaixo."},

                {"2. ELEGIBILIDADE",
                        "Poderão participar exclusivamente pessoas que se identifiquem como mulheres, independentemente de nacionalidade, desde que autoras da obra submetida. A autora declara ser a legítima titular dos direitos autorais da obra submetida, responsabilizando-se integralmente por sua originalidade. Não serão aceitas obras que violem direitos autorais de terceiros, contenham conteúdo discriminatório, ofensivo ou ilegal, ou não estejam alinhadas com a temática proposta."},

                {"3. SOBRE A OBRA SUBMETIDA",
                        "Serão aceitas obras em diversos formatos literários, incluindo crônicas, contos, romances, biografias, quadrinhos e ficção em geral. A obra poderá ser inédita ou já publicada, desde que a autora possua os direitos necessários para sua submissão e exibição."},

                {"4. MODALIDADE DE EXIBIÇÃO PÚBLICA",
                        "No ato da submissão, a autora escolhe entre disponibilizar um trecho da obra ou a obra completa publicamente. A autora declara estar ciente de que a escolha pela exibição integral pode impactar futuras oportunidades editoriais, especialmente em relação a critérios de ineditismo adotados por editoras."},

                {"5. ACESSO DA BANCA AVALIADORA",
                        "Independentemente da modalidade de exibição pública, a autora disponibiliza a obra completa para a organização. A obra integral será acessível exclusivamente à banca avaliadora, para fins de análise técnica e julgamento. A organização compromete-se a garantir o acesso restrito ao conteúdo integral."},

                {"6. LICENÇA DE USO E EXIBIÇÃO",
                        "A autora concede à organização uma licença não exclusiva, gratuita e por prazo determinado, para exibir a obra na plataforma digital, utilizar trechos para divulgação institucional e associar a obra à identidade visual da competição. Esta licença não implica transferência de titularidade dos direitos autorais e não impede a autora de publicar ou licenciar sua obra por outros meios."},

                {"7. PRAZO DE EXIBIÇÃO E REMOÇÃO",
                        "A obra permanecerá disponível durante o período da competição. Após o encerramento, a autora poderá solicitar a remoção ou a manutenção da obra como parte do acervo da plataforma, pelos canais oficiais da organização."},

                {"8. FORMATO DA COMPETIÇÃO",
                        "As obras selecionadas participarão de um sistema eliminatório inspirado no formato de Copa do Mundo. O avanço nas etapas ocorrerá conforme regras previamente divulgadas, podendo envolver votação popular, avaliação técnica da banca ou modelo híbrido."},

                {"9. OBRA VENCEDORA",
                        "A obra vencedora poderá ser convidada a participar de processo de publicação. Eventual publicação dependerá de negociação específica entre a autora e a organização e/ou editoras parceiras. Nenhuma obrigação de cessão de direitos será imposta automaticamente."},

                {"10. RESPONSABILIDADES DA AUTORA",
                        "A autora é integralmente responsável pelo conteúdo submetido, declarando que a obra é original ou devidamente autorizada, não infringe direitos de terceiros e possui autorização para uso de eventuais elementos protegidos."},

                {"11. DISPOSIÇÕES FINAIS",
                        "A organização poderá realizar ajustes operacionais no formato da competição, desde que não prejudiquem os direitos das participantes. Casos omissos serão resolvidos com base nos princípios de boa-fé e promoção cultural. Ao submeter a obra, a autora declara sua concordância integral com este Termo."}
        };

        for (String[] secao : secoes) {
            doc.add(new Paragraph(secao[0])
                    .setFont(bold)
                    .setFontSize(9)
                    .setFontColor(VERDE_MEDIO)
                    .setMarginTop(8)
                    .setMarginBottom(2));

            doc.add(new Paragraph(secao[1])
                    .setFont(normal)
                    .setFontSize(8.5f)
                    .setFontColor(CINZA_TEXTO)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMarginBottom(2)
                    .setFirstLineIndent(12));
        }

        doc.add(new Paragraph("\n").setFontSize(4));
    }

    // ── REGISTRO DE ACEITE ─────────────────────────────────────────────────
    private void adicionarRegistroAceite(Document doc, PdfFont bold, PdfFont normal,
                                         AceiteTermo aceite) {

        Table tabela = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth()
                .setBackgroundColor(CINZA_CLARO)
                .setBorder(new SolidBorder(VERDE_MEDIO, 1f))
                .setMarginTop(8)
                .setMarginBottom(12);

        Cell cabecalho = new Cell(1, 2)
                .add(new Paragraph("REGISTRO DIGITAL DE ACEITE")
                        .setFont(bold)
                        .setFontSize(9)
                        .setFontColor(VERDE_ESCURO))
                .setBackgroundColor(CINZA_CLARO)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(VERDE_MEDIO, 0.5f))
                .setPadding(8);
        tabela.addCell(cabecalho);

        adicionarLinhaAceite(tabela, bold, normal,
                "Li e concordo com todos os termos",             aceite.getAceiteTermoCompleto());
        adicionarLinhaAceite(tabela, bold, normal,
                "Declaro ser a legítima autora da obra",         aceite.getAceiteAutoria());
        adicionarLinhaAceite(tabela, bold, normal,
                "Autorizo a exibição na modalidade escolhida",   aceite.getAceiteExibicao());
        adicionarLinhaAceite(tabela, bold, normal,
                "Autorizo o acesso da banca à obra completa",    aceite.getAceiteBanca());
        adicionarLinhaAceite(tabela, bold, normal,
                "Declaro ser titular dos direitos da obra",      aceite.getAceiteTitularidade());

        doc.add(tabela);
    }

    private void adicionarLinhaAceite(Table tabela, PdfFont bold, PdfFont normal,
                                      String descricao, Boolean aceito) {
        String icone = Boolean.TRUE.equals(aceito) ? "✓  " : "✗  ";
        DeviceRgb cor = Boolean.TRUE.equals(aceito) ? VERDE_ESCURO : new DeviceRgb(183, 28, 28);

        tabela.addCell(new Cell()
                .add(new Paragraph(descricao)
                        .setFont(normal)
                        .setFontSize(9)
                        .setFontColor(CINZA_TEXTO))
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(220, 220, 220), 0.3f))
                .setBackgroundColor(CINZA_CLARO)
                .setPaddingLeft(8).setPaddingTop(5).setPaddingBottom(5));

        tabela.addCell(new Cell()
                .add(new Paragraph(icone + (Boolean.TRUE.equals(aceito) ? "SIM" : "NÃO"))
                        .setFont(bold)
                        .setFontSize(9)
                        .setFontColor(cor)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(220, 220, 220), 0.3f))
                .setBackgroundColor(CINZA_CLARO)
                .setPaddingTop(5).setPaddingBottom(5));
    }

    // ── RODAPÉ ─────────────────────────────────────────────────────────────
    private void adicionarRodape(Document doc, PdfFont italico, AceiteTermo aceite) {

        // Linha separadora
        Table linha = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .setMarginBottom(6);
        linha.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderTop(new SolidBorder(VERDE_MEDIO, 1f))
                .setPadding(0));
        doc.add(linha);

        // Data e hora do aceite
        String dataFormatada = aceite.getDataAceite() != null
                ? aceite.getDataAceite().format(FORMATO_DATA)
                : "—";

        doc.add(new Paragraph("Aceite registrado digitalmente em " + dataFormatada
                + "  •  IP: " + (aceite.getIpAddress() != null ? aceite.getIpAddress() : "não registrado")
                + "  •  Versão do termo: " + aceite.getVersaoTermo())
                .setFont(italico)
                .setFontSize(7.5f)
                .setFontColor(new DeviceRgb(120, 120, 120))
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(
                "Este documento é gerado automaticamente e constitui registro formal do aceite digital dos termos de participação.\n"
                        + "Copa de Literatura de Futebol Feminino — todos os direitos das autoras preservados.")
                .setFont(italico)
                .setFontSize(7f)
                .setFontColor(new DeviceRgb(160, 160, 160))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(4));
    }
}
