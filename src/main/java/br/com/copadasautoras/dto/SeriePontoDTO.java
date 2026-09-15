package br.com.copadasautoras.dto;

/**
 * Um ponto da série "obras inscritas por semana".
 *
 * A semana é identificada pela sua segunda-feira (padrão ISO).
 * - {@code rotulo}: rótulo curto para o eixo do gráfico (dd/MM).
 * - {@code chaveOrdenacao}: data ISO (yyyy-MM-dd) da segunda-feira,
 *   já ordenável lexicograficamente.
 * - {@code total}: quantidade de obras inscritas na semana.
 *
 * Nenhum dado identificável — apenas contagem.
 */
public record SeriePontoDTO(
        String rotulo,
        String chaveOrdenacao,
        long total
) {}
