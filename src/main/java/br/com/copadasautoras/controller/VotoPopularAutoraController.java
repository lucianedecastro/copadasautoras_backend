package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.VotoPopularMinhaDTO;
import br.com.copadasautoras.entity.Autora;
import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.repository.AutoraRepository;
import br.com.copadasautoras.repository.CompeticaoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VotoPopularAutoraController {

    private final AutoraRepository autoraRepository;
    private final SubmissaoRepository submissaoRepository;
    private final CompeticaoRepository competicaoRepository;

    @Operation(
            summary = "Situação do Escolha do Público para a minha obra",
            description = """
                    Informa à autora autenticada se a votação foi encerrada
                    e se a obra dela recebeu o selo Escolha do Público.
                    Requer perfil AUTORA.
                    """
    )
    @PreAuthorize("hasRole('AUTORA')")
    @GetMapping("/submissoes/minha/escolha-do-publico")
    public ResponseEntity<VotoPopularMinhaDTO> minha() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Autora autora = autoraRepository
                .findByUsuarioEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Autora autenticada não encontrada."
                        )
                );

        boolean encerrada = competicaoRepository.findAll()
                .stream()
                .findFirst()
                .map(c -> c.isVotacaoPopularEncerrada())
                .orElse(false);

        boolean selo = submissaoRepository
                .findFirstByAutoraId(autora.getId())
                .map(Submissao::isSeloEscolhaPublico)
                .orElse(false);

        // O selo só é mostrado depois do encerramento.
        return ResponseEntity.ok(
                new VotoPopularMinhaDTO(encerrada, encerrada && selo)
        );
    }
}
