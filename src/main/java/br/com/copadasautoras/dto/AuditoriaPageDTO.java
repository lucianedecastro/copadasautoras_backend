package br.com.copadasautoras.dto;

import java.util.List;

/**
 * Uma página do log de auditoria, para o "carregar mais" do painel.
 */
public record AuditoriaPageDTO(

        List<RegistroAuditoriaDTO> itens,
        int pagina,
        int tamanho,
        long total,
        boolean temMais

) {}
