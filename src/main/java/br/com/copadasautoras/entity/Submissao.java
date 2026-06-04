package br.com.copadasautoras.entity;

import jakarta.persistence.*;
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