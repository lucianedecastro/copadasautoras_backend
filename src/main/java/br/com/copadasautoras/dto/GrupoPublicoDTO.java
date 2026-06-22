package br.com.copadasautoras.dto;

import java.util.List;

public record GrupoPublicoDTO(

        String nomeGrupo,
        List<ObraPublicaDTO> obras

) {}
