package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.VotoPopularEncerramentoDTO;
import br.com.copadasautoras.dto.VotoPopularEncerrarRequestDTO;
import br.com.copadasautoras.dto.VotoPopularResultadoDTO;
import br.com.copadasautoras.service.VotoPopularAdminService;
import br.com.copadasautoras.service.VotoPopularExportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
public class VotoPopularAdminController {

    private final VotoPopularAdminService votoPopularAdminService;
    private final VotoPopularExportService votoPopularExportService;

    @Operation(
            summary = "Placar do Escolha do Público (admin)",
            description = """
                    Retorna as semifinalistas que autorizaram o trecho, com votos
                    confirmados, votos aguardando confirmação e percentual.
                    Somente números agregados: nenhum e-mail é exposto.
                    """
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/votacao-popular/resultado")
    public ResponseEntity<VotoPopularResultadoDTO> resultado() {
        return ResponseEntity.ok(votoPopularAdminService.resultado());
    }

    @Operation(
            summary = "Encerrar a votação e aplicar o selo (admin)",
            description = """
                    Fecha o Escolha do Público e aplica o selo na obra mais
                    votada. Em caso de empate no topo, nada é alterado e a
                    resposta traz as obras empatadas (empate=true): chame de
                    novo informando o obraId escolhido. O corpo é opcional.
                    """
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/votacao-popular/encerrar")
    public ResponseEntity<VotoPopularEncerramentoDTO> encerrar(
            @RequestBody(required = false) VotoPopularEncerrarRequestDTO dto
    ) {
        Long obraId = dto != null ? dto.obraId() : null;
        return ResponseEntity.ok(votoPopularAdminService.encerrar(obraId));
    }

    @Operation(
            summary = "Reabrir a votação e retirar o selo (admin)",
            description = """
                    Opcional. Reabre a votação, retira o selo de quem o
                    recebeu e registra na auditoria.
                    """
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/votacao-popular/reabrir")
    public ResponseEntity<VotoPopularResultadoDTO> reabrir() {
        votoPopularAdminService.reabrir();
        return ResponseEntity.ok(votoPopularAdminService.resultado());
    }

    @Operation(
            summary = "Exportar placar do Escolha do Público em Excel (admin)",
            description = """
                    Gera .xlsx com as abas Resumo e Placar. Somente números
                    agregados: nenhum e-mail de eleitor é exportado.
                    """
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/votacao-popular/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel() {

        byte[] conteudo = votoPopularExportService.gerarExcel(
                votoPopularAdminService.resultado());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nomeArquivo("xlsx") + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(conteudo);
    }

    @Operation(
            summary = "Exportar placar do Escolha do Público em PDF (admin)",
            description = """
                    Gera PDF na identidade da Copa: cartões com os totais e
                    tabela do placar. Somente números agregados.
                    """
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/votacao-popular/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf() {

        byte[] conteudo = votoPopularExportService.gerarPdf(
                votoPopularAdminService.resultado());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nomeArquivo("pdf") + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(conteudo);
    }

    private String nomeArquivo(String extensao) {
        return "escolha-do-publico-"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "." + extensao;
    }
}
