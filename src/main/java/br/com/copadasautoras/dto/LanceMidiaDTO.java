package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.LanceMidia;
import br.com.copadasautoras.entity.OrigemMidia;
import br.com.copadasautoras.entity.TipoMidia;

public record LanceMidiaDTO(

        Long id,
        TipoMidia tipo,
        OrigemMidia origem,
        String url,
        String legenda,
        Integer ordem

) {

    public static LanceMidiaDTO fromEntity(LanceMidia midia) {
        return new LanceMidiaDTO(
                midia.getId(),
                midia.getTipo(),
                midia.getOrigem(),
                midia.getUrl(),
                midia.getLegenda(),
                midia.getOrdem()
        );
    }
}
