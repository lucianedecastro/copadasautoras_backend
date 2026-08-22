package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.OrigemMidia;
import br.com.copadasautoras.entity.TipoMidia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Registra no lance uma mídia que já existe em algum lugar:
 *  - UPLOAD → URL do Cloudinary devolvida pelo upload direto do navegador;
 *  - EMBED  → URL do veículo (rádio/TV/YouTube).
 *
 * É o mesmo endpoint para os dois casos — o que muda é a origem. Isso
 * também permite adicionar embed a um lance já criado (algo que o upload
 * multipart original não cobria).
 */
public record MidiaRegistroDTO(

        @NotNull(message = "Tipo da mídia é obrigatório")
        TipoMidia tipo,

        @NotNull(message = "Origem da mídia é obrigatória")
        OrigemMidia origem,

        @NotBlank(message = "URL da mídia é obrigatória")
        String url,

        String legenda,

        Integer ordem

) {}
