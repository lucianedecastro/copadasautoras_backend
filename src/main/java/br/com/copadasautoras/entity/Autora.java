package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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
     * Motivo da exclusão (registro interno).
     *
     * Só faz sentido quando o status é EXCLUIDA. Guardado como texto e
     * usado apenas para decidir a mensagem GENÉRICA que a autora vê no
     * painel — nunca a justificativa livre nem o motivo específico.
     * NULL nas linhas anteriores à migration V15: o painel trata NULL
     * como encerramento neutro (jamais como "inadequação").
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_exclusao", length = 20)
    private MotivoExclusao motivoExclusao;

    /**
     * Data de cadastro da autora.
     *
     * Preenchida uma única vez, no momento do cadastro, pelo Hibernate
     * ({@link CreationTimestamp}). É NULLABLE de propósito: as autoras
     * cadastradas antes da migration V13 não têm data conhecida e ficam
     * como NULL ("legado"). Não é editável.
     */
    @CreationTimestamp
    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    /**
     * Data da última alteração do perfil.
     *
     * Preenchida no cadastro e reescrita a cada atualização
     * ({@link UpdateTimestamp}). Semente do log: registra QUANDO o
     * perfil mudou pela última vez — não quem mudou nem o quê (isso
     * é papel de uma tabela de auditoria própria, ainda a fazer).
     * Também NULLABLE: as autoras legado só ganham valor quando forem
     * editadas pela primeira vez.
     */
    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

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
