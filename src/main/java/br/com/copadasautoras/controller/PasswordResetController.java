package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.RedefinirSenhaDTO;
import br.com.copadasautoras.dto.SolicitarResetDTO;
import br.com.copadasautoras.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "Solicitar redefinição de senha",
            description = """
                    Recebe um e-mail e, se houver conta vinculada, envia o link
                    de redefinição. A resposta é sempre a mesma, exista a conta
                    ou não, para não revelar quais e-mails estão cadastrados.
                    """
    )
    @PostMapping("/esqueci-senha")
    public ResponseEntity<String> esqueciSenha(
            @Valid @RequestBody SolicitarResetDTO request
    ) {
        passwordResetService.solicitarReset(request.email());

        return ResponseEntity.ok(
                "Se houver uma conta com este e-mail, enviamos as instruções de redefinição."
        );
    }

    @Operation(
            summary = "Redefinir senha",
            description = "Valida o token recebido por e-mail e grava a nova senha."
    )
    @PostMapping("/redefinir-senha")
    public ResponseEntity<String> redefinirSenha(
            @Valid @RequestBody RedefinirSenhaDTO request
    ) {
        try {
            passwordResetService.redefinirSenha(
                    request.token(),
                    request.novaSenha()
            );

            return ResponseEntity.ok(
                    "Senha redefinida com sucesso. Você já pode entrar com a nova senha."
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
