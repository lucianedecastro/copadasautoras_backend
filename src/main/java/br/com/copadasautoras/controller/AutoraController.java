package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.*;
import br.com.copadasautoras.entity.StatusAutora;
import br.com.copadasautoras.service.AutoraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
            description = "Suspende autora aprovada."
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
}
