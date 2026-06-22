package br.com.copadasautoras.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "competicao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FaseCompeticao faseAtual;

    @Enumerated(EnumType.STRING)
    private StatusFase statusFase;

    /**
     * Controla se a página pública de chaveamento/finalistas está
     * visível. Ativado manualmente pelo admin (botão "Publicar
     * Chaveamento"), independente da fase técnica da competição.
     */
    @Column(name = "chaveamento_publicado", nullable = false)
    private Boolean chaveamentoPublicado;

    @PrePersist
    public void prePersist() {
        if (faseAtual == null) {
            faseAtual = FaseCompeticao.FASE_32;
        }
        if (statusFase == null) {
            statusFase = StatusFase.NAO_INICIADA;
        }
        if (chaveamentoPublicado == null) {
            chaveamentoPublicado = false;
        }
    }
}