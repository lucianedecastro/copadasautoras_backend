package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String categoria;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    private TipoExibicao tipoExibicao;

    private String arquivoCompletoUrl;

    private String arquivoPublicoUrl;

    /**
     * Timestamp automático de criação da submissão.
     * Controlado pelo Hibernate.
     */
    @CreationTimestamp
    @Column(
            name = "data_submissao",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataSubmissao;

    @ManyToOne
    @JoinColumn(name = "autora_id")
    private Autora autora;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;

    /**
     * Grupo da competição (FASE_32).
     * Uma submissão pertence a apenas um grupo.
     * Permanece nulo até a seleção para a edição.
     */
    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private GrupoCompeticao grupo;

    @Enumerated(EnumType.STRING)
    private StatusSubmissao status;

    @Enumerated(EnumType.STRING)
    private FaseCompeticao faseAtual;

    /**
     * Justificativa administrativa interna.
     *
     * Não é exibida para autora nem para banca.
     * Utilizada apenas para controle editorial
     * da organização da Copa.
     */
    @Column(columnDefinition = "TEXT")
    private String justificativaNaoSelecao;

    /**
     * Data em que a curadoria editorial
     * registrou a decisão sobre a obra.
     */
    private LocalDateTime dataDecisaoEditorial;

    /**
     * Trecho selecionado pela própria autora para
     * divulgação pública em caso de elegibilidade
     * à votação popular ("Escolha do Público").
     *
     * Entre 4.200 e 5.600 caracteres (~3 a 4 laudas).
     * Pode ser nulo/vazio até a autora preencher —
     * só é exigido no momento em que ela marca
     * autorizaVotoPopular = true.
     */
    @Size(
            min = 4200,
            max = 5600,
            message = "O trecho deve ter entre 4.200 e 5.600 caracteres"
    )
    @Column(name = "trecho_liberado", length = 5600)
    private String trechoLiberado;

    /**
     * Consentimento da autora para divulgação pública
     * do trechoLiberado, caso a obra se torne elegível
     * (semifinalista). Opt-in explícito, não presumido.
     */
    @Column(name = "autoriza_voto_popular", nullable = false)
    @Builder.Default
    private boolean autorizaVotoPopular = false;

    /**
     * Flag de fato: true assim que a submissão atinge
     * SEMIFINAL pela primeira vez. Nunca é revertido,
     * independentemente do que aconteça depois com
     * status/faseAtual (obra eliminada na semifinal
     * permanece elegível ao voto popular).
     */
    @Column(name = "elegivel_voto_popular", nullable = false)
    @Builder.Default
    private boolean elegivelVotoPopular = false;

    /**
     * Selo "Escolha do Público". Aplicado pelo admin ao encerrar a
     * votação popular, na obra mais votada (ou na escolhida por ele em
     * caso de empate). Removido se a votação for reaberta.
     */
    @Column(name = "selo_escolha_publico", nullable = false)
    @Builder.Default
    private boolean seloEscolhaPublico = false;

    @PrePersist
    public void prePersist() {

        /**
         * Toda obra nasce apenas como SUBMETIDA.
         * Ainda não participa da competição.
         */
        if (this.status == null) {
            this.status =
                    StatusSubmissao
                            .SUBMETIDA;
        }

        /**
         * A fase será definida posteriormente
         * quando a obra for selecionada para
         * compor a edição da Copa.
         */
    }
}