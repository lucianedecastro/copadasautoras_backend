package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.CategoriaLance;
import br.com.copadasautoras.entity.Lance;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Geração dos relatórios do Lance a Lance.
 *
 * Excel via Apache POI (.xlsx) e PDF via iText7, ambos no
 * mesmo recorte que o admin filtrou na tela. É o insumo do
 * relatório de resultados da Copa.
 */
@Service
public class LanceExportService {

    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Vinho da Copa (#7A1F35) para os cabeçalhos.
    private static final byte[] VINHO_RGB =
            new byte[]{(byte) 0x7A, (byte) 0x1F, (byte) 0x35};

    private static final String[] COLUNAS = {
            "Data", "Categoria", "Golaço", "Título",
            "Veículo", "Resumo", "Link", "Status"
    };

    // =========================
    // EXCEL (Apache POI)
    // =========================

    public byte[] gerarExcel(List<Lance> lances) {

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Lance a Lance");

            CellStyle estiloCabecalho = criarEstiloCabecalho(workbook);

            // Larguras fixas (evita autoSize, que depende de
            // métricas de fonte e falha em ambiente headless).
            int[] larguras = {14, 20, 10, 40, 24, 60, 40, 14};
            for (int i = 0; i < larguras.length; i++) {
                sheet.setColumnWidth(i, larguras[i] * 256);
            }

            // Cabeçalho
            Row cabecalho = sheet.createRow(0);
            for (int i = 0; i < COLUNAS.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = cabecalho.createCell(i);
                cell.setCellValue(COLUNAS[i]);
                cell.setCellStyle(estiloCabecalho);
            }

            // Linhas
            int linha = 1;
            for (Lance l : lances) {
                Row row = sheet.createRow(linha++);
                row.createCell(0).setCellValue(formatarData(l.getDataAcontecimento()));
                row.createCell(1).setCellValue(rotuloCategoria(l.getCategoria()));
                row.createCell(2).setCellValue(l.isGolaco() ? "Sim" : "—");
                row.createCell(3).setCellValue(nz(l.getTitulo()));
                row.createCell(4).setCellValue(nz(l.getVeiculo()));
                row.createCell(5).setCellValue(nz(l.getResumo()));
                row.createCell(6).setCellValue(nz(l.getLinkExterno()));
                row.createCell(7).setCellValue(
                        l.getStatus() != null ? l.getStatus().name() : "");
            }

            sheet.createFreezePane(0, 1);

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao gerar Excel do Lance a Lance: " + e.getMessage(), e);
        }
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

    // =========================
    // PDF (iText7)
    // =========================

    public byte[] gerarPdf(
            List<Lance> lances,
            CategoriaLance categoria,
            boolean apenasGolaco
    ) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4.rotate());
            doc.setMargins(28, 28, 28, 28);

            Color vinho = new DeviceRgb(0x7A, 0x1F, 0x35);

            // Título do relatório
            doc.add(new Paragraph("Copa das Autoras — Lance a Lance")
                    .setBold()
                    .setFontSize(16)
                    .setFontColor(vinho));

            doc.add(new Paragraph(subtitulo(categoria, apenasGolaco))
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(12));

            // Tabela: Data, Categoria, Golaço, Título, Veículo, Status
            Table table = new Table(UnitValue.createPercentArray(
                    new float[]{10, 16, 8, 34, 20, 12}))
                    .useAllAvailableWidth();

            for (String titulo : new String[]{
                    "Data", "Categoria", "Golaço", "Título", "Veículo", "Status"}) {
                table.addHeaderCell(celulaCabecalho(titulo, vinho));
            }

            for (Lance l : lances) {
                table.addCell(celula(formatarData(l.getDataAcontecimento())));
                table.addCell(celula(rotuloCategoria(l.getCategoria())));
                table.addCell(celula(l.isGolaco() ? "Golaço" : "—"));
                table.addCell(celula(nz(l.getTitulo())));
                table.addCell(celula(nz(l.getVeiculo())));
                table.addCell(celula(l.getStatus() != null ? l.getStatus().name() : ""));
            }

            doc.add(table);

            doc.add(new Paragraph("Total de lances: " + lances.size())
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(10));

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao gerar PDF do Lance a Lance: " + e.getMessage(), e);
        }
    }

    private Cell celulaCabecalho(String texto, Color vinho) {
        return new Cell()
                .add(new Paragraph(texto).setBold().setFontSize(9))
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(vinho)
                .setPadding(5);
    }

    private Cell celula(String texto) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(9))
                .setPadding(5);
    }

    // =========================
    // HELPERS
    // =========================

    private String subtitulo(CategoriaLance categoria, boolean apenasGolaco) {

        StringBuilder sb = new StringBuilder("Recorte: ");

        if (apenasGolaco) {
            sb.append("Golaços");
            if (categoria != null) {
                sb.append(" · ").append(rotuloCategoria(categoria));
            }
        } else if (categoria != null) {
            sb.append(rotuloCategoria(categoria));
        } else {
            sb.append("todos os lances");
        }

        sb.append("  ·  gerado em ")
                .append(LocalDate.now().format(DATA_BR));

        return sb.toString();
    }

    private String rotuloCategoria(CategoriaLance categoria) {
        if (categoria == null) {
            return "";
        }
        return switch (categoria) {
            case CLIPPING -> "Clipping";
            case APOIO_PATROCINIO -> "Apoio & Patrocínio";
            case EMBAIXADORA -> "Embaixadora";
            case TEMA -> "Tema";
        };
    }

    private String formatarData(LocalDate data) {
        return data != null ? data.format(DATA_BR) : "";
    }

    private String nz(String valor) {
        return valor != null ? valor : "";
    }
}
