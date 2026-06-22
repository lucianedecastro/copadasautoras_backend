package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.FaseDetalheResponseDTO;
import br.com.copadasautoras.entity.Competicao;
import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.service.CompeticaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/competicao")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CompeticaoController {

    private final CompeticaoService service;

    // =========================
    // 📊 CONSULTAR COMPETIÇÃO
    // =========================
    @Operation(
            summary = "Consultar competição",
            description = "Retorna o estado atual da competição. Requer autenticação."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Competicao> obter() {

        return ResponseEntity.ok(
                service.obter()
        );
    }

    // =========================
    // ▶️ INICIAR COMPETIÇÃO
    // =========================
    @Operation(
            summary = "Iniciar competição",
            description = "Inicia a competição. Requer perfil ADMIN."
    )
    @PostMapping("/iniciar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Competicao> iniciar() {

        return ResponseEntity.ok(
                service.iniciar()
        );
    }

    // =========================
    // ⏹️ ENCERRAR FASE
    // =========================
    @Operation(
            summary = "Encerrar fase",
            description = "Encerra a fase atual da competição. Requer perfil ADMIN."
    )
    @PostMapping("/encerrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Competicao> encerrarFase() {

        return ResponseEntity.ok(
                service.encerrarFase()
        );
    }

    // =========================
    // ⏭️ AVANÇAR FASE
    // =========================
    @Operation(
            summary = "Avançar fase",
            description = "Avança para a próxima fase da competição. Requer perfil ADMIN."
    )
    @PostMapping("/avancar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Competicao> avancarFase() {

        return ResponseEntity.ok(
                service.avancarFase()
        );
    }

    // =========================
    // 🏆 REVELAR CAMPEÃ
    // =========================
    @Operation(
            summary = "Revelar campeã",
            description = """
                    Apura os votos da FINAL
                    e revela oficialmente
                    a obra campeã.

                    Apenas ADMIN.

                    A competição deve estar
                    encerrada.
                    """
    )
    @PostMapping("/revelar-campea")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Submissao>
    revelarCampea() {

        return ResponseEntity.ok(
                service.revelarCampea()
        );
    }

    // =========================
    // 📋 DETALHAR FASE (CONFRONTOS, CLASSIFICADAS E ELIMINADAS)
    // =========================
    @Operation(
            summary = "Detalhar fase",
            description = """
                    Retorna os confrontos gerados para a fase, além das
                    obras classificadas e eliminadas dentro dela.
                    Requer perfil ADMIN.
                    """
    )
    @GetMapping("/fases/{fase}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FaseDetalheResponseDTO> obterFase(
            @PathVariable FaseCompeticao fase
    ) {

        return ResponseEntity.ok(
                service.obterFase(fase)
        );
    }
}