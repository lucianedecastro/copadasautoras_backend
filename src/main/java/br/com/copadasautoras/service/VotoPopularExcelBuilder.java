package br.com.copadasautoras.service;

import br.com.copadasautoras.dto.VotoPopularResultadoDTO;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;

/**
 * Excel do placar do "Escolha do Público" (Apache POI, .xlsx).
 * Abas: Resumo e Placar. Sem e-mails de eleitores.
 */
final class VotoPopularExcelBuilder {

    // Vinho da Copa (#7A1F35) para os cabeçalhos.
    private static final byte[] VINHO_RGB =
            new byte[]{(byte) 0x7A, (byte) 0x1F, (byte) 0x35};

    private VotoPopularExcelBuilder() {
    }

    static byte[] gerar(VotoPopularResultadoDTO r) {

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle cabecalho = estiloCabecalho(wb);
            CellStyle negrito = estiloNegrito(wb);

            resumo(wb, r, cabecalho, negrito);
            placar(wb, r, cabecalho);

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao gerar Excel do Escolha do Público: "
                            + e.getMessage(), e);
        }
    }

    private static void resumo(
            XSSFWorkbook wb, VotoPopularResultadoDTO r,
            CellStyle cabecalho, CellStyle negrito
    ) {

        Sheet s = wb.createSheet("Resumo");
        s.setColumnWidth(0, 40 * 256);
        s.setColumnWidth(1, 46 * 256);

        Row h = s.createRow(0);
        texto(h, 0, "Indicador", cabecalho);
        texto(h, 1, "Valor", cabecalho);

        Object[][] linhas = {
                {"Atualizado em", r.geradoEm()},
                {"Votos confirmados", r.totalVotosConfirmados()},
                {"Aguardando confirmação", r.totalVotosPendentes()},
                {"Obras na votação", r.totalObras()},
                {"Semifinalistas fora da votação (sem trecho)",
                        r.semifinalistasForaDaVotacao()},
                {"Situação da votação", r.votacaoEncerrada()
                        ? "Encerrada em " + r.encerradaEm()
                        : "Em andamento"},
                {"Selo Escolha do Público", r.obras().stream()
                        .filter(VotoPopularResultadoDTO.Obra::selo)
                        .map(o -> o.titulo() + " (" + o.nomeAutora() + ")")
                        .findFirst()
                        .orElse("—")},
        };

        int i = 1;
        for (Object[] l : linhas) {
            Row row = s.createRow(i++);
            texto(row, 0, l[0].toString(), negrito);
            Cell c = row.createCell(1);
            if (l[1] instanceof Number n) {
                c.setCellValue(n.doubleValue());
            } else {
                c.setCellValue(l[1].toString());
            }
        }
    }

    private static void placar(
            XSSFWorkbook wb, VotoPopularResultadoDTO r, CellStyle cabecalho
    ) {

        Sheet s = wb.createSheet("Placar");

        String[] colunas = {"Posição", "Obra", "Autora", "Categoria",
                "Trecho liberado", "Votos confirmados",
                "Aguardando confirmação", "% dos votos"};
        int[] larguras = {10, 40, 30, 16, 16, 18, 22, 14};

        // Larguras fixas (evita autoSize, que falha em ambiente headless).
        for (int i = 0; i < larguras.length; i++) {
            s.setColumnWidth(i, larguras[i] * 256);
        }

        Row h = s.createRow(0);
        for (int i = 0; i < colunas.length; i++) {
            texto(h, i, colunas[i], cabecalho);
        }

        // Percentual como fração (0..1) para sair 12,3% no Excel.
        XSSFCellStyle pct = wb.createCellStyle();
        pct.setDataFormat(wb.createDataFormat().getFormat("0.0%"));

        int i = 1;
        for (VotoPopularResultadoDTO.Obra o : r.obras()) {
            Row row = s.createRow(i);
            row.createCell(0).setCellValue(i);
            row.createCell(1).setCellValue(nz(o.titulo()));
            row.createCell(2).setCellValue(nz(o.nomeAutora()));
            row.createCell(3).setCellValue(nz(o.categoria()));
            row.createCell(4).setCellValue(o.trechoLiberado() ? "Sim" : "Não");
            row.createCell(5).setCellValue(o.votosConfirmados());
            row.createCell(6).setCellValue(o.votosPendentes());
            Cell p = row.createCell(7);
            p.setCellValue(o.percentual() / 100.0);
            p.setCellStyle(pct);
            i++;
        }
    }

    private static void texto(Row row, int col, String valor, CellStyle estilo) {
        Cell c = row.createCell(col);
        c.setCellValue(valor);
        c.setCellStyle(estilo);
    }

    private static CellStyle estiloCabecalho(XSSFWorkbook wb) {

        XSSFCellStyle estilo = wb.createCellStyle();
        estilo.setFillForegroundColor(new XSSFColor(VINHO_RGB, null));
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.LEFT);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont fonte = wb.createFont();
        fonte.setBold(true);
        fonte.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fonte);

        return estilo;
    }

    private static CellStyle estiloNegrito(XSSFWorkbook wb) {

        XSSFCellStyle estilo = wb.createCellStyle();
        XSSFFont fonte = wb.createFont();
        fonte.setBold(true);
        estilo.setFont(fonte);

        return estilo;
    }

    private static String nz(String v) {
        return v != null ? v : "";
    }
}
