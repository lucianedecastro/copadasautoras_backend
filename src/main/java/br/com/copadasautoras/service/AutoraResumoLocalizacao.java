package br.com.copadasautoras.service;

import br.com.copadasautoras.entity.Autora;
import br.com.copadasautoras.util.UfsBrasil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contagens de localização de um recorte de autoras (por UF, região e cidade).
 * Cálculo em memória: o volume é pequeno e o recorte vem do filtro do painel.
 */
final class AutoraResumoLocalizacao {

    /** Cidades com menos autoras que isto ficam agrupadas ("Outras cidades"). */
    static final int MIN_CIDADE = 3;

    record Cidade(String uf, String nome, long autoras) {
    }

    final long total;
    final long comLocalizacao;
    final Map<String, Long> porUf = new HashMap<>();
    final Map<String, Long> porRegiao = new HashMap<>();
    final List<Cidade> cidades;

    AutoraResumoLocalizacao(List<Autora> autoras) {

        long com = 0;
        Map<String, Long> porCidade = new HashMap<>();

        for (Autora a : autoras) {

            String uf = textoOuNulo(a.getEstado());

            if (uf == null) {
                continue;
            }

            com++;
            porUf.merge(uf, 1L, Long::sum);
            porRegiao.merge(UfsBrasil.regiao(uf), 1L, Long::sum);

            String cidade = textoOuNulo(a.getCidade());
            if (cidade != null) {
                porCidade.merge(uf + "|" + cidade, 1L, Long::sum);
            }
        }

        this.total = autoras.size();
        this.comLocalizacao = com;

        List<Cidade> lista = new ArrayList<>();
        for (Map.Entry<String, Long> e : porCidade.entrySet()) {
            int i = e.getKey().indexOf('|');
            lista.add(new Cidade(
                    e.getKey().substring(0, i),
                    e.getKey().substring(i + 1),
                    e.getValue()));
        }

        lista.sort(Comparator
                .comparingLong(Cidade::autoras).reversed()
                .thenComparing(Cidade::uf)
                .thenComparing(Cidade::nome));

        this.cidades = List.copyOf(lista);
    }

    long semLocalizacao() {
        return total - comLocalizacao;
    }

    long maximoPorUf() {
        return porUf.values().stream().mapToLong(Long::longValue).max().orElse(0);
    }

    /** Fração sobre as autoras COM localização (0 se não há nenhuma). */
    double fracao(long n) {
        return comLocalizacao == 0 ? 0 : (double) n / comLocalizacao;
    }

    /** Cobertura do dado: com localização sobre o total do recorte. */
    double cobertura() {
        return total == 0 ? 0 : (double) comLocalizacao / total;
    }

    /** UFs com pelo menos uma autora, das mais para as menos numerosas. */
    List<String> ufsOrdenadas() {
        return porUf.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(e -> UfsBrasil.nome(e.getKey())))
                .map(Map.Entry::getKey)
                .toList();
    }

    /** Cidades que atingem o mínimo (já ordenadas). */
    List<Cidade> cidadesVisiveis() {
        return cidades.stream().filter(c -> c.autoras() >= MIN_CIDADE).toList();
    }

    /** Autoras em cidades abaixo do mínimo — somam em "Outras cidades". */
    long autorasEmOutrasCidades() {
        return cidades.stream()
                .filter(c -> c.autoras() < MIN_CIDADE)
                .mapToLong(Cidade::autoras)
                .sum();
    }

    static String textoOuNulo(String s) {
        return (s == null || s.isBlank()) ? null : s.strip();
    }
}
