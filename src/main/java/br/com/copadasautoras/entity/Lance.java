package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Uma entrada da timeline "Lance a Lance".
 *
 * É o arquivo permanente dos feitos da Copa — clipping,
 * apoios/patrocínios, embaixadoras e temas. Ao fim da edição,
 * alimenta o relatório de resultados (exportação Excel/PDF).
 */
@Entity
@Table(name = "lance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String resumo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaLance categoria;

    /**
     * Flag de destaque. Quando true, o lance recebe o
     * selo dourado ("Golaço") na timeline e pode ser
     * filtrado à parte no relatório.
     */
    @Column(nullable = false)
    private boolean golaco;

    /**
     * Veículo de imprensa, quando aplicável
     * (ex.: "Rádio Guaíba", "CBN Ribeirão Preto").
     */
    private String veiculo;

    /**
     * Link direto para a fonte externa (modelo híbrido):
     * quando o lance é conteúdo do veículo, a audiência é
     * levada para lá em vez de reproduzirmos a matéria.
     */
    @Column(name = "link_externo", columnDefinition = "TEXT")
    private String linkExterno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLance status;

    /**
     * Data do acontecimento. Ordena a timeline e aparece
     * na régua de datas — o "minuto" do Lance a Lance.
     */
    @Column(name = "data_acontecimento", nullable = false)
    private LocalDate dataAcontecimento;

    /**
     * Momento em que um lance AGENDADO deve tornar-se
     * público. A visibilidade é resolvida na consulta:
     * nada de robô em segundo plano.
     */
    @Column(name = "publicar_em")
    private LocalDateTime publicarEm;

    /**
     * Identificador amigável para a URL da página de
     * detalhe. Gerado a partir do título no cadastro e
     * mantido estável depois, para não quebrar links.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @OneToMany(
            mappedBy = "lance",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("ordem ASC, id ASC")
    @Builder.Default
    private List<LanceMidia> midias = new ArrayList<>();

    @PrePersist
    public void prePersist() {

        /**
         * Todo lance nasce como RASCUNHO se nada for informado.
         * Só vira público por decisão explícita da organização.
         */
        if (this.status == null) {
            this.status = StatusLance.RASCUNHO;
        }
    }

    // =========================
    // SINCRONIA DA RELAÇÃO
    // =========================

    public void adicionarMidia(LanceMidia midia) {
        midias.add(midia);
        midia.setLance(this);
    }

    public void removerMidia(LanceMidia midia) {
        midias.remove(midia);
        midia.setLance(null);
    }
}
