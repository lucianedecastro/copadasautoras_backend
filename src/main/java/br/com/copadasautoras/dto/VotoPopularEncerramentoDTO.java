package br.com.copadasautoras.dto;

import java.util.List;

/**
 * Resposta do encerramento.
 * - encerrada=true, vencedora preenchida: votação fechada e selo aplicado.
 * - empate=true, encerrada=false: nada mudou; o admin deve escolher uma
 *   das obras em "empatadas" e chamar de novo com o obraId.
 */
public record VotoPopularEncerramentoDTO(
        boolean encerrada,
        boolean empate,
        List<VotoPopularResultadoDTO.Obra> empatadas,
        SeloEscolhaPublicoDTO vencedora
) {}
