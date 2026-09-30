package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.Autora;
import br.com.copadasautoras.entity.StatusAutora;
import br.com.copadasautoras.util.MapaBrasilVetor;
import br.com.copadasautoras.util.UfsBrasil;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * PDF de autoras (iText7) na identidade da Copa: vinho #7A1F35, cartões,
 * tabelas limpas — mesmo desenho dos PDFs de Lance a Lance e Métricas.
 *
 * Estrutura: cartões → mapa + regiões → por estado → cidades → (opcional)
 * lista de autoras. As tabelas agregadas nunca trazem e-mail nem nome civil;
 * a lista traz só o nome de exibição.
 */
final class AutoraPdfBuilder {

    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DeviceRgb VINHO = new DeviceRgb(0x7A, 0x1F, 0x35);
    private static final DeviceRgb BORDA = new DeviceRgb(0xDD, 0xD6, 0xCF);
    private static final DeviceRgb CINZA = new DeviceRgb(0x6F, 0x6A, 0x64);

    private static final int MAX_CIDADES = 20;

    private static final String NOTA_RODAPE =
            "Localização informada pelas próprias autoras (estado e cidade), "
          + "usada apenas em estatísticas agregadas. Nas cidades com menos de "
          + AutoraResumoLocalizacao.MIN_CIDADE
          + " autoras, os números aparecem agrupados em \"Outras cidades\" "
          + "para não identificar ninguém. Mapa: @svg-maps/brazil, "
          + "licença CC BY 4.0.";

    private AutoraPdfBuilder() {
    }

    static byte[] gerar(
            List<Autora> autoras,
            StatusAutora filtro,
            boolean incluirLista
    ) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfDocument pdf = new PdfDocument(new PdfWriter(out));
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(28, 28, 28, 28);

            AutoraResumoLocalizacao r = new AutoraResumoLocalizacao(autoras);

            // ---- Título ----
            doc.add(new Paragraph("Copa das Autoras — Autoras")
                    .setBold()
                    .setFontSize(16)
                    .setFontColor(VINHO));

            doc.add(new Paragraph(subtitulo(filtro, incluirLista))
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(12));

            // ---- Cartões ----
            doc.add(cartoes(r));

            // ---- Mapa + regiões ----
            doc.add(secao("Onde estão as autoras"));
            doc.add(blocoMapa(pdf, r));

            // ---- Por estado ----
            doc.add(secao("Por estado"));
            doc.add(tabelaEstados(r));

            // ---- Cidades ----
            doc.add(secao("Cidades com maior presença"));
            doc.add(tabelaCidades(r));

            // ---- Lista ----
            if (incluirLista) {
                doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                doc.add(secao("Lista de autoras").setMarginTop(0));
                doc.add(tabelaLista(autoras));
            }

            doc.add(new Paragraph(NOTA_RODAPE)
                    .setFontSize(8)
                    .setFontColor(CINZA)
                    .setMarginTop(14));

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao gerar PDF de autoras: " + e.getMessage(), e);
        }
    }

    // =========================
    // BLOCOS
    // =========================

    private static Table cartoes(AutoraResumoLocalizacao r) {

        Table t = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth();

        t.addCell(cartao(String.valueOf(r.total), "Autoras no recorte"));
        t.addCell(cartao(String.valueOf(r.comLocalizacao),
                "Com localização informada"));
        t.addCell(cartao(
                r.total == 0 ? "—" : percentual(r.cobertura()),
                "Cobertura do dado"));

        return t;
    }

    private static Cell cartao(String numero, String rotulo) {
        return new Cell()
                .setBorder(new SolidBorder(BORDA, 0.75f))
                .setPadding(9)
                .add(new Paragraph(numero)
                        .setBold()
                        .setFontSize(20)
                        .setFontColor(VINHO)
                        .setMarginBottom(0))
                .add(new Paragraph(rotulo)
                        .setFontSize(8)
                        .setFontColor(CINZA)
                        .setMarginTop(0));
    }

    private static Table blocoMapa(PdfDocument pdf, AutoraResumoLocalizacao r)
            throws java.io.IOException {

        long[] faixas = MapaBrasilVetor.calcularFaixas(r.maximoPorUf());

        PdfFormXObject xobj = MapaBrasilPdf.desenhar(pdf, r.porUf, faixas);

        Table bloco = new Table(UnitValue.createPointArray(
                new float[]{320, 219}))
                .setWidth(539)
                .setKeepTogether(true);

        // Coluna do mapa (+ legenda)
        Cell esq = new Cell().setBorder(Border.NO_BORDER).setPadding(0);
        esq.add(new Image(xobj).scaleToFit(310, 330));
        esq.add(legenda(faixas));
        bloco.addCell(esq);

        // Coluna de números
        Cell dir = new Cell().setBorder(Border.NO_BORDER)
                .setPadding(0).setPaddingLeft(12);

        dir.add(new Paragraph("Por região")
                .setBold().setFontSize(10).setFontColor(VINHO)
                .setMarginBottom(4));
        dir.add(tabelaRegioes(r));

        dir.add(new Paragraph("Maiores concentrações")
                .setBold().setFontSize(10).setFontColor(VINHO)
                .setMarginTop(14).setMarginBottom(4));
        dir.add(tabelaTop(r));

        bloco.addCell(dir);

        return bloco;
    }

    private static Table legenda(long[] faixas) {

        Table t = new Table(UnitValue.createPointArray(new float[]{14, 110}))
                .setMarginTop(6);

        t.addCell(chip(MapaBrasilVetor.COR_ZERO));
        t.addCell(rotuloLegenda("Nenhuma autora"));

        for (int i = 0; i < faixas.length; i++) {
            t.addCell(chip(MapaBrasilVetor.corDaFaixa(i, faixas.length)));
            t.addCell(rotuloLegenda(
                    MapaBrasilVetor.rotuloDaFaixa(i, faixas) + " autoras"));
        }

        return t;
    }

    private static Cell chip(int rgb) {
        return new Cell()
                .setBackgroundColor(new DeviceRgb(
                        (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF))
                .setBorder(new SolidBorder(ColorConstants.WHITE, 1.5f))
                .setHeight(9)
                .setPadding(0);
    }

    private static Cell rotuloLegenda(String texto) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(1)
                .setPaddingLeft(4)
                .add(new Paragraph(texto).setFontSize(8).setFontColor(CINZA));
    }

    private static Table tabelaRegioes(AutoraResumoLocalizacao r) {

        Table t = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25}))
                .useAllAvailableWidth();

        for (String h : new String[]{"Região", "Autoras", "%"}) {
            t.addHeaderCell(cabecalho(h, !h.equals("Região")));
        }

        for (String regiao : UfsBrasil.REGIOES) {
            long n = r.porRegiao.getOrDefault(regiao, 0L);
            t.addCell(celula(regiao, false));
            t.addCell(celula(String.valueOf(n), true));
            t.addCell(celula(percentual(r.fracao(n)), true));
        }

        return t;
    }

    private static Table tabelaTop(AutoraResumoLocalizacao r) {

        Table t = new Table(UnitValue.createPercentArray(new float[]{75, 25}))
                .useAllAvailableWidth();

        List<String> ufs = r.ufsOrdenadas();

        if (ufs.isEmpty()) {
            t.addCell(celula("Sem localização informada", false)
                    .setFontColor(CINZA));
            t.addCell(celula("", true));
            return t;
        }

        for (String uf : ufs.subList(0, Math.min(5, ufs.size()))) {
            t.addCell(celula(UfsBrasil.nome(uf), false));
            t.addCell(celula(String.valueOf(r.porUf.get(uf)), true));
        }

        return t;
    }

    private static Table tabelaEstados(AutoraResumoLocalizacao r) {

        Table t = new Table(UnitValue.createPercentArray(
                new float[]{8, 34, 24, 16, 18}))
                .useAllAvailableWidth();

        t.addHeaderCell(cabecalho("UF", false));
        t.addHeaderCell(cabecalho("Estado", false));
        t.addHeaderCell(cabecalho("Região", false));
        t.addHeaderCell(cabecalho("Autoras", true));
        t.addHeaderCell(cabecalho("%", true));

        for (String uf : r.ufsOrdenadas()) {
            long n = r.porUf.get(uf);
            t.addCell(celula(uf, false));
            t.addCell(celula(UfsBrasil.nome(uf), false));
            t.addCell(celula(UfsBrasil.regiao(uf), false));
            t.addCell(celula(String.valueOf(n), true));
            t.addCell(celula(percentual(r.fracao(n)), true));
        }

        if (r.porUf.isEmpty()) {
            t.addCell(new Cell(1, 5)
                    .add(new Paragraph("Nenhuma autora com localização "
                            + "informada neste recorte.")
                            .setFontSize(9).setFontColor(CINZA))
                    .setBorder(new SolidBorder(BORDA, 0.5f))
                    .setPadding(5));
        }

        long sem = r.semLocalizacao();
        if (sem > 0) {
            t.addCell(celula("", false));
            t.addCell(celula("Sem localização informada", false)
                    .setFontColor(CINZA));
            t.addCell(celula("", false));
            t.addCell(celula(String.valueOf(sem), true).setFontColor(CINZA));
            t.addCell(celula("", true));
        }

        return t;
    }

    private static Table tabelaCidades(AutoraResumoLocalizacao r) {

        Table t = new Table(UnitValue.createPercentArray(
                new float[]{56, 12, 16, 16}))
                .useAllAvailableWidth();

        t.addHeaderCell(cabecalho("Cidade", false));
        t.addHeaderCell(cabecalho("UF", false));
        t.addHeaderCell(cabecalho("Autoras", true));
        t.addHeaderCell(cabecalho("%", true));

        List<AutoraResumoLocalizacao.Cidade> visiveis = r.cidadesVisiveis();
        int mostrar = Math.min(MAX_CIDADES, visiveis.size());

        long agrupadas = r.autorasEmOutrasCidades();

        // O que passou do limite de linhas também entra em "Outras cidades".
        for (int i = mostrar; i < visiveis.size(); i++) {
            agrupadas += visiveis.get(i).autoras();
        }

        if (mostrar == 0 && agrupadas == 0) {
            t.addCell(new Cell(1, 4)
                    .add(new Paragraph("Nenhuma cidade informada "
                            + "neste recorte.")
                            .setFontSize(9).setFontColor(CINZA))
                    .setBorder(new SolidBorder(BORDA, 0.5f))
                    .setPadding(5));
        }

        for (int i = 0; i < mostrar; i++) {
            AutoraResumoLocalizacao.Cidade c = visiveis.get(i);
            t.addCell(celula(c.nome(), false));
            t.addCell(celula(c.uf(), false));
            t.addCell(celula(String.valueOf(c.autoras()), true));
            t.addCell(celula(percentual(r.fracao(c.autoras())), true));
        }

        if (agrupadas > 0) {
            t.addCell(celula("Outras cidades", false).setFontColor(CINZA));
            t.addCell(celula("", false));
            t.addCell(celula(String.valueOf(agrupadas), true)
                    .setFontColor(CINZA));
            t.addCell(celula(percentual(r.fracao(agrupadas)), true)
                    .setFontColor(CINZA));
        }

        return t;
    }

    private static Table tabelaLista(List<Autora> autoras) {

        Table t = new Table(UnitValue.createPercentArray(
                new float[]{34, 14, 7, 29, 16}))
                .useAllAvailableWidth();

        for (String h : new String[]{
                "Nome de exibição", "Status", "UF", "Cidade", "Cadastro"}) {
            t.addHeaderCell(cabecalho(h, false));
        }

        Collator ordem = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        ordem.setStrength(Collator.PRIMARY);

        List<Autora> ordenadas = autoras.stream()
                .sorted((a, b) -> ordem.compare(
                        nz(a.getNomeExibicao()), nz(b.getNomeExibicao())))
                .toList();

        for (Autora a : ordenadas) {
            t.addCell(celula(nz(a.getNomeExibicao()), false));
            t.addCell(celula(rotuloStatus(a.getStatusAutora()), false));
            t.addCell(celula(nz(a.getEstado()), false));
            t.addCell(celula(nz(a.getCidade()), false));
            t.addCell(celula(a.getDataCadastro() != null
                    ? a.getDataCadastro().toLocalDate().format(DATA_BR)
                    : "", false));
        }

        return t;
    }

    // =========================
    // CÉLULAS E TEXTOS
    // =========================

    private static Paragraph secao(String titulo) {
        return new Paragraph(titulo)
                .setBold()
                .setFontSize(12)
                .setFontColor(VINHO)
                .setMarginTop(16)
                .setMarginBottom(6);
    }

    private static Cell cabecalho(String texto, boolean direita) {
        Cell c = new Cell()
                .add(new Paragraph(texto).setBold().setFontSize(9)
                        .setTextAlignment(direita
                                ? TextAlignment.RIGHT : TextAlignment.LEFT))
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(VINHO)
                .setBorder(new SolidBorder(BORDA, 0.5f))
                .setPadding(5);
        return c;
    }

    private static Cell celula(String texto, boolean direita) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(9)
                        .setTextAlignment(direita
                                ? TextAlignment.RIGHT : TextAlignment.LEFT))
                .setBorder(new SolidBorder(BORDA, 0.5f))
                .setPadding(5);
    }

    private static String subtitulo(StatusAutora filtro, boolean incluirLista) {

        return "Recorte: " + rotuloRecorte(filtro)
                + "  ·  " + (incluirLista
                        ? "com lista de autoras"
                        : "somente dados agregados")
                + "  ·  gerado em " + LocalDate.now().format(DATA_BR);
    }

    static String rotuloRecorte(StatusAutora filtro) {
        if (filtro == null) {
            return "todas as autoras";
        }
        return switch (filtro) {
            case PENDENTE -> "autoras pendentes";
            case APROVADA -> "autoras aprovadas";
            case SUSPENSA -> "autoras suspensas";
            case EXCLUIDA -> "autoras excluídas";
        };
    }

    static String rotuloStatus(StatusAutora status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case PENDENTE -> "Pendente";
            case APROVADA -> "Aprovada";
            case SUSPENSA -> "Suspensa";
            case EXCLUIDA -> "Excluída";
        };
    }

    private static String percentual(double fracao) {
        return String.format(Locale.forLanguageTag("pt-BR"),
                "%.1f%%", fracao * 100);
    }

    private static String nz(String v) {
        return v != null ? v : "";
    }
}
