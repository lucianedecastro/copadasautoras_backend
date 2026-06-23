package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.FaseCompeticao;

import java.util.List;

public record FaseResponseDTO(

        FaseCompeticao fase,
        int total,

        List<ConfrontoResponseDTO> confrontos,

        List<SubmissaoResponseDTO> classificadas,
        List<SubmissaoResponseDTO> eliminadas,

        // Só preenchido na FASE_32 (grupos de 4 obras, sem confronto 1x1).
        // Nas demais fases vem como lista vazia.
        List<GrupoPublicoDTO> grupos

) {}