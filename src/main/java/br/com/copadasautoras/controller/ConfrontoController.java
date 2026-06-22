package br.com.copadasautoras.controller;

import br.com.copadasautoras.entity.GrupoCompeticao;
import br.com.copadasautoras.service.ConfrontoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/confrontos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ConfrontoController {

    private final ConfrontoService service;

    // =========================
    // 🎲 SORTEAR JURADAS DA FINAL
    // =========================
    @Operation(
            summary = "Sortear juradas da final",
            description = """
                    Sorteia 3 juradas dentre as cadastradas para avaliar
                    as 2 obras finalistas. Requer que a FINAL já possua
                    exatamente 2 obras classificadas.

                    Apenas ADMIN.
                    """
    )
    @PostMapping("/final/sortear-juradas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GrupoCompeticao>> sortearJuradasFinal() {

        return ResponseEntity.ok(
                service.sortearJuradasFinal()
        );
    }
}