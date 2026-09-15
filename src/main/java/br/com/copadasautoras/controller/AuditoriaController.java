package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.AuditoriaPageDTO;
import br.com.copadasautoras.entity.AcaoAuditoria;
import br.com.copadasautoras.entity.OrigemAuditoria;
import br.com.copadasautoras.entity.TipoEntidadeAuditoria;
import br.com.copadasautoras.service.AuditoriaConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Leitura do log de auditoria — painel admin.
 *
 * Controller próprio, separado do AdminController, para não mexer no
 * que já está no ar. Só leitura; a escrita mora nos services de
 * negócio via AuditoriaService.
 */
@RestController
@RequestMapping("/admin/competicao/auditoria")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AuditoriaController {

    private final AuditoriaConsultaService auditoriaConsultaService;

    @Operation(
            summary = "Consultar log de auditoria",
            description = """
                    Feed de decisões (autora, obra e banca), mais recente
                    primeiro, com filtros opcionais por origem, entidade,
                    id da entidade, ação e intervalo de datas. Paginado.
                    Apenas ADMIN.
                    """
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditoriaPageDTO> consultar(
            @RequestParam(required = false) OrigemAuditoria origem,
            @RequestParam(required = false) TipoEntidadeAuditoria entidade,
            @RequestParam(required = false) Long entidadeId,
            @RequestParam(required = false) AcaoAuditoria acao,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "25") int tamanho
    ) {

        return ResponseEntity.ok(
                auditoriaConsultaService.consultar(
                        origem, entidade, entidadeId, acao,
                        inicio, fim, pagina, tamanho
                )
        );
    }
}
