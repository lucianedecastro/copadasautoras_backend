package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.*;
import br.com.copadasautoras.entity.StatusAutora;
import br.com.copadasautoras.service.AutoraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/autoras")
@RequiredArgsConstructor
@Tag(
        name = "Autoras",
        description = "CRUD de autoras da Copa de Literatura de Futebol Feminino"
)
public class AutoraController {

    private final AutoraService autoraService;

    // =====================================================
    // PERFIL PRIVADO DA AUTORA
    // =====================================================

    @Operation(
            summary = "Buscar meu perfil",
            description = "Retorna o perfil privado da autora autenticada."
    )
    @PreAuthorize("hasRole('AUTORA')")
    @GetMapping("/me")
    public ResponseEntity<AutoraResponseDTO> buscarMeuPerfil() {

        return ResponseEntity.ok(
                autoraService.buscarMeuPerfil()
        );
    }

    @Operation(
            summary = "Atualizar meu perfil",
            description = """
                    Permite atualizar:
                    - nome de exibição
                    - biografia
                    - site
                    - rede social
                    - localização (estado e cidade, sempre juntos)
                    
                    Não permite alterar:
                    - nome
                    - email
                    - status
                    """
    )
    @PreAuthorize("hasRole('AUTORA')")
    @PutMapping("/me")
    public ResponseEntity<AutoraResponseDTO> atualizarMeuPerfil(
            @Valid
            @RequestBody
            AutoraUpdateRequestDTO request
    ) {

        return ResponseEntity.ok(
                autoraService.atualizarMeuPerfil(
                        request
                )
        );
    }

    @Operation(
            summary = "Solicitar exclusão de perfil",
            description = """
                    Solicita exclusão institucional do perfil.
                    O perfil não é removido fisicamente do banco.
                    """
    )
    @PreAuthorize("hasRole('AUTORA')")
    @DeleteMapping("/me")
    public ResponseEntity<String> solicitarExclusao(
            @Valid
            @RequestBody
            SolicitacaoExclusaoDTO request
    ) {

        autoraService.solicitarExclusao(
                request
        );

        return ResponseEntity.ok(
                "Solicitação de exclusão registrada com sucesso."
        );
    }

    // =====================================================
    // PERFIL PÚBLICO
    // =====================================================

    @Operation(
            summary = "Buscar perfil público da autora",
            description = """
                    Retorna perfil público contendo:
                    - nome de exibição
                    - minibio
                    - site
                    - rede social
                    - obra inscrita
                    - categoria
                    """
    )
    @GetMapping("/publico/{id}")
    public ResponseEntity<AutoraPublicResponseDTO>
    buscarPerfilPublico(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                autoraService.buscarPerfilPublico(
                        id
                )
        );
    }

    // =====================================================
    // ADMIN
    // =====================================================

    @Operation(
            summary = "Buscar autora por ID (admin)",
            description = """
                    Retorna a autora completa para uso administrativo,
                    incluindo status e o sinal de perfil completo — usado
                    na tela de conferência do painel do admin.
                    """
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseEntity<AutoraResponseDTO>
    buscarPorIdAdmin(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                autoraService.buscarPorIdAdmin(
                        id
                )
        );
    }

    @Operation(
            summary = "Relatório de localização das autoras (admin)",
            description = """
                    Contagem agregada de autoras ativas (aprovadas e em
                    análise) por estado e por cidade, mais a cobertura do
                    dado (quantas já informaram a localização). Nunca
                    devolve dados individuais.
                    """
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/relatorio/localizacao")
    public ResponseEntity<RelatorioLocalizacaoDTO>
    relatorioLocalizacao() {

        return ResponseEntity.ok(
                autoraService.relatorioLocalizacao()
        );
    }

    // =====================================================
    // EXPORTAÇÃO (ADMIN)
    // =====================================================

    @Operation(
            summary = "Exportar autoras em Excel (admin)",
            description = """
                    Gera .xlsx do recorte filtrado por status (sem status =
                    todas). Abas: resumo, lista de autoras (com nome
                    completo e e-mail — uso interno), por estado, por região
                    e por cidade.
                    """
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) StatusAutora status
    ) {

        byte[] conteudo = autoraService.exportarExcel(status);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nomeArquivo("xlsx") + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(conteudo);
    }

    @Operation(
            summary = "Exportar autoras em PDF com mapa (admin)",
            description = """
                    Gera .pdf do recorte filtrado por status (sem status =
                    todas): cartões, mapa do Brasil, números por região,
                    estado e cidade. Com incluirLista=true (padrão) traz
                    também a lista de autoras (nome de exibição, status,
                    UF, cidade); com incluirLista=false sai só o agregado,
                    versão indicada para apresentar a patrocinadores.
                    """
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) StatusAutora status,
            @RequestParam(defaultValue = "true") boolean incluirLista
    ) {

        byte[] conteudo = autoraService.exportarPdf(status, incluirLista);

        String base = incluirLista ? "autoras" : "autoras-mapa";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nomeArquivo(base, "pdf") + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(conteudo);
    }

    @Operation(
            summary = "Listar autoras por status",
            description = "Lista autoras por status institucional."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AutoraResponseDTO>>
    listarPorStatus(
            @PathVariable StatusAutora status
    ) {

        return ResponseEntity.ok(
                autoraService.listarPorStatus(
                        status
                )
        );
    }

    @Operation(
            summary = "Aprovar autora",
            description = "Aprova autora pendente."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/aprovar")
    public ResponseEntity<AutoraResponseDTO>
    aprovarAutora(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                autoraService.aprovarAutora(
                        id
                )
        );
    }

    @Operation(
            summary = "Suspender autora",
            description = "Suspende a autora. Vale a partir de qualquer status ativo."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/suspender")
    public ResponseEntity<AutoraResponseDTO>
    suspenderAutora(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                autoraService.suspenderAutora(
                        id
                )
        );
    }

    @Operation(
            summary = "Excluir autora (admin)",
            description = """
                    Exclusão institucional (soft delete): marca a autora
                    como EXCLUIDA sem remover fisicamente do banco.
                    Reversível — a autora pode ser reativada depois.
                    
                    Recebe no corpo:
                    - motivo (obrigatório): categoria que define a mensagem
                      genérica exibida à autora (INADEQUACAO, ADMINISTRATIVA).
                    - justificativa (opcional): registro interno, vai para a
                      auditoria e nunca aparece pra autora.
                    """
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/excluir")
    public ResponseEntity<AutoraResponseDTO>
    excluirAutora(
            @PathVariable Long id,
            @Valid
            @RequestBody
            ExclusaoAdminRequestDTO request
    ) {

        return ResponseEntity.ok(
                autoraService.excluirAutora(
                        id,
                        request.motivo(),
                        request.justificativa()
                )
        );
    }

    private String nomeArquivo(String extensao) {
        return nomeArquivo("autoras", extensao);
    }

    private String nomeArquivo(String base, String extensao) {
        String data = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return base + "-" + data + "." + extensao;
    }
}
