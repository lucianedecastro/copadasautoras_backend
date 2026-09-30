package br.com.copadasautoras.dto;

import br.com.copadasautoras.util.MunicipiosIbge;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AutoraUpdateRequestDTO(

        @NotBlank(
                message = "O nome de exibição é obrigatório"
        )
        @Size(
                min = 2,
                max = 255,
                message = "O nome de exibição deve possuir entre 2 e 255 caracteres"
        )
        String nomeExibicao,

        @NotBlank(
                message = "A biografia é obrigatória"
        )
        @Size(
                max = 2000,
                message = "A biografia deve ter no máximo 2000 caracteres"
        )
        String biografia,

        // Site é o único campo opcional do perfil — só valida o tamanho
        // quando a autora tiver um. Sem @NotBlank de propósito.
        @Size(
                max = 500,
                message = "O site deve ter no máximo 500 caracteres"
        )
        String site,

        @NotBlank(
                message = "O link de rede social é obrigatório"
        )
        @Size(
                max = 255,
                message = "A rede social deve ter no máximo 255 caracteres"
        )
        String redesSociais,

        // Localização: opcional no backend (o front decide quando exigir).
        // Vazio ou uma das 27 UFs; estado e cidade andam juntos.
        @Pattern(
                regexp = "^$|^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$",
                message = "Estado inválido"
        )
        String estado,

        @Size(
                max = 120,
                message = "A cidade deve ter no máximo 120 caracteres"
        )
        String cidade
) {

    /**
     * Estado e cidade vêm juntos ou não vêm: meia localização não serve
     * pra estatística. Falha vira 400, como as demais validações do DTO.
     */
    @JsonIgnore
    @AssertTrue(message = "Informe estado e cidade juntos, ou deixe os dois em branco.")
    public boolean isLocalizacaoConsistente() {
        boolean temEstado = estado != null && !estado.isBlank();
        boolean temCidade = cidade != null && !cidade.isBlank();
        return temEstado == temCidade;
    }

    /**
     * A cidade precisa existir na UF informada (lista oficial do IBGE).
     * Garante o padrão do nome mesmo para chamadas que não passam pela
     * tela. Quando falta estado ou cidade, quem barra é a regra acima.
     */
    @JsonIgnore
    @AssertTrue(message = "Cidade não encontrada para o estado informado.")
    public boolean isCidadeDoEstado() {
        boolean temEstado = estado != null && !estado.isBlank();
        boolean temCidade = cidade != null && !cidade.isBlank();

        if (!temEstado || !temCidade) {
            return true;
        }

        return MunicipiosIbge.existe(estado, cidade);
    }
}
