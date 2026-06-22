package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.FaseCompeticao;

import java.util.List;

public record FaseDetalheResponseDTO(

        FaseCompeticao fase,
        Integer total,
        List<ConfrontoResponseDTO> confrontos,
        List<SubmissaoResponseDTO> classificadas,
        List<SubmissaoResponseDTO> eliminadas

) {}