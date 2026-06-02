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

    @Enumerated(EnumType.STRING)
    private StatusSubmissao status;

    @Enumerated(EnumType.STRING)
    private FaseCompeticao faseAtual;

    @PrePersist
    public void prePersist() {

        if (this.status == null) {
            this.status =
                    StatusSubmissao
                            .EM_COMPETICAO;
        }

        if (this.faseAtual == null) {
            this.faseAtual =
                    FaseCompeticao
                            .FASE_32;
        }
    }
}