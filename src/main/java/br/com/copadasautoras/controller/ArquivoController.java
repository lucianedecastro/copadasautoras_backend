package br.com.copadasautoras.controller;

import br.com.copadasautoras.entity.AceiteTermo;
import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.repository.AceiteTermoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/submissoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ArquivoController {

    private final SubmissaoRepository    submissaoRepository;
    private final AceiteTermoRepository  aceiteTermoRepository;  // ← adicionado

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
    public ResponseEntity<Void> arquivoCompleto(@PathVariable Long id) {

        Submissao submissao = submissaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submissão não encontrada"));

        if (submissao.getArquivoCompletoUrl() == null) {
            throw new RuntimeException("Arquivo completo não disponível");
        }

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, submissao.getArquivoCompletoUrl())
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
    public ResponseEntity<Void> arquivoPublico(@PathVariable Long id) {

        Submissao submissao = submissaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submissão não encontrada"));

        if (submissao.getArquivoPublicoUrl() == null) {
            throw new RuntimeException("Arquivo público não disponível");
        }

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, submissao.getArquivoPublicoUrl())
                .build();
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
                    """
    )
    @GetMapping("/minha/termo")
    @PreAuthorize("hasRole('AUTORA')")
    public ResponseEntity<Void> meuTermo(Authentication authentication) {

        // authentication.getName() retorna o subject do JWT,
        // que neste projeto é o e-mail do Usuário (confirmado via TermoService)
        String email = authentication.getName();

        AceiteTermo aceite = aceiteTermoRepository
                .findByAutora_Usuario_Email(email)
                .orElseThrow(() -> new RuntimeException("Termo de aceite não encontrado"));

        if (aceite.getTermoPdfUrl() == null) {
            throw new RuntimeException("PDF do termo ainda não foi gerado para esta submissão");
        }

        // ATENÇÃO — CloudinaryStorageService.uploadTermoPdf() salva o arquivo com
        // access_mode = "authenticated". O redirect 302 para essa URL funcionará apenas
        // se o Cloudinary estiver configurado para permitir acesso público via URL direta.
        //
        // Se o download falhar com 401/403 no Cloudinary:
        //
        //   OPÇÃO A (recomendada) — mudar access_mode do termo para "public":
        //   O termo tem UUID no path (ex: termos/1/termo-aceite-{uuid}), então a URL
        //   já é não-adivinhável. Altere CloudinaryStorageService.uploadTermoPdf():
        //
        //       "access_mode", "public"    ← em vez de "authenticated"
        //
        //   Termos existentes precisarão ser re-gerados ou ter o access_mode atualizado
        //   via Cloudinary Admin API: cloudinary.api().update(publicId, ObjectUtils.asMap("access_control", ...))
        //
        //   OPÇÃO B — gerar URL assinada temporária no CloudinaryStorageService:
        //
        //       public String gerarUrlAssinada(String url) {
        //           String publicId = extrairPublicId(url);
        //           return cloudinary.url()
        //               .resourceType("raw")
        //               .signed(true)
        //               .expireAt((System.currentTimeMillis() / 1000) + 300) // 5 min
        //               .generate(publicId);
        //       }
        //
        //   E neste endpoint: .header(LOCATION, cloudinaryService.gerarUrlAssinada(aceite.getTermoPdfUrl()))

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, aceite.getTermoPdfUrl())
                .build();
    }
}