package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.VotoPopularResultadoDTO;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * PDF do placar do "Escolha do Público" (iText7) na identidade da Copa:
 * vinho #7A1F35, cartões e tabela limpa — mesmo desenho dos demais PDFs.
 *
 * Só números agregados: nenhum e-mail de eleitor entra no documento.
 */
final class VotoPopularPdfBuilder {

    private static final DeviceRgb VINHO = new DeviceRgb(0x7A, 0x1F, 0x35);
    private static final DeviceRgb BORDA = new DeviceRgb(0xDD, 0xD6, 0xCF);
    private static final DeviceRgb CINZA = new DeviceRgb(0x6F, 0x6A, 0x64);

    private static final String NOTA =
            "Somente votos confirmados por e-mail entram no placar; cada "
          + "e-mail vale um voto. O Escolha do Público é um reconhecimento "
          + "à parte do resultado do júri. O documento traz apenas números "
          + "agregados: nenhum e-mail de eleitor é exibido.";

    private VotoPopularPdfBuilder() {
    }

    private static String situacao(VotoPopularResultadoDTO r) {
        String base = r.votacaoEncerrada()
                ? "Votação encerrada em " + r.encerradaEm()
                : "Votação em andamento";
        String selo = r.obras().stream()
                .filter(VotoPopularResultadoDTO.Obra::selo)
                .map(o -> o.titulo() + " (" + o.nomeAutora() + ")")
                .findFirst()
                .map(x -> "  ·  Selo Escolha do Público: " + x)
                .orElse("");
        String fora = r.semifinalistasForaDaVotacao() > 0
                ? "  ·  " + r.semifinalistasForaDaVotacao()
                + " semifinalista(s) sem trecho, fora da votação"
                : "";
        return base + selo + fora;
    }

    static byte[] gerar(VotoPopularResultadoDTO r) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfDocument pdf = new PdfDocument(new PdfWriter(out));
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(28, 28, 28, 28);

            doc.add(new Paragraph("Copa das Autoras — Escolha do Público")
                    .setBold().setFontSize(16).setFontColor(VINHO));

            doc.add(new Paragraph("Placar da votação popular  ·  atualizado em "
                    + r.geradoEm())
                    .setFontSize(9).setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(6));

            doc.add(new Paragraph(situacao(r))
                    .setFontSize(10).setBold().setFontColor(VINHO)
                    .setMarginBottom(12));

            Table cartoes = new Table(
                    UnitValue.createPercentArray(new float[]{1, 1, 1}))
                    .useAllAvailableWidth();
            cartoes.addCell(cartao(String.valueOf(r.totalVotosConfirmados()),
                    "Votos confirmados"));
            cartoes.addCell(cartao(String.valueOf(r.totalVotosPendentes()),
                    "Aguardando confirmação"));
            cartoes.addCell(cartao(String.valueOf(r.totalObras()),
                    "Obras na votação"));
            doc.add(cartoes);

            doc.add(new Paragraph("Placar")
                    .setBold().setFontSize(12).setFontColor(VINHO)
                    .setMarginTop(16).setMarginBottom(6));

            Table t = new Table(UnitValue.createPercentArray(
                    new float[]{7, 36, 25, 12, 10, 10}))
                    .useAllAvailableWidth();

            t.addHeaderCell(cabecalho("Pos.", false));
            t.addHeaderCell(cabecalho("Obra", false));
            t.addHeaderCell(cabecalho("Autora", false));
            t.addHeaderCell(cabecalho("Categoria", false));
            t.addHeaderCell(cabecalho("Votos", true));
            t.addHeaderCell(cabecalho("%", true));

            if (r.obras().isEmpty()) {
                t.addCell(new Cell(1, 6)
                        .add(new Paragraph("Nenhuma obra elegível ainda.")
                                .setFontSize(9).setFontColor(CINZA))
                        .setBorder(new SolidBorder(BORDA, 0.5f))
                        .setPadding(5));
            }

            int pos = 1;
            for (VotoPopularResultadoDTO.Obra o : r.obras()) {
                t.addCell(celula(pos++ + "º", false));
                t.addCell(celula(nz(o.titulo()), false));
                t.addCell(celula(nz(o.nomeAutora()), false));
                t.addCell(celula(nz(o.categoria()), false));
                t.addCell(celula(String.valueOf(o.votosConfirmados()), true));
                t.addCell(celula(percentual(o.percentual()), true));
            }
            doc.add(t);

            doc.add(new Paragraph(NOTA)
                    .setFontSize(8).setFontColor(CINZA).setMarginTop(14));

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao gerar PDF do Escolha do Público: "
                            + e.getMessage(), e);
        }
    }

    private static Cell cartao(String numero, String rotulo) {
        return new Cell()
                .setBorder(new SolidBorder(BORDA, 0.75f))
                .setPadding(9)
                .add(new Paragraph(numero).setBold().setFontSize(20)
                        .setFontColor(VINHO).setMarginBottom(0))
                .add(new Paragraph(rotulo).setFontSize(8)
                        .setFontColor(CINZA).setMarginTop(0));
    }

    private static Cell cabecalho(String texto, boolean direita) {
        return new Cell()
                .add(new Paragraph(texto).setBold().setFontSize(9)
                        .setTextAlignment(direita
                                ? TextAlignment.RIGHT : TextAlignment.LEFT))
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(VINHO)
                .setBorder(new SolidBorder(BORDA, 0.5f))
                .setPadding(5);
    }

    private static Cell celula(String texto, boolean direita) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(9)
                        .setTextAlignment(direita
                                ? TextAlignment.RIGHT : TextAlignment.LEFT))
                .setBorder(new SolidBorder(BORDA, 0.5f))
                .setPadding(5);
    }

    private static String percentual(double pct) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.1f%%", pct);
    }

    private static String nz(String v) {
        return v != null ? v : "";
    }
}
