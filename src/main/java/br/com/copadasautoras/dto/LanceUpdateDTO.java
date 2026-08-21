package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.CategoriaLance;
import br.com.copadasautoras.entity.StatusLance;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Edição dos dados de um lance.
 *
 * As mídias não entram aqui — são gerenciadas pelos
 * endpoints próprios de mídia. O slug também não muda:
 * é mantido estável para não quebrar links já divulgados.
 */
public record LanceUpdateDTO(

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

        LocalDateTime publicarEm

) {

    @AssertTrue(message = "Lance agendado exige data e hora de publicação (publicarEm)")
    public boolean isAgendamentoValido() {
        return status != StatusLance.AGENDADO || publicarEm != null;
    }
}
