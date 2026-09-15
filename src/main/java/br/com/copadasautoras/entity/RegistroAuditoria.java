package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Uma linha do log de auditoria — uma decisão registrada.
 *
 * Imutável por contrato: a aplicação só cria e lê registros, nunca
 * edita nem apaga. Os setters existem por consistência com o padrão
 * Lombok do projeto, mas não são usados após a criação.
 *
 * O nome do ator é gravado como snapshot ({@code atorNome}) além do
 * id, para o log continuar legível mesmo que o usuário seja renomeado
 * ou removido depois.
 */
@Entity
@Table(name = "registro_auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Quando a decisão foi registrada. Preenchido pelo Hibernate.
     */
    @CreationTimestamp
    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    /**
     * De onde partiu (ADMIN, BANCA, AUTORA, SISTEMA).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemAuditoria origem;

    /**
     * Id do usuário que agiu. Pode ser nulo (ações de sistema).
     */
    @Column(name = "ator_id")
    private Long atorId;

    /**
     * Snapshot do nome do ator no momento da ação.
     */
    @Column(name = "ator_nome")
    private String atorNome;

    /**
     * Sobre o que a decisão recai (AUTORA, SUBMISSAO).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEntidadeAuditoria entidade;

    /**
     * Id da autora/obra afetada. Sem FK de propósito: o log sobrevive
     * à exclusão do que ele registra.
     */
    @Column(name = "entidade_id")
    private Long entidadeId;

    /**
     * A decisão em si.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AcaoAuditoria acao;

    /**
     * Estado antes da decisão, quando houver.
     */
    @Column(name = "valor_anterior", length = 60)
    private String valorAnterior;

    /**
     * Estado depois da decisão, quando houver.
     */
    @Column(name = "valor_novo", length = 60)
    private String valorNovo;

    /**
     * O porquê da decisão, quando existir (texto livre).
     */
    @Column(columnDefinition = "TEXT")
    private String justificativa;
}
