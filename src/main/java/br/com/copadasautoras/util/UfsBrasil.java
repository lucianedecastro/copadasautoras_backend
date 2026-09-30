package br.com.copadasautoras.util;

import java.util.List;
import java.util.Map;

/**
 * Nome e região de cada UF — usado nos relatórios (PDF/Excel) de autoras.
 */
public final class UfsBrasil {

    private UfsBrasil() {
    }

    /** Ordem em que as regiões aparecem nos relatórios. */
    public static final List<String> REGIOES =
            List.of("Sudeste", "Nordeste", "Sul", "Centro-Oeste", "Norte");

    private record Uf(String nome, String regiao) {
    }

    private static final Map<String, Uf> UFS = Map.ofEntries(
            Map.entry("AC", new Uf("Acre", "Norte")),
            Map.entry("AL", new Uf("Alagoas", "Nordeste")),
            Map.entry("AP", new Uf("Amapá", "Norte")),
            Map.entry("AM", new Uf("Amazonas", "Norte")),
            Map.entry("BA", new Uf("Bahia", "Nordeste")),
            Map.entry("CE", new Uf("Ceará", "Nordeste")),
            Map.entry("DF", new Uf("Distrito Federal", "Centro-Oeste")),
            Map.entry("ES", new Uf("Espírito Santo", "Sudeste")),
            Map.entry("GO", new Uf("Goiás", "Centro-Oeste")),
            Map.entry("MA", new Uf("Maranhão", "Nordeste")),
            Map.entry("MT", new Uf("Mato Grosso", "Centro-Oeste")),
            Map.entry("MS", new Uf("Mato Grosso do Sul", "Centro-Oeste")),
            Map.entry("MG", new Uf("Minas Gerais", "Sudeste")),
            Map.entry("PA", new Uf("Pará", "Norte")),
            Map.entry("PB", new Uf("Paraíba", "Nordeste")),
            Map.entry("PR", new Uf("Paraná", "Sul")),
            Map.entry("PE", new Uf("Pernambuco", "Nordeste")),
            Map.entry("PI", new Uf("Piauí", "Nordeste")),
            Map.entry("RJ", new Uf("Rio de Janeiro", "Sudeste")),
            Map.entry("RN", new Uf("Rio Grande do Norte", "Nordeste")),
            Map.entry("RS", new Uf("Rio Grande do Sul", "Sul")),
            Map.entry("RO", new Uf("Rondônia", "Norte")),
            Map.entry("RR", new Uf("Roraima", "Norte")),
            Map.entry("SC", new Uf("Santa Catarina", "Sul")),
            Map.entry("SP", new Uf("São Paulo", "Sudeste")),
            Map.entry("SE", new Uf("Sergipe", "Nordeste")),
            Map.entry("TO", new Uf("Tocantins", "Norte"))
    );

    /** As 27 siglas, em ordem alfabética. */
    public static List<String> siglas() {
        return UFS.keySet().stream().sorted().toList();
    }

    public static String nome(String sigla) {
        Uf uf = UFS.get(sigla);
        return uf != null ? uf.nome() : sigla;
    }

    public static String regiao(String sigla) {
        Uf uf = UFS.get(sigla);
        return uf != null ? uf.regiao() : "";
    }
}
