package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.LanceAdminDTO;
import br.com.copadasautoras.dto.LanceRequestDTO;
import br.com.copadasautoras.dto.LanceUpdateDTO;
import br.com.copadasautoras.entity.CategoriaLance;
import br.com.copadasautoras.entity.TipoMidia;
import br.com.copadasautoras.service.LanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Gestão do Lance a Lance — painel admin.
 *
 * Tudo sob /admin/** e com hasRole('ADMIN'). A exportação
 * (o "ouro" dos dados) mora aqui, nunca na página pública.
 */
@RestController
@RequestMapping("/admin/lances")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class LanceAdminController {

    private final LanceService lanceService;

    // =========================
    // LISTAGEM / DETALHE
    // =========================

    @Operation(summary = "Listar lances (admin)")
    @GetMapping
    public ResponseEntity<List<LanceAdminDTO>> listar(
            @RequestParam(required = false) CategoriaLance categoria,
            @RequestParam(defaultValue = "false") boolean golaco
    ) {
        return ResponseEntity.ok(
                lanceService.listarAdmin(categoria, golaco)
        );
    }

    @Operation(summary = "Detalhe de um lance (admin)")
    @GetMapping("/{id}")
    public ResponseEntity<LanceAdminDTO> detalhe(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                lanceService.buscarAdminPorId(id)
        );
    }

    // =========================
    // CRUD
    // =========================

    @Operation(
            summary = "Criar lance",
            description = """
                    Cadastra um novo lance. Mídias por embed (URLs de
                    rádio/TV/YouTube) podem vir no corpo; uploads de arquivos
                    próprios entram depois, pelo endpoint de mídias.
                    """
    )
    @PostMapping
    public ResponseEntity<LanceAdminDTO> criar(
            @Valid @RequestBody LanceRequestDTO dto
    ) {
        return ResponseEntity.ok(
                lanceService.criar(dto)
        );
    }

    @Operation(summary = "Editar lance")
    @PutMapping("/{id}")
    public ResponseEntity<LanceAdminDTO> editar(
            @PathVariable Long id,
            @Valid @RequestBody LanceUpdateDTO dto
    ) {
        return ResponseEntity.ok(
                lanceService.editar(id, dto)
        );
    }

    @Operation(summary = "Excluir lance")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id
    ) {
        lanceService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // =========================
    // MÍDIAS (UPLOAD)
    // =========================

    @Operation(
            summary = "Adicionar mídia por upload",
            description = """
                    Envia uma foto/vídeo próprio para o Cloudinary e o vincula
                    ao lance. Para mídias de veículo (rádio/TV/YouTube), use
                    embed no cadastro em vez de upload.
                    """
    )
    @PostMapping(
            value = "/{id}/midias",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<LanceAdminDTO> adicionarMidia(
            @PathVariable Long id,
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestParam TipoMidia tipo,
            @RequestParam(required = false) String legenda,
            @RequestParam(required = false) Integer ordem
    ) {
        return ResponseEntity.ok(
                lanceService.adicionarMidiaUpload(
                        id, arquivo, tipo, legenda, ordem
                )
        );
    }

    @Operation(summary = "Remover mídia de um lance")
    @DeleteMapping("/{id}/midias/{midiaId}")
    public ResponseEntity<LanceAdminDTO> removerMidia(
            @PathVariable Long id,
            @PathVariable Long midiaId
    ) {
        return ResponseEntity.ok(
                lanceService.removerMidia(id, midiaId)
        );
    }

    // =========================
    // EXPORTAÇÃO (RELATÓRIO)
    // =========================

    @Operation(
            summary = "Exportar lances em Excel",
            description = "Gera .xlsx do recorte filtrado (categoria/golaço)."
    )
    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) CategoriaLance categoria,
            @RequestParam(defaultValue = "false") boolean golaco
    ) {
        byte[] conteudo = lanceService.exportarExcel(categoria, golaco);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nomeArquivo("xlsx") + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(conteudo);
    }

    @Operation(
            summary = "Exportar lances em PDF",
            description = "Gera .pdf do recorte filtrado (categoria/golaço)."
    )
    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) CategoriaLance categoria,
            @RequestParam(defaultValue = "false") boolean golaco
    ) {
        byte[] conteudo = lanceService.exportarPdf(categoria, golaco);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nomeArquivo("pdf") + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(conteudo);
    }

    private String nomeArquivo(String extensao) {
        String data = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "lance-a-lance-" + data + "." + extensao;
    }
}
