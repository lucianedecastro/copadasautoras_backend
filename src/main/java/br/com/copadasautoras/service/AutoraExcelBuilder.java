package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.Autora;
import br.com.copadasautoras.entity.StatusAutora;
import br.com.copadasautoras.util.UfsBrasil;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Excel de autoras (Apache POI, .xlsx), sem mapa.
 *
 * Abas: Resumo, Autoras (lista completa — uso interno, com e-mail e nome
 * civil), Por estado, Por região e Por cidade (todas as cidades, sem o piso
 * de 3 autoras do PDF, por ser de uso administrativo).
 */
final class AutoraExcelBuilder {

    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Vinho da Copa (#7A1F35) para os cabeçalhos.
    private static final byte[] VINHO_RGB =
            new byte[]{(byte) 0x7A, (byte) 0x1F, (byte) 0x35};

    /** Valor percentual (fração 0..1) para a célula sair como 12,3%. */
    private record Pct(double fracao) {
    }

    private AutoraExcelBuilder() {
    }

    static byte[] gerar(List<Autora> autoras, StatusAutora filtro) {

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            AutoraResumoLocalizacao r = new AutoraResumoLocalizacao(autoras);

            CellStyle cabecalho = estiloCabecalho(wb);
            CellStyle negrito = estiloNegrito(wb);
            CellStyle pct = estiloPercentual(wb);

            resumo(wb, r, filtro, cabecalho, negrito, pct);
            lista(wb, autoras, cabecalho);
            porEstado(wb, r, cabecalho, negrito, pct);
            porRegiao(wb, r, cabecalho, pct);
            porCidade(wb, r, cabecalho, pct);

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao gerar Excel de autoras: " + e.getMessage(), e);
        }
    }

    // =========================
    // ABAS
    // =========================

    private static void resumo(
            XSSFWorkbook wb, AutoraResumoLocalizacao r, StatusAutora filtro,
            CellStyle cabecalho, CellStyle negrito, CellStyle pct
    ) {

        Sheet s = wb.createSheet("Resumo");
        s.setColumnWidth(0, 34 * 256);
        s.setColumnWidth(1, 26 * 256);

        Row h = s.createRow(0);
        celula(h, 0, "Indicador", cabecalho, null);
        celula(h, 1, "Valor", cabecalho, null);

        Object[][] linhas = {
                {"Recorte", AutoraPdfBuilder.rotuloRecorte(filtro)},
                {"Gerado em", LocalDate.now().format(DATA_BR)},
                {"Autoras no recorte", r.total},
                {"Com localização informada", r.comLocalizacao},
                {"Sem localização informada", r.semLocalizacao()},
                {"Cobertura do dado", new Pct(r.cobertura())},
        };

        int i = 1;
        for (Object[] l : linhas) {
            Row row = s.createRow(i++);
            celula(row, 0, l[0], negrito, pct);
            celula(row, 1, l[1], null, pct);
        }
    }

    private static void lista(
            XSSFWorkbook wb, List<Autora> autoras, CellStyle cabecalho
    ) {

        Sheet s = wb.createSheet("Autoras");

        String[] colunas = {"Nome de exibição", "Nome completo", "E-mail",
                "Status", "Estado", "Cidade", "Cadastro"};
        int[] larguras = {32, 32, 34, 14, 10, 28, 14};

        cabecalhoDaAba(s, colunas, larguras, cabecalho);

        Collator ordem = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        ordem.setStrength(Collator.PRIMARY);

        List<Autora> ordenadas = new ArrayList<>(autoras);
        ordenadas.sort((a, b) -> ordem.compare(
                nz(a.getNomeExibicao()), nz(b.getNomeExibicao())));

        int i = 1;
        for (Autora a : ordenadas) {
            Row row = s.createRow(i++);
            row.createCell(0).setCellValue(nz(a.getNomeExibicao()));
            row.createCell(1).setCellValue(nz(a.getNome()));
            row.createCell(2).setCellValue(
                    a.getUsuario() != null ? nz(a.getUsuario().getEmail()) : "");
            row.createCell(3).setCellValue(
                    AutoraPdfBuilder.rotuloStatus(a.getStatusAutora()));
            row.createCell(4).setCellValue(nz(a.getEstado()));
            row.createCell(5).setCellValue(nz(a.getCidade()));
            row.createCell(6).setCellValue(a.getDataCadastro() != null
                    ? a.getDataCadastro().toLocalDate().format(DATA_BR) : "");
        }

        s.createFreezePane(0, 1);
        s.setAutoFilter(new CellRangeAddress(0, Math.max(0, i - 1), 0,
                colunas.length - 1));
    }

    private static void porEstado(
            XSSFWorkbook wb, AutoraResumoLocalizacao r,
            CellStyle cabecalho, CellStyle negrito, CellStyle pct
    ) {

        Sheet s = wb.createSheet("Por estado");

        cabecalhoDaAba(s,
                new String[]{"UF", "Estado", "Região", "Autoras", "% (sobre as com localização)"},
                new int[]{8, 26, 18, 12, 30}, cabecalho);

        // Todas as 27 UFs (as sem autoras aparecem com 0), das mais numerosas.
        List<String> ufs = new ArrayList<>(UfsBrasil.siglas());
        ufs.sort((a, b) -> {
            int c = Long.compare(
                    r.porUf.getOrDefault(b, 0L), r.porUf.getOrDefault(a, 0L));
            return c != 0 ? c
                    : UfsBrasil.nome(a).compareToIgnoreCase(UfsBrasil.nome(b));
        });

        int i = 1;
        for (String uf : ufs) {
            long n = r.porUf.getOrDefault(uf, 0L);
            Row row = s.createRow(i++);
            celula(row, 0, uf, null, pct);
            celula(row, 1, UfsBrasil.nome(uf), null, pct);
            celula(row, 2, UfsBrasil.regiao(uf), null, pct);
            celula(row, 3, n, null, pct);
            celula(row, 4, new Pct(r.fracao(n)), null, pct);
        }

        Row sem = s.createRow(i++);
        celula(sem, 1, "Sem localização informada", null, pct);
        celula(sem, 3, r.semLocalizacao(), null, pct);

        Row total = s.createRow(i);
        celula(total, 1, "Total", negrito, pct);
        celula(total, 3, r.total, negrito, pct);

        s.createFreezePane(0, 1);
    }

    private static void porRegiao(
            XSSFWorkbook wb, AutoraResumoLocalizacao r,
            CellStyle cabecalho, CellStyle pct
    ) {

        Sheet s = wb.createSheet("Por região");

        cabecalhoDaAba(s,
                new String[]{"Região", "Autoras", "% (sobre as com localização)"},
                new int[]{18, 12, 30}, cabecalho);

        int i = 1;
        for (String regiao : UfsBrasil.REGIOES) {
            long n = r.porRegiao.getOrDefault(regiao, 0L);
            Row row = s.createRow(i++);
            celula(row, 0, regiao, null, pct);
            celula(row, 1, n, null, pct);
            celula(row, 2, new Pct(r.fracao(n)), null, pct);
        }

        s.createFreezePane(0, 1);
    }

    private static void porCidade(
            XSSFWorkbook wb, AutoraResumoLocalizacao r,
            CellStyle cabecalho, CellStyle pct
    ) {

        Sheet s = wb.createSheet("Por cidade");

        cabecalhoDaAba(s,
                new String[]{"Cidade", "UF", "Autoras", "% (sobre as com localização)"},
                new int[]{34, 8, 12, 30}, cabecalho);

        int i = 1;
        for (AutoraResumoLocalizacao.Cidade c : r.cidades) {
            Row row = s.createRow(i++);
            celula(row, 0, c.nome(), null, pct);
            celula(row, 1, c.uf(), null, pct);
            celula(row, 2, c.autoras(), null, pct);
            celula(row, 3, new Pct(r.fracao(c.autoras())), null, pct);
        }

        s.createFreezePane(0, 1);
    }

    // =========================
    // HELPERS
    // =========================

    private static void cabecalhoDaAba(
            Sheet s, String[] colunas, int[] larguras, CellStyle estilo
    ) {

        // Larguras fixas (evita autoSize, que falha em ambiente headless).
        for (int i = 0; i < larguras.length; i++) {
            s.setColumnWidth(i, larguras[i] * 256);
        }

        Row h = s.createRow(0);
        for (int i = 0; i < colunas.length; i++) {
            celula(h, i, colunas[i], estilo, null);
        }
    }

    /** Escreve a célula conforme o tipo do valor (texto, número ou percentual). */
    private static void celula(
            Row row, int col, Object valor, CellStyle estilo, CellStyle estiloPct
    ) {

        Cell c = row.createCell(col);

        if (valor instanceof Pct p) {
            c.setCellValue(p.fracao());
            if (estiloPct != null) {
                c.setCellStyle(estiloPct);
            }
            return;
        }

        if (valor instanceof Number n) {
            c.setCellValue(n.doubleValue());
        } else {
            c.setCellValue(valor != null ? valor.toString() : "");
        }

        if (estilo != null) {
            c.setCellStyle(estilo);
        }
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

    private static CellStyle estiloPercentual(XSSFWorkbook wb) {

        XSSFCellStyle estilo = wb.createCellStyle();
        estilo.setDataFormat(wb.createDataFormat().getFormat("0.0%"));

        return estilo;
    }

    private static String nz(String v) {
        return v != null ? v : "";
    }
}
