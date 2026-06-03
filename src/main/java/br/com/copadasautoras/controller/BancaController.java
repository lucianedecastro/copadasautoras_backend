package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.BancaClassificacaoRequestDTO;
import br.com.copadasautoras.dto.BancaObraResponseDTO;
import br.com.copadasautoras.dto.VotoFinalRequestDTO;
import br.com.copadasautoras.service.BancaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/banca")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BancaController {

    private final BancaService bancaService;

    // =========================
    // 📚 MINHAS OBRAS (BLIND REVIEW)
    // =========================
    @Operation(
            summary = "Minhas obras da banca",
            description = """
                    Retorna somente as obras
                    atribuídas à jurada logada.

                    Não expõe dados da autora,
                    garantindo blind review.
                    """
    )
    @GetMapping("/minhas-obras")
    @PreAuthorize("hasRole('BANCA')")
    public ResponseEntity<List<BancaObraResponseDTO>>
    minhasObras() {

        return ResponseEntity.ok(
                bancaService.minhasObras()
        );
    }

    // =========================
    // 🏆 CLASSIFICAR OBRAS
    // =========================
    @Operation(
            summary = "Classificar obras do grupo",
            description = """
                    Permite à jurada selecionar
                    as obras classificadas da fase.

                    FASE_32 = 2 classificadas
                    eliminatórias = 1 classificada.
                    """
    )
    @PostMapping("/classificar")
    @PreAuthorize("hasRole('BANCA')")
    public ResponseEntity<String> classificar(
            @RequestBody
            BancaClassificacaoRequestDTO request
    ) {

        bancaService.classificar(
                request
        );

        return ResponseEntity.ok(
                "Classificação realizada com sucesso."
        );
    }

    // =========================
    // 🗳️ VOTAR FINAL
    // =========================
    @Operation(
            summary = "Registrar voto da final",
            description = """
                    Permite à jurada registrar
                    seu voto em uma das obras
                    finalistas.

                    Apenas disponível durante
                    a FINAL.

                    Cada jurada possui apenas
                    um voto.
                    """
    )
    @PostMapping("/votar-final")
    @PreAuthorize("hasRole('BANCA')")
    public ResponseEntity<String> votarFinal(
            @RequestBody
            VotoFinalRequestDTO request
    ) {

        bancaService.votarFinal(
                request
        );

        return ResponseEntity.ok(
                "Voto registrado com sucesso."
        );
    }
}