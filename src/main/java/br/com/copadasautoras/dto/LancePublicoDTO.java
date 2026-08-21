package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.CategoriaLance;
import br.com.copadasautoras.entity.Lance;

import java.time.LocalDate;
import java.util.List;

/**
 * Visão pública de um lance — só o que a timeline mostra.
 *
 * De propósito NÃO expõe status, publicarEm, datas de
 * auditoria nem qualquer campo interno. Assim, mesmo quem
 * fuçar a API só enxerga o que já é público. O recorte
 * completo (relatório) nunca sai do admin.
 */
public record LancePublicoDTO(

        Long id,
        String titulo,
        String resumo,
        CategoriaLance categoria,
        boolean golaco,
        String veiculo,
        String linkExterno,
        LocalDate dataAcontecimento,
        String slug,
        List<LanceMidiaDTO> midias

) {

    public static LancePublicoDTO fromEntity(Lance lance) {
        return new LancePublicoDTO(
                lance.getId(),
                lance.getTitulo(),
                lance.getResumo(),
                lance.getCategoria(),
                lance.isGolaco(),
                lance.getVeiculo(),
                lance.getLinkExterno(),
                lance.getDataAcontecimento(),
                lance.getSlug(),
                lance.getMidias().stream()
                        .map(LanceMidiaDTO::fromEntity)
                        .toList()
        );
    }
}
