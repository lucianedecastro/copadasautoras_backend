package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.CategoriaLance;
import br.com.copadasautoras.entity.Lance;
import br.com.copadasautoras.entity.StatusLance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Visão completa de um lance para o painel admin —
 * inclui status, agendamento e datas de auditoria.
 */
public record LanceAdminDTO(

        Long id,
        String titulo,
        String resumo,
        CategoriaLance categoria,
        boolean golaco,
        String veiculo,
        String linkExterno,
        StatusLance status,
        LocalDate dataAcontecimento,
        LocalDateTime publicarEm,
        String slug,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        List<LanceMidiaDTO> midias

) {

    public static LanceAdminDTO fromEntity(Lance lance) {
        return new LanceAdminDTO(
                lance.getId(),
                lance.getTitulo(),
                lance.getResumo(),
                lance.getCategoria(),
                lance.isGolaco(),
                lance.getVeiculo(),
                lance.getLinkExterno(),
                lance.getStatus(),
                lance.getDataAcontecimento(),
                lance.getPublicarEm(),
                lance.getSlug(),
                lance.getDataCriacao(),
                lance.getDataAtualizacao(),
                lance.getMidias().stream()
                        .map(LanceMidiaDTO::fromEntity)
                        .toList()
        );
    }
}
