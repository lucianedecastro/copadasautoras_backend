package br.com.copadasautoras.controller;

import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.repository.SubmissaoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/submissoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ArquivoController {

    private final SubmissaoRepository submissaoRepository;

    // =========================
    // 🔒 ARQUIVO COMPLETO
    // =========================
    @Operation(
            summary = "Baixar arquivo completo",
            description = """
                    Retorna o arquivo completo da submissão.
                    Acesso permitido apenas para ADMIN e BANCA.
                    """
    )
    @GetMapping("/{id}/arquivo-completo")
    @PreAuthorize("hasAnyRole('ADMIN', 'BANCA')")
    public ResponseEntity<Void> arquivoCompleto(
            @PathVariable Long id
    ) {

        Submissao submissao = submissaoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Submissão não encontrada")
                );

        if (submissao.getArquivoCompletoUrl() == null) {
            throw new RuntimeException(
                    "Arquivo completo não disponível"
            );
        }

        return ResponseEntity.status(302)
                .header(
                        HttpHeaders.LOCATION,
                        submissao.getArquivoCompletoUrl()
                )
                .build();
    }

    // =========================
    // 🔓 ARQUIVO PÚBLICO
    // =========================
    @Operation(
            summary = "Baixar arquivo público",
            description = """
                    Retorna o arquivo público da submissão.
                    Requer autenticação.
                    """
    )
    @GetMapping("/{id}/arquivo-publico")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> arquivoPublico(
            @PathVariable Long id
    ) {

        Submissao submissao = submissaoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Submissão não encontrada")
                );

        if (submissao.getArquivoPublicoUrl() == null) {
            throw new RuntimeException(
                    "Arquivo público não disponível"
            );
        }

        return ResponseEntity.status(302)
                .header(
                        HttpHeaders.LOCATION,
                        submissao.getArquivoPublicoUrl()
                )
                .build();
    }
}

