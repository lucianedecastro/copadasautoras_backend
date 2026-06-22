package br.com.copadasautoras.dto;

import java.util.List;

public record ChaveamentoPublicoResponseDTO(

        boolean publicado,

        List<GrupoPublicoDTO> fase32,
        List<ConfrontoPublicoDTO> oitavas,
        List<ConfrontoPublicoDTO> quartas,
        List<ConfrontoPublicoDTO> semifinal,

        // Antes da campeã revelada: as 2 obras ainda em disputa.
        // Depois: as mesmas 2, já com uma marcada como vencedora (campea != null).
        List<ObraPublicaDTO> finalistas,

        // null até o admin revelar a campeã
        CampeaPublicaDTO campea

) {}