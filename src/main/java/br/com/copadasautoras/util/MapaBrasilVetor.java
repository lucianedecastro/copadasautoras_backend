package br.com.copadasautoras.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Geometria do mapa do Brasil (27 UFs) e as regras de cor do mapa coroplético.
 *
 * Classe pura (sem iText): carrega os contornos de /mapa-brasil.txt, já em
 * coordenadas absolutas (formato "UF;cx;cy;largura;altura;M x y L x y ... Z"),
 * e concentra as regras de faixas e cores para que o PDF fique igual ao mapa
 * da tela.
 *
 * Contornos: pacote @svg-maps/brazil (CC BY 4.0) — a atribuição consta no
 * rodapé do PDF.
 */
public final class MapaBrasilVetor {

    private MapaBrasilVetor() {
    }

    /** Área de desenho original do mapa (viewBox 0 0 613 639). */
    public static final double LARGURA = 613;
    public static final double ALTURA = 639;

    /** Rampa de um só matiz (vinho da Copa), claro → escuro: mais autoras = mais escuro. */
    public static final int[] RAMPA = {
            0xF0C9CF, 0xDDA0AB, 0xC47483, 0xA0465A, 0x7A1F35
    };
    public static final int COR_ZERO = 0xE9E4DE;
    public static final int COR_HACHURA = 0xC9C2B9;
    public static final int COR_ROTULO_ESCURO = 0x161616;
    public static final int COR_ROTULO_CLARO = 0xFFFFFF;
    public static final int COR_ROTULO_ZERO = 0x6F6A64;

    /** Um estado: contornos (anéis) e caixa envolvente. */
    public record Estado(
            String sigla,
            double cx,
            double cy,
            double largura,
            double altura,
            List<double[][]> aneis
    ) {
        /** Só rotula onde cabe (mesma regra do mapa da tela). */
        public boolean rotuloCabe() {
            return largura >= 16 && altura >= 12;
        }
    }

    private static final class Holder {
        static final List<Estado> ESTADOS = carregar();
    }

    public static List<Estado> estados() {
        return Holder.ESTADOS;
    }

    private static List<Estado> carregar() {

        try (InputStream in = MapaBrasilVetor.class
                .getResourceAsStream("/mapa-brasil.txt")) {

            if (in == null) {
                throw new IllegalStateException(
                        "Recurso /mapa-brasil.txt não encontrado.");
            }

            List<Estado> estados = new ArrayList<>();

            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {

                String linha;
                while ((linha = r.readLine()) != null) {
                    linha = linha.strip();
                    if (!linha.isEmpty()) {
                        estados.add(lerLinha(linha));
                    }
                }
            }

            return List.copyOf(estados);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Estado lerLinha(String linha) {

        String[] p = linha.split(";", 6);

        List<double[][]> aneis = new ArrayList<>();
        List<double[]> atual = null;

        String[] t = p[5].trim().split("\\s+");

        for (int i = 0; i < t.length; ) {
            switch (t[i]) {
                case "M" -> {
                    if (atual != null && !atual.isEmpty()) {
                        aneis.add(atual.toArray(new double[0][]));
                    }
                    atual = new ArrayList<>();
                    atual.add(new double[]{num(t[i + 1]), num(t[i + 2])});
                    i += 3;
                }
                case "L" -> {
                    atual.add(new double[]{num(t[i + 1]), num(t[i + 2])});
                    i += 3;
                }
                case "Z" -> {
                    if (atual != null && !atual.isEmpty()) {
                        aneis.add(atual.toArray(new double[0][]));
                    }
                    atual = null;
                    i++;
                }
                default -> throw new IllegalStateException(
                        "Comando de mapa inválido: " + t[i]);
            }
        }

        if (atual != null && !atual.isEmpty()) {
            aneis.add(atual.toArray(new double[0][]));
        }

        return new Estado(
                p[0], num(p[1]), num(p[2]), num(p[3]), num(p[4]),
                List.copyOf(aneis));
    }

    private static double num(String s) {
        return Double.parseDouble(s);
    }

    // =========================
    // FAIXAS E CORES
    // =========================

    /**
     * Limites superiores (inclusivos) das faixas, com escala em raiz
     * quadrada para o estado líder não "apagar" os demais. Até 5 faixas,
     * sem repetir limites. Sem dados (max ≤ 0) devolve vazio.
     */
    public static long[] calcularFaixas(long max) {

        if (max <= 0) {
            return new long[0];
        }

        List<Long> sup = new ArrayList<>();

        for (int k = 1; k <= 5; k++) {
            double f = k / 5.0;
            long v = (long) Math.ceil(max * f * f);
            if (sup.isEmpty() || v > sup.get(sup.size() - 1)) {
                sup.add(v);
            }
        }

        sup.set(sup.size() - 1, max);

        return sup.stream().mapToLong(Long::longValue).toArray();
    }

    /** Índice (0-based) da faixa do valor; -1 para zero. */
    public static int faixaDoValor(long valor, long[] faixas) {

        if (valor <= 0 || faixas.length == 0) {
            return -1;
        }

        for (int i = 0; i < faixas.length; i++) {
            if (valor <= faixas[i]) {
                return i;
            }
        }

        return faixas.length - 1;
    }

    /** Cor RGB da faixa i entre n, espalhada sobre a rampa inteira. */
    public static int corDaFaixa(int i, int n) {

        int idx = (n == 1)
                ? RAMPA.length - 1
                : (int) Math.round(i * (RAMPA.length - 1) / (double) (n - 1));

        return RAMPA[idx];
    }

    /** Cor de preenchimento do estado (COR_ZERO se não tem autoras). */
    public static int corDoEstado(long valor, long[] faixas) {

        int f = faixaDoValor(valor, faixas);

        return f < 0 ? COR_ZERO : corDaFaixa(f, faixas.length);
    }

    /** Rótulo escuro ou claro, o que tiver mais contraste com o fundo. */
    public static int corDoRotulo(int fundo) {

        double lf = luminancia(fundo);

        double cEscuro = contraste(lf, luminancia(COR_ROTULO_ESCURO));
        double cClaro = contraste(lf, luminancia(COR_ROTULO_CLARO));

        return cEscuro >= cClaro ? COR_ROTULO_ESCURO : COR_ROTULO_CLARO;
    }

    static double luminancia(int rgb) {
        return 0.2126 * canal((rgb >> 16) & 0xFF)
                + 0.7152 * canal((rgb >> 8) & 0xFF)
                + 0.0722 * canal(rgb & 0xFF);
    }

    private static double canal(int c) {
        double s = c / 255.0;
        return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
    }

    static double contraste(double l1, double l2) {
        double a = Math.max(l1, l2);
        double b = Math.min(l1, l2);
        return (a + 0.05) / (b + 0.05);
    }

    /** Texto da faixa para a legenda ("1", "2–5", "6–12"...). */
    public static String rotuloDaFaixa(int i, long[] faixas) {

        long ini = (i == 0) ? 1 : faixas[i - 1] + 1;
        long fim = faixas[i];

        return ini == fim
                ? String.valueOf(ini)
                : ini + "–" + fim;
    }

    /** "#RRGGBB" — útil para conferência/teste. */
    public static String hex(int rgb) {
        return String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
    }
}
