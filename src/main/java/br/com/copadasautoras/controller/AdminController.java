package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.AdminDashboardDTO;
import br.com.copadasautoras.dto.CreateUsuarioRequest;
import br.com.copadasautoras.dto.UsuarioAdminResponseDTO;
import br.com.copadasautoras.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/competicao")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    // =========================
    // 👤 USUÁRIOS ADMIN/BANCA
    // =========================

    @Operation(
            summary = "Criar usuário administrativo",
            description = """
                    Cria usuários com perfil ADMIN ou BANCA.
                    Endpoint restrito a ADMIN.
                    """
    )
    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> criarUsuario(
            @Valid @RequestBody CreateUsuarioRequest request
    ) {

        adminService.criarUsuario(request);

        return ResponseEntity.ok(
                "Usuário criado com sucesso."
        );
    }

    @Operation(
            summary = "Listar usuários administrativos",
            description = """
                    Retorna usuários com perfil ADMIN e BANCA.
                    Endpoint restrito a ADMIN.
                    """
    )
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioAdminResponseDTO>>
    listarUsuariosAdministrativos() {

        return ResponseEntity.ok(
                adminService.listarUsuariosAdministrativos()
        );
    }

    // =========================
    // 📊 DASHBOARD
    // =========================

    @Operation(
            summary = "Dashboard da competição",
            description = "Retorna dados administrativos da competição."
    )
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardDTO> dashboard() {

        return ResponseEntity.ok(
                adminService.obterDashboard()
        );
    }

    // =========================
    // 🔓 REABRIR FASE
    // =========================

    @Operation(
            summary = "Reabrir fase",
            description = "Reabre uma fase encerrada."
    )
    @PostMapping("/reabrir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reabrirFase() {

        adminService.reabrirFase();

        return ResponseEntity.ok().build();
    }

    // =========================
    // ⏭️ AVANÇAR MANUAL
    // =========================

    @Operation(
            summary = "Avançar fase",
            description = "Avança manualmente a fase da competição."
    )
    @PostMapping("/avancar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> avancarFase() {

        adminService.avancarFaseManual();

        return ResponseEntity.ok().build();
    }

    // =========================
    // 🔁 REGERAR CONFRONTOS
    // =========================

    @Operation(
            summary = "Regenerar confrontos",
            description = "Regenera confrontos da fase atual."
    )
    @PostMapping("/regenerar-confrontos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> regenerar() {

        adminService.regenerarConfrontos();

        return ResponseEntity.ok().build();
    }

    // =========================
    // 🔒 PAUSAR FASE
    // =========================

    @Operation(
            summary = "Pausar fase",
            description = "Pausa a fase atual da competição."
    )
    @PostMapping("/pausar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> pausar() {

        adminService.pausarFase();

        return ResponseEntity.ok().build();
    }
}

