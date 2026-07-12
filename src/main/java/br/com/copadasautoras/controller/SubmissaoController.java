package br.com.copadasautoras.controller;

import br.com.copadasautoras.dto.FaseResponseDTO;
import br.com.copadasautoras.dto.SubmissaoRequestDTO;
import br.com.copadasautoras.dto.SubmissaoResponseDTO;
import br.com.copadasautoras.dto.SubmissaoUpdateDTO;
import br.com.copadasautoras.entity.FaseCompeticao;
import br.com.copadasautoras.service.SubmissaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SubmissaoController {

    private final SubmissaoService submissaoService;
    private final ObjectMapper objectMapper;

    // =========================
    // 📝 SUBMISSÕES — /submissoes
    // =========================

    @Operation(
            summary = "Criar submissão",
            description = """
                    Realiza o envio de uma submissão da autora.
                    Requer perfil AUTORA.
                    Aceita upload multipart contendo dados da submissão,
                    arquivo completo e arquivo público opcional.
                    """
    )
    @PostMapping(
            value = "/submissoes",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('AUTORA')")
    public ResponseEntity<SubmissaoResponseDTO> criar(
            @RequestPart("dados") String dadosJson,

            @RequestPart("arquivoCompleto")
            MultipartFile arquivoCompleto,

            @RequestPart(
                    value = "arquivoPublico",
                    required = false
            )
            MultipartFile arquivoPublico
    ) throws Exception {

        SubmissaoRequestDTO dto =
                objectMapper.readValue(
                        dadosJson,
                        SubmissaoRequestDTO.class
                );

        return ResponseEntity.ok(
                submissaoService.criar(
                        dto,
                        arquivoCompleto,
                        arquivoPublico
                )
        );
    }

    // =========================
    // 📖 MINHA SUBMISSÃO
    // =========================

    @Operation(
            summary = "Consultar minha submissão",
            description = """
                Retorna a submissão vinculada à autora autenticada.
                Requer perfil AUTORA.
                """
    )
    @GetMapping("/submissoes/minha")
    @PreAuthorize("hasRole('AUTORA')")
    public ResponseEntity<SubmissaoResponseDTO> minhaSubmissao() {

        return ResponseEntity.ok(
                submissaoService.buscarMinhaSubmissao()
        );
    }

    // =========================
    // ✏️ EDITAR DADOS DA MINHA SUBMISSÃO
    // =========================

    @Operation(
            summary = "Editar os dados da minha submissão",
            description = """
                    Corrige título, categoria, descrição e tipo de exibição da
                    obra já submetida.

                    Os cinco aceites do Art. 19 não são pedidos de novo: foram
                    declarados no envio e continuam valendo. Mas o termo de
                    aceite em PDF é regerado, porque ele imprime o título, a
                    categoria e a modalidade de exibição.

                    Só é permitido enquanto a obra estiver com status SUBMETIDA
                    e o evento estiver ativo.

                    Requer perfil AUTORA.
                    """
    )
    @PutMapping("/submissoes/minha")
    @PreAuthorize("hasRole('AUTORA')")
    public ResponseEntity<SubmissaoResponseDTO> editarMinhaSubmissao(
            @Valid @RequestBody SubmissaoUpdateDTO dto
    ) {

        return ResponseEntity.ok(
                submissaoService.editarMinhaSubmissao(dto)
        );
    }

    // =========================
    // ♻️ SUBSTITUIR ARQUIVOS
    // =========================

    @Operation(
            summary = "Substituir os arquivos da minha submissão",
            description = """
                    Troca o arquivo completo e/ou o arquivo público da obra,
                    preservando o id e a data de submissão originais.

                    Atende ao Art. 18 §3º do regulamento, que prevê novo envio
                    do arquivo em caso de problema técnico de leitura.

                    Só é permitido enquanto a obra estiver com status SUBMETIDA
                    e o evento estiver ativo. Depois que a obra entra em
                    competição, trocar o arquivo corromperia a avaliação cega.

                    Envie ao menos um dos dois arquivos. O que não for enviado
                    permanece como está.

                    Requer perfil AUTORA.
                    """
    )
    @PutMapping(
            value = "/submissoes/minha/arquivos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('AUTORA')")
    public ResponseEntity<SubmissaoResponseDTO> substituirArquivos(

            @RequestPart(
                    value = "arquivoCompleto",
                    required = false
            )
            MultipartFile arquivoCompleto,

            @RequestPart(
                    value = "arquivoPublico",
                    required = false
            )
            MultipartFile arquivoPublico
    ) {

        return ResponseEntity.ok(
                submissaoService.substituirArquivos(
                        arquivoCompleto,
                        arquivoPublico
                )
        );
    }

    // =========================
    // 🗑️ EXCLUIR MINHA SUBMISSÃO
    // =========================

    @Operation(
            summary = "Excluir minha submissão",
            description = """
                    Exclui a obra da autora autenticada, junto com o termo de
                    aceite e todos os arquivos armazenados.

                    Ação irreversível. Depois da exclusão, a autora volta a
                    poder inscrever uma obra enquanto o prazo estiver aberto.

                    Só é permitido enquanto a obra estiver com status SUBMETIDA
                    e o evento estiver ativo. Depois que a obra entra em
                    competição, excluí-la quebraria o chaveamento.

                    Requer perfil AUTORA.
                    """
    )
    @DeleteMapping("/submissoes/minha")
    @PreAuthorize("hasRole('AUTORA')")
    public ResponseEntity<Void> excluirMinhaSubmissao() {

        submissaoService.excluirMinhaSubmissao();

        return ResponseEntity.noContent().build();
    }

    // =========================
    // 🏆 FASES — /competicao/fases
    // =========================

    @Operation(
            summary = "Consultar fase da competição",
            description = """
                    Retorna as informações de uma fase da competição.
                    Requer autenticação.
                    """
    )
    @GetMapping("/competicao/fases/{fase}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FaseResponseDTO> obterFase(
            @PathVariable FaseCompeticao fase
    ) {

        return ResponseEntity.ok(
                submissaoService.obterFase(fase)
        );
    }

    // =========================
    // ⚔️ CONFRONTOS — /confrontos
    // =========================

    @Operation(
            summary = "Resolver confronto",
            description = """
                    Define o vencedor de um confronto. Ferramenta de
                    correção manual do admin — no fluxo normal, o
                    resultado já é registrado quando a jurada classifica
                    pelo /banca/classificar.

                    Apenas ADMIN.
                    """
    )
    @PostMapping("/confrontos/{id}/resultado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resolverConfronto(
            @PathVariable Long id,
            @RequestParam Long vencedorId
    ) {

        submissaoService.resolverConfronto(
                id,
                vencedorId
        );

        return ResponseEntity.ok().build();
    }
}