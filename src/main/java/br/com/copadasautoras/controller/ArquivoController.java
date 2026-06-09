package br.com.copadasautoras.controller;

import br.com.copadasautoras.entity.AceiteTermo;
import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.repository.AceiteTermoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import br.com.copadasautoras.storage.CloudinaryStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/submissoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ArquivoController {

    private final SubmissaoRepository       submissaoRepository;
    private final AceiteTermoRepository     aceiteTermoRepository;
    private final CloudinaryStorageService  storageService;

    // =========================
    // 🔒 ARQUIVO COMPLETO
    // =========================

    @Operation(
            summary = "Baixar arquivo completo",
            description = """
                    Retorna o arquivo completo da submissão.
                    Acesso permitido apenas para ADMIN e BANCA.
                    O arquivo é streamado pelo backend — sem redirect para o Cloudinary.
                    """
    )
    @GetMapping("/{id}/arquivo-completo")
    @PreAuthorize("hasAnyRole('ADMIN', 'BANCA')")
    public ResponseEntity<byte[]> arquivoCompleto(@PathVariable Long id) {

        Submissao submissao = submissaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submissão não encontrada"));

        if (submissao.getArquivoCompletoUrl() == null) {
            throw new RuntimeException("Arquivo completo não disponível");
        }

        byte[] bytes = storageService.baixarArquivo(submissao.getArquivoCompletoUrl());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"obra-completa-" + id + ".pdf\"")
                .body(bytes);
    }

    // =========================
    // 🔓 ARQUIVO PÚBLICO
    // =========================

    @Operation(
            summary = "Baixar arquivo público",
            description = """
                    Retorna o arquivo público (trecho) da submissão.
                    Requer autenticação.
                    O arquivo é streamado pelo backend — sem redirect para o Cloudinary.
                    """
    )
    @GetMapping("/{id}/arquivo-publico")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> arquivoPublico(@PathVariable Long id) {

        Submissao submissao = submissaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submissão não encontrada"));

        if (submissao.getArquivoPublicoUrl() == null) {
            throw new RuntimeException("Arquivo público não disponível");
        }

        byte[] bytes = storageService.baixarArquivo(submissao.getArquivoPublicoUrl());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"trecho-" + id + ".pdf\"")
                .body(bytes);
    }

    // =========================
    // 📄 TERMO DE ACEITE
    // =========================

    @Operation(
            summary = "Baixar meu termo de aceite",
            description = """
                    Retorna o PDF do termo de aceite da autora autenticada.
                    A autora é identificada pelo token JWT — não é necessário informar ID.
                    Acesso permitido apenas para AUTORA.
                    O arquivo é streamado pelo backend — sem redirect para o Cloudinary.
                    """
    )
    @GetMapping("/minha/termo")
    @PreAuthorize("hasRole('AUTORA')")
    public ResponseEntity<byte[]> meuTermo(Authentication authentication) {

        String email = authentication.getName();

        AceiteTermo aceite = aceiteTermoRepository
                .findByAutora_Usuario_Email(email)
                .orElseThrow(() -> new RuntimeException("Termo de aceite não encontrado"));

        if (aceite.getTermoPdfUrl() == null) {
            throw new RuntimeException("PDF do termo ainda não foi gerado para esta submissão");
        }

        byte[] bytes = storageService.baixarArquivo(aceite.getTermoPdfUrl());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"termo-de-aceite.pdf\"")
                .body(bytes);
    }
}