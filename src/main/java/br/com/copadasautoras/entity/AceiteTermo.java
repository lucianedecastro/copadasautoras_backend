package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AceiteTermo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "autora_id", nullable = false)
    private Autora autora;

    @OneToOne
    @JoinColumn(name = "submissao_id", nullable = false)
    private Submissao submissao;

    @Column(nullable = false)
    private Boolean aceiteAutoria;

    @Column(nullable = false)
    private Boolean aceiteExibicao;

    @Column(nullable = false)
    private Boolean aceiteBanca;

    @Column(nullable = false)
    private Boolean aceiteTitularidade;

    @Column(nullable = false)
    private Boolean aceiteTermoCompleto;

    @Column(nullable = false)
    private String versaoTermo;

    private String ipAddress;

    private String userAgent;

    /**
     * URL do PDF institucional do termo,
     * armazenado no Cloudinary.
     */
    @Column(length = 1000)
    private String termoPdfUrl;

    private LocalDateTime dataAceite;

    @PrePersist
    public void prePersist() {

        if (this.dataAceite == null) {
            this.dataAceite =
                    LocalDateTime.now();
        }
    }
}