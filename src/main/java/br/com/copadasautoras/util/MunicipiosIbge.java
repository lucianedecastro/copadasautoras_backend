package br.com.copadasautoras.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Lista oficial de municípios por UF (IBGE): 5.570 municípios mais
 * Fernando de Noronha (PE), 5.571 no total.
 *
 * Fonte de verdade da localização das autoras no backend: barra cidade
 * fora do padrão mesmo que a chamada não venha da tela (API direta, tela
 * antiga em cache).
 *
 * Lê /municipios-ibge.txt (src/main/resources), uma linha por município no
 * formato "UF;Nome", em UTF-8. A carga acontece uma vez, na primeira
 * consulta. Se o arquivo faltar no deploy, falha de forma explícita em vez
 * de aceitar qualquer cidade em silêncio.
 */
public final class MunicipiosIbge {

    private static final String RECURSO = "/municipios-ibge.txt";

    private MunicipiosIbge() {
    }

    /**
     * Carga preguiçosa e thread-safe (idiom do holder).
     */
    private static final class Holder {
        static final Set<String> CHAVES = carregar();
    }

    /**
     * Indica se a cidade existe na UF informada. Comparação exata do
     * nome (com acentos), já que a tela envia o nome da própria lista.
     */
    public static boolean existe(String uf, String cidade) {

        if (uf == null || cidade == null) {
            return false;
        }

        return Holder.CHAVES.contains(chave(uf, cidade));
    }

    /**
     * Quantidade de municípios carregados (útil em teste e diagnóstico).
     */
    public static int total() {
        return Holder.CHAVES.size();
    }

    private static String chave(String uf, String cidade) {

        return uf.trim().toUpperCase(Locale.ROOT)
                + "|"
                + Normalizer.normalize(cidade.trim(), Normalizer.Form.NFC);
    }

    private static Set<String> carregar() {

        try (InputStream in = MunicipiosIbge.class.getResourceAsStream(RECURSO)) {

            if (in == null) {
                throw new IllegalStateException(
                        "Arquivo " + RECURSO + " não encontrado no classpath."
                );
            }

            Set<String> chaves = new HashSet<>(8192);

            try (BufferedReader leitor = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {

                String linha;

                while ((linha = leitor.readLine()) != null) {

                    int sep = linha.indexOf(';');

                    if (sep != 2) {
                        continue;
                    }

                    chaves.add(chave(
                            linha.substring(0, sep),
                            linha.substring(sep + 1)
                    ));
                }
            }

            return chaves;

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Falha ao ler " + RECURSO, e
            );
        }
    }
}
