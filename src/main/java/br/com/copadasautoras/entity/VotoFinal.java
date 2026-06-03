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
public class VotoFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Jurada que votou.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banca_id", nullable = false)
    private Usuario banca;

    /**
     * Finalista escolhida.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submissao_id", nullable = false)
    private Submissao submissao;

    /**
     * Controle temporal e auditoria.
     */
    @Column(nullable = false)
    private LocalDateTime dataVoto;

    @PrePersist
    public void prePersist() {
        this.dataVoto = LocalDateTime.now();
    }
}
