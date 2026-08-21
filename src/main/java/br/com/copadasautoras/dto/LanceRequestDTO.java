package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.CategoriaLance;
import br.com.copadasautoras.entity.StatusLance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cadastro de um novo lance.
 *
 * As mídias por embed (URLs de veículo) podem vir aqui.
 * Uploads de fotos/vídeos próprios entram depois, pelo
 * endpoint /admin/lances/{id}/midias.
 */
public record LanceRequestDTO(

        @NotBlank(message = "Título é obrigatório")
        String titulo,

        String resumo,

        @NotNull(message = "Categoria é obrigatória")
        CategoriaLance categoria,

        boolean golaco,

        String veiculo,

        String linkExterno,

        @NotNull(message = "Status é obrigatório")
        StatusLance status,

        @NotNull(message = "Data do acontecimento é obrigatória")
        LocalDate dataAcontecimento,

        LocalDateTime publicarEm,

        @Valid
        List<LanceEmbedMidiaDTO> midiasEmbed

) {

    /**
     * Um lance AGENDADO sem data/hora de publicação nunca
     * apareceria na timeline — a regra de visibilidade
     * exige publicarEm. Barramos isso já na entrada.
     */
    @AssertTrue(message = "Lance agendado exige data e hora de publicação (publicarEm)")
    public boolean isAgendamentoValido() {
        return status != StatusLance.AGENDADO || publicarEm != null;
    }
}
