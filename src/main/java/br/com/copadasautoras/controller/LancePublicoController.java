package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.LancePublicoDTO;
import br.com.copadasautoras.entity.CategoriaLance;
import br.com.copadasautoras.service.LanceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Timeline pública "Lance a Lance".
 *
 * Sob /publico/** (permitAll no SecurityConfig). Devolve
 * só lances visíveis e só campos de exibição — nada de
 * rascunho, agendado fora de hora ou campo interno.
 */
@RestController
@RequestMapping("/publico/lances")
@RequiredArgsConstructor
public class LancePublicoController {

    private final LanceService lanceService;

    @Operation(
            summary = "Listar lances públicos",
            description = """
                    Retorna a timeline pública, do mais recente ao mais antigo.
                    Filtros opcionais por categoria e por destaque (golaço).
                    """
    )
    @GetMapping
    public ResponseEntity<List<LancePublicoDTO>> listar(
            @RequestParam(required = false) CategoriaLance categoria,
            @RequestParam(defaultValue = "false") boolean golaco
    ) {
        return ResponseEntity.ok(
                lanceService.listarPublico(categoria, golaco)
        );
    }

    @Operation(
            summary = "Detalhe público de um lance",
            description = """
                    Retorna um lance pelo slug, desde que esteja visível.
                    Usado na página de detalhe (lances com mídia própria).
                    """
    )
    @GetMapping("/{slug}")
    public ResponseEntity<LancePublicoDTO> detalhe(
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(
                lanceService.buscarPublicoPorSlug(slug)
        );
    }
}
