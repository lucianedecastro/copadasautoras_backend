package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.SeloEscolhaPublicoDTO;
import br.com.copadasautoras.dto.VotoPopularElegivelDTO;
import br.com.copadasautoras.dto.VotoPopularRequestDTO;
import br.com.copadasautoras.entity.Submissao;
import br.com.copadasautoras.repository.CompeticaoRepository;
import br.com.copadasautoras.repository.SubmissaoRepository;
import br.com.copadasautoras.service.VotoPopularService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VotoPopularController {

    private final VotoPopularService votoPopularService;
    private final SubmissaoRepository submissaoRepository;
    private final CompeticaoRepository competicaoRepository;

    // =========================
    // 🗳️ VOTAÇÃO POPULAR — /publico/votacao-popular
    //
    // Sob /publico/**, liberado sem autenticação pelo
    // SecurityConfig (mesmo padrão do chaveamento/finalistas).
    // =========================

    @Operation(
            summary = "Registrar voto no Escolha do Público",
            description = """
                    Registra um voto pendente para uma obra elegível
                    (semifinalista) e envia e-mail de confirmação.

                    O voto só passa a valer após a confirmação pelo link
                    enviado por e-mail. Um mesmo e-mail só pode confirmar
                    um voto em toda a votação.
                    """
    )
    @PostMapping("/publico/votacao-popular/votar")
    public ResponseEntity<Map<String, String>> votar(
            @Valid @RequestBody VotoPopularRequestDTO dto
    ) {

        votoPopularService.solicitarVoto(
                dto.submissaoId(),
                dto.email()
        );

        return ResponseEntity.ok(Map.of(
                "mensagem",
                "Enviamos um e-mail de confirmação para " + dto.email()
        ));
    }

    @Operation(
            summary = "Confirmar voto no Escolha do Público",
            description = """
                    Confirma um voto pendente a partir do token enviado
                    por e-mail. Link expira ao ser usado.
                    """
    )
    @GetMapping("/publico/votacao-popular/confirmar")
    public ResponseEntity<Map<String, String>> confirmar(
            @RequestParam String token
    ) {

        votoPopularService.confirmarVoto(token);

        return ResponseEntity.ok(Map.of(
                "mensagem", "Voto confirmado com sucesso!"
        ));
    }

    @Operation(
            summary = "Listar obras elegíveis ao Escolha do Público",
            description = """
                    Retorna as obras semifinalistas que autorizaram o voto
                    popular, com o trecho liberado pela autora, para leitura
                    e votação pública.
                    """
    )
    @GetMapping("/publico/votacao-popular/elegiveis")
    public ResponseEntity<List<VotoPopularElegivelDTO>> elegiveis() {

        List<VotoPopularElegivelDTO> resultado =
                submissaoRepository
                        .findByElegivelVotoPopularTrueAndAutorizaVotoPopularTrue()
                        .stream()
                        .map(this::paraElegivelDTO)
                        .toList();

        return ResponseEntity.ok(resultado);
    }

    @Operation(
            summary = "Estado do Escolha do Público e obra com selo",
            description = """
                    Informa se a votação foi encerrada e, se sim, qual obra
                    recebeu o selo Escolha do Público. Enquanto a votação
                    estiver aberta, vencedora vem nula.
                    """
    )
    @GetMapping("/publico/votacao-popular/selo")
    public ResponseEntity<Map<String, Object>> selo() {

        boolean encerrada = competicaoRepository.findAll()
                .stream()
                .findFirst()
                .map(c -> c.isVotacaoPopularEncerrada())
                .orElse(false);

        SeloEscolhaPublicoDTO vencedora = !encerrada
                ? null
                : submissaoRepository.findBySeloEscolhaPublicoTrue()
                .stream()
                .findFirst()
                .map(s -> new SeloEscolhaPublicoDTO(
                        s.getId(),
                        s.getTitulo(),
                        s.getAutora() != null
                                ? s.getAutora().getNomeExibicao()
                                : null))
                .orElse(null);

        Map<String, Object> corpo = new java.util.HashMap<>();
        corpo.put("votacaoEncerrada", encerrada);
        corpo.put("vencedora", vencedora);

        return ResponseEntity.ok(corpo);
    }

    private VotoPopularElegivelDTO paraElegivelDTO(Submissao submissao) {

        return new VotoPopularElegivelDTO(
                submissao.getId(),
                submissao.getTitulo(),
                submissao.getCategoria(),
                submissao.getAutora().getNomeExibicao(),
                submissao.getTrechoLiberado()
        );
    }
}