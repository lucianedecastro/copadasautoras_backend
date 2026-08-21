package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.TipoMidia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Mídia por EMBED informada junto do cadastro do lance.
 *
 * São só URLs de veículos (rádio, TV, YouTube, Spotify) —
 * por isso viajam no JSON. Uploads de arquivos próprios
 * entram por endpoint multipart separado.
 */
public record LanceEmbedMidiaDTO(

        @NotNull(message = "Tipo da mídia é obrigatório")
        TipoMidia tipo,

        @NotBlank(message = "URL do embed é obrigatória")
        String url,

        String legenda,

        Integer ordem

) {}
