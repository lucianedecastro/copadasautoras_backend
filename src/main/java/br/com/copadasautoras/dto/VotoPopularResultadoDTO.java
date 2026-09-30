package br.com.copadasautoras.dto;

import java.util.List;

/**
 * Placar do "Escolha do Público" para o painel admin.
 * Só números agregados: nenhum e-mail de eleitor é exposto.
 *
 * Participam apenas semifinalistas com autorização e trecho.
 * semifinalistasForaDaVotacao conta as elegíveis que não autorizaram.
 */
public record VotoPopularResultadoDTO(
        int totalObras,
        long totalVotosConfirmados,
        long totalVotosPendentes,
        String geradoEm,
        boolean votacaoEncerrada,
        String encerradaEm,
        int semifinalistasForaDaVotacao,
        List<Obra> obras
) {

    public record Obra(
            Long id,
            String titulo,
            String categoria,
            String nomeAutora,
            boolean trechoLiberado,
            long votosConfirmados,
            long votosPendentes,
            double percentual,
            boolean selo,
            boolean empatadaNoTopo
    ) {}
}
