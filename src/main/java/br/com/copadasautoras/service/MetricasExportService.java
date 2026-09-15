package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.RelatorioMetricasDTO;
import br.com.copadasautoras.dto.SeriePontoDTO;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Geração dos relatórios de inscrições da Copa.
 *
 * Excel via Apache POI (.xlsx) e PDF via iText7, na identidade da
 * Copa das Autoras (vinho #7A1F35, layout limpo). Mesma arquitetura
 * do LanceExportService.
 *
 * O relatório é 100% agregado: só números. Nenhum nome de autora,
 * nenhum título de obra — por decisão editorial.
 */
@Service
public class MetricasExportService {

    // Vinho da Copa (#7A1F35).
    private static final byte[] VINHO_RGB =
            new byte[]{(byte) 0x7A, (byte) 0x1F, (byte) 0x35};

    private static final DeviceRgb VINHO =
            new DeviceRgb(0x7A, 0x1F, 0x35);

    private static final DeviceRgb BORDA =
            new DeviceRgb(0xDD, 0xD6, 0xCF);

    private static final String NOTA_RODAPE =
            "Relatório agregado, sem dados que identifiquem autoras ou obras. "
          + "As autoras são contadas pelo total acumulado até a geração — o "
          + "cadastro não registra data de inscrição, portanto não é filtrado "
          + "por período.";

    // =========================
    // EXCEL (Apache POI)
    // =========================

    public byte[] gerarExcel(RelatorioMetricasDTO r) {

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle cabecalho = criarEstiloCabecalho(workbook);
            CellStyle rotulo = criarEstiloRotulo(workbook);

            // ---- Aba Resumo ----
            Sheet resumo = workbook.createSheet("Resumo");
            resumo.setColumnWidth(0, 42 * 256);
            resumo.setColumnWidth(1, 18 * 256);

            int linha = 0;

            linha = secao(resumo, linha,
                    "Copa das Autoras — Relatório de inscrições", cabecalho);
            linha = kv(resumo, linha, "Período",
                    r.periodoDefinido()
                            ? (r.periodoInicio() + " a " + r.periodoFim())
                            : "desde o início até hoje");
            linha = kv(resumo, linha, "Gerado em", r.geradoEm());
            linha++;

            linha = secao(resumo, linha,
                    "Autoras (total acumulado)", cabecalho);
            linha = kv(resumo, linha, "Total",
                    String.valueOf(r.totalAutoras()));
            linha = kv(resumo, linha, "Aprovadas",
                    String.valueOf(r.autorasAprovadas()));
            linha = kv(resumo, linha, "Pendentes",
                    String.valueOf(r.autorasPendentes()));
            linha = kv(resumo, linha, "Suspensas",
                    String.valueOf(r.autorasSuspensas()));
            linha = kv(resumo, linha, "Excluídas",
                    String.valueOf(r.autorasExcluidas()));
            linha++;

            linha = secao(resumo, linha,
                    "Obras inscritas no período (por status atual)", cabecalho);
            linha = kv(resumo, linha, "Total no período",
                    String.valueOf(r.obrasNoPeriodo()));
            linha = kv(resumo, linha, "Em curadoria (submetidas)",
                    String.valueOf(r.obrasEmCuradoria()));
            linha = kv(resumo, linha, "Não selecionadas",
                    String.valueOf(r.obrasNaoSelecionadas()));
            linha = kv(resumo, linha, "Em competição",
                    String.valueOf(r.obrasEmCompeticao()));
            linha = kv(resumo, linha, "Classificadas",
                    String.valueOf(r.obrasClassificadas()));
            linha = kv(resumo, linha, "Eliminadas",
                    String.valueOf(r.obrasEliminadas()));
            linha = kv(resumo, linha, "Campeã",
                    String.valueOf(r.obrasCampea()));
            linha++;

            Row nota = resumo.createRow(linha);
            nota.createCell(0).setCellValue(NOTA_RODAPE);

            // ---- Aba Obras por semana ----
            Sheet semanal = workbook.createSheet("Obras por semana");
            semanal.setColumnWidth(0, 22 * 256);
            semanal.setColumnWidth(1, 18 * 256);

            Row cab = semanal.createRow(0);
            org.apache.poi.ss.usermodel.Cell h0 = cab.createCell(0);
            h0.setCellValue("Semana (início)");
            h0.setCellStyle(cabecalho);
            org.apache.poi.ss.usermodel.Cell h1 = cab.createCell(1);
            h1.setCellValue("Obras inscritas");
            h1.setCellStyle(cabecalho);

            int rr = 1;
            long soma = 0;
            if (r.obrasPorSemana() != null) {
                for (SeriePontoDTO p : r.obrasPorSemana()) {
                    Row row = semanal.createRow(rr++);
                    row.createCell(0).setCellValue(p.rotulo());
                    row.createCell(1).setCellValue(p.total());
                    soma += p.total();
                }
            }

            Row totalRow = semanal.createRow(rr);
            org.apache.poi.ss.usermodel.Cell t0 = totalRow.createCell(0);
            t0.setCellValue("Total");
            t0.setCellStyle(rotulo);
            org.apache.poi.ss.usermodel.Cell t1 = totalRow.createCell(1);
            t1.setCellValue(soma);
            t1.setCellStyle(rotulo);

            semanal.createFreezePane(0, 1);

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao gerar Excel de métricas: " + e.getMessage(), e);
        }
    }

    private int secao(Sheet sheet, int linha, String texto, CellStyle cabecalho) {
        Row row = sheet.createRow(linha);
        org.apache.poi.ss.usermodel.Cell c0 = row.createCell(0);
        c0.setCellValue(texto);
        c0.setCellStyle(cabecalho);
        org.apache.poi.ss.usermodel.Cell c1 = row.createCell(1);
        c1.setCellValue("");
        c1.setCellStyle(cabecalho);
        return linha + 1;
    }

    private int kv(Sheet sheet, int linha, String chave, String valor) {
        Row row = sheet.createRow(linha);
        row.createCell(0).setCellValue(chave);
        row.createCell(1).setCellValue(valor);
        return linha + 1;
    }

    private CellStyle criarEstiloCabecalho(XSSFWorkbook workbook) {
        XSSFCellStyle estilo = workbook.createCellStyle();
        estilo.setFillForegroundColor(new XSSFColor(VINHO_RGB, null));
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.LEFT);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont fonte = workbook.createFont();
        fonte.setBold(true);
        fonte.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fonte);

        return estilo;
    }

    private CellStyle criarEstiloRotulo(XSSFWorkbook workbook) {
        XSSFCellStyle estilo = workbook.createCellStyle();
        XSSFFont fonte = workbook.createFont();
        fonte.setBold(true);
        estilo.setFont(fonte);
        return estilo;
    }

    // =========================
    // PDF (iText7)
    // =========================

    public byte[] gerarPdf(RelatorioMetricasDTO r) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(36, 36, 36, 36);

            // Cabeçalho
            doc.add(new Paragraph("Copa das Autoras")
                    .setBold()
                    .setFontSize(20)
                    .setFontColor(VINHO)
                    .setMarginBottom(0));

            doc.add(new Paragraph("Relatório de inscrições")
                    .setFontSize(12)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginTop(0)
                    .setMarginBottom(2));

            String periodo = r.periodoDefinido()
                    ? ("Período: " + r.periodoInicio() + " a " + r.periodoFim())
                    : "Período: desde o início até hoje";

            doc.add(new Paragraph(periodo + "  ·  gerado em " + r.geradoEm())
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(8));

            // Números grandes
            Table cartoes = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth();
            cartoes.addCell(cartaoNumero("Autoras inscritas", r.totalAutoras()));
            cartoes.addCell(cartaoNumero("Obras no período", r.obrasNoPeriodo()));
            doc.add(cartoes);

            // Autoras por status
            tabelaStatus(doc, "Autoras por status", new String[][]{
                    {"Aprovadas", String.valueOf(r.autorasAprovadas())},
                    {"Pendentes", String.valueOf(r.autorasPendentes())},
                    {"Suspensas", String.valueOf(r.autorasSuspensas())},
                    {"Excluídas", String.valueOf(r.autorasExcluidas())},
                    {"Total", String.valueOf(r.totalAutoras())},
            });

            // Obras por status
            tabelaStatus(doc, "Obras por status atual (inscritas no período)",
                    new String[][]{
                            {"Em curadoria (submetidas)", String.valueOf(r.obrasEmCuradoria())},
                            {"Não selecionadas", String.valueOf(r.obrasNaoSelecionadas())},
                            {"Em competição", String.valueOf(r.obrasEmCompeticao())},
                            {"Classificadas", String.valueOf(r.obrasClassificadas())},
                            {"Eliminadas", String.valueOf(r.obrasEliminadas())},
                            {"Campeã", String.valueOf(r.obrasCampea())},
                            {"Total no período", String.valueOf(r.obrasNoPeriodo())},
                    });

            // Gráfico: obras por semana
            grafico(doc, r.obrasPorSemana());

            // Rodapé
            doc.add(new Paragraph(NOTA_RODAPE)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(16));

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao gerar PDF de métricas: " + e.getMessage(), e);
        }
    }

    private Cell cartaoNumero(String rotulo, long valor) {
        Cell c = new Cell()
                .setPadding(14)
                .setBorder(new SolidBorder(BORDA, 1));
        c.add(new Paragraph(String.valueOf(valor))
                .setFontSize(30)
                .setBold()
                .setFontColor(VINHO)
                .setMarginBottom(0));
        c.add(new Paragraph(rotulo)
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(0));
        return c;
    }

    private void tabelaStatus(Document doc, String titulo, String[][] linhas) {
        doc.add(new Paragraph(titulo)
                .setBold()
                .setFontSize(12)
                .setFontColor(VINHO)
                .setMarginTop(16)
                .setMarginBottom(6));

        Table t = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .useAllAvailableWidth();

        for (String[] l : linhas) {
            t.addCell(new Cell()
                    .add(new Paragraph(l[0]).setFontSize(9))
                    .setPadding(4));
            t.addCell(new Cell()
                    .add(new Paragraph(l[1]).setFontSize(9).setBold())
                    .setPadding(4)
                    .setTextAlignment(TextAlignment.RIGHT));
        }
        doc.add(t);
    }

    private void grafico(Document doc, List<SeriePontoDTO> serie) {

        doc.add(new Paragraph("Obras inscritas por semana")
                .setBold()
                .setFontSize(12)
                .setFontColor(VINHO)
                .setMarginTop(16)
                .setMarginBottom(6));

        if (serie == null || serie.isEmpty()) {
            doc.add(new Paragraph("Sem inscrições no período selecionado.")
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY));
            return;
        }

        long max = 1;
        for (SeriePontoDTO p : serie) {
            if (p.total() > max) {
                max = p.total();
            }
        }

        Table chart = new Table(UnitValue.createPercentArray(new float[]{16, 74, 10}))
                .useAllAvailableWidth();

        for (SeriePontoDTO p : serie) {
            chart.addCell(new Cell()
                    .add(new Paragraph(p.rotulo()).setFontSize(8))
                    .setBorder(Border.NO_BORDER));
            chart.addCell(barra(p.total(), max));
            chart.addCell(new Cell()
                    .add(new Paragraph(String.valueOf(p.total()))
                            .setFontSize(8).setBold())
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT));
        }
        doc.add(chart);
    }

    private Cell barra(long total, long max) {
        Cell container = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(2);

        if (total <= 0) {
            container.add(new Paragraph("\u00A0").setFontSize(8));
            return container;
        }

        float pct = (float) total / (float) max * 100f;
        if (pct < 4f) {
            pct = 4f;
        }
        if (pct > 100f) {
            pct = 100f;
        }

        Table barraTabela = new Table(new float[]{1});
        barraTabela.setWidth(UnitValue.createPercentValue(pct));
        barraTabela.addCell(new Cell()
                .setBackgroundColor(VINHO)
                .setHeight(12)
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph("\u00A0").setFontSize(6)));

        container.add(barraTabela);
        return container;
    }
}
