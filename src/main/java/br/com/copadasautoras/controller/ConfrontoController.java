package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.ConfrontoResultadoDTO;
import br.com.copadasautoras.entity.Confronto;
import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.entity.GrupoCompeticao;
import br.com.copadasautoras.service.ConfrontoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/confrontos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ConfrontoController {

    private final ConfrontoService service;

    // =========================
    // 🎲 GERAR CONFRONTOS DA FASE
    // =========================
    @Operation(
            summary = "Gerar confrontos da fase",
            description = """
                    Sorteia pares de obras classificadas para a fase informada
                    e sorteia uma jurada por par. Válido apenas para OITAVAS,
                    QUARTAS e SEMIFINAL — FASE_32 é gerada pelo /competicao/iniciar
                    e FINAL usa /confrontos/final/sortear-juradas.

                    Apenas ADMIN.
                    """
    )
    @PostMapping("/gerar/{fase}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Confronto>> gerarConfrontos(
            @PathVariable FaseCompeticao fase
    ) {

        return ResponseEntity.ok(
                service.gerarConfrontos(fase)
        );
    }

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

    // =========================
    // ✏️ REGISTRAR RESULTADO MANUAL
    // =========================
    @Operation(
            summary = "Registrar resultado manual de um confronto",
            description = """
                    Permite o admin corrigir/registrar manualmente o
                    resultado de um confronto. Uso excepcional — no fluxo
                    normal o resultado é sincronizado automaticamente
                    quando a jurada classifica.

                    Apenas ADMIN.
                    """
    )
    @PostMapping("/resultado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Confronto> registrarResultadoManual(
            @RequestBody ConfrontoResultadoDTO request
    ) {

        return ResponseEntity.ok(
                service.registrarResultadoManual(request)
        );
    }

    // =========================
    // 📋 LISTAR CONFRONTOS DA FASE
    // =========================
    @Operation(
            summary = "Listar confrontos de uma fase",
            description = "Retorna os confrontos já gerados para a fase informada. Apenas ADMIN."
    )
    @GetMapping("/{fase}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Confronto>> listarPorFase(
            @PathVariable FaseCompeticao fase
    ) {

        return ResponseEntity.ok(
                service.listarPorFase(fase)
        );
    }
}