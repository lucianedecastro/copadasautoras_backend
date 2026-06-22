package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.FaseResponseDTO;
import br.com.copadasautoras.dto.SubmissaoRequestDTO;
import br.com.copadasautoras.dto.SubmissaoResponseDTO;
import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.service.SubmissaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SubmissaoController {

    private final SubmissaoService submissaoService;
    private final ObjectMapper objectMapper;

    // =========================
    // 📝 SUBMISSÕES — /submissoes
    // =========================

    @Operation(
            summary = "Criar submissão",
            description = """
                    Realiza o envio de uma submissão da autora.
                    Requer perfil AUTORA.
                    Aceita upload multipart contendo dados da submissão,
                    arquivo completo e arquivo público opcional.
                    """
    )
    @PostMapping(
            value = "/submissoes",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('AUTORA')")
    public ResponseEntity<SubmissaoResponseDTO> criar(
            @RequestPart("dados") String dadosJson,

            @RequestPart("arquivoCompleto")
            MultipartFile arquivoCompleto,

            @RequestPart(
                    value = "arquivoPublico",
                    required = false
            )
            MultipartFile arquivoPublico
    ) throws Exception {

        SubmissaoRequestDTO dto =
                objectMapper.readValue(
                        dadosJson,
                        SubmissaoRequestDTO.class
                );

        return ResponseEntity.ok(
                submissaoService.criar(
                        dto,
                        arquivoCompleto,
                        arquivoPublico
                )
        );
    }

    // =========================
// 📖 MINHA SUBMISSÃO
// =========================

    @Operation(
            summary = "Consultar minha submissão",
            description = """
                Retorna a submissão vinculada à autora autenticada.
                Requer perfil AUTORA.
                """
    )
    @GetMapping("/submissoes/minha")
    @PreAuthorize("hasRole('AUTORA')")
    public ResponseEntity<SubmissaoResponseDTO> minhaSubmissao() {

        return ResponseEntity.ok(
                submissaoService.buscarMinhaSubmissao()
        );
    }

    // =========================
    // 🏆 FASES — /competicao/fases
    // =========================

    @Operation(
            summary = "Consultar fase da competição",
            description = """
                    Retorna as informações de uma fase da competição.
                    Requer autenticação.
                    """
    )
    @GetMapping("/competicao/fases/{fase}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FaseResponseDTO> obterFase(
            @PathVariable FaseCompeticao fase
    ) {

        return ResponseEntity.ok(
                submissaoService.obterFase(fase)
        );
    }

    // =========================
    // ⚔️ CONFRONTOS — /confrontos
    // =========================

    @Operation(
            summary = "Resolver confronto",
            description = """
                    Define o vencedor de um confronto. Ferramenta de
                    correção manual do admin — no fluxo normal, o
                    resultado já é registrado quando a jurada classifica
                    pelo /banca/classificar.

                    Apenas ADMIN.
                    """
    )
    @PostMapping("/confrontos/{id}/resultado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resolverConfronto(
            @PathVariable Long id,
            @RequestParam Long vencedorId
    ) {

        submissaoService.resolverConfronto(
                id,
                vencedorId
        );

        return ResponseEntity.ok().build();
    }
}