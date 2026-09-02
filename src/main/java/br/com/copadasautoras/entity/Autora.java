package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Autora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome interno/civil da autora.
     * Não editável pela autora após cadastro.
     */
    @Column(nullable = false)
    private String nome;

    /**
     * Nome usado publicamente na obra e na competição.
     */
    @Column(name = "nome_exibicao", nullable = false)
    private String nomeExibicao;

    /**
     * Minibio pública da autora.
     */
    @Column(length = 2000)
    private String biografia;

    /**
     * Rede social escolhida pela autora (opcional).
     */
    private String redesSociais;

    /**
     * Site pessoal da autora (opcional).
     */
    private String site;

    /**
     * Status institucional da autora na plataforma.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_autora", nullable = false)
    private StatusAutora statusAutora;

    /**
     * Justificativa para solicitação de exclusão de perfil.
     */
    @Column(name = "justificativa_exclusao", columnDefinition = "TEXT")
    private String justificativaExclusao;

    /**
     * Usuário responsável pela autenticação.
     */
    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /**
     * Fonte de verdade única do "perfil completo".
     *
     * O perfil só é considerado completo quando os campos obrigatórios
     * estão preenchidos: nome completo, nome de exibição, biografia e
     * link de rede social. O site é opcional e não entra na regra.
     *
     * Usado como trava institucional na aprovação da autora — se a regra
     * mudar, muda só aqui.
     */
    public boolean isPerfilCompleto() {
        return isPreenchido(nome)
                && isPreenchido(nomeExibicao)
                && isPreenchido(biografia)
                && isPreenchido(redesSociais);
    }

    private static boolean isPreenchido(String valor) {
        return valor != null && !valor.isBlank();
    }
}
