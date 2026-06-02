package br.com.copadasautoras.controller;

import br.com.copadasautoras.entity.Competicao;
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
        return ResponseEntity.ok(service.obter());
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
        return ResponseEntity.ok(service.iniciar());
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
        return ResponseEntity.ok(service.encerrarFase());
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
        return ResponseEntity.ok(service.avancarFase());
    }
}

