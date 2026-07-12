package br.com.copadasautoras.dto;

import br.com.copadasautoras.entity.TipoExibicao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Edição dos dados da obra já submetida.
 *
 * Não repete os aceites do Art. 19 de propósito: eles foram declarados no
 * envio e continuam valendo. Corrigir um título não é declarar de novo a
 * autoria — e obrigar a autora a remarcar os cinco checkboxes para consertar
 * uma vírgula seria atrito sem propósito.
 *
 * O que muda aqui, no entanto, aparece impresso no termo de aceite (Título,
 * Categoria, Modalidade de exibição). Por isso o termo é regerado a cada
 * edição: um documento jurídico que mostra o título antigo é um documento
 * que mente.
 */
public record SubmissaoUpdateDTO(

        @NotBlank(message = "Título é obrigatório")
        String titulo,

        String categoria,

        String descricao,

        @NotNull(message = "Tipo de exibição é obrigatório")
        TipoExibicao tipoExibicao

) {}
