package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.ChaveamentoPublicoResponseDTO;
import br.com.copadasautoras.service.PublicoService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publico")
@RequiredArgsConstructor
public class PublicoController {

    private final PublicoService publicoService;

    // =========================
    // 🏆 CHAVEAMENTO PÚBLICO
    // =========================
    @Operation(
            summary = "Chaveamento público",
            description = """
                    Retorna o chaveamento completo (FASE_32, OITAVAS,
                    QUARTAS, SEMIFINAL, finalistas e campeã, quando
                    revelada). Sem autenticação.

                    Se a competição ainda não tiver publicado o
                    chaveamento (Competicao.chaveamentoPublicado = false),
                    retorna { publicado: false } com listas vazias —
                    a página pública deve tratar esse caso mostrando
                    um "em breve".
                    """
    )
    @GetMapping("/chaveamento")
    public ResponseEntity<ChaveamentoPublicoResponseDTO> chaveamento() {

        return ResponseEntity.ok(
                publicoService.obterChaveamento()
        );
    }
}